package br.net.fabiozumbi12.rankupper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

@Plugin(id="rankupper", 
name="RankUpper", 
version="2.5",
authors="FabioZumbi12", 
description="Auto rankup plugin based on economy, time or xps")
public class RankUpper {

	public static PluginContainer plugin;
	static String configDir;
	public static Game game;
	public static EconomyService econ;
	public static RUConfig cfgs;
	public static PermsAPI perms;
	public RUAFK ruafk;
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
        try {
        	game = Sponge.getGame();    	
        	plugin = Sponge.getPluginManager().getPlugin("rankupper").get();
            configDir = game.getConfigManager().getSharedConfig(RankUpper.plugin).getDirectory()+File.separator+"RankUpper"+File.separator;
            
            cfgs = new RUConfig(plugin);
            RULang.init();
            perms = new PermsAPI(game);

            if(cfgs.getBool("afk-support")) {
				if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
					RULogger.info("Nucleus found. AFK support enabled.");
				} else {
					cfgs.setConfig("afk-support", false);
					RULogger.info("Nucleus is not installed. AFK support has been disabled in your config.");
				}
				//Initialize even if nucleus not installed - avoid nulls (these wont be called).
				ruafk = new RUAFK();
			}

            
            CommandSpec cs = CommandSpec.builder()
            	    .executor(new RUCommands())
            	    .arguments(GenericArguments.seq(
            	    		GenericArguments.onlyOne(GenericArguments.string(Text.of("0"))),
            	    		GenericArguments.optional(GenericArguments.string(Text.of("1"))),
            	    		GenericArguments.optional(GenericArguments.string(Text.of("2"))),
            	    		GenericArguments.optional(GenericArguments.integer(Text.of("3"))),
            	    		GenericArguments.optional(GenericArguments.integer(Text.of("4"))),
            	    		GenericArguments.optional(GenericArguments.integer(Text.of("5"))),
            	    		GenericArguments.optional(GenericArguments.integer(Text.of("6")))))
            	    		.extendedDescription(Text.of("For more info use /ru help"))
            	    .build();
            
            game.getCommandManager().register(plugin, cs, Arrays.asList("rankupper","rupper","rank","ru"));
            game.getEventManager().registerListeners(plugin, new RUListener());
            PlayerCounterHandler();
            AutoSaveHandler();
            
            RULogger.success(plugin.getName() + " enabled.");
            
        } catch (Exception e) {
        	e.printStackTrace();
    		RULogger.severe("Error enabling RankUpper! Plugin Disabled.");
        }
	}
	public RUAFK getRUAFK(){
		//may return null if afk support enabled but nucleus not correctly installed
		return ruafk;
	}
        
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		cfgs.savePlayersStats();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        RULogger.severe(RankUpper.plugin.getName() + " disabled.");
    }
	
	public static void reload(){
		for (Task task:Sponge.getScheduler().getScheduledTasks(plugin)){
			task.cancel();
		}
		RankUpper.cfgs.savePlayersStats();
		RankUpper.cfgs = new RUConfig(RankUpper.plugin);
		RULang.init();
		PlayerCounterHandler();
		AutoSaveHandler();
	}
	
	private static void PlayerCounterHandler() {
		RULogger.info("Updating player times every "+ cfgs.getInt("update-player-time-minutes") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(RankUpper.plugin).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				if (Sponge.getServer().getOnlinePlayers().size() > 0){
					RULogger.debug("Updating played times to players!");
					cfgs.AddPlayerTimes();
				}					
			} 
		},cfgs.getInt("update-player-time-minutes"), cfgs.getInt("update-player-time-minutes"), TimeUnit.MINUTES);	
	}	
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	private static void AutoSaveHandler() {
		RULogger.info("Saving database every "+ cfgs.getInt("flat-file-save-interval") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(RankUpper.plugin).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				RULogger.debug("Saving Database File!");
				cfgs.savePlayersStats();	
				} 
			},cfgs.getInt("flat-file-save-interval"), cfgs.getInt("flat-file-save-interval"), TimeUnit.MINUTES);	
	}

}

class RULogger{
	   
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
        if (RankUpper.cfgs.getBool("debug-messages")) {
        	Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&b"+s+"&r]"));
        }  
    }
}