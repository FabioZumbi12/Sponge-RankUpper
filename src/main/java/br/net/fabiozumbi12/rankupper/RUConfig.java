package br.net.fabiozumbi12.rankupper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import com.google.common.reflect.TypeToken;

public class RUConfig{
				
	private File defConfig = new File(RankUpper.configDir+"config.conf");
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	
	private File pStats = new File(RankUpper.configDir+"playerstats.conf");
	private ConfigurationLoader<CommentedConfigurationNode> statsManager;
	
	private CommentedConfigurationNode configNode;
	private CommentedConfigurationNode tempConfigNode;
	private CommentedConfigurationNode statsNode;
	
	//getters	
	public CommentedConfigurationNode configs(){
		return configNode;
	}
	
	//getters	
	public CommentedConfigurationNode stats(){
		return statsNode;
	}

	RankUpper plugin;
		
	private CommentedConfigurationNode updateFromIn(CommentedConfigurationNode temp, CommentedConfigurationNode out){
		for (Object key:temp.getChildrenMap().keySet()){          	
        	if (temp.getNode(key).hasMapChildren()){        		
        		for (Object key2:temp.getNode(key).getChildrenMap().keySet()){          			
        			if (temp.getNode(key,key2).hasMapChildren()){		        				
		        		for (Object key3:temp.getNode(key,key2).getChildrenMap().keySet()){  
		        			out.getNode(key,key2,key3).setValue(temp.getNode(key,key2,key3).getValue());  
		        			continue;
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue());  
        			continue;
        		}
        	}
        	out.getNode(key).setValue(temp.getNode(key).getValue());    	            	   	            	
        }
		return out;
	}
	
	private CommentedConfigurationNode updateFromOut(CommentedConfigurationNode temp, CommentedConfigurationNode out){
		for (Object key:out.getChildrenMap().keySet()){          	
        	if (out.getNode(key).hasMapChildren()){        		
        		for (Object key2:out.getNode(key).getChildrenMap().keySet()){          			
        			if (out.getNode(key,key2).hasMapChildren()){		        				
		        		for (Object key3:out.getNode(key,key2).getChildrenMap().keySet()){  
		        			out.getNode(key,key2,key3).setValue(temp.getNode(key,key2,key3).getValue(out.getNode(key,key2,key3).getValue()));  
		        			continue;
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue(out.getNode(key,key2).getValue()));  
        			continue;
        		}
        	}
        	out.getNode(key).setValue(temp.getNode(key).getValue(out.getNode(key).getValue()));    	            	   	            	
        }
		return out;
	}
	
	public RUConfig(PluginContainer plugin) {     
		try {
			if (!new File(RankUpper.configDir).exists()){
				new File(RankUpper.configDir).mkdir();
			}
					
	        if (!defConfig.exists()) {
		         defConfig.createNewFile();
		         configManager = HoconConfigurationLoader.builder().setURL(this.getClass().getResource("config.conf")).build();
		         configNode = configManager.load();
		         configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();
		         configManager.save(configNode);
		     }
			
		 	    
		 	 
		} catch (IOException e1) {			
			RULogger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}
		
		//load configs
        try {
        	//tempconfig
        	configManager = HoconConfigurationLoader.builder().setURL(this.getClass().getResource("config.conf")).build();
        	tempConfigNode = configManager.load();
        	
        	configManager = HoconConfigurationLoader.builder().setPath(defConfig.toPath()).build();
        	configNode = configManager.load();
						
		} catch (IOException e1) {
			RULogger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}
               
        //load default configs
        configNode = updateFromIn(tempConfigNode, configNode); 
        
        try {
        	configManager = HoconConfigurationLoader.builder().setPath(defConfig.toPath()).build();
			tempConfigNode = configManager.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

        configNode = updateFromOut(tempConfigNode, configNode); 
        
        //load playerstats
        loadPlayerStats();
                
		save();        			
		RULogger.info("All configurations loaded!");
	}
    
	public void loadPlayerStats(){
		try {
			if (!pStats.exists()) {
		 		pStats.createNewFile();
			 	statsManager = HoconConfigurationLoader.builder().setURL(this.getClass().getResource("playerstats.conf")).build();
			 	statsNode = statsManager.load();
				statsManager = HoconConfigurationLoader.builder().setFile(pStats).build();
				statsManager.save(statsNode);
		    }
			
	    	statsManager = HoconConfigurationLoader.builder().setPath(pStats.toPath()).build();
	    	statsNode = statsManager.load();
	    	
		} catch (IOException e1) {			
			RULogger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}		
	}
	
	public void setConfig(String key, Object value){		
		getNodes(key).setValue(value);
	}
	
    public Boolean getBool(String key){		
		return getNodes(key).getBoolean();
	}
        
    public String getString(String key){		
		return getNodes(key).getString();
	}
    
    public Integer getInt(String key){		
		return getNodes(key).getInt();
	}
    
    public List<String> getStringList(String key){		
		try {
			return getNodes(key).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return null;
	}    
    
    private CommentedConfigurationNode getStatsNodes(String key){    	
    	String[] args = key.split("\\.");
    	if (args.length == 1){
    		return statsNode.getNode(args[0]);
    	}
    	if (args.length == 2){
    		return statsNode.getNode(args[0],args[1]);
    	}
    	if (args.length == 3){
    		return statsNode.getNode(args[0],args[1],args[2]);
    	}
    	if (args.length == 4){
    		return statsNode.getNode(args[0],args[1],args[2],args[3]);
    	}
    	if (args.length == 5){
    		return statsNode.getNode(args[0],args[1],args[2],args[3],args[4]);
    	}
    	return null;
    }
    
    private CommentedConfigurationNode getNodes(String key) {    	
    	String[] args = key.split("\\.");
    	if (args.length == 1){
    		return configNode.getNode(args[0]);
    	}
    	if (args.length == 2){
    		return configNode.getNode(args[0],args[1]);
    	}
    	if (args.length == 3){
    		return configNode.getNode(args[0],args[1],args[2]);
    	}
    	if (args.length == 4){
    		return configNode.getNode(args[0],args[1],args[2],args[3]);
    	}
    	if (args.length == 5){
    		return configNode.getNode(args[0],args[1],args[2],args[3],args[4]);
    	}
    	return null;
    }
    
    public void save(){
    	try {
			configManager.save(configNode);
		} catch (IOException e) {
			RULogger.severe("Problems during save file:");
			e.printStackTrace();
		}
		savePlayersStats(); 
    }
    
    public void savePlayersStats(){
    	try {
			statsManager.save(statsNode);
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
		try {
			statsNode.getNode(PlayerString,"PlayerName").setValue(TypeToken.of(String.class), p.getName());
			statsNode.getNode(PlayerString,"JoinDate").setValue(TypeToken.of(String.class), RUUtil.DateNow());		
			statsNode.getNode(PlayerString,"LastVisist").setValue(TypeToken.of(String.class), RUUtil.DateNow());	
			statsNode.getNode(PlayerString,"TimePlayed").setValue(TypeToken.of(Integer.class), 0);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}			
	}
	
	public void AddPlayer(User p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		try {
			statsNode.getNode(PlayerString,"PlayerName").setValue(TypeToken.of(String.class), p.getName());
			statsNode.getNode(PlayerString,"JoinDate").setValue(TypeToken.of(String.class), RUUtil.DateNow());		
			statsNode.getNode(PlayerString,"LastVisist").setValue(TypeToken.of(String.class), RUUtil.DateNow());	
			statsNode.getNode(PlayerString,"TimePlayed").setValue(TypeToken.of(Integer.class), 0);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setPlayerTime(String pkey, int time){		
		try {
			statsNode.getNode(pkey,"TimePlayed").setValue(TypeToken.of(Integer.class), time);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}
	
	public int addPlayerTime(User p, int ammount){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		int time = getStatsNodes(PlayerString + ".TimePlayed").getInt();
		try {
			statsNode.getNode(PlayerString,"TimePlayed").setValue(TypeToken.of(Integer.class), time+ammount);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return time+ammount;
	}
	
	public void setLastVisit(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
	    try {
			statsNode.getNode(PlayerString,"LastVisist").setValue(TypeToken.of(String.class),  RUUtil.DateNow());
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}
		
	public int getStatInt(String key){
		return getStatsNodes(key).getInt();
	}
	
	public String getStatString(String key){
		return getStatsNodes(key).getString();
	}
		
	public int getPlayerTime(String uuid){		
		return getStatsNodes(uuid+".TimePlayed").getInt();
	}
	
	public String getPlayerKey(User user){
		if (useUUIDs() && statsNode.getNode(user.getUniqueId().toString(),"PlayerName").getString() != null){
			return user.getUniqueId().toString();
		} else if (!useUUIDs() && statsNode.getNode(user.getName(),"PlayerName").getString() != null){
			return user.getName();
		}
		return null;
	}
	
	public HashMap<String, Object> getPlayerDB(User p){
		HashMap<String, Object> pdb = new HashMap<String, Object>();		
		if (useUUIDs() && statsNode.getNode(p.getUniqueId().toString(),"PlayerName").getString() != null){
			pdb.put("PlayerName", getStatsNodes(p.getUniqueId().toString() + ".PlayerName").getString());
			pdb.put("JoinDate", getStatsNodes(p.getUniqueId().toString() + ".JoinDate").getString());
			pdb.put("LastVisist", getStatsNodes(p.getUniqueId().toString() + ".LastVisist").getString());
			pdb.put("TimePlayed", getStatsNodes(p.getUniqueId().toString() + ".TimePlayed").getString());
			return pdb;
		} else if (statsNode.getNode(p.getName(),"PlayerName").getString() != null){
			pdb.put("PlayerName", getStatsNodes(p.getUniqueId().toString() + ".PlayerName").getString());
			pdb.put("JoinDate", getStatsNodes(p.getUniqueId().toString() + ".JoinDate").getString());
			pdb.put("LastVisist", getStatsNodes(p.getUniqueId().toString() + ".LastVisist").getString());
			pdb.put("TimePlayed", getStatsNodes(p.getUniqueId().toString() + ".TimePlayed").getString());
			return pdb;
		}		
		return null;		
	}
	
	public void AddPlayerTimes() {
		for (Player p:Sponge.getServer().getOnlinePlayers()){
			if(getBool("afk-support")){
				if(plugin.getRUAFK().isPlayer(p)){
					return;
				}
			}
			addPlayerTime(p, getInt("update-player-time-minutes"));
			if (!checkRankup(p)){
				continue;
			}
		}
	}
	
	public boolean groupExists(String group){
		return RankUpper.cfgs.getString("ranked-groups." + group + ".next-group") != null;
	}
	
	public Boolean addGroup(String group, String newGroup, int time, int levels, int money, int index) {
		if (groupExists(group)){
			return false;
		}	
		
		List<String> cmds = Arrays.asList("pex user {player} parent delete group {oldgroup}","pex user {player} parent add group {newgroup}");
		try {			
			getNodes("ranked-groups." + group + ".execute-commands").setValue(cmds);
			getNodes("ranked-groups." + group + ".message-broadcast").setValue(TypeToken.of(String.class), "&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} on server.");
			getNodes("ranked-groups." + group + ".minutes-needed").setValue(TypeToken.of(Integer.class), time);
			getNodes("ranked-groups." + group + ".levels-needed").setValue(TypeToken.of(Integer.class), levels);
			getNodes("ranked-groups." + group + ".money-needed").setValue(TypeToken.of(Integer.class), money);
			getNodes("ranked-groups." + group + ".next-group").setValue(TypeToken.of(String.class), newGroup);
			getNodes("ranked-groups." + group + ".index").setValue(TypeToken.of(Integer.class), index);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
		save();
		return true;
	}
	
	public Boolean setGroup(String group, String newGroup, int time, int levels, int money) {
		if (!groupExists(group)){
			return false;
		}	
		
		try {
			getNodes("ranked-groups." + group + ".minutes-needed").setValue(TypeToken.of(Integer.class), time);
			getNodes("ranked-groups." + group + ".levels-needed").setValue(TypeToken.of(Integer.class), levels);
			getNodes("ranked-groups." + group + ".money-needed").setValue(TypeToken.of(Integer.class), money);
			getNodes("ranked-groups." + group + ".next-group").setValue(TypeToken.of(String.class), newGroup);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
		save();
		return true;
	}
	
	boolean checkRankup(User p){
		
		String pgroup = RankUpper.perms.getHighestGroup(p);
		if(pgroup.isEmpty()){
			return false;
		}
		RULogger.debug("Highest Group is: "+pgroup);
		
		String ngroup = RankUpper.cfgs.getString("ranked-groups."+ pgroup +".next-group");
					
		if (ngroup == null || ngroup.isEmpty()){
			return false;
		}
						
		int minutesNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".minutes-needed");
		int moneyNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".money-needed");
		int levelNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".levels-needed");

		if (minutesNeeded != 0){
			if (RankUpper.cfgs.getPlayerTime(RankUpper.cfgs.getPlayerKey(p)) < minutesNeeded){
				return false;
			}					
		}

		if (moneyNeeded != 0){
			UniqueAccount acc = RankUpper.econ.getOrCreateAccount(p.getUniqueId()).get();
			if (acc.getBalance(RankUpper.econ.getDefaultCurrency()).intValue() < moneyNeeded){
				return false;
			} 
		}

		if (levelNeeded != 0){				
			if (p.get(Keys.EXPERIENCE_LEVEL).isPresent() && p.get(Keys.EXPERIENCE_LEVEL).get() < levelNeeded){
				return false;
			} 
		}
		
		for (String cmd:getStringList("ranked-groups."+ pgroup +".execute-commands")){
			RankUpper.game.getCommandManager().process(Sponge.getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{oldgroup}", pgroup).replace("{newgroup}", ngroup));
		}
		String message = getString("ranked-groups."+ pgroup +".message-broadcast");	
		if (message != null && !message.equals("")){
			Sponge.getServer().getBroadcastChannel().send(RUUtil.toText(message.replace("{player}", p.getName()).replace("{time}", RUUtil.timeDescript(minutesNeeded)).replace("{newgroup}", ngroup)));
		}				
		return true;
	}	
}
   
