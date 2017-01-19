package br.net.fabiozumbi12.rankupper;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

import java.util.ArrayList;
import java.util.List;

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

	public String getHighestGroup(User player){
		String best = "";
		int beb = -1;		
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				//RULogger.info(sub.getIdentifier() + " - " + sub.getParents().size());
				if(beb < sub.getParents().size()){
					boolean isExcluded = false;
					for(String excl : RankUpper.cfgs.getExcludedGroups()){
						if(excl.equalsIgnoreCase(sub.getIdentifier())){
							isExcluded = true;
						}
					}
					if(!isExcluded) {
						beb = sub.getParents().size();
						best = sub.getIdentifier();
					}
				}
			}
		}
		//return gps;
		//RULogger.info("best= "+best);
		return best;
	}
	
	public SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
