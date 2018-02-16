package br.net.fabiozumbi12.RankUpper;

import java.util.List;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

public interface RUPerms {
	public List<String> getAllGroups();
	
	public Subject getHighestGroup(User player);
	
	public SubjectCollection getGroups();
}
