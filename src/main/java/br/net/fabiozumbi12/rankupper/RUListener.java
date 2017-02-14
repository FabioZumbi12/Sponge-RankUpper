package br.net.fabiozumbi12.rankupper;

import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class RUListener {

	RankUpper plugin;

	public RUListener() {}
	
	@Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){    	
    	User p = e.getTargetUser();
    	
    	RULogger.debug("Player Join Event!");
    	if (RankUpper.cfgs.getPlayerKey(p) == null){
    		RankUpper.cfgs.AddPlayer(p);
    	}    	
    	RankUpper.cfgs.setLastVisit(p);
	}

	@Listener void PlayerLogout(ClientConnectionEvent.Disconnect e){
        if(RankUpper.cfgs.getBool("afk-support")) {
            Player p = e.getTargetEntity();
            if(plugin.getRUAFK().isPlayer(p)){
                plugin.getRUAFK().removePlayer(p);
            }
        }
    }

	@Listener
	public void PlayerGoingAFK(NucleusAFKEvent.GoingAFK e) {
		if(RankUpper.cfgs.getBool("afk-support")) {
			Player p = e.getTargetEntity();
			if(!plugin.getRUAFK().isPlayer(p)){
			    plugin.getRUAFK().removePlayer(p);
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
