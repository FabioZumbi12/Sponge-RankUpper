package br.net.fabiozumbi12.RankUpper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.statistic.Statistic;

import com.google.common.reflect.TypeToken;

public class RUConfig{
	
	//getters	
	private CommentedConfigurationNode config;
	public CommentedConfigurationNode configs(){
		return config;
	}
	
	//getters	
	private ConfigurationLoader<CommentedConfigurationNode> statsManager;
	private CommentedConfigurationNode stats;
	public CommentedConfigurationNode stats(){
		return stats;
	}

	RankUpper plugin;
	private Path defDir;
	
	public RUConfig(RankUpper plugin, Path configDir, File defConfig) {
		this.defDir = configDir;
		this.plugin = plugin;
		try {
			Files.createDirectories(configDir);
			if (!defConfig.exists()){
				plugin.getLogger().info("Creating config file...");
				defConfig.createNewFile();
			}
			
			config = plugin.getCfManager().load();
			
			config.getNode("date-format").setValue(config.getNode("date-format").getString("dd/MM/yyyy"))
			.setComment("Date format to save data info of players.");
			
			config.getNode("debug-messages").setValue(config.getNode("debug-messages").getBoolean(false))
			.setComment("Enable debug messages?");
			
			config.getNode("afk-support").setValue(config.getNode("afk-support").getBoolean(false))
			.setComment("Stop counting time when a player is AFK? (Using Nucleus' API only!)\n"
					+ "Setting this true without Nucleus (0.23.1+) installed will result in player's time not being counted.");
			
			config.getNode("flat-file-save-interval").setValue(config.getNode("flat-file-save-interval").getInt(20))
			.setComment("Save to file every X minutes.");
			
			config.getNode("language").setValue(config.getNode("language").getString("EN-US"))
			.setComment("Date format to save data info of players.");
			
			config.getNode("update-player-time-minutes").setValue(config.getNode("update-player-time-minutes").getInt(5))
			.setComment("Interval to add for online players.");
			
			config.getNode("use-uuids-instead-names").setValue(config.getNode("use-uuids-instead-names").getBoolean(true))
			.setComment("Use uuids to store players stats on playerstats.conf?");
			
			try {
				config.getNode("exclude-groups").setValue(config.getNode("exclude-groups").getList(TypeToken.of(String.class), Arrays.asList("staff group","donor group")))
				.setComment("List of groups which will not be considered when checking for possible rank up scenarios.");
			} catch (ObjectMappingException e) {
				e.printStackTrace();
			}
			
			config.getNode("ranked-groups").setComment("All configurations for promote players based on requeriments.");
			if (!config.getNode("ranked-groups").hasMapChildren()){
				
				
				config.getNode("ranked-groups","default").setComment(
						"##################################################################################\n"
						+ "IMPORTANT: Change from \"default\" to exact group name the player need to is in to be promoted to nex group.\n"
						+ "Use the placeholders {newgroup} and {oldgroup} on commands to remove or change a {player} group.\n"
						+ "##################################################################################");
				try {
					config.getNode("ranked-groups","default","execute-commands").setValue(config.getNode("ranked-groups","default","execute-commands")
							.getList(TypeToken.of(String.class), Arrays.asList(
									"lp user {player} parent unset {oldgroup}",
									"lp user {player} parent set {newgroup}",
									"xp 50L {player}")))
									.setComment("Commands to execute when promote. These commands will depend on your permission plugin.");
					
					config.getNode("ranked-groups","default","levels-needed").setValue(config.getNode("ranked-groups","default","levels-needed").getInt(50))
					.setComment("Levels(not experience) needed to promote.");
					
					config.getNode("ranked-groups","default","message-broadcast").setValue(config.getNode("ranked-groups","default","message-broadcast")
							.getString("&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} of server."))
					.setComment("Broadcast the promote messsage to all players.");
					
					config.getNode("ranked-groups","default","minutes-needed").setValue(config.getNode("ranked-groups","default","minutes-needed").getInt(120))
					.setComment("Minutes played needed.");
					
					config.getNode("ranked-groups","default","money-needed").setValue(config.getNode("ranked-groups","default","money-needed").getInt(1000))
					.setComment("Money needed. Do not requires additional plugin.");
					
					config.getNode("ranked-groups","default","next-group").setValue(config.getNode("ranked-groups","default","next-group").getString("Member"))
					.setComment(
							"##################################################################################\n"
							+ "IMPORTANT: Exact name of group to promote player on match the requiriments.\n"
							+ "IMPORTANT: Pay attention on group inherits. I (dev of this plugin) cant help you with permissions plugin configuration.\n"
							+ "The player will be promoted based on command you put on \"execute-commands\" and with placeholder {newgroup} on commands.\n"
							+ "##################################################################################");
					
				} catch (ObjectMappingException e) {
					e.printStackTrace();
				}				
			}
		} catch (IOException e1) {			
			RankUpper.get().getLogger().severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
			return;
		}
		
        //load playerstats
        loadPlayerStats();
        /*---------------*/
                
		save();        			
		RankUpper.get().getLogger().info("All configurations loaded!");
	}
    
