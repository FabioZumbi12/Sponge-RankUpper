package br.net.fabiozumbi12.RankUpper.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.net.fabiozumbi12.RankUpper.RUAFK;
import br.net.fabiozumbi12.RankUpper.RUUtil;
import br.net.fabiozumbi12.RankUpper.RankUpper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.statistic.Statistic;

import com.google.common.reflect.TypeToken;

public class RUConfig{
	
	//getters
	private CommentedConfigurationNode configRoot;
	private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
	private MainCategory root;
	public MainCategory root(){
		return this.root;
	}

	private SqlService sql;
	private StatsCategory stats;
	public StatsCategory stats(){
		return this.stats;
	}

	private File defConfig = new File(RankUpper.get().getConfigDir() ,"rankupper.conf");
	
	public RUConfig(GuiceObjectMapperFactory factory) throws IOException {
		try {
			Files.createDirectories(RankUpper.get().getConfigDir().toPath());
			if (!defConfig.exists()){
				RankUpper.get().getLogger().log("Creating config file...");
				defConfig.createNewFile();
			}

			/*--------------------- rankupper.conf ---------------------------*/
			cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();
			configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
			root = configRoot.getValue(TypeToken.of(MainCategory.class), new MainCategory());


		} catch (IOException e1) {			
			RankUpper.get().getLogger().severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
			return;
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}

		//load playerstats
        loadPlayerStats();
        /*---------------*/
                
		save();        			
		RankUpper.get().getLogger().log("All configurations loaded!");
	}

	private String dbPath;
	public void loadPlayerStats() {
		stats = new StatsCategory();

		dbPath = String.format(root.database.uri, RankUpper.get().getDefConfig().getParentFile().getAbsolutePath());

		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}

		try {
			//convert old stats db
			File statConfig = new File(RankUpper.get().getConfigDir(), "playerstats.conf");
			if (statConfig.exists()){
				ConfigurationLoader<CommentedConfigurationNode> statsManager = HoconConfigurationLoader.builder().setFile(statConfig).build();
				CommentedConfigurationNode tempStats = statsManager.load();
				RankUpper.get().getLogger().warning("Converting player stats to new format...");
				for (Entry<Object, ? extends CommentedConfigurationNode> node : tempStats.getChildrenMap().entrySet()) {
					stats.players.put(node.getKey().toString(), new StatsCategory.PlayerInfoCategory(
							node.getValue().getNode("JoinDate").getString(),
							node.getValue().getNode("LastVisist").getString(),
							node.getValue().getNode("PlayerName").getString(),
							node.getValue().getNode("TimePlayed").getInt()
					));
					RankUpper.get().getLogger().warning("Importing: "+node.getKey().toString()+" = "
							+ node.getValue().getNode("JoinDate").getString()+", "
							+ node.getValue().getNode("LastVisist").getString()+", "
							+ node.getValue().getNode("PlayerName").getString()+", "
							+ node.getValue().getNode("TimePlayed").getString()+", ");
				}
				RankUpper.get().getLogger().warning("Player stats imported to database!");
				statConfig.renameTo(new File(RankUpper.get().getConfigDir(), "playerstats-old.conf"));
			}
		} catch(IOException e1){
			e1.printStackTrace();
		}

