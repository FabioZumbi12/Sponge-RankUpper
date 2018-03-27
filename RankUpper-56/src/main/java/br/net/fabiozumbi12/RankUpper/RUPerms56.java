package br.net.fabiozumbi12.RankUpper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

public class RUPerms56 implements RUPerms {
	private PermissionService permissionService;
	
	public RUPerms56(){
		this.permissionService = RankUpper.get().game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public List<String> getAllGroups(){
		List<String> groups = new ArrayList<String>();
		for (Subject group:getGroups().getAllSubjects()){
			groups.add(group.getIdentifier());
		}
		return groups;
	}

	public List<Subject> getPlayerGroups(User player){
		List<Subject> subs = new ArrayList<>();
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) &&
					(sub.getIdentifier() != null) && !RankUpper.get().getConfig().root().exclude_groups.contains(sub.getIdentifier())){
				subs.add(sub);
			}
		}
		return subs;
	}
	
	public Subject getHighestGroup(User player){
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();		
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && 
					(sub.getIdentifier() != null) && !RankUpper.get().getConfig().root().exclude_groups.contains(sub.getIdentifier())){
				subs.put(sub.getParents().size(), sub);				
			}			
		}
		if (!subs.isEmpty()){
			return subs.get(Collections.max(subs.keySet()));
		}
		return null;
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
