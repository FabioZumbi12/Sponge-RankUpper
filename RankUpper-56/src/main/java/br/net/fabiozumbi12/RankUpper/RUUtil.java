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

import me.rojo8399.placeholderapi.PlaceholderService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class RUUtil {
	
	public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RankUpper.get().getConfig().root().date_format);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

	public static String timeDescript(int timeNeeded) {
		long day = TimeUnit.MINUTES.toDays(timeNeeded);
		long hours = TimeUnit.MINUTES.toHours(timeNeeded-TimeUnit.DAYS.toMinutes(day));
		long minutes = TimeUnit.MINUTES.toMinutes((timeNeeded-TimeUnit.DAYS.toMinutes(day))-TimeUnit.HOURS.toMinutes(hours));		
		StringBuilder msg = new StringBuilder();
		if (day > 0){
			msg.append(day).append(" ").append(RankUpper.get().getLang().get("config.day")).append(", ");
		}
		if (hours > 0 ){
			msg.append(hours).append(" ").append(RankUpper.get().getLang().get("config.hour")).append(", ");
		}
		if (minutes > 0){
			msg.append(minutes).append(" ").append(RankUpper.get().getLang().get("config.minute")).append(", ");
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
		list.sort((o2, o1) -> (o1.getValue()).compareTo(o2.getValue()));
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
		return sortedMap;
	}

	public static double getPlaceholderValue(PlaceholderService papi, String placeholder, User p) {
		Text text = papi.replacePlaceholders(placeholder, p, null);
		if (text != null && !text.isEmpty() && text.toPlain() != null) {
			try
			{
				return Double.parseDouble(text.toPlain());
			}
			catch(NumberFormatException e)
			{
				return 0;
			}
		}
		return 0;
	}
	
	public static Text toText(String str){
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }	
}
