package ro.raizen.src.reactionrewards;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class ReactionRewards extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	private ChatListener cl;
	
	//Configs
	private ConfigHandler main;
	private ConfigHandler lang;
	private ConfigHandler rewards;
	private ConfigHandler trivia;
	
	//Question Handler
	private QuestionHandler question;
	//Question Generator Task
	private GeneratorTask generator;
	//Vault Economy
	private static Economy econ = null;
	//Database Handler
	private Database db;
	
	@Override
	public void onEnable() {
  
		/*
		 * implement debug
		 */
		
		boolean hasDependencies = true;
		
		//Check if Vault is installed
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			log.severe(String.format("[%s] Dependency Vault not found", getDescription().getName()));
			hasDependencies = false;
			getPluginLoader().disablePlugin(this);
		}
		
		//Check if SQLibrary is installed
		if(getServer().getPluginManager().getPlugin("SQLibrary") == null) {
			log.severe(String.format("[%s] Dependency SQLibrary not found", getDescription().getName()));
			hasDependencies = false;
			getPluginLoader().disablePlugin(this);
		}
		
		if(hasDependencies) {
			
			db = new Database(this);
			
			setupEconomy();
			
			//ChatListener checks for the correct answer
			cl = new ChatListener(this);
			getServer().getPluginManager().registerEvents(cl, this);
			
			initConfigs();
			
			question = new QuestionHandler(this);

			if(getCfg("main").getBoolean("enabled") == true) {
				startGen();
			}
			
			CommandHandler cmd = new CommandHandler(this);
			
			getCommand("reactionrewards").setExecutor(cmd);
			getCommand("rr").setExecutor(cmd);
		}
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		db.Close();
	}
	
	// Initialize all configurations
	private void initConfigs() {
		main = new ConfigHandler(this, "config.yml");
		main.saveDefaultConfig();
		lang = new ConfigHandler(this, "lang.yml");
		lang.saveDefaultConfig();
		rewards = new ConfigHandler(this, "rewards.yml");
		rewards.saveDefaultConfig();
		trivia = new ConfigHandler(this, "trivia.yml");
		trivia.saveDefaultConfig();
	}
	
	//Reload config files
	public void reloadConfigs() {
		main.reloadConfig();
		lang.reloadConfig();
		rewards.reloadConfig();
		trivia.reloadConfig();
	}
	
	// Get specified configuration
	public FileConfiguration getCfg(String c) {
		switch(c) {
			case "main":
				return main.getConfig(); 
			case "lang":
				return lang.getConfig();
			case "rewards":
				return rewards.getConfig();
			case "trivia":
				return trivia.getConfig();
		}
		return null;
	}
	
	public void saveCfg(String c) {
		switch(c) {
			case "main":
				main.saveConfig(); 
			case "lang":
				lang.saveConfig();
			case "rewards":
				rewards.saveConfig();
			case "trivia":
				trivia.saveConfig();
		}
	}
	
	public QuestionHandler getQuestionHandler() {
		return question;
	}
	
	//setup vault economy
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    public static Economy getEconomy() {
    	return econ;
    }
	
    //JavaPlugin has getDatabase already
    public Database getDb() {
    	return db;
    }
    
    //Get a string from lang.yml with the plugin prefix in front of it
    public String getLang(String path) {
    	if(getCfg("lang").contains(path) && getCfg("lang").getString(path) != "") {
    		String s = getCfg("lang").getString(path);
    		return parseString(s);
    	} else {
    		return null;
    	}
    }
    
    //Format a string to have the plugin prefix in front of it
    public String parseString(String s) {
    	if(getCfg("lang").contains("Prefix") && getCfg("lang").getString("Prefix") != "") {
    		return ChatColor.translateAlternateColorCodes('&', getCfg("lang").getString("Prefix") + s);
    	} else {
    		return ChatColor.translateAlternateColorCodes('&', s);
    	}
    }
    
    public static boolean isNumeric(String s) {  
      try  
      {  
        @SuppressWarnings("unused")
		int i = Integer.parseInt(s);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
    
    public void startGen() {
		int id;
		generator = new GeneratorTask(this);
		id = getServer().getScheduler().scheduleSyncDelayedTask(this, generator);
		generator.setId(id);
    }
    
    public void stopGen() {
    	getServer().getScheduler().cancelTask(generator.taskId);
    }
    
}
