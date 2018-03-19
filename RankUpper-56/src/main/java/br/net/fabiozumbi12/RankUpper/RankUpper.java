package br.net.fabiozumbi12.RankUpper;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
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

import br.net.fabiozumbi12.RankUpper.config.VersionData;

import com.google.inject.Inject;

@Plugin(id="rankupper", 
name="RankUpper", 
version=VersionData.VERSION,
authors="FabioZumbi12", 
description="Auto rankup plugin based on economy, time or xps")
public class RankUpper {
	
	@Inject
	public Game game;
	private EconomyService econ;
	public EconomyService getEconomy(){
		return this.econ;
	}
	
	private RUConfig cfgs;
	public RUConfig getConfig(){
		return this.cfgs;
	}
	
	private RUPerms perms;
	public RUPerms getPerms(){
		return this.perms;
	}
	
	@Inject
	private PluginContainer instance;
	public PluginContainer instance(){
		return this.instance;
	}
	
	private static RankUpper rankupper;
	public static RankUpper get(){
		return rankupper;
	}
	
	private RULogger logger;
	public RULogger getLogger(){	
		return this.logger;
	}
	
	private RULang lang;
	public RULang getLang(){
		return this.lang;
	}
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	public Path getConfigDir(){
		return this.configDir;
	}

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
        	rankupper = this;
        	logger = new RULogger();
        	
        	logger.info("Init config module...");
        	configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();	
            cfgs = new RUConfig(this, configDir, defConfig);
            
            logger.info("Init lang module...");
            lang = new RULang();
            
            logger.info("Init permissions module...");
            String v = game.getPlatform().getContainer(Component.API).getVersion().isPresent() ? game.getPlatform().getContainer(Component.API).getVersion().get() : "8";

            if (v.startsWith("5") || v.startsWith("6")){
                perms = (RUPerms)Class.forName("br.net.fabiozumbi12.RankUpper.RUPerms56").getConstructor().newInstance();
            }
            if (v.startsWith("7") || v.startsWith("8")){
                perms = (RUPerms)Class.forName("br.net.fabiozumbi12.RankUpper.RUPerms78").getConstructor().newInstance();
            }

            logger.info("Init commands module...");
            RUCommands.init(this);
            
            game.getEventManager().registerListeners(this, new RUListener());
            PlayerCounterHandler();
            AutoSaveHandler();
            
            //hook afk with nucleus
            registerNucleus();
            
            logger.success("RankUpper enabled.");
            
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.severe("Error enabling RankUpper! Plugin Disabled.");
        }
	}
	
	private void registerNucleus(){
		if(cfgs.getBool("afk-support")) {
			if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
				logger.info("Nucleus found. AFK support enabled.");
				RUAFK.init();
			} else {
				cfgs.setConfig(false, "afk-support");
				logger.info("Nucleus is not installed. AFK support has been disabled in your config.");
			}
		}
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		cfgs.savePlayersStats();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        logger.severe("RankUpper disabled.");
    }
	
	public void reload(){
		for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
			task.cancel();
		}
		cfgs.savePlayersStats();
		cfgs = new RUConfig(this, configDir, defConfig);
		lang = new RULang();
		PlayerCounterHandler();
		AutoSaveHandler();
		registerNucleus();
	}
	
	private void PlayerCounterHandler() {
		logger.info("Updating player times every "+ cfgs.getInt("update-player-time-minutes") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(() -> {
            logger.debug("Updating played times to players!");
            cfgs.AddPlayerTimes();
        },cfgs.getInt("update-player-time-minutes"), cfgs.getInt("update-player-time-minutes"), TimeUnit.MINUTES);
	}	
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	private void AutoSaveHandler() {
		logger.info("Saving database every "+ cfgs.getInt("flat-file-save-interval") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(() -> {
            logger.debug("Saving Database File!");
            cfgs.savePlayersStats();
            },cfgs.getInt("flat-file-save-interval"), cfgs.getInt("flat-file-save-interval"), TimeUnit.MINUTES);
	}

}

class RULogger{	
	
	public void success(String s) {
		Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }
    
    public void warning(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (RankUpper.get().getConfig().getBool("debug-messages")) {
        	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&b"+s+"&r]"));
        }  
    }
}