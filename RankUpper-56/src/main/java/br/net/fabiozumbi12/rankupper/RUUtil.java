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
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class RUUtil {
	private static RankUpper plugin;
	
	public static void init(RankUpper pl){
		plugin = pl;
	}
	
	public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(plugin.cfgs.getString("date-format"));
        Date today = Calendar.getInstance().getTime(); 
        String now = df.format(today);
		return now;    	
    }

	public static String timeDescript(int timeNeeded) {
		long day = TimeUnit.MINUTES.toDays(timeNeeded);
		long hours = TimeUnit.MINUTES.toHours(timeNeeded-TimeUnit.DAYS.toMinutes(day));
		long minutes = TimeUnit.MINUTES.toMinutes((timeNeeded-TimeUnit.DAYS.toMinutes(day))-TimeUnit.HOURS.toMinutes(hours));		
		StringBuilder msg = new StringBuilder();
		if (day > 0){
			msg.append(day+" "+RULang.get("config.day")+", ");
		}
		if (hours > 0 ){
			msg.append(hours+" "+RULang.get("config.hour")+", ");
		}
		if (minutes > 0){
			msg.append(minutes+" "+RULang.get("config.minute")+", ");
		}
		
		try{
			msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1,"");
			if (msg.toString().contains(",")){
				msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1," "+RULang.get("config.and"));
			}			
		} catch(StringIndexOutOfBoundsException ex){
			return RULang.get("config.lessThan");
		}
		
		if (msg.toString().endsWith(" ")){
			return msg.toString().substring(0, msg.toString().length()-1);
		}		
		return msg.toString();		
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
}
