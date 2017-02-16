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
	
	public String getHighestGroup(User player){
		for (Subject sub:player.getParents()) {
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)) {
				RULogger.debug(sub.getIdentifier() + " - " + sub.getParents().size());
				for (String excl : RankUpper.cfgs.getStringList("excluded-groups")) {
					if (excl.equalsIgnoreCase(sub.getIdentifier())) {
						break;
					}
				}
				return sub.getIdentifier();
			}
		}
		return "";
	}
	
	public SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}
