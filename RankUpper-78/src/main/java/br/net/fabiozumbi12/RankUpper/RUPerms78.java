package br.net.fabiozumbi12.RankUpper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

public class RUPerms78 implements RUPerms {
	private PermissionService permissionService;
	
	public RUPerms78(){
		this.permissionService = RankUpper.get().game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public List<String> getAllGroups(){
		List<String> groups = new ArrayList<String>();
		for (Subject group:getGroups().getLoadedSubjects()){ 
			groups.add(group.getIdentifier());
		}
		return groups;
	}

	public List<Subject> getPlayerGroups(User player){
        List<Subject> subs = new ArrayList<>();
        try {
            for (SubjectReference sub:player.getParents()){
                if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
                    Subject subj = sub.resolve().get();
                    if (!RankUpper.get().getConfig().getStringList("exclude-groups").contains(subj.getIdentifier())){
                        subs.add(subj);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return subs;
    }
	
	public Subject getHighestGroup(User player){
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();
		try {					
			for (SubjectReference sub:player.getParents()){
				if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
					Subject subj = sub.resolve().get();
					if (!RankUpper.get().getConfig().getStringList("exclude-groups").contains(subj.getIdentifier())){
						subs.put(subj.getParents().size(), subj);	
					}								
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
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
