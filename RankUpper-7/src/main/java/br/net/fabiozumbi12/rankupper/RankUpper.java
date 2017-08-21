package br.net.fabiozumbi12.rankupper;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import com.google.inject.Inject;

import br.net.fabiozumbi12.rankupper.config.VersionData;

@Plugin(id="rankupper", 
name="RankUpper", 
version=VersionData.VERSION,
authors="FabioZumbi12", 
description="Auto rankup plugin based on economy, time or xps")
public class RankUpper {
	
	public Game game;
	public EconomyService econ;
	public RUConfig cfgs;
	public RUPermsAPI perms;
	
	private PluginContainer instance;
	public PluginContainer get(){
		return this.instance;
	}
	
	@Inject private Logger logger;
	public Logger getLogger(){	
		return logger;
	}
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File defConfig;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;	
	public ConfigurationLoader<CommentedConfigurationNode> getCfManager(){
		return configManager;
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
        try {
        	game = Sponge.getGame();
        	instance = Sponge.getPluginManager().getPlugin("rankupper").get();
        	RULogger.init(this);
        	RUUtil.init(this);
        	
        	logger.info("Init config module...");
        	configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();	
            cfgs = new RUConfig(this, configDir, defConfig);
            
            logger.info("Init lang module...");
            RULang.init(this, configDir);
            
            logger.info("Init permissions module...");
            perms = new RUPermsAPI(game);

            logger.info("Init commands module...");
            RUCommands.init(this);
            
            game.getEventManager().registerListeners(this, new RUListener(this));
            PlayerCounterHandler();
            AutoSaveHandler();
            
            //hook afk with nucleus
            registerNucleus();
            
            RULogger.success("RankUpper enabled.");
            
        } catch (Exception e) {
        	e.printStackTrace();
    		RULogger.severe("Error enabling RankUpper! Plugin Disabled.");
        }
	}
	
	private void registerNucleus(){
		if(cfgs.getBool("afk-support")) {
			if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
				RULogger.info("Nucleus found. AFK support enabled.");
				RUAFK.init();
			} else {
				cfgs.setConfig(false, "afk-support");
				RULogger.info("Nucleus is not installed. AFK support has been disabled in your config.");
			}
		}
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		cfgs.savePlayersStats();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        RULogger.severe("RankUpper disabled.");
    }
	
	public void reload(){
		for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
			task.cancel();
		}
		cfgs.savePlayersStats();
		cfgs = new RUConfig(this, configDir, defConfig);
		RULang.init(this, configDir);
		PlayerCounterHandler();
		AutoSaveHandler();
		registerNucleus();
	}
	
	private void PlayerCounterHandler() {
		RULogger.info("Updating player times every "+ cfgs.getInt("update-player-time-minutes") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				RULogger.debug("Updating played times to players!");
				cfgs.AddPlayerTimes();					
			} 
		},cfgs.getInt("update-player-time-minutes"), cfgs.getInt("update-player-time-minutes"), TimeUnit.MINUTES);	
	}	
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	private void AutoSaveHandler() {
		RULogger.info("Saving database every "+ cfgs.getInt("flat-file-save-interval") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				RULogger.debug("Saving Database File!");
				cfgs.savePlayersStats();	
				} 
			},cfgs.getInt("flat-file-save-interval"), cfgs.getInt("flat-file-save-interval"), TimeUnit.MINUTES);	
	}

}

class RULogger{	
	private static RankUpper plugin;

	public static void init(RankUpper pl){
		plugin = pl;
	}
	
	public static void success(String s) {
		Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&a&l"+s+"&r]"));
    }
	
    public static void info(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }
    
    public static void warning(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&6"+s+"&r]"));
    }
    
    public static void severe(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&c&l"+s+"&r]"));
    }
    
    public static void log(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }
    
    public static void debug(String s) {
        if (plugin.cfgs.getBool("debug-messages")) {
        	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&b"+s+"&r]"));
        }  
    }
}