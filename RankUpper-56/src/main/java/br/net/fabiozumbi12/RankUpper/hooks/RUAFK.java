package br.net.fabiozumbi12.RankUpper.hooks;

import io.github.nucleuspowered.nucleus.api.module.afk.NucleusAFKService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ProviderRegistration;

import java.util.Optional;

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
