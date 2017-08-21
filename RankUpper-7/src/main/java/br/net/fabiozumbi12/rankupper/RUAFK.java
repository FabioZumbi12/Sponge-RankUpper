package br.net.fabiozumbi12.rankupper;

import java.util.Optional;

import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ProviderRegistration;

public class RUAFK {
	private static Optional<ProviderRegistration<NucleusAFKService>> prov;
	
	public static void init(){
		prov = Sponge.getServiceManager().getRegistration(NucleusAFKService.class);
	}
	
    public static boolean isAFK(Player p){
    	if (prov.isPresent()){
    		NucleusAFKService afkmanager = prov.get().getProvider();    	
        	return afkmanager.isAFK(p);
    	}
    	return false;
    }
}
