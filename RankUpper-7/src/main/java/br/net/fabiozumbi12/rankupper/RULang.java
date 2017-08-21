package br.net.fabiozumbi12.rankupper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class RULang {
	
	private static final HashMap<Player, String> DelayedMessage = new HashMap<Player, String>();
	static HashMap<String, String> BaseLang = new HashMap<String, String>();
	static HashMap<String, String> Lang = new HashMap<String, String>();
    static String pathLang;
    static File langFile;
    static String resLang;
    static Path defaultDir;
    static RankUpper pl;
	
	public static SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (String help:Lang.keySet()){
			if (help.startsWith("commands.help.")){
				values.add(help.replace("commands.help.", ""));
			}
		}
		return values;
	}
	
	public static void init(RankUpper plugin, Path defDir) {
		pl = plugin;
		defaultDir = defDir;
		pathLang = defDir + File.separator + "lang" + plugin.cfgs.getString("language") + ".properties"; 
		langFile = new File(pathLang);
		resLang = "lang" + plugin.cfgs.getString("language") + ".properties";
						
		if (!langFile.exists()) {
			if (RankUpper.class.getResource(resLang) == null){		
				plugin.cfgs.setConfig("EN-US", "language");
				plugin.cfgs.save();
				resLang = "langEN-US.properties";
				pathLang = defDir + File.separator + "langEN-US.properties";
			}
			
			try {
				InputStream isReader = RankUpper.class.getResourceAsStream(resLang);
				FileOutputStream fos = new FileOutputStream(langFile);
				while (isReader.available() > 0) {  // write contents of 'is' to 'fos'
			        fos.write(isReader.read());
			    }
			    fos.close();
			    isReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//RankUpper.class.getResource(resLang);//create lang file
			RULogger.info("Created config file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		RULogger.info("Language file loaded - Using: "+ plugin.cfgs.getString("language"));	
	}
	
	static void loadBaseLang(){
	    BaseLang.clear();
	    Properties properties = new Properties();
	    try {
	    	InputStream fileInput = RankUpper.class.getResourceAsStream("langEN-US.properties");	      
	        Reader reader = new InputStreamReader(fileInput, "UTF-8");
	        properties.load(reader);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    for (Object key : properties.keySet()) {
	      if ((key instanceof String)) {
	    	  BaseLang.put((String)key, properties.getProperty((String)key));
	      }
	    }
	    updateLang();
	  }
	
	static void loadLang() {
		Lang.clear();
		Properties properties = new Properties();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Object key : properties.keySet()) {
			if (!(key instanceof String)) {
				continue;
			}			
			Lang.put((String) key, properties.getProperty((String) key));
		}		
		
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").replace(".", ""));
			int rpv = Integer.parseInt(pl.get().getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				RULogger.warning("Your lang file is outdated. Probally need strings updates!");
				RULogger.warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", pl.get().getVersion().get());
			}
		}		
	}
	
	static void updateLang(){
	    for (String linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", pl.get().getVersion().get());
    	}
	    try {
	      Properties properties = new Properties()
	      {
	        private static final long serialVersionUID = 1L;	        
	        public synchronized Enumeration<Object> keys(){
	          return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	        }
	      };
	      FileReader reader = new FileReader(pathLang);
	      BufferedReader bufferedReader = new BufferedReader(reader);
	      properties.load(bufferedReader);
	      bufferedReader.close();
	      reader.close();
	      properties.clear();
	      for (String key : Lang.keySet()) {
	        if ((key instanceof String)) {
	          properties.put(key, Lang.get(key));
	        }
	      }
	      properties.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	
	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4" + key;
		} else {
			FMsg = Lang.get(key);
		}
		
		return FMsg;
	}
	
	public static void sendMessage(CommandSource p, String key){		
		if (Lang.get(key) == null){
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+get(key)));
		}
	}
	
	public static void sendMessage(Player p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+get(key)));
		}		
		
		DelayedMessage.put(p,key);
		Sponge.getScheduler().createSyncExecutor(pl).schedule(new Runnable() { 
			public void run() {
				if (DelayedMessage.containsKey(p)){
					DelayedMessage.remove(p);
				}
				} 
			},1, TimeUnit.SECONDS);
	}
	
	static String translBool(String bool){		
		return get("region."+bool);
	}
	
	static String translBool(Boolean bool){		
		return get("region."+bool.toString());
	}
}
