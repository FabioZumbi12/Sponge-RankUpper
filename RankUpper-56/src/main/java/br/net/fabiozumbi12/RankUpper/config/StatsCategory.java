package br.net.fabiozumbi12.RankUpper.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatsCategory {
    public StatsCategory(){}

    public ConcurrentHashMap<String, PlayerInfoCategory> players = new ConcurrentHashMap<>();

    public static class PlayerInfoCategory {
        public PlayerInfoCategory(){}

        public PlayerInfoCategory(String JoinDate, String LastVisit, String PlayerName, int TimePlayed){
            this.JoinDate = JoinDate;
            this.LastVisit = LastVisit;
            this.PlayerName = PlayerName;
            this.TimePlayed = TimePlayed;
        }

        public String JoinDate = "";
        public String LastVisit = "";
        public String PlayerName = "";
        public int TimePlayed = 0;
    }
}
