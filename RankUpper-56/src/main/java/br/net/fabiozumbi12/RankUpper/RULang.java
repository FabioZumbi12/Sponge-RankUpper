package br.net.fabiozumbi12.RankUpper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
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
	static Properties BaseLang = new Properties();
	static Properties Lang = new Properties();
    static String pathLang;
    static File langFile;
    static String resLang;
    static Path defaultDir;
    static RankUpper pl;
	
	public SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (Object help:Lang.keySet()){
			if (help.toString().startsWith("commands.help.")){
				values.add(help.toString().replace("commands.help.", ""));
			}
		}
		return values;
	}
	
	public RULang() {
		pathLang = RankUpper.get().getConfigDir() + File.separator + "lang" + RankUpper.get().getConfig().getString("language") + ".properties"; 		
		resLang = "lang" + RankUpper.get().getConfig().getString("language") + ".properties";
			
		langFile = new File(pathLang);
		if (!langFile.exists()) {
			if (RankUpper.get().instance().getAsset(resLang).isPresent()){
				resLang = "langEN-US.properties";
				pathLang = RankUpper.get().getConfigDir() + File.separator + "langEN-US.properties";
			}
			
			try {
				RankUpper.get().instance().getAsset(resLang).get().copyToDirectory(RankUpper.get().getConfigDir());
			}  catch (IOException e) {
				e.printStackTrace();
			}
			RankUpper.get().getLogger().info("Created config file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		RankUpper.get().getLogger().info("Language file loaded - Using: "+ RankUpper.get().getConfig().getString("language"));	
	}
	
	private void loadBaseLang(){
	    BaseLang.clear();
	    try {
	        BaseLang.load(RankUpper.get().instance().getAsset("langEN-US.properties").get().getUrl().openStream());
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    updateLang();
	  }
	
	private void loadLang() {
		Lang.clear();
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
			int rpv = Integer.parseInt(RankUpper.get().instance().getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				RankUpper.get().getLogger().warning("Your lang file is outdated. Probally need strings updates!");
				RankUpper.get().getLogger().warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", RankUpper.get().instance().getVersion().get());
			}
		}		
	}
	
	private void updateLang(){
	    for (Object linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", RankUpper.get().instance().getVersion().get());
    	}
	      try {
			Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	  }
	
	public String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4" + key;
		} else {
			FMsg = Lang.get(key).toString();
		}
		
		return FMsg;
	}
	
	public void sendMessage(CommandSource p, String key){		
		if (Lang.get(key) == null){
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(RUUtil.toText(get("_rankupper.prefix")+" "+get(key)));
		}
	}
	
	public void sendMessage(Player p, String key){
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
	
	public String translBool(String bool){		
		return get("region."+bool);
	}
	
	public String translBool(Boolean bool){		
		return get("region."+bool.toString());
	}
}