	public void loadPlayerStats(){
		File pStats = new File(defDir+File.separator+"playerstats.conf");
		try {
			if (!pStats.exists()) {
		 		pStats.createNewFile();			 	
		    }
			
	    	statsManager = HoconConfigurationLoader.builder().setFile(pStats).build();
	    	stats = statsManager.load();	    	
		} catch (IOException e1) {			
			RankUpper.get().getLogger().severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}		
	}
	
	public void setConfig(Object value, Object... key){		
		config.getNode(key).setValue(value);
	}
	
    public Boolean getBool(Object... key){		
		return config.getNode(key).getBoolean();
	}
        
    public String getString(Object... key){		
		return config.getNode(key).getString();
	}
    
    public Integer getInt(Object... key){		
		return config.getNode(key).getInt();
	}
    
    public List<String> getStringList(Object... key){		
		try {
			return config.getNode(key).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return null;
	}    
    
    public void save(){
    	try {
			plugin.getCfManager().save(config);
		} catch (IOException e) {
			RankUpper.get().getLogger().severe("Problems during save file:");
			e.printStackTrace();
		}
		savePlayersStats(); 
    }
    
    public void savePlayersStats(){
    	try {
			statsManager.save(stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public boolean useUUIDs() {
    	return getBool("use-uuids-instead-names");
    }
    
    /*
	public String getPlayerKeyByName(String player) {
		for (Object key:statsNode.getChildrenMap().keySet()){
			if (statsNode.getNode(key,"PlayerName").getString() != null && statsNode.getNode(key,"PlayerName").getString().equalsIgnoreCase(player)){
				return key;
			}			
		}
		return null;
	}*/

	public void AddPlayer(Player p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		stats.getNode(PlayerString,"PlayerName").setValue(p.getName());
		stats.getNode(PlayerString,"JoinDate").setValue(RUUtil.DateNow());		
		stats.getNode(PlayerString,"LastVisist").setValue(RUUtil.DateNow());	
		stats.getNode(PlayerString,"TimePlayed").setValue(0);			
	}
	
	public void AddPlayer(User p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		stats.getNode(PlayerString,"PlayerName").setValue( p.getName());
		stats.getNode(PlayerString,"JoinDate").setValue(RUUtil.DateNow());		
		stats.getNode(PlayerString,"LastVisist").setValue(RUUtil.DateNow());	
		stats.getNode(PlayerString,"TimePlayed").setValue(0);
		
	}
	
	public void setPlayerTime(String pkey, int time){		
		stats.getNode(pkey,"TimePlayed").setValue(time);
	}
	
	public int addPlayerTime(User p, int ammount){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		int time = stats.getNode(PlayerString,"TimePlayed").getInt();
		stats.getNode(PlayerString,"TimePlayed").setValue(time+ammount);
		return time+ammount;
	}
	
	public void setLastVisit(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
	    stats.getNode(PlayerString,"LastVisist").setValue(RUUtil.DateNow());
	}
		
	public int getStatInt(String key){
		return stats.getNode(key).getInt();
	}
	
	public String getStatString(String key){
		return stats.getNode(key).getString();
	}
		
	public int getPlayerTime(String uuid){		
		return stats.getNode(uuid,"TimePlayed").getInt();
	}
	
	public String getPlayerKey(User user){
		if (useUUIDs() && stats.getNode(user.getUniqueId().toString(),"PlayerName").getString() != null){
			return user.getUniqueId().toString();
		} else if (!useUUIDs() && stats.getNode(user.getName(),"PlayerName").getString() != null){
			return user.getName();
		}
		return null;
	}
	
	public HashMap<String, Object> getPlayerDB(User p){
		HashMap<String, Object> pdb = new HashMap<String, Object>();		
		if (useUUIDs() && stats.getNode(p.getUniqueId().toString(),"PlayerName").getString() != null){
			pdb.put("PlayerName", stats.getNode(p.getUniqueId().toString(),"PlayerName").getString());
			pdb.put("JoinDate", stats.getNode(p.getUniqueId().toString(),"JoinDate").getString());
			pdb.put("LastVisist", stats.getNode(p.getUniqueId().toString(),"LastVisist").getString());
			pdb.put("TimePlayed", stats.getNode(p.getUniqueId().toString(),"TimePlayed").getString());
			return pdb;
		} else if (stats.getNode(p.getName(),"PlayerName").getString() != null){
			pdb.put("PlayerName", stats.getNode(p.getUniqueId().toString(),"PlayerName").getString());
			pdb.put("JoinDate", stats.getNode(p.getUniqueId().toString(),"JoinDate").getString());
			pdb.put("LastVisist", stats.getNode(p.getUniqueId().toString(),"LastVisist").getString());
			pdb.put("TimePlayed", stats.getNode(p.getUniqueId().toString(),"TimePlayed").getString());
			return pdb;
		}		
		return null;		
	}
		
	public void AddPlayerTimes() {
		for (Player p:Sponge.getServer().getOnlinePlayers()){
			RankUpper.get().getLogger().debug("Passou");
			
			if (config.getNode("afk-support").getBoolean()){
				RankUpper.get().getLogger().debug("Berore check AFK!");
				if (RUAFK.isAFK(p)){
					RankUpper.get().getLogger().debug("After check AFK!");
					continue;
				}
			}
			addPlayerTime(p, getInt("update-player-time-minutes"));
			checkRankup(p);
		}
	}
	
	public boolean groupExists(String group){
		return config.getNode("ranked-groups",group).hasMapChildren();
	}
	
	public boolean addGroup(String group, String newGroup, int time, int levels, int money) {
		if (groupExists(group)){
			return false;
		}	
		
		List<String> cmds = Arrays.asList("lp user {player} parent unset {oldgroup}","lp user {player} parent set {newgroup}");
		config.getNode("ranked-groups",group,"execute-commands").setValue(cmds);
		config.getNode("ranked-groups",group,"message-broadcast").setValue("&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} on server.");
		config.getNode("ranked-groups",group,"minutes-needed").setValue(time);
		config.getNode("ranked-groups",group,"levels-needed").setValue(levels);
		config.getNode("ranked-groups",group,"money-needed").setValue(money);
		config.getNode("ranked-groups",group,"next-group").setValue(newGroup);
		save();
		return true;
	}
	
	public Boolean setGroup(String group, String newGroup, int time, int levels, int money) {
		if (!groupExists(group)){
			return false;
		}	
		
		config.getNode("ranked-groups",group,"minutes-needed").setValue(time);
		config.getNode("ranked-groups",group,"levels-needed").setValue(levels);
		config.getNode("ranked-groups",group,"money-needed").setValue(money);
		config.getNode("ranked-groups",group,"next-group").setValue(newGroup);
		save();
		return true;
	}
	
	boolean checkRankup(User p){
		Subject subG = RankUpper.get().getPerms().getHighestGroup(p);

        String pgroup = null;
		if(subG == null){
            List<Subject> pgroups = RankUpper.get().getPerms().getPlayerGroups(p);
            for (Subject sub:pgroups){
                if (config.getNode("ranked-groups",sub.getIdentifier(),"next-group") != null){
                    pgroup = sub.getIdentifier();
                    RankUpper.get().getLogger().debug("Ranked Player Group (not primary) is: "+pgroup);
                    break;
                }
            }
            if (pgroup == null) return false;
		} else {
            pgroup = subG.getIdentifier();
        }

		RankUpper.get().getLogger().debug("Highest Group is: "+pgroup);
		
		String ngroup = config.getNode("ranked-groups",pgroup,"next-group").getString();
					
		if (ngroup == null || ngroup.isEmpty() || !RankUpper.get().getPerms().getAllGroups().contains(ngroup)){
			return false;
		}
						
		int minutesNeeded = config.getNode("ranked-groups",pgroup,"minutes-needed").getInt();
		int moneyNeeded = config.getNode("ranked-groups",pgroup,"money-needed").getInt();
		int levelNeeded = config.getNode("ranked-groups",pgroup,"levels-needed").getInt();
		
		//check for statistics
		for (Entry<Object, ? extends CommentedConfigurationNode> key:config.getNode("ranked-groups",pgroup).getChildrenMap().entrySet()){
			if (Sponge.getRegistry().getType(Statistic.class, key.toString()).isPresent()){
				Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.toString()).get();
				if (p.getStatisticData().get(stat).isPresent() && p.getStatisticData().get(stat).get() < key.getValue().getLong()){
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
				
		for (String cmd:getStringList("ranked-groups",pgroup,"execute-commands")){
			plugin.game.getCommandManager().process(Sponge.getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{oldgroup}", pgroup).replace("{newgroup}", ngroup));
		}
		String message = config.getNode("ranked-groups",pgroup,"message-broadcast").getString();	
		if (message != null && !message.equals("")){
			Sponge.getServer().getBroadcastChannel().send(RUUtil.toText(message.replace("{player}", p.getName()).replace("{time}", RUUtil.timeDescript(minutesNeeded)).replace("{newgroup}", ngroup)));
		}				
		return true;
	}	
}
   
