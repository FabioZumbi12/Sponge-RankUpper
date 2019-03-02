package br.net.fabiozumbi12.RankUpper.config;

import br.net.fabiozumbi12.RankUpper.RUAFK;
import br.net.fabiozumbi12.RankUpper.RUUtil;
import br.net.fabiozumbi12.RankUpper.RankUpper;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatsDB {

    private StatsCategory stats;
    public StatsCategory stats(){
        return this.stats;
    }

    public PlayerStatsDB() {
        stats = new StatsCategory();

        try {
            //convert old stats db
            File statConfig = new File(RankUpper.get().getConfigDir(), "playerstats.conf");
            if (statConfig.exists()){
                ConfigurationLoader<CommentedConfigurationNode> statsManager = HoconConfigurationLoader.builder().setFile(statConfig).build();
                CommentedConfigurationNode tempStats = statsManager.load();
                RankUpper.get().getLogger().warning("Converting player stats to new format...");
                for (Map.Entry<Object, ? extends CommentedConfigurationNode> node : tempStats.getChildrenMap().entrySet()) {
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

        //begin load
        loadPlayerStats();

        savePlayersStats();
    }

    public void loadPlayerStats(){
        //begin start
        try (Connection conn = RankUpper.get().getConnection()) {

            String table = "CREATE TABLE IF NOT EXISTS " + RankUpper.get().getConfig().root().database.prefix + "players ("
                    + "uuid varchar(64) primary key, "
                    + "joindate varchar(64), "
                    + "name varchar(64), "
                    + "lastvisit varchar(64), "
                    + "time int)";
            conn.prepareStatement(table).execute();

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + RankUpper.get().getConfig().root().database.prefix + "players");
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

    public void savePlayersStats(){
        try (Connection conn = RankUpper.get().getConnection()) {
            for (Map.Entry<String, StatsCategory.PlayerInfoCategory> stat:stats.players.entrySet()){
                String sql = "INSERT INTO " + RankUpper.get().getConfig().root().database.prefix + "players (uuid, joindate, lastvisit, name, time) VALUES(?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "joindate = ?, " +
                        "lastvisit = ?, " +
                        "name = ?, " +
                        "time = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, stat.getKey());
                stmt.setString(2, stat.getValue().JoinDate);
                stmt.setString(3, stat.getValue().LastVisit);
                stmt.setString(4, stat.getValue().PlayerName);
                stmt.setInt(5, stat.getValue().TimePlayed);

                stmt.setString(6, stat.getValue().JoinDate);
                stmt.setString(7, stat.getValue().LastVisit);
                stmt.setString(8, stat.getValue().PlayerName);
                stmt.setInt(9, stat.getValue().TimePlayed);

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void AddPlayer(Player p) {
        String PlayerString = p.getUniqueId().toString();
        if (!RankUpper.get().getConfig().root().use_uuids_instead_names){
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
        if (!RankUpper.get().getConfig().root().use_uuids_instead_names){
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
        if (!RankUpper.get().getConfig().root().use_uuids_instead_names){
            PlayerString = p.getName();
        }
        int time = stats.players.get(PlayerString).TimePlayed+ammount;
        stats.players.get(PlayerString).TimePlayed = (time);
        return time;
    }

    public void setLastVisit(User p){
        String PlayerString = p.getUniqueId().toString();
        if (!RankUpper.get().getConfig().root().use_uuids_instead_names){
            PlayerString = p.getName();
        }
        stats.players.get(PlayerString).LastVisit = RUUtil.DateNow();
    }

    public int getPlayerTime(String uuid){
        return stats.players.get(uuid).TimePlayed;
    }

    public String getPlayerKey(User user){
        if (RankUpper.get().getConfig().root().use_uuids_instead_names && stats.players.containsKey(user.getUniqueId().toString())){
            return user.getUniqueId().toString();
        } else if (!RankUpper.get().getConfig().root().use_uuids_instead_names && stats.players.containsKey(user.getName())){
            return user.getName();
        }
        return null;
    }

    public HashMap<String, Object> getPlayerDB(User p){
        HashMap<String, Object> pdb = new HashMap<>();
        String PlayerString = p.getUniqueId().toString();
        if (!RankUpper.get().getConfig().root().use_uuids_instead_names){
            PlayerString = p.getName();
        }
        pdb.put("PlayerName", stats.players.get(PlayerString).PlayerName);
        pdb.put("JoinDate", stats.players.get(PlayerString).JoinDate);
        pdb.put("LastVisit", stats.players.get(PlayerString).LastVisit);
        pdb.put("TimePlayed", stats.players.get(PlayerString).TimePlayed);
        return pdb;
    }

    public void AddPlayerTimes() {
        for (Player p: Sponge.getServer().getOnlinePlayers()){

            if (RankUpper.get().getConfig().root().afk_support){
                RankUpper.get().getLogger().debug("Berore check AFK!");
                if (RUAFK.isAFK(p)){
                    RankUpper.get().getLogger().debug("After check AFK!");
                    continue;
                }
            }
            addPlayerTime(p, RankUpper.get().getConfig().root().update_player_time_minutes);

            if (RankUpper.get().getConfig().root().auto_rankup)
                RankUpper.get().getConfig().checkRankup(p);
        }
    }

}
