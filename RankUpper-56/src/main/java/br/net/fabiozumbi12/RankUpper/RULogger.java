package br.net.fabiozumbi12.RankUpper;

import org.spongepowered.api.Sponge;

public class RULogger{

    public void success(String s) {
        Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&a&l"+s+"&r]"));
    }

    public void info(String s) {
        Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }

    public void warning(String s) {
        Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&6"+s+"&r]"));
    }

    public void severe(String s) {
        Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&c&l"+s+"&r]"));
    }

    public void log(String s) {
        Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: ["+s+"]"));
    }

    public void debug(String s) {
        if (RankUpper.get().getConfig().root().debug_messages) {
            Sponge.getServer().getConsole().sendMessage(RUUtil.toText("RankUpper: [&b"+s+"&r]"));
        }
    }
}