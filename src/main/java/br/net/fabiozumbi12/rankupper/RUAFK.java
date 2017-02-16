package br.net.fabiozumbi12.rankupper;

import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rob5Underscores on 13/02/2017.
 */
public class RUAFK
{
    private List<Player> afkPlayers;
    RankUpper plugin;

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

    @Listener
    public void PlayerGoingAFK(NucleusAFKEvent.GoingAFK e) {
        if(RankUpper.cfgs.getBool("afk-support")) {
            Player p = e.getTargetEntity();
            if(!plugin.getRUAFK().isPlayer(p)){
                plugin.getRUAFK().addPlayer(p);
            }
        }
    }

    @Listener
    public void PlayerReturningFromAFK(NucleusAFKEvent.ReturningFromAFK e) {
        if(RankUpper.cfgs.getBool("afk-support")) {
            Player p = e.getTargetEntity();
            if(plugin.getRUAFK().isPlayer(p)){
                plugin.getRUAFK().removePlayer(p);
            }
        }
    }

}
