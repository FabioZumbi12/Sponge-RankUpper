package br.net.fabiozumbi12.rankupper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class RUUtil {

	static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RankUpper.cfgs.getString("date-format"));
        Date today = Calendar.getInstance().getTime(); 
        String now = df.format(today);
		return now;    	
    }

	public static String timeDescript(int timeNeeded) {
		String message = "";
		if (timeNeeded > 60){
			int hours = timeNeeded/60%24;
			int minutes = timeNeeded%60;
			int day = timeNeeded/24/60;
			if (timeNeeded/60 > 24 && day > 0){		
				message = day + " " + RULang.get("config.day") + ", " + hours + " " + RULang.get("config.hour") + " " + RULang.get("config.and") + " " + minutes + " " + RULang.get("config.minute");
			} else {
				message = hours + " " + RULang.get("config.hour") + " " + RULang.get("config.and") + " " + minutes + " " + RULang.get("config.minute");
			}			
		} else {
			message = timeNeeded + " " + RULang.get("config.minute");
		}		
		
		message = message
		.replace(" 0 "+RULang.get("config.day")+", ", " ")
		.replace(" 0 "+RULang.get("config.hour")+" "+RULang.get("config.and")+" ", " ")
		.replace(" 0 "+RULang.get("config.minute")+" "+ RULang.get("config.and")+" ", " ");
		return message;		
	}
	
	public static Map<String, Integer> sort(Map<String, Integer> unsortMap) {
		List<Map.Entry<String, Integer>> list =	new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o2, Map.Entry<String, Integer> o1) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static Text toText(String str){
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
	
	public static User getUser(UUID uuid){
		UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
		if (uss.get(uuid).isPresent()){
			return uss.get(uuid).get();
		}
		return null;
	}
	
	public static User getUser(String name){
		UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
		if (uss.get(name).isPresent()){
			return uss.get(name).get();
		}		
		return null;
	}
}
