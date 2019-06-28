package br.net.fabiozumbi12.RankUpper;

import br.net.fabiozumbi12.RankUpper.config.RankedGroupsCategory;
import br.net.fabiozumbi12.RankUpper.config.StatsCategory;
import me.rojo8399.placeholderapi.PlaceholderService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
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
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
			    	RankUpper.get().getStats().setPlayerTime(RankUpper.get().getStats().getPlayerKey(args.<User>getOne("player").get()), time);
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
					RankUpper.get().getLang().sendMessage(src, RankUpper.get().getLang().get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.get().getStats().addPlayerTime(args.<User>getOne("player").get(), time))).replace("{player}", args.<User>getOne("player").get().getName()));
					return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec check = CommandSpec.builder()
				.description(Text.of("Check if requirements is done to rankup."))
				.permission("rankupper.check")
				.arguments(
						GenericArguments.optional(GenericArguments.user(Text.of("player"))))
			    .executor((src, args) -> { {
			    	if (args.hasAny("player") && src.hasPermission("rankupper.check-others")){
                        if (RankUpper.get().getConfig().root().check_rankup && RankUpper.get().getConfig().checkRankup(args.<User>getOne("player").get())){
                            return CommandResult.success();
                        }
			    		SendCheckMessage(src, args.<User>getOne("player").get());
			    	}
			    	else if (src instanceof Player){
                        if (RankUpper.get().getConfig().root().check_rankup && RankUpper.get().getConfig().checkRankup((Player)src)){
                            return CommandResult.success();
                        }
						SendCheckMessage(src, (Player)src);
			    	}			    	
					return CommandResult.success();
			    }})
			    .build();

        CommandSpec rankup = CommandSpec.builder()
                .description(Text.of("If requirements is done, rankup."))
                .permission("rankupper.rankup")
                .arguments(
                        GenericArguments.optional(GenericArguments.user(Text.of("player"))))
                .executor((src, args) -> { {
                    if (args.hasAny("player") && src.hasPermission("rankupper.rankup-others")){
                        if (RankUpper.get().getConfig().checkRankup(args.<User>getOne("player").get())){
                            return CommandResult.success();
                        }
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
			    	HashMap<String, Object> pdb = RankUpper.get().getStats().getPlayerDB(args.<User>getOne("player").get());
			    	if (!pdb.isEmpty()){
						RankUpper.get().getLang().sendMessage(src, "Player Info:");
						src.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						src.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						src.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisit")));
						src.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript((int)pdb.get("TimePlayed"))));
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
                    RankUpper.get().getLang().sendMessage(src, "&aRankUpper reloaded!");
			    	return CommandResult.success();	
			    }})
			    .build();
		
		CommandSpec saveAll = CommandSpec.builder()
				.description(Text.of("Force save rankuppper stats."))
				.permission("rankupper.save-all")
			    .executor((src, args) -> { {	
			    	RankUpper.get().getStats().savePlayersStats();
                    RankUpper.get().getLang().sendMessage(src, "&aPlayer stats Saved!");
			    	return CommandResult.success();	
			    }})
			    .build();

		CommandSpec loadAll = CommandSpec.builder()
				.description(Text.of("Force load rankuppper stats."))
				.permission("rankupper.load-all")
			    .executor((src, args) -> { {
			    	RankUpper.get().getStats().loadPlayerStats();
                    RankUpper.get().getLang().sendMessage(src, "&aPlayer stats Loaded!");
			    	return CommandResult.success();
			    }})
			    .build();

		CommandSpec listGroups = CommandSpec.builder()
				.description(Text.of("List all groups from on config."))
				.permission("rankupper.list-groups")
			    .executor((src, args) -> { {	
			    	Map<String, RankedGroupsCategory> groups = RankUpper.get().getConfig().root().ranked_groups;
			    	src.sendMessage(RUUtil.toText("&b--------------- RankUpper Groups ---------------"));
					groups.keySet().forEach((key)->{
			    		src.sendMessage(RUUtil.toText("&7Group: &a"+key));
			    		try {
			    			src.sendMessage(RUUtil.toText("&7Commands: "));
							groups.get(key).execute_commands.forEach((cmd)-> src.sendMessage(RUUtil.toText("&a-- &b"+cmd)));
						} catch (Exception e) {
							e.printStackTrace();
						}
			    		src.sendMessage(RUUtil.toText("&7Minutes: &a"+groups.get(key).minutes_needed+" ("+RUUtil.timeDescript(groups.get(key).minutes_needed)+")"));
			    		src.sendMessage(RUUtil.toText("&7Levels: &a"+groups.get(key).levels_needed));
			    		src.sendMessage(RUUtil.toText("&7Money: &a"+groups.get(key).money_needed));
			    		src.sendMessage(RUUtil.toText("&7Next Group: &a"+groups.get(key).next_group));
			    		src.sendMessage(RUUtil.toText("&7Message: &a"+groups.get(key).message_broadcast));
			    		src.sendMessage(RUUtil.toText("&b---------------------------------------------"));
			    	}); 
			    	return CommandResult.success();	
			    }})
			    .build();

        CommandSpec backup = CommandSpec.builder()
                .description(Text.of("Mak a backup of player stats to file."))
                .permission("rankupper.backup")
                .executor((src, args) -> { {
                    try {
                        File statsConf = new File(RankUpper.get().getConfigDir() ,"playerstats.conf");
                        ConfigurationLoader<CommentedConfigurationNode> statsManager = HoconConfigurationLoader.builder().setFile(statsConf).build();
                        CommentedConfigurationNode tempStats = statsManager.load();

                        RankUpper.get().getStats().stats().players.forEach((key, value) -> {
                            String pKey = key;
                            if (pKey == null || pKey.isEmpty()){
                                if (Sponge.getServer().getPlayer(value.PlayerName).isPresent())
                                    pKey = Sponge.getServer().getPlayer(value.PlayerName).get().getUniqueId().toString();
                                else
                                    pKey = value.PlayerName;
                            }
                            CommentedConfigurationNode pNode = tempStats.getNode(pKey);
                            pNode.getNode("JoinDate").setValue(value.JoinDate);
                            pNode.getNode("LastVisist").setValue(value.LastVisit);
                            pNode.getNode("PlayerName").setValue(value.PlayerName);
                            pNode.getNode("TimePlayed").setValue(value.TimePlayed);
                        });

                        statsManager.save(tempStats);
                        RankUpper.get().getLang().sendMessage(src, "&aBackup saved to playerstats.conf\n&eThis backup will be loaded on next server start or on ru reload!");

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return CommandResult.success();
                }})
                .build();

        return CommandSpec.builder()
                .description(Text.of("Main command for rankupper."))
                .executor((src, args) -> { {
                    //no args
                    src.sendMessage(RUUtil.toText("&b---------------- "+RankUpper.get().instance().getName()+" "+RankUpper.get().instance().getVersion().get()+" ---------------"));
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
                .child(rankup, "rankup", "up")
                .child(top, "top")
                .child(playerInfo, "player-info")
                .child(saveAll, "save-all")
                .child(loadAll, "load-all")
                .child(listGroups, "list-groups")
                .child(backup, "backup")
                .build();
	}

	private static void ExecuteTopCount(CommandSource p) {
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		for (StatsCategory.PlayerInfoCategory key:RankUpper.get().getStats().stats().players.values()){
			String play = key.PlayerName;
			if (key.TimePlayed > 0){
				stats.put(play, key.TimePlayed);
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
			    break;
		    }
	    }		
	}

	private static void SendCheckMessage(CommandSource sender, User playerToCheck) {
		int time = RankUpper.get().getStats().getPlayerTime(RankUpper.get().getStats().getPlayerKey(playerToCheck));
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
        String ngroup = RankUpper.get().getConfig().root().ranked_groups.get(pgroup) != null ? RankUpper.get().getConfig().root().ranked_groups.get(pgroup).next_group : null;
				

		if (ngroup == null || ngroup.isEmpty() || !RankUpper.get().getPerms().getAllGroups().contains(ngroup)){
			return;
		}
		
		sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("commands.nextgroup").replace("{group}", ngroup)));
		
		int minutesNeeded = RankUpper.get().getConfig().root().ranked_groups.get(pgroup).minutes_needed;
		int moneyNeeded = RankUpper.get().getConfig().root().ranked_groups.get(pgroup).money_needed;
		int levelNeeded = RankUpper.get().getConfig().root().ranked_groups.get(pgroup).levels_needed;
				
		if (minutesNeeded > 0){
			if (RankUpper.get().getStats().getPlayerTime(RankUpper.get().getStats().getPlayerKey(playerToCheck)) >= minutesNeeded){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.time") + ": &a" + RUUtil.timeDescript(minutesNeeded) + " - " + RankUpper.get().getLang().get("config.ok")));
			} else {
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.time") + ": &c" + RUUtil.timeDescript(minutesNeeded)));
			}
		}

		if (moneyNeeded > 0 && RankUpper.get().getEconomy() != null){
			UniqueAccount acc = RankUpper.get().getEconomy().getOrCreateAccount(playerToCheck.getUniqueId()).get();
			double userMoney = acc.getBalance(RankUpper.get().getEconomy().getDefaultCurrency()).doubleValue();
			String userMoneyStr = String.format("%.2f",userMoney);
			if (userMoney >= moneyNeeded){
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.money") + ": &a" + RankUpper.get().getLang().get("config.cifra")+" "+ userMoneyStr+"/"+moneyNeeded + " - " + RankUpper.get().getLang().get("config.ok")));
			} else {
				sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.money") + ": &c" + RankUpper.get().getLang().get("config.cifra")+" "+ userMoneyStr+"/"+moneyNeeded));
			}
		}

		if (levelNeeded > 0){
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
		for (Entry<String, Long> key:RankUpper.get().getConfig().root().ranked_groups.get(pgroup).minecraft_statistic.entrySet()){
			if (key.getValue() > 0 && Sponge.getRegistry().getType(Statistic.class, key.getKey()).isPresent()){
				Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.getKey()).get();
				long needed = key.getValue();
				long actual = playerToCheck.getStatisticData().get(stat).isPresent() ? playerToCheck.getStatisticData().get(stat).get() : 0;
				if (actual >= needed){
					sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.statistics").replace("{statistic}", stat.getName()) + ": &a"+actual+"/"+needed + " - " + RankUpper.get().getLang().get("config.ok")));
				} else {
					sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.statistics").replace("{statistic}", stat.getName()) + ": &c"+actual+"/"+needed));
				}
			}
		}

        //check scoreboards
        if (Sponge.getServer().getServerScoreboard().isPresent()){
            for (Entry<String, Long> key:RankUpper.get().getConfig().root().ranked_groups.get(pgroup).minecraft_scoreboards.entrySet()){
                if (key.getValue() > 0 && !Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).isEmpty()){
                    Score score = Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).stream().findFirst().get();
                    long needed = key.getValue();
                    long actual = score.getScore();
                    if (actual >= needed){
                        sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.scoreboards").replace("{score}", score.getName().toPlain()) + ": &a"+actual+"/"+needed + " - " + RankUpper.get().getLang().get("config.ok")));
                    } else {
                        sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.scoreboards").replace("{score}", score.getName().toPlain()) + ": &c"+actual+"/"+needed));
                    }
                }
            }
        }

        //placeholderAPI requirements
        if (Sponge.getPluginManager().getPlugin("placeholderapi").isPresent()) {
            Optional<PlaceholderService> phapiOpt = Sponge.getServiceManager().provide(PlaceholderService.class);
            if (phapiOpt.isPresent()) {
                PlaceholderService phapi = phapiOpt.get();
                for (Entry<String, Long> key:RankUpper.get().getConfig().root().ranked_groups.get(pgroup).placeholder_api_requirements.entrySet()){
                    Optional<Long> optVal = phapi.parse(key.getKey(), playerToCheck.getPlayer().isPresent() ? playerToCheck.getPlayer().get(): playerToCheck, null, Long.class);
                    if (optVal.isPresent() && phapiOpt.get().isRegistered(key.getKey())){
                        long needed = key.getValue();
                        long actual = optVal.get();
                        if (actual >= key.getValue()){
                            sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.placeholderapi").replace("{placeholder}", key.getKey().replace("%", "")) + ": &a"+actual+"/"+needed + " - " + RankUpper.get().getLang().get("config.ok")));
                        } else {
                            sender.sendMessage(RUUtil.toText(RankUpper.get().getLang().get("config.placeholderapi").replace("{placeholder}", key.getKey().replace("%", "")) + ": &c"+actual+"/"+needed));
                        }
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
