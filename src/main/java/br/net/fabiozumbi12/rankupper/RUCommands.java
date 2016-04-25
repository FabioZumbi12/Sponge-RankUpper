package br.net.fabiozumbi12.rankupper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

public class RUCommands implements CommandCallable{

	@Override
	public CommandResult process(CommandSource sender, String arguments) throws CommandException {
    	CommandResult cmdr = CommandResult.success();
    	
		String[] args = arguments.split(" ");
		
		if (sender instanceof Player){
			Player p = (Player) sender;
			
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("reload") && p.hasPermission("rankupper.reload")) {
					RankUpper.reload();
					p.sendMessage(RUUtil.toText("&a"+RankUpper.plugin.getName()+ " reloaded!"));
		    		return cmdr;
		    	}
				
				if (args[0].equalsIgnoreCase("save-all") && p.hasPermission("rankupper.save-all")) {
					RankUpper.cfgs.savePlayersStats();
					p.sendMessage(RUUtil.toText("&aPlayer stats Saved!"));
		    		return cmdr;
		    	}
				
				if (args[0].equalsIgnoreCase("load-all") && p.hasPermission("rankupper.load-all")) {
					RankUpper.cfgs.loadPlayerStats();
					p.sendMessage(RUUtil.toText("&aPlayer stats Loaded!"));
		    		return cmdr;
		    	}
								
				if (args[0].equalsIgnoreCase("check") && p.hasPermission("rankupper.check")) {
					if (RankUpper.cfgs.checkRankup(p)){
						return cmdr;
					}
					SendCheckMessage(sender, p);
					return cmdr;
				}
				
				if (args[0].equalsIgnoreCase("top") && p.hasPermission("rankupper.top")) {
					ExecuteTopCount(p);
					return cmdr;
				}
				
			}
			
