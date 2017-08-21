package br.net.fabiozumbi12.rankupper;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class RUListener {
	private RankUpper plugin;

	public RUListener(RankUpper plugin) {
		this.plugin = plugin;
	}
	
	@Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){    	
    	User p = e.getTargetUser();
    	
    	RULogger.debug("Player Join Event!");
    	if (plugin.cfgs.getPlayerKey(p) == null){
    		plugin.cfgs.AddPlayer(p);
    	}    	
    	plugin.cfgs.setLastVisit(p);
	}	
}