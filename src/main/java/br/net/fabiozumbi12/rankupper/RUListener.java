package br.net.fabiozumbi12.rankupper;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class RUListener {

	RankUpper plugin;

	public RUListener() {}
	
	@Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){    	
    	User p = e.getTargetUser();
    	
    	RULogger.debug("is Player Join Event");    	
    	if (RankUpper.cfgs.getPlayerKey(p) == null){
    		RankUpper.cfgs.AddPlayer(p);
    	}    	
    	RankUpper.cfgs.setLastVisit(p);
	}
	
}
