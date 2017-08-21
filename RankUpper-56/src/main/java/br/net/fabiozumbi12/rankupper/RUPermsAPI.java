package br.net.fabiozumbi12.rankupper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

public class RUPermsAPI {
	private PermissionService permissionService;
	
	public RUPermsAPI(Game game){
		this.permissionService = game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public List<String> getAllGroups(){
		List<String> groups = new ArrayList<String>();
		for (Subject group:getGroups().getAllSubjects()){ 
			RULogger.severe("group: "+group.getIdentifier()); 
			groups.add(group.getIdentifier());
		}
		return groups;
	}
	
	public Subject getHighestGroup(User player){
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();		
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				subs.put(sub.getParents().size(), sub);				
			}			
		}
		if (!subs.isEmpty()){
			return subs.get(Collections.max(subs.keySet()));
		}
		return null;
	}
	
	public SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
