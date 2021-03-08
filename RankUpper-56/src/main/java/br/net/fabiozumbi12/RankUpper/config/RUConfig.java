package br.net.fabiozumbi12.RankUpper.config;

import br.net.fabiozumbi12.RankUpper.RUUtil;
import br.net.fabiozumbi12.RankUpper.RankUpper;
import com.google.common.reflect.TypeToken;
import me.rojo8399.placeholderapi.PlaceholderService;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public class RUConfig {

    //getters
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private MainCategory root;

    public MainCategory root() {
        return this.root;
    }

    public RUConfig(GuiceObjectMapperFactory factory) {
        try {
            Files.createDirectories(RankUpper.get().getConfigDir().toPath());
            File defConfig = new File(RankUpper.get().getConfigDir(), "rankupper.conf");
            boolean newConfig = false;
            if (!defConfig.exists()) {
                newConfig = true;
                RankUpper.get().getLogger().log("Creating config file...");
                defConfig.createNewFile();
            }

            /*--------------------- rankupper.conf ---------------------------*/
            cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();
            configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
            root = configRoot.getValue(TypeToken.of(MainCategory.class), new MainCategory());

            if (root.ranked_groups.isEmpty()) {
                RankedGroupsCategory rgc = new RankedGroupsCategory(Arrays.asList(
                        "lp user {player} parent unset {oldgroup}",
                        "lp user {player} parent set {newgroup}",
                        "xp 50L {player}"),
                        50,
                        "&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} of server.",
                        120,
                        1000,
                        "member");
                if (newConfig) {
                    rgc.minecraft_statistic.put("MOB_KILLS", 100L);
                    rgc.minecraft_scoreboards.put("TeamBlue", 50L);
                    rgc.placeholder_api_requirements.put("%Pokedex%", 200L);
                    root.ranked_groups.put("group-example", rgc);
                }
            }

            for (RankedGroupsCategory group : root.ranked_groups.values()) {
                if (newConfig && group.minecraft_statistic.isEmpty())
                    group.minecraft_statistic.put("MOB_KILLS", -1L);
                if (newConfig && group.minecraft_scoreboards.isEmpty())
                    group.minecraft_scoreboards.put("TeamBlue", -1L);
                if (newConfig && group.placeholder_api_requirements.isEmpty())
                    group.placeholder_api_requirements.put("%Pokedex%", -1L);
            }

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

    private void save() {
        try {
            configRoot.setValue(TypeToken.of(MainCategory.class), root);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            RankUpper.get().getLogger().severe("Problems during save file:");
            e.printStackTrace();
        }
    }

    private boolean groupExists(String group) {
        return root.ranked_groups.containsKey(group);
    }

    public boolean addGroup(String group, String newGroup, int time, int levels, int money) {
        if (groupExists(group)) {
            return false;
        }
        List<String> cmds = Arrays.asList("lp user {player} parent unset {oldgroup}", "lp user {player} parent set {newgroup}");
        RankedGroupsCategory groupvalues = new RankedGroupsCategory(cmds,
                levels, "&a>> The player &6{player} &ahas played for &6{time} &aand now is rank {newgroup} on server.",
                time, money, newGroup);
        root.ranked_groups.put(group, groupvalues);
        save();
        return true;
    }

    public Boolean setGroup(String group, String newGroup, int time, int levels, int money) {
        if (!groupExists(group)) {
            return false;
        }
        root.ranked_groups.get(group).minutes_needed = time;
        root.ranked_groups.get(group).levels_needed = levels;
        root.ranked_groups.get(group).money_needed = money;
        root.ranked_groups.get(group).next_group = newGroup;
        save();
        return true;
    }

    public boolean checkRankup(User p) {
        Subject subG = RankUpper.get().getPerms().getHighestGroup(p);

        String pgroup = null;
        if (subG == null) {
            List<Subject> pgroups = RankUpper.get().getPerms().getPlayerGroups(p);
            for (Subject sub : pgroups) {
                if (root.ranked_groups.containsKey(sub.getIdentifier())) {
                    pgroup = sub.getIdentifier();
                    RankUpper.get().getLogger().debug("Ranked Player Group (not primary) is: " + pgroup);
                    break;
                }
            }
            if (pgroup == null) return false;
        } else {
            pgroup = subG.getIdentifier();
            RankUpper.get().getLogger().debug("Highest Group is: " + pgroup);
        }

        String ngroup = root.ranked_groups.get(pgroup) != null ? root.ranked_groups.get(pgroup).next_group : null;

        if (ngroup == null || ngroup.isEmpty() || !RankUpper.get().getPerms().getAllGroups().contains(ngroup)) {
            return false;
        }

        int minutesNeeded = root.ranked_groups.get(pgroup).minutes_needed;
        int moneyNeeded = root.ranked_groups.get(pgroup).money_needed;
        int levelNeeded = root.ranked_groups.get(pgroup).levels_needed;

        //check for statistics
        for (Entry<String, Long> key : root.ranked_groups.get(pgroup).minecraft_statistic.entrySet()) {
            if (key.getValue() > 0 && Sponge.getRegistry().getType(Statistic.class, key.getKey()).isPresent()) {
                Statistic stat = Sponge.getRegistry().getType(Statistic.class, key.getKey()).get();
                try {
                    if (!p.getStatisticData().get(stat).isPresent() || p.getStatisticData().get(stat).get() < key.getValue()) {
                        return false;
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }
        }

        //check scoreboards
        if (Sponge.getServer().getServerScoreboard().isPresent()) {
            for (Entry<String, Long> key : root.ranked_groups.get(pgroup).minecraft_scoreboards.entrySet()) {
                if (key.getValue() > 0 && !Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).isEmpty()) {
                    Score score = Sponge.getServer().getServerScoreboard().get().getScores(Text.of(key.getKey())).stream().findFirst().get();
                    if (score.getScore() < key.getValue()) {
                        return false;
                    }
                }
            }
        }

        //placeholderAPI requirements
        if (Sponge.getPluginManager().getPlugin("placeholderapi").isPresent()) {
            Optional<PlaceholderService> phapiOpt = Sponge.getServiceManager().provide(PlaceholderService.class);
            if (phapiOpt.isPresent()) {
                PlaceholderService phapi = phapiOpt.get();
                for (Entry<String, Long> key : root.ranked_groups.get(pgroup).placeholder_api_requirements.entrySet()) {
                    double optVal = RUUtil.getPlaceholderValue(phapi, key.getKey(), p.getPlayer().isPresent() ? p.getPlayer().get() : p);
                    if (key.getValue() > 0) {
                        if (optVal < key.getValue()) {
                            return false;
                        }
                    }
                }
            }
        }

        if (minutesNeeded > 0) {
            if (RankUpper.get().getStats().getPlayerTime(RankUpper.get().getStats().getPlayerKey(p)) < minutesNeeded) {
                return false;
            }
        }

        if (moneyNeeded > 0 && RankUpper.get().getEconomy() != null) {
            UniqueAccount acc = RankUpper.get().getEconomy().getOrCreateAccount(p.getUniqueId()).get();
            if (acc.getBalance(RankUpper.get().getEconomy().getDefaultCurrency()).intValue() < moneyNeeded) {
                return false;
            }
        }

        if (levelNeeded > 0) {
            if (p.get(Keys.EXPERIENCE_LEVEL).isPresent() && p.get(Keys.EXPERIENCE_LEVEL).get() < levelNeeded) {
                return false;
            }
        }

        for (String cmd : root.ranked_groups.get(pgroup).execute_commands) {
            RankUpper.get().game.getCommandManager().process(Sponge.getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{oldgroup}", pgroup).replace("{newgroup}", ngroup));
        }
        String message = root.ranked_groups.get(pgroup).message_broadcast;
        if (message != null && !message.equals("")) {
            Sponge.getServer().getBroadcastChannel().send(RUUtil.toText(message.replace("{player}", p.getName()).replace("{time}", RUUtil.timeDescript(minutesNeeded)).replace("{newgroup}", ngroup)));
        }
        return true;
    }
}
   
