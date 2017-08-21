package br.net.fabiozumbi12.rankupper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

public class RUPermsAPI {
	private PermissionService permissionService;
	
	public RUPermsAPI(Game game){
		this.permissionService = game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public List<String> getAllGroups(){
		List<String> groups = new ArrayList<String>();
		for (Subject group:getGroups().getLoadedSubjects()){ 
			groups.add(group.getIdentifier());
		}
		return groups;
	}
	
	public Subject getHighestGroup(User player){
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();
		try {					
			for (SubjectReference sub:player.getParents()){
				if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
					Subject subj;
					subj = sub.resolve().get();
					subs.put(subj.getParents().size(), subj);				
				}			
			}			
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
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