		//begin start
		try (Connection conn = sql.getDataSource(dbPath).getConnection()) {
			String table = "CREATE TABLE IF NOT EXISTS " + root.database.prefix + "players ("
					+ "uuid varchar(64) primary key, "
					+ "joindate varchar(64), "
					+ "name varchar(64), "
					+ "lastvisit varchar(64), "
					+ "time int)";
			conn.prepareStatement(table).execute();

			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + root.database.prefix + "players");
			ResultSet results = stmt.executeQuery();
			while (results.next()) {
				StatsCategory.PlayerInfoCategory pinfo = new StatsCategory.PlayerInfoCategory(
						results.getString("joindate"),
						results.getString("lastvisit"),
						results.getString("name"),
						results.getInt("time")
				);
				stats.players.put(results.getString("uuid"), pinfo);
			}
		} catch (SQLException e) {
			RankUpper.get().getLogger().severe("Player stats could not be loaded or created!");
			e.printStackTrace();
		}
	}

	public void closeConn(){
		try {
			this.sql.getDataSource(dbPath).getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
    public void save(){
    	try {
			cfgLoader.save(configRoot);
		} catch (IOException e) {
			RankUpper.get().getLogger().severe("Problems during save file:");
			e.printStackTrace();
		}
		savePlayersStats(); 
    }
    
    public void savePlayersStats(){
    	try (Connection conn = sql.getDataSource(dbPath).getConnection()) {
    		for (Entry<String, StatsCategory.PlayerInfoCategory> stat:stats.players.entrySet()){
    			String sql = String.format("MERGE INTO %splayers(%s, %s, %s, %s, %s) values(?, ?, ?, ?, ?)",
						root.database.prefix, "uuid", "joindate", "lastvisit", "name", "time");

				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, stat.getKey());
				stmt.setString(2, stat.getValue().JoinDate);
				stmt.setString(3, stat.getValue().LastVisit);
				stmt.setString(4, stat.getValue().PlayerName);
				stmt.setInt(5, stat.getValue().TimePlayed);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

	public void AddPlayer(Player p) {
		String PlayerString = p.getUniqueId().toString();
		if (!root.use_uuids_instead_names){
			PlayerString = p.getName();
		}
		StatsCategory.PlayerInfoCategory pStat = new StatsCategory.PlayerInfoCategory();

		pStat.PlayerName = p.getName();
		pStat.JoinDate = RUUtil.DateNow();
		pStat.LastVisit = RUUtil.DateNow();
		pStat.TimePlayed = 0;

		stats.players.put(PlayerString, pStat);
		savePlayersStats();
	}
	
	public void AddPlayer(User p) {
		String PlayerString = p.getUniqueId().toString();
		if (!root.use_uuids_instead_names){
			PlayerString = p.getName();
		}
		StatsCategory.PlayerInfoCategory pStat = new StatsCategory.PlayerInfoCategory();

		pStat.PlayerName = p.getName();
		pStat.JoinDate = RUUtil.DateNow();
		pStat.LastVisit = RUUtil.DateNow();
		pStat.TimePlayed = 0;

		stats.players.put(PlayerString, pStat);
		savePlayersStats();
	}
	
	public void setPlayerTime(String pkey, int time){		
		stats.players.get(pkey).TimePlayed = time;
	}
	
	public int addPlayerTime(User p, int ammount){
		String PlayerString = p.getUniqueId().toString();
		if (!root.use_uuids_instead_names){
			PlayerString = p.getName();
		}
		int time = stats.players.get(PlayerString).TimePlayed+ammount;
		stats.players.get(PlayerString).TimePlayed = (time);
		return time;
	}
	
	public void setLastVisit(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!root.use_uuids_instead_names){
			PlayerString = p.getName();
		}
	    stats.players.get(PlayerString).LastVisit = RUUtil.DateNow();
	}

	public int getPlayerTime(String uuid){		
		return stats.players.get(uuid).TimePlayed;
	}
	
	public String getPlayerKey(User user){
		if (root.use_uuids_instead_names && stats.players.containsKey(user.getUniqueId().toString())){
			return user.getUniqueId().toString();
		} else if (!root.use_uuids_instead_names && stats.players.containsKey(user.getName())){
			return user.getName();
		}
		return null;
	}
	
	public HashMap<String, Object> getPlayerDB(User p){
		HashMap<String, Object> pdb = new HashMap<>();
		String PlayerString = p.getUniqueId().toString();
		if (!root.use_uuids_instead_names){
			PlayerString = p.getName();
		}
		pdb.put("PlayerName", stats.players.get(PlayerString).PlayerName);
		pdb.put("JoinDate", stats.players.get(PlayerString).JoinDate);
		pdb.put("LastVisit", stats.players.get(PlayerString).LastVisit);
		pdb.put("TimePlayed", stats.players.get(PlayerString).TimePlayed);
		return pdb;
	}
		
	public void AddPlayerTimes() {
		for (Player p:Sponge.getServer().getOnlinePlayers()){
			RankUpper.get().getLogger().debug("Passou");
			
			if (root.afk_support){
				RankUpper.get().getLogger().debug("Berore check AFK!");
				if (RUAFK.isAFK(p)){
					RankUpper.get().getLogger().debug("After check AFK!");
					continue;
				}
			}
			addPlayerTime(p, root.update_player_time_minutes);
			checkRankup(p);
		}
	}
	
	public boolean groupExists(String group){
		return root.ranked_groups.containsKey(group);
	}
	
	public boolean addGroup(String group, String newGroup, int time, int levels, int money) {
		if (groupExists(group)){
			return false;
		}
		List<String> cmds = Arrays.asList("lp user {player} parent unset {oldgroup}","lp user {player} parent set {newgroup}");
		RankedGroupsCategory groupvalues = new RankedGroupsCategory(cmds,
				levels, "&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} on server.",
				time, money, newGroup);
		root.ranked_groups.put(group, groupvalues);
		save();
		return true;
	}
	
	public Boolean setGroup(String group, String newGroup, int time, int levels, int money) {
		if (!groupExists(group)){
			return false;
		}	
		root.ranked_groups.get(group).minutes_needed = time;
		root.ranked_groups.get(group).levels_needed = levels;
		root.ranked_groups.get(group).money_needed = money;
		root.ranked_groups.get(group).next_group = newGroup;
		save();
		return true;
	}
	
	public boolean checkRankup(User p){
		Subject subG = RankUpper.get().getPerms().getHighestGroup(p);

        String pgroup = null;
		if(subG == null){
            List<Subject> pgroups = RankUpper.get().getPerms().getPlayerGroups(p);
            for (Subject sub:pgroups){
                if (root.ranked_groups.containsKey(sub.getIdentifier())){
                    pgroup = sub.getIdentifier();
                    RankUpper.get().getLogger().debug("Ranked Player Group (not primary) is: "+pgroup);
                    break;
                }
            }
            if (pgroup == null) return false;
		} else {
            pgroup = subG.getIdentifier();
            RankUpper.get().getLogger().debug("Highest Group is: "+pgroup);
        }
		
		String ngroup = root.ranked_groups.get(pgroup) != null ? root.ranked_groups.get(pgroup).next_group : null;
					
		if (ngroup == null || ngroup.isEmpty() || !RankUpper.get().getPerms().getAllGroups().contains(ngroup)){
			return false;
		}
						
		int minutesNeeded = root.ranked_groups.get(pgroup).minutes_needed;
		int moneyNeeded = root.ranked_groups.get(pgroup).money_needed;
		int levelNeeded = root.ranked_groups.get(pgroup).levels_needed;
		
		//check for statistics
		for (Entry<String, Long> key:root.ranked_groups.get(pgroup).minecraft_statistic.entrySet()){
			if (Sponge.getRegistry().getType(Statistic.class, key.getKey()).isPresent()){
				Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.getKey()).get();
				if (!p.getStatisticData().get(stat).isPresent() || p.getStatisticData().get(stat).get() < key.getValue()){
					return false;
				}
			}
		}
		
		if (minutesNeeded != 0){
			if (getPlayerTime(getPlayerKey(p)) < minutesNeeded){
				return false;
			}					
		}

		if (moneyNeeded != 0 && RankUpper.get().getEconomy() != null){
			UniqueAccount acc = RankUpper.get().getEconomy().getOrCreateAccount(p.getUniqueId()).get();
			if (acc.getBalance(RankUpper.get().getEconomy().getDefaultCurrency()).intValue() < moneyNeeded){
				return false;
			} 
		}

		if (levelNeeded != 0){				
			if (p.get(Keys.EXPERIENCE_LEVEL).isPresent() && p.get(Keys.EXPERIENCE_LEVEL).get() < levelNeeded){
				return false;
			} 
		}
				
		for (String cmd: root.ranked_groups.get(pgroup).execute_commands){
			RankUpper.get().game.getCommandManager().process(Sponge.getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{oldgroup}", pgroup).replace("{newgroup}", ngroup));
		}
		String message =  root.ranked_groups.get(pgroup).message_broadcast;
		if (message != null && !message.equals("")){
			Sponge.getServer().getBroadcastChannel().send(RUUtil.toText(message.replace("{player}", p.getName()).replace("{time}", RUUtil.timeDescript(minutesNeeded)).replace("{newgroup}", ngroup)));
		}				
		return true;
	}	
}
   