			if (args.length == 2){
				if (args[0].equalsIgnoreCase("check") && p.hasPermission("rankupper.check.others")) {
					if (RUUtil.getUser(args[1]) != null){
						SendCheckMessage(sender, RUUtil.getUser(args[1]));
					} else {					
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
					}					
					return cmdr;
				}
				
				if (args[0].equalsIgnoreCase("player-info") && p.hasPermission("rankupper.player-info")) {
					HashMap<String, Object> pdb = new HashMap<String, Object>();
					if (RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args[1])) != null){
						pdb = RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args[1]));
						RULang.sendMessage(p, "Player Info:");
						p.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						p.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						p.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisist")));
						p.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript(Integer.parseInt((String)pdb.get("TimePlayed")))));
			    		return cmdr;
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
						return cmdr;
					}					
		    	}
			}		
			
			if (args.length == 3){
				if (args[0].equalsIgnoreCase("set") && p.hasPermission("rankupper.set")) {					
					if (RUUtil.getUser(args[1]) != null){
						try {
							int time = Integer.parseInt(args[2]);
							RankUpper.cfgs.setPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args[1])), time);
							RULang.sendMessage(p, RULang.get("commands.setto").replace("{time}", RUUtil.timeDescript(time)).replace("{player}", args[1]));
						} catch (Exception e){
							RULang.sendMessage(p, RULang.get("commands.notnumber").replace("{arg}", args[2]));
						}
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
					}
					return cmdr;
				}
			}
			
			if (args.length == 3){
				if (args[0].equalsIgnoreCase("add") && p.hasPermission("rankupper.add")) {					
					if (RUUtil.getUser(args[1]) != null){
						try {
							int time = Integer.parseInt(args[2]);
							RULang.sendMessage(p, RULang.get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.cfgs.addPlayerTime(RUUtil.getUser(args[1]), time))).replace("{player}", args[1]));
						} catch (Exception e){
							RULang.sendMessage(p, RULang.get("commands.notnumber").replace("{arg}", args[2]));
						}
					} else {
						RULang.sendMessage(p, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
					}
					return cmdr;
				}
			}
		} 
		
		//if console
		else {
			if (args.length == 1){
				
				if (args[0].equalsIgnoreCase("reload")) {
					RankUpper.reload();
					RULogger.sucess(RankUpper.plugin.getName() + " reloaded!");
		    		return cmdr;
		    	}
				
				if (args[0].equalsIgnoreCase("save-all")) {
					RankUpper.cfgs.savePlayersStats();
					RULogger.sucess("Player stats Saved!");
		    		return cmdr;
		    	}
				
				if (args[0].equalsIgnoreCase("load-all")) {
					RankUpper.cfgs.loadPlayerStats();
					RULogger.sucess("Player stats Loaded!");
		    		return cmdr;
		    	}
				
				if (args[0].equalsIgnoreCase("top")) {
					ExecuteTopCount(sender);
					return cmdr;
				}				
			}
			
			if (args.length == 2){
				
				if (args[0].equalsIgnoreCase("player-info")) {
					HashMap<String, Object> pdb = new HashMap<String, Object>();
					if (RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args[1])) != null){
						pdb = RankUpper.cfgs.getPlayerDB(RUUtil.getUser(args[1]));
						RULang.sendMessage(sender, "Player Info:");
						sender.sendMessage(RUUtil.toText("&3- Nick: &b" + pdb.get("PlayerName")));
						sender.sendMessage(RUUtil.toText("&3- Joind Date: &b" + pdb.get("JoinDate")));
						sender.sendMessage(RUUtil.toText("&3- Last Visit: &b" + pdb.get("LastVisist")));
						sender.sendMessage(RUUtil.toText("&3- Time Played: &b" + RUUtil.timeDescript(Integer.parseInt((String)pdb.get("TimePlayed")))));
			    		return cmdr;
					} else {
						RULang.sendMessage(sender, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
						return cmdr;
					}					
		    	}
				
				if (args[0].equalsIgnoreCase("check")) {
					if (RUUtil.getUser(args[1]) != null){
						SendCheckMessage(sender, RUUtil.getUser(args[1]));
					} else {					
						if (RUUtil.getUser(args[1]) != null){
							int time = RankUpper.cfgs.getPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args[1])));
							RULang.sendMessage(sender, RULang.get("commands.check.otherplayed").replace("{time}", RUUtil.timeDescript(time)));
						} else {
							RULang.sendMessage(sender, RULang.get("commands.unknownplayer").replace("{player}", args[1]));
						}
					}
					return cmdr;
				}
			}
			
			if (args.length == 3){
				if (args[0].equalsIgnoreCase("set")) {					
					if (RUUtil.getUser(args[1]) != null){
						try {
							int time = Integer.parseInt(args[2]);
							RankUpper.cfgs.setPlayerTime(RankUpper.cfgs.getPlayerKey(RUUtil.getUser(args[1])), time);
							sender.sendMessage(RUUtil.toText(RULang.get("commands.setto").replace("{time}", RUUtil.timeDescript(time)).replace("{player}", args[1])));
						} catch (Exception e){
							sender.sendMessage(RUUtil.toText(RULang.get("commands.notnumber").replace("{arg}", args[2])));
						}
					} else {
						sender.sendMessage(RUUtil.toText(RULang.get("commands.unknownplayer").replace("{player}", args[1])));
					}
					return cmdr;
				}
				
				if (args[0].equalsIgnoreCase("add")) {					
					if (RUUtil.getUser(args[1]) != null){
						try {
							int time = Integer.parseInt(args[2]);
							sender.sendMessage(RUUtil.toText(RULang.get("commands.added").replace("{time}", RUUtil.timeDescript(RankUpper.cfgs.addPlayerTime(RUUtil.getUser(args[1]), time))).replace("{player}", args[1])));
						} catch (Exception e){
							sender.sendMessage(RUUtil.toText(RULang.get("commands.notnumber").replace("{arg}", args[2])));
						}
					} else {
						sender.sendMessage(RUUtil.toText(RULang.get("commands.unknownplayer").replace("{player}", args[1])));
					}
					return cmdr;
				}
			}			
		}		 
		sendHelpMessage(sender);
		return cmdr;
	}

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
	
	private void sendHelpMessage(CommandSource p){
		RULang.sendMessage(p, RULang.get("commands.commandshelp"));
		for (String key:RULang.helpStrings()){			
			if (p.hasPermission("rankupper."+key)) {
				p.sendMessage(RUUtil.toText(RULang.get("commands.help."+key)));					
			} else
			if (p.hasPermission("rankupper."+key+".others")){
				p.sendMessage(RUUtil.toText(RULang.get("commands.help."+key+".others")));
			}			
		}		
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments)
			throws CommandException {
		return new ArrayList<String>();
	}

	@Override
	public boolean testPermission(CommandSource source) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<? extends Text> getShortDescription(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<? extends Text> getHelp(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Text getUsage(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}

}
