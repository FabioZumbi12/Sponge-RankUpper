package br.net.fabiozumbi12.rankupper;

import java.util.HashMap;
import java.util.Map;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;

public class RUCommands implements CommandExecutor{

	private void ExecuteTopCount(CommandSource p) {
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		for (Object uuid:RankUpper.cfgs.stats().getChildrenMap().keySet()){
			String play = RankUpper.cfgs.stats().getNode(uuid.toString(),"PlayerName").getString();
			if (RankUpper.cfgs.getPlayerTime(uuid.toString()) > 0){
				stats.put(play, RankUpper.cfgs.getPlayerTime(uuid.toString()));
			}
		}				
		int top10 = 0; 
		Map<String, Integer> Sorted = RUUtil.sort(stats);
		RULang.sendMessage(p, "commands.top10");
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

	private void SendCheckMessage(CommandSource sender, User playerToCheck) {
		int time = RankUpper.cfgs.getPlayerTime(RankUpper.cfgs.getPlayerKey(playerToCheck));
		String pgroup = RankUpper.perms.getGroup(playerToCheck);
		if (pgroup == null){
			RULang.sendMessage(sender, RULang.get("commands.check.youplayed").replace("{time}", RUUtil.timeDescript(time)).replace("{group}", "None"));
			return;
		}
		RULang.sendMessage(sender, RULang.get("commands.check.youplayed").replace("{time}", RUUtil.timeDescript(time)).replace("{group}", pgroup));
		
		String ngroup = RankUpper.cfgs.getString("ranked-groups."+ pgroup +".next-group");					
		if (ngroup != null && !ngroup.equals("")){
			RULang.sendMessage(sender, RULang.get("commands.nextgroup").replace("{group}", ngroup));
			int minutesNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".minutes-needed");
			int moneyNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".money-needed");
			int levelNeeded = RankUpper.cfgs.getInt("ranked-groups."+ pgroup +".levels-needed");
			
			if (minutesNeeded != 0){
				if (RankUpper.cfgs.getPlayerTime(RankUpper.cfgs.getPlayerKey(playerToCheck)) >= minutesNeeded){
					RULang.sendMessage(sender, RULang.get("config.time") + ": &a" + RUUtil.timeDescript(minutesNeeded) + " " + RULang.get("config.ok"));
				} else {
					RULang.sendMessage(sender, RULang.get("config.time") + ": &4" + RUUtil.timeDescript(minutesNeeded));
				}							
			}
			
			if (moneyNeeded != 0){
				UniqueAccount acc = RankUpper.econ.getOrCreateAccount(playerToCheck.getUniqueId()).get();
				if (acc.getBalance(RankUpper.econ.getDefaultCurrency()).intValue() >= moneyNeeded){
					RULang.sendMessage(sender, RULang.get("config.money") + ": &a" + RULang.get("config.cifra") + moneyNeeded + " " + RULang.get("config.ok"));
				} else {
					RULang.sendMessage(sender, RULang.get("config.money") + ": &4" + RULang.get("config.cifra") + moneyNeeded);
				}							
			}
			
			if (levelNeeded != 0){
				if (!playerToCheck.get(Keys.EXPERIENCE_LEVEL).isPresent()){
					RULang.sendMessage(sender, RULang.get("config.levels") + ": " + levelNeeded + "Lvs.");
					return;
				}
				if (playerToCheck.get(Keys.EXPERIENCE_LEVEL).get() >= levelNeeded){
					RULang.sendMessage(sender, RULang.get("config.levels") + ": &a" + levelNeeded + "Lvs. " + RULang.get("config.ok"));
				} else {
					RULang.sendMessage(sender, RULang.get("config.levels") + ": &4" + levelNeeded + "Lvs.");
				}							
			}
		}		
	}
	
	void sendHelp(CommandSource source){
		RULang.sendMessage(source, RULang.get("commands.commandshelp"));
		for (String key:RULang.helpStrings()){			
			if (source.hasPermission("rankupper."+key)) {
				source.sendMessage(RUUtil.toText(RULang.get("commands.help."+key)));					
			} else
			if (source.hasPermission("rankupper."+key+".others")){
				source.sendMessage(RUUtil.toText(RULang.get("commands.help."+key+".others")));
			}			
		}
	}
	
	@Override
	public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        CommandResult cmdr = CommandResult.success();
    			
		if (sender instanceof Player){
			Player p = (Player) sender;
			
			if (args.hasAny("2")){
				if (args.<String>getOne("0").get().equalsIgnoreCase("set") && p.hasPermission("rankupper.set")) {					
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						try {
							int time = Integer.parseInt(args.<String>getOne("2").get());
							RankUpper.cfgs.setPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args.<String>getOne("1").get())), time);
							RULang.sendMessage(p, RULang.get("commands.setto").replace("{time}", RUUtil.timeDescript(time)).replace("{player}", args.<String>getOne("1").get()));
						} catch (Exception e){
							RULang.sendMessage(p, RULang.get("commands.notnumber").replace("{arg}", args.<String>getOne("2").get()));
						}
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
					}
					return cmdr;
				}
			}
			
			if (args.hasAny("1")){
				if (args.<String>getOne("0").get().equalsIgnoreCase("check") && p.hasPermission("rankupper.check.others")) {
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						SendCheckMessage(sender, RUUtil.getUser(args.<String>getOne("1").get()));
					} else {					
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
					}					
					return cmdr;
				}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("player-info") && p.hasPermission("rankupper.player-info")) {
					HashMap<String, Object> pdb = new HashMap<String, Object>();
					if (RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args.<String>getOne("1").get())) != null){
						pdb = RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args.<String>getOne("1").get()));
						RULang.sendMessage(p, "Player Info:");
						p.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						p.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						p.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisist")));
						p.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript(Integer.parseInt((String)pdb.get("TimePlayed")))));
			    		return cmdr;
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
						return cmdr;
					}					
		    	}
			}		
			
			if (args.hasAny("0")){
				if (args.<String>getOne("0").get().equalsIgnoreCase("reload") && p.hasPermission("rankupper.reload")) {
					RankUpper.reload();
					p.sendMessage(RUUtil.toText("&a"+RankUpper.plugin.getName()+ " reloaded!"));
		    		return cmdr;
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("save-all") && p.hasPermission("rankupper.save-all")) {
					RankUpper.cfgs.savePlayersStats();
					p.sendMessage(RUUtil.toText("&aPlayer stats Saved!"));
		    		return cmdr;
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("load-all") && p.hasPermission("rankupper.load-all")) {
					RankUpper.cfgs.loadPlayerStats();
					p.sendMessage(RUUtil.toText("&aPlayer stats Loaded!"));
		    		return cmdr;
		    	}
								
				if (args.<String>getOne("0").get().equalsIgnoreCase("check") && p.hasPermission("rankupper.check")) {
					if (RankUpper.cfgs.checkRankup(p)){
						return cmdr;
					}
					SendCheckMessage(sender, p);
					return cmdr;
				}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("top") && p.hasPermission("rankupper.top")) {
					ExecuteTopCount(p);
					return cmdr;
				}				
			}			
			
			if (args.hasAny("2")){
				if (args.<String>getOne("0").get().equalsIgnoreCase("add") && p.hasPermission("rankupper.add")) {					
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						try {
							int time = Integer.parseInt(args.<String>getOne("2").get());
							RULang.sendMessage(p, RULang.get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.cfgs.addPlayerTime(RUUtil.getUser(args.<String>getOne("1").get()), time))).replace("{player}", args.<String>getOne("1").get()));
						} catch (Exception e){
							RULang.sendMessage(p, RULang.get("commands.notnumber").replace("{arg}", args.<String>getOne("2").get()));
						}
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
					}
					return cmdr;
				}
			}
		} 
		
		//if console
		else {
			
			if (args.hasAny("2")){
				if (args.<String>getOne("0").get().equalsIgnoreCase("set")) {					
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						try {
							int time = Integer.parseInt(args.<String>getOne("2").get());
							RankUpper.cfgs.setPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args.<String>getOne("1").get())), time);
							sender.sendMessage(RUUtil.toText(RULang.get("commands.setto").replace("{time}", RUUtil.timeDescript(time)).replace("{player}", args.<String>getOne("1").get())));
						} catch (Exception e){
							sender.sendMessage(RUUtil.toText(RULang.get("commands.notnumber").replace("{arg}", args.<String>getOne("2").get())));
						}
					} else {
						sender.sendMessage(RUUtil.toText(RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get())));
					}
					return cmdr;
				}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("add")) {					
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						try {
							int time = Integer.parseInt(args.<String>getOne("2").get());
							sender.sendMessage(RUUtil.toText(RULang.get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.cfgs.addPlayerTime(RUUtil.getUser(args.<String>getOne("1").get()), time))).replace("{player}", args.<String>getOne("1").get())));
						} catch (Exception e){
							sender.sendMessage(RUUtil.toText(RULang.get("commands.notnumber").replace("{arg}", args.<String>getOne("2").get())));
						}
					} else {
						sender.sendMessage(RUUtil.toText(RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get())));
					}
					return cmdr;
				}
			}
			
            if (args.hasAny("1")){
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("player-info")) {
					HashMap<String, Object> pdb = new HashMap<String, Object>();
					if (RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args.<String>getOne("1").get())) != null){
						pdb = RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args.<String>getOne("1").get()));
						RULang.sendMessage(sender, "Player Info:");
						sender.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						sender.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						sender.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisist")));
						sender.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript(Integer.parseInt((String)pdb.get("TimePlayed")))));
			    		return cmdr;
					} else {
						RULang.sendMessage(sender, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
						return cmdr;
					}					
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("check")) {
					if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
						SendCheckMessage(sender, RUUtil.getUser(args.<String>getOne("1").get()));
					} else {					
						if (RUUtil.getUser(args.<String>getOne("1").get()) != null){
							int time = RankUpper.cfgs.getPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args.<String>getOne("1").get())));
							RULang.sendMessage(sender, RULang.get("commands.check.otherplayed").replace("{time}", RUUtil.timeDescript(time)));
						} else {
							RULang.sendMessage(sender, RULang.get("commands.unknownplayer").replace("{player}", args.<String>getOne("1").get()));
						}
					}
					return cmdr;
				}
			}

			if (args.hasAny("0")){
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("reload")) {
					RankUpper.reload();
					RULogger.sucess(RankUpper.plugin.getName() + " reloaded!");
		    		return cmdr;
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("save-all")) {
					RankUpper.cfgs.savePlayersStats();
					RULogger.sucess("Player stats Saved!");
		    		return cmdr;
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("load-all")) {
					RankUpper.cfgs.loadPlayerStats();
					RULogger.sucess("Player stats Loaded!");
		    		return cmdr;
		    	}
				
				if (args.<String>getOne("0").get().equalsIgnoreCase("top")) {
					ExecuteTopCount(sender);
					return cmdr;
				}				
			}								
		}
		sendHelp(sender);
		return cmdr;
	}

}
