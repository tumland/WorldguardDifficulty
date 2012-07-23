package com.coffeejawa.WorldguardDifficulty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldguardDifficulty extends JavaPlugin {

	public final Logger logger = Logger.getLogger("Minecraft");
	
	public void onDisable(){ 
		this.saveConfig();
		getLogger().info("Your plugin has been disabled.");
	}
	
	public WorldGuardPlugin worldguard;
	public boolean bWorldGuardEnabled;
	
	@Override
	public void onEnable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		
		bWorldGuardEnabled = false;
		
		getConfiguration();
		reloadRegionConfig();
		
		this.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " Has Been Enabled!");
        getServer().getPluginManager().registerEvents(new WgdEntityListener(this), this);

    }
	public void getConfiguration()
	{	
		this.reloadConfig();
		
		if(getConfig().getBoolean("useWorldGuard")){
			this.bWorldGuardEnabled = checkWorldGuard();
		}
		
		if (this.bWorldGuardEnabled == true && worldguard != null)
			logger.info("[" + this.getDescription().getName()
					+ "] WorldGuard support enabled.");
	}

	private boolean checkWorldGuard()
	{
		if (getServer().getPluginManager().getPlugin("WorldGuard") == null)
		{
			getLogger().info("WorldGuard plugin not found");
			return false;
		}
		worldguard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
		return true;
	}
	
	private FileConfiguration regionConfig = null;
	private File regionConfigFile = null;
	
	public void reloadRegionConfig() {
	    if (regionConfigFile == null) {
	    	regionConfigFile = new File(getDataFolder(), "regions.yml");
	    }
	    regionConfig = YamlConfiguration.loadConfiguration(regionConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("regions.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        regionConfig.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getRegionConfig() {
	    if (regionConfig == null) {
	        this.reloadRegionConfig();
	    }
	    return regionConfig;
	}
	
	public void saveRegionConfig() {
	    if (regionConfig == null || regionConfigFile == null) {
	    	return;
	    }
	    try {
	    	getRegionConfig().save(regionConfigFile);
	    } catch (IOException ex) {
	        this.getLogger().log(Level.SEVERE, "Could not save config to " + regionConfigFile, ex);
	    }
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String name = cmd.getName();
		if (name.equalsIgnoreCase("wgd")){
			// check the first arg and call any appropriate nested parsers
			if(args.length == 0){
				sender.sendMessage("WorldguardDifficulty Command Interface");
				sender.sendMessage("Usage: /wgd [add|set|list|reset|remove|debug]");
			}
			if (args.length > 0){
				HashMap<String,Object> defaultRegionOptions = new HashMap<String,Object>();
				defaultRegionOptions.put("mobDamageMult", 1.0);
				defaultRegionOptions.put("mobHealthMult", 1.0);
				
				String arg = args[0];
				if (arg.equalsIgnoreCase("add") )
				{
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd add <regionID>");
						sender.sendMessage("Description: adds mappings to regions config");
						sender.sendMessage("with default configuration values");
						return false;
					}
					
					if(!sender.hasPermission("wgd.add")){
						sender.sendMessage("Add failed: requires permission" + "wgd.add");
						return false;
					}
					
                    // add new region define
                    String sectionName = "regions."+args[1];
                    // set up defaults
                    if(!getRegionConfig().isConfigurationSection(sectionName)){
                        getRegionConfig().createSection(sectionName, defaultRegionOptions);
                        saveRegionConfig();
                        reloadRegionConfig();
                        sender.sendMessage("Region "+args[1]+" added");
                    }
                    else{
                        sender.sendMessage("Error: region already exists in config.");
                        return false;
                    }
				}
				else if (arg.equalsIgnoreCase("set")){
					if(args.length != 4){
						sender.sendMessage("Usage: /wgd set <regionID> <property name> <value>");
						sender.sendMessage("Description: Sets region property values");
						return false;
					}
					if(!sender.hasPermission("wgd.add")){
						sender.sendMessage("Add failed: requires permission" + "wgd.add");
						return false;
					}
					String sectionName = "regions."+args[1];
					String propName = args[2];
					String value = args[3];
					
					sender.sendMessage("Warning: only double types currently allowed for property values.");
					
					// Is the property Name in list of known args?
					if(!defaultRegionOptions.containsKey(propName)){
						sender.sendMessage("Unrecognized property.");
						return false;
					}
					
					if(!getRegionConfig().isConfigurationSection(sectionName)){
						getRegionConfig().createSection(sectionName, defaultRegionOptions);
					}
					getRegionConfig().set(sectionName+"."+propName, Double.parseDouble(value));
					sender.sendMessage("Set "+args[1]+"'s property "+propName+" to "+value);
					
					this.saveRegionConfig();
				}	
				else if (arg.equalsIgnoreCase("list")){
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd list <regionID>");
						sender.sendMessage("OR /wgd list Regions");
						sender.sendMessage("Description: Lists all properties and values for a given region");
						return false;
					}
					if(!sender.hasPermission("wgd.add")){
						sender.sendMessage("Add failed: requires permission" + "wgd.add");
						return false;
					}
					if(args[1].equalsIgnoreCase("regions")){
					    
					    ConfigurationSection regionsSection = getRegionConfig().getConfigurationSection("regions");
					    if(regionsSection == null){
					        sender.sendMessage("Error: Regions section missing from regions.yml");
					        return false;
					    }
					    
					    Set<String> sections = regionsSection.getKeys(false);
					    String msgString = "Configured regions: ";
					    for(String sectionName : sections) {
					        msgString += ", "+sectionName;
					    }
					    sender.sendMessage(msgString);
					    return true;
					}
										
					sender.sendMessage("List of properties for region "+args[1]+":");
					String sectionName = "regions."+args[1];
					ConfigurationSection section = this.getRegionConfig().getConfigurationSection(sectionName);
					
					//If region hasn't been added, add it now.
					if(section == null){
					    section = this.regionConfig.createSection(sectionName, defaultRegionOptions);
	                    saveRegionConfig();
	                    reloadRegionConfig();
					}
					Map<String,Object> values = section.getValues(false);
					Iterator<Map.Entry<String, Object>> it = values.entrySet().iterator();
					
					sender.sendMessage("Region "+args[1]+" properties: ");
					while (it.hasNext()){
						Map.Entry<String, Object> pairs = it.next();
						sender.sendMessage(pairs.getKey()+" : "+pairs.getValue().toString());
						it.remove();
					}
				}
				else if(arg.equalsIgnoreCase("remove")){
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd remove <regionID>");
						sender.sendMessage("Description: Removes region from database.");
						return false;
					}
					if(!sender.hasPermission("wgd.remove")){
						sender.sendMessage("Remove failed: requires permission" + "wgd.remove");
						return false;
					}
					String sectionName = "regions."+args[1];
					getRegionConfig().set(sectionName, null);
                    saveRegionConfig();
                    reloadRegionConfig();
				}
				else if(arg.equalsIgnoreCase("reset")){
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd reset <regionID>");
						sender.sendMessage("Description: Resets region to default values.");
						return false;
					}
					if(!sender.hasPermission("wgd.reset")){
						sender.sendMessage("Reset failed: requires permission" + "wgd.reset");
						return false;
					}
					String sectionName = "regions."+args[1];
					this.regionConfig.createSection(sectionName, defaultRegionOptions);
					sender.sendMessage("Region "+args[1]+" configuration reset to default options");
				}
				else if(arg.equalsIgnoreCase("debug")){
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd debug <on|off>");
						sender.sendMessage("Description: Enables debug mode to display damage amounts");
						return false;
					}
					if(args[1].equalsIgnoreCase("on")){
						this.getConfig().set("debug", true);
						sender.sendMessage("Toggled DEBUG mode to ON");
					}
					else if(args[1].equalsIgnoreCase("off")){
						this.getConfig().set("debug", false);
						sender.sendMessage("Toggled DEBUG mode to OFF");
					}
					else{
						sender.sendMessage("Unrecognized input");
						sender.sendMessage("Usage: /wgd debug <on|off>");
						return false;
					}
				}
			}
	
		}
		return true;
	}
}
