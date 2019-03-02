package br.net.fabiozumbi12.RankUpper.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class RankedGroupsCategory {

    public RankedGroupsCategory(){}

    @Setting(value="execute-commands", comment = "Commands to execute when promote. These commands will depend on your permission plugin.\n" +
            "Available placeholders: {player}, {oldgroup}, {newgroup}")
    public List<String> execute_commands = new ArrayList<>();

    @Setting(value="levels-needed", comment = "Levels(not experience) needed to promote.")
    public int levels_needed = 0;

    @Setting(value="message-broadcast", comment = "Broadcast the promote message to all players.\n" +
            "Available placeholders: {player}, {time}, {newgroup}")
    public String message_broadcast = "";

    @Setting(value="minutes-needed", comment = "Minutes played needed.")
    public int minutes_needed = 0;

    @Setting(value="money-needed", comment = "Money needed. Do not requires additional plugin.")
    public int money_needed = 0;

    @Setting(value="next-group", comment = "Exact name of group to promote player on match the requirements.")
    public String next_group = "";

    @Setting(value = "minecraft-statistics", comment="Use some Minecraft Statistics to track for rankup.\n" +
            "This option will only accept LONG types for statistics.\n" +
            "See all statistics names here: https://jd.spongepowered.org/7.0.0/org/spongepowered/api/statistic/Statistics.html\n" +
            "Set to 0 or -1 to disable.")
    public Map<String, Long> minecraft_statistic = createMapStats();
    private Map<String, Long> createMapStats()
    {
        Map<String,Long> myMap = new HashMap<>();
        myMap.put("MOB_KILLS", 100L);
        return myMap;
    }

    @Setting(value = "minecraft-scoreboards", comment="Use the values of your scoreboard's Scores by name to rank up players.\n" +
            "Set to 0 or -1 to disable.")
    public Map<String, Long> minecraft_scoreboards = createMapScores();
    private Map<String, Long> createMapScores()
    {
        Map<String,Long> myMap = new HashMap<>();
        myMap.put("TeamBlue", 50L);
        return myMap;
    }

    RankedGroupsCategory(List<String> execute_commands, int levels_needed, String message_broadcast, int minutes_needed, int money_needed, String next_group){
        this.execute_commands = execute_commands;
        this.levels_needed = levels_needed;
        this.message_broadcast = message_broadcast;
        this.minutes_needed = minutes_needed;
        this.money_needed = money_needed;
        this.next_group = next_group;
    }
}
