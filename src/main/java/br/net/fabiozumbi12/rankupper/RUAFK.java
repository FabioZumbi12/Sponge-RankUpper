package br.net.fabiozumbi12.rankupper;

import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rob5Underscores on 13/02/2017.
 */
public class RUAFK
{
    private List<Player> afkPlayers;

    public void addPlayer(Player p){
        afkPlayers.add(p);
    }
    public void removePlayer(Player p){
        afkPlayers.remove(p);
    }
    public boolean isPlayer(Player p){
        return afkPlayers.contains(p);
    }

    public void initialize() {
        afkPlayers = new ArrayList<>();
    }
}
