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

    RUAFK() {
        this.afkPlayers = new ArrayList<>();
    }

    public void addPlayer(Player p){
        this.afkPlayers.add(p);
    }
    public void removePlayer(Player p){
        this.afkPlayers.remove(p);
    }
    public boolean isPlayer(Player p){
        return this.afkPlayers.contains(p);
    }

}
