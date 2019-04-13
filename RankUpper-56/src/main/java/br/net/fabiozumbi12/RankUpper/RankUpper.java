package br.net.fabiozumbi12.RankUpper;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import br.net.fabiozumbi12.RankUpper.config.PlayerStatsDB;
import br.net.fabiozumbi12.RankUpper.config.RUConfig;

import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;

import br.net.fabiozumbi12.RankUpper.config.VersionData;

import com.google.inject.Inject;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;

@Plugin(id="rankupper", 
name="RankUpper", 
version=VersionData.VERSION,
authors="FabioZumbi12", 
description="Auto rankup plugin based on various requirements",
dependencies = {
        @Dependency(id = "placeholderapi", optional = true)
})
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
	@DefaultConfig(sharedRoot = false)
	private File defaultConfig;
	public File getDefConfig(){
		return this.defaultConfig;
	}

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	public File getConfigDir(){
		return this.configDir.toFile();
	}

	private PlayerStatsDB stats;
	public PlayerStatsDB getStats() {
		return this.stats;
	}

	private String dbPath;
	private DataSource dataSource;
	public Connection getConnection(){
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Inject
	public GuiceObjectMapperFactory factory;

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
        try {
        	rankupper = this;
			this.logger = new RULogger();

			logger.info("Init config module...");
			this.cfgs = new RUConfig(this.factory);

        	logger.info("Init stats module...");
        	this.dbPath = String.format(this.cfgs.root().database.uri, RankUpper.get().getDefConfig().getParentFile().getAbsolutePath());
            this.dataSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource(dbPath);
			this.stats = new PlayerStatsDB();

            logger.info("Init lang module...");
			this.lang = new RULang();
            
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
		if(cfgs.root().afk_support) {
			if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
				logger.info("Nucleus found. AFK support enabled.");
				RUAFK.init();
			} else {
				cfgs.root().afk_support = false;
				logger.info("Nucleus is not installed. AFK support has been disabled in your config.");
			}
		}
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		getStats().savePlayersStats();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        logger.severe("RankUpper disabled.");
    }
	
	public void reload(){
		for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
			task.cancel();
		}

		try{
		    getStats().savePlayersStats();
		    getConnection().close();
        } catch (Exception ignored){}

        cfgs = new RUConfig(this.factory);
		lang = new RULang();

        try {
            this.dataSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource(dbPath);
            this.stats = new PlayerStatsDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }

		PlayerCounterHandler();
		AutoSaveHandler();
		registerNucleus();
	}
	
	private void PlayerCounterHandler() {
		logger.info("Updating player times every "+ cfgs.root().update_player_time_minutes + " minute(s)!");
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(() -> {
            logger.debug("Updating played times to players!");
			getStats().AddPlayerTimes();
        },cfgs.root().update_player_time_minutes, cfgs.root().update_player_time_minutes, TimeUnit.MINUTES);
	}	
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	private void AutoSaveHandler() {
		logger.info("Saving database every "+ cfgs.root().database_save_interval + " minute(s)!");
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(() -> {
            logger.debug("Saving Database File!");
			getStats().savePlayersStats();
            },cfgs.root().database_save_interval, cfgs.root().database_save_interval, TimeUnit.MINUTES);
	}
}