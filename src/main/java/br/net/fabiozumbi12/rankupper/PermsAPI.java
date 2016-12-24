package br.net.fabiozumbi12.rankupper;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

public class PermsAPI {
	private PermissionService permissionService;

	public PermsAPI(Game game){
		this.permissionService = game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public List<String> getGroups(User player){
		List<String> gps = new ArrayList<String>();
		for (Subject sub:player.getParents()){			
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				gps.add(sub.getIdentifier());
			}
		}
		return gps;	
	}
	
	public SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
