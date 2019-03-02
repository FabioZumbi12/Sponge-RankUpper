package br.net.fabiozumbi12.RankUpper.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.statistic.Statistic;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.text.Text;

public class RUConfig{
	
	//getters
	private CommentedConfigurationNode configRoot;
	private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
	private MainCategory root;
	public MainCategory root(){
		return this.root;
	}

	private File defConfig = new File(RankUpper.get().getConfigDir() ,"rankupper.conf");
	
	public RUConfig(GuiceObjectMapperFactory factory) {
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
        /*---------------*/
                
		save();        			
		RankUpper.get().getLogger().log("All configurations loaded!");
	}

    public void save(){
    	try {
			cfgLoader.save(configRoot);
		} catch (IOException e) {
			RankUpper.get().getLogger().severe("Problems during save file:");
			e.printStackTrace();
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
			if (key.getValue() > 0 && Sponge.getRegistry().getType(Statistic.class, key.getKey()).isPresent()){
				Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.getKey()).get();
				if (!p.getStatisticData().get(stat).isPresent() || p.getStatisticData().get(stat).get() < key.getValue()){
					return false;
				}
			}
		}

		//check scoreboards
        if (Sponge.getServer().getServerScoreboard().isPresent()){
            for (Entry<String, Long> key:root.ranked_groups.get(pgroup).minecraft_scoreboards.entrySet()){
                if (key.getValue() > 0 && !Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).isEmpty()){
                    Score score = Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).stream().findFirst().get();
                    if (score.getScore() < key.getValue()){
                        return false;
                    }
                }
            }
        }

		if (minutesNeeded != 0){
			if (RankUpper.get().getStats().getPlayerTime(RankUpper.get().getStats().getPlayerKey(p)) < minutesNeeded){
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
   
