package br.net.fabiozumbi12.RankUpper;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class RUListener {
	
	@Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){    	
    	User p = e.getTargetUser();
    	
    	RankUpper.get().getLogger().debug("Player Join Event!");
    	if (RankUpper.get().getStats().getPlayerKey(p) == null){
    		RankUpper.get().getStats().AddPlayer(p);
    	}
        RankUpper.get().getStats().setLastVisit(p);
	}	
}