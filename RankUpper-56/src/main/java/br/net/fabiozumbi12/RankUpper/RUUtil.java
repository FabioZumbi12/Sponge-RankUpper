package br.net.fabiozumbi12.RankUpper;

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
	
	public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RankUpper.get().getConfig().root().date_format);
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
			msg.append(day+" "+RankUpper.get().getLang().get("config.day")+", ");
		}
		if (hours > 0 ){
			msg.append(hours+" "+RankUpper.get().getLang().get("config.hour")+", ");
		}
		if (minutes > 0){
			msg.append(minutes+" "+RankUpper.get().getLang().get("config.minute")+", ");
		}
		
		try{
			msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1,"");
			if (msg.toString().contains(",")){
				msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1," "+RankUpper.get().getLang().get("config.and"));
			}			
		} catch(StringIndexOutOfBoundsException ex){
			return RankUpper.get().getLang().get("config.lessThan");
		}
		
		if (msg.toString().endsWith(" ")){
			return msg.toString().substring(0, msg.toString().length()-1);
		}		
		return msg.toString();		
	}
	
	public static Map<String, Integer> sort(Map<String, Integer> unsortMap) {
		List<Map.Entry<String, Integer>> list =	new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
		Collections.sort(list, (o2, o1) -> (o1.getValue()).compareTo(o2.getValue()));
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
