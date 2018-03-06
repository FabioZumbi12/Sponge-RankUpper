package br.net.fabiozumbi12.RankUpper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

public class RUCommands {

	private static RankUpper plugin;

	public static void init(RankUpper pl){
		plugin = pl;		
		Sponge.getCommandManager().register(plugin, rankUpper(), "rankupper", "ru");
	}
	
	private static CommandCallable rankUpper() {
		//ru addgroup <group> <newgroup> <minutes> <levels> <money>
		CommandSpec addgroup = CommandSpec.builder()
				.description(Text.of("Add groups to config."))
				.permission("rankupper.addgroup")
				.arguments(
						GenericArguments.string(Text.of("group")),
						GenericArguments.string(Text.of("next-group")),
						GenericArguments.integer(Text.of("minutes")),
						GenericArguments.integer(Text.of("levels")),
						GenericArguments.integer(Text.of("money")))
			    .executor((src, args) -> { {	
			    	int time = args.<Integer>getOne("minutes").get();
					int levels = args.<Integer>getOne("levels").get();
					int money = args.<Integer>getOne("money").get();
					String group = args.<String>getOne("group").get();
					String newGroup = args.<String>getOne("next-group").get();
					if (RankUpper.get().getConfig().addGroup(group, newGroup, time, levels, money)){						
						RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("config.addedgroup"));
					} else {
						RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("config.notaddedgroup"));
					}
			    	return CommandResult.success();	
			    }})
			    .build();
		
		//ru setgroup <group> <newgroup> <minutes> <levels> <money>
		CommandSpec setgroup = CommandSpec.builder()
				.description(Text.of("Edit groups on config."))
				.permission("rankupper.setgroup")
				.arguments(
						GenericArguments.string(Text.of("group")),
						GenericArguments.string(Text.of("next-group")),
						GenericArguments.integer(Text.of("minutes")),
						GenericArguments.integer(Text.of("levels")),
						GenericArguments.integer(Text.of("money")))
			    .executor((src, args) -> { {	
			    	int time = args.<Integer>getOne("minutes").get();
					int levels = args.<Integer>getOne("levels").get();
					int money = args.<Integer>getOne("money").get();
					String group = args.<String>getOne("group").get();
					String newGroup = args.<String>getOne("next-group").get();
					if (RankUpper.get().getConfig().setGroup(group, newGroup, time, levels, money)){						
						RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("config.setgroup"));
					} else {
						RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("config.notsetgroup"));
					}
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec set = CommandSpec.builder()
				.description(Text.of("Set time played by player."))
				.permission("rankupper.set")
				.arguments(
						GenericArguments.user(Text.of("player")),
						GenericArguments.integer(Text.of("minutes")))
			    .executor((src, args) -> { {
			    	int time = args.<Integer>getOne("minutes").get();
			    	RankUpper.get().getConfig().setPlayerTime(RankUpper.get().getConfig().getPlayerKey(args.<User>getOne("player").get()), time);
					RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("commands.setto").replace("{time}", RUUtil.timeDescript(time)).replace("{player}", args.<User>getOne("player").get().getName()));
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec add = CommandSpec.builder()
				.description(Text.of("Add time played by player."))
				.permission("rankupper.add")
				.arguments(
						GenericArguments.user(Text.of("player")),
						GenericArguments.integer(Text.of("minutes")))
			    .executor((src, args) -> { {
			    	int time = args.<Integer>getOne("minutes").get();
					RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.get().getConfig().addPlayerTime(args.<User>getOne("player").get(), time))).replace("{player}", args.<User>getOne("player").get().getName()));
					return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec check = CommandSpec.builder()
				.description(Text.of("Check if requeriments is done to rankup."))
				.permission("rankupper.check")
				.arguments(
						GenericArguments.optional(GenericArguments.user(Text.of("player"))))
			    .executor((src, args) -> { {
			    	if (args.hasAny("player") && src.hasPermission("rankupper.check-others")){
			    		SendCheckMessage(src, args.<User>getOne("player").get());
			    	}
			    	else if (src instanceof Player){
			    		if (RankUpper.get().getConfig().checkRankup((Player)src)){
							return CommandResult.success();
						}
						SendCheckMessage(src, (Player)src);
			    	}			    	
					return CommandResult.success();
			    }})
			    .build();
		
		CommandSpec playerInfo = CommandSpec.builder()
				.description(Text.of("See player infos."))
				.permission("rankupper.player-info")
				.arguments(GenericArguments.user(Text.of("player")))
			    .executor((src, args) -> { {	
			    	HashMap<String, Object> pdb = new HashMap<String, Object>();
			    	if (RankUpper.get().getConfig().getPlayerDB(args.<User>getOne("player").get()) != null){
						pdb = RankUpper.get().getConfig().getPlayerDB(args.<User>getOne("player").get());
						RankUpper.get().getLang().sendMessage(src, "Player Info:");
						src.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						src.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						src.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisist")));
						src.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript(Integer.parseInt((String)pdb.get("TimePlayed")))));
						return CommandResult.success();	
					} else {
						throw new CommandException(RUUtil.toText(RankUpper.get().getLang().get("commands.unknownplayer").replace("{player}", args.<User>getOne("player").get().getName())), true);
					}
			    }})
			    .build();
		
		CommandSpec top = CommandSpec.builder()
				.description(Text.of("Check the top played times."))
				.permission("rankupper.top")
			    .executor((src, args) -> { {	
			    	ExecuteTopCount(src);
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec help = CommandSpec.builder()
				.description(Text.of("Help command for rankupper."))
			    .executor((src, args) -> { {	
			    	sendHelp(src);
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec reload = CommandSpec.builder()
				.description(Text.of("Reload rankupper."))
				.permission("rankupper.reload")
			    .executor((src, args) -> { {	
			    	plugin.reload();
			    	src.sendMessage(RUUtil.toText("&aRankUpper reloaded!"));
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec saveAll = CommandSpec.builder()
				.description(Text.of("Force save rankuppper stats."))
				.permission("rankupper.save-all")
			    .executor((src, args) -> { {	
			    	RankUpper.get().getConfig().savePlayersStats();
					src.sendMessage(RUUtil.toText("&aPlayer stats Saved!"));
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec loadAll = CommandSpec.builder()
				.description(Text.of("Force load rankuppper stats."))
				.permission("rankupper.load-all")
			    .executor((src, args) -> { {	
			    	RankUpper.get().getConfig().loadPlayerStats();
					src.sendMessage(RUUtil.toText("&aPlayer stats Loaded!"));
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec listGroups = CommandSpec.builder()
				.description(Text.of("List all groups from on config."))
				.permission("rankupper.list-groups")
			    .executor((src, args) -> { {	
			    	CommentedConfigurationNode node = RankUpper.get().getConfig().configs().getNode("ranked-groups");
			    	src.sendMessage(RUUtil.toText("&b--------------- RankUpper Groups ---------------"));
			    	node.getChildrenMap().keySet().stream().forEachOrdered((key)->{
			    		src.sendMessage(RUUtil.toText("&7Group: &a"+key.toString()));
			    		try {
			    			src.sendMessage(RUUtil.toText("&7Commands: "));
			    			node.getNode(key,"execute-commands").getList(TypeToken.of(String.class)).stream().forEach((cmd)->{
								src.sendMessage(RUUtil.toText("&a-- &b"+cmd));
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
			    		src.sendMessage(RUUtil.toText("&7Minutes: &a"+node.getNode(key,"minutes-needed").getInt(0)+" ("+RUUtil.timeDescript(node.getNode(key,"minutes-needed").getInt(0))+")"));
			    		src.sendMessage(RUUtil.toText("&7Levels: &a"+node.getNode(key,"levels-needed").getInt(0)));			    		
			    		src.sendMessage(RUUtil.toText("&7Money: &a"+node.getNode(key,"money-needed").getInt(0)));			    		
			    		src.sendMessage(RUUtil.toText("&7Next Group: &a"+node.getNode(key,"next-group").getString("&cNone")));
			    		src.sendMessage(RUUtil.toText("&7Message: &a"+node.getNode(key,"message-broadcast").getString("")));
			    		src.sendMessage(RUUtil.toText("&b---------------------------------------------"));
			    	}); 
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec ru = CommandSpec.builder()
			    .description(Text.of("Main command for rankupper."))
			    .executor((src, args) -> { {	    	
			    	RankUpper.get();
					//no args
			    	src.sendMessage(RUUtil.toText("&b---------------- "+RankUpper.get().instance()+" "+RankUpper.get().instance().getVersion().get()+" ---------------"));
			    	src.sendMessage(RUUtil.toText("&bDeveloped by &6" + RankUpper.get().instance().getAuthors().get(0) + "."));
			    	src.sendMessage(RUUtil.toText("&bFor more information about the commands, type [" + "&6/ru ?&b]."));
			    	src.sendMessage(RUUtil.toText("&b---------------------------------------------------"));			         
			    	return CommandResult.success();	
			    }})
			    .child(help, "?", "help")
			    .child(reload, "reload", "rl")
			    .child(addgroup, "addgroup")
			    .child(setgroup, "setgroup")
			    .child(add, "add")
			    .child(set, "set")
			    .child(check, "check")
			    .child(top, "top")
			    .child(playerInfo, "player-info")
			    .child(saveAll, "save-all")
			    .child(loadAll, "load-all")
			    .child(listGroups, "list-groups")
			    .build();
		
		return ru;	
	}

	private static void ExecuteTopCount(CommandSource p) {
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		for (Object uuid:RankUpper.get().getConfig().stats().getChildrenMap().keySet()){
			String play = RankUpper.get().getConfig().stats().getNode(uuid.toString(),"PlayerName").getString();
			if (RankUpper.get().getConfig().getPlayerTime(uuid.toString()) > 0){
				stats.put(play, RankUpper.get().getConfig().getPlayerTime(uuid.toString()));
			}
		}				
		int top10 = 0; 
		Map<String, Integer> Sorted = RUUtil.sort(stats);
		RankUpper.get().getLang().sendMessage(p, "commands.top10");
	    for (String plr:Sorted.keySet()){
		    top10++;
		    int time = Sorted.get(plr);					    
		    String color = "&2";
		    if (top10 == 1){color = "&e&l";}
		    if (top10 == 2){color = "&7&l";}
		    if (top10 == 3){color = "&6&l";}
		    if (top10 == 10){color = "&c";}
		    p.sendMessage(RUUtil.toText("&3" + top10 + ". "+color+plr+": &a" + RUUtil.timeDescript(time)));
		    if (top10 == 10){
		    	top10 = 0;
			    break;
		    }
	    }		
	}

	private static void SendCheckMessage(CommandSource sender, User playerToCheck) {
		int time = RankUpper.get().getConfig().getPlayerTime(RankUpper.get().getConfig().getPlayerKey(playerToCheck));
		Subject subG = RankUpper.get().getPerms().getHighestGroup(playerToCheck);
		String pgroup = "";
		String dispName = "";
		if (subG != null){
			pgroup = subG.getIdentifier();
			if (subG.getOption("display_name").isPresent()){
				dispName = subG.getOption("display_name").get();
			} else {
				dispName = pgroup;
			}	
		} 
		
		RankUpper.get().getLogger().debug("Player Groups: "+pgroup);
		if(pgroup.isEmpty()) {
			RankUpper.get().getLang().sendMessage(sender, RankUpper.get().getLang().get("commands.check.youplayed").replace("{time}", RUUtil.timeDescript(time)).replace("{group}", "none"));
		} else {
			RankUpper.get().getLang().sendMessage(sender, RankUpper.get().getLang().get("commands.check.youplayed").replace("{time}", RUUtil.timeDescript(time)).replace("{group}", dispName));
		}
        String ngroup = RankUpper.get().getConfig().getString("ranked-groups",pgroup,"next-group");
				

		if (ngroup == null || ngroup.isEmpty() || !RankUpper.get().getPerms().getAllGroups().contains(ngroup)){
			return;
		}
		
		sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("commands.nextgroup").replace("{group}", ngroup)));
		
		int minutesNeeded = RankUpper.get().getConfig().getInt("ranked-groups",pgroup,"minutes-needed");
		int moneyNeeded = RankUpper.get().getConfig().getInt("ranked-groups",pgroup,"money-needed");
		int levelNeeded = RankUpper.get().getConfig().getInt("ranked-groups",pgroup,"levels-needed");		
				
		if (minutesNeeded != 0){
			if (RankUpper.get().getConfig().getPlayerTime(RankUpper.get().getConfig().getPlayerKey(playerToCheck)) >= minutesNeeded){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.time") + ": &a" + RUUtil.timeDescript(minutesNeeded) + " - " + RankUpper.get().getLang().get("config.ok")));
			} else {
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.time") + ": &c" + RUUtil.timeDescript(minutesNeeded)));
			}
		}

		if (moneyNeeded != 0 && RankUpper.get().getEconomy() != null){
			UniqueAccount acc = RankUpper.get().getEconomy().getOrCreateAccount(playerToCheck.getUniqueId()).get();
			int usermoney = acc.getBalance(RankUpper.get().getEconomy().getDefaultCurrency()).intValue();
			if (usermoney >= moneyNeeded){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.money") + ": &a" + RankUpper.get().getLang().get("config.cifra")+" "+ usermoney+"/"+moneyNeeded + " - " + RankUpper.get().getLang().get("config.ok")));
			} else {
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.money") + ": &c" + RankUpper.get().getLang().get("config.cifra")+" "+ usermoney+"/"+moneyNeeded));
			}
		}

		if (levelNeeded != 0){
			if (!playerToCheck.get(Keys.EXPERIENCE_LEVEL).isPresent()){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.levels") + ": &c0/"+levelNeeded + " Lvs."));
				return;
			}
			int lvls = playerToCheck.get(Keys.EXPERIENCE_LEVEL).get();
			if (lvls >= levelNeeded){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.levels") + ": &a" + lvls+"/"+levelNeeded + " Lvs. - " + RankUpper.get().getLang().get("config.ok")));
			} else {
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.levels") + ": &c" + lvls+"/"+levelNeeded + " Lvs."));
			}
		}		

		//check for statistics
		for (Entry<Object, ? extends CommentedConfigurationNode> key:RankUpper.get().getConfig().configs().getNode("ranked-groups",pgroup).getChildrenMap().entrySet()){
			if (Sponge.getRegistry().getType(Statistic.class, key.toString()).isPresent()){
				Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.toString()).get();
				long needed = key.getValue().getLong();
				long actual =  playerToCheck.getStatisticData().get(stat).get();
				if (playerToCheck.getStatisticData().get(stat).isPresent()){
					if (actual >= needed){
						sender.sendMessage(RUUtil.toText(stat.getName() + ": &a"+actual+"/"+needed + " - " + RankUpper.get().getLang().get("config.ok")));
					} else {
						sender.sendMessage(RUUtil.toText(stat.getName() + ": &c"+actual+"/"+needed));
					}
				}										
			}
		}
	}
	
	private static void sendHelp(CommandSource source){
		RankUpper.get().getLang().sendMessage(source, RankUpper.get().getLang().get("commands.commandshelp"));
		for (String key:RankUpper.get().getLang().helpStrings()){			
			if (source.hasPermission("rankupper."+key)) {
				source.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("commands.help."+key)));					
			} else
			if (source.hasPermission("rankupper."+key+".others")){
				source.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("commands.help."+key+".others")));
			}			
		}
	}	
}
