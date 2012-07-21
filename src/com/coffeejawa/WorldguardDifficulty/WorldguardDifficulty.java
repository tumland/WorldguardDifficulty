package com.coffeejawa.WorldguardDifficulty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        getServer().getPluginManager().registerEvents(new WgdDamageListener(this), this);

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
				sender.sendMessage("Usage: /wgd [add|set|list|debug]");
			}
			if (args.length > 0){
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
					HashMap<String,Object> defaultRegionOptions = new HashMap<String,Object>();
					defaultRegionOptions.put("mobDamageMult", 1.0);
					defaultRegionOptions.put("mobHealthMult", 1.0);
					this.regionConfig.createSection(sectionName, defaultRegionOptions);
					this.saveRegionConfig();
					this.reloadRegionConfig();
					sender.sendMessage("Region "+args[1]+" added");
				}
				if (arg.equalsIgnoreCase("remove") )
				{
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd remove <regionID>");
						sender.sendMessage("Description: removes mappings from regions config");
						return false;
					}
					
					if(!sender.hasPermission("wgd.remove")){
						sender.sendMessage("Add failed: requires permission" + "wgd.remove");
						return false;
					}
					
					// add new region define
					String sectionName = "regions."+args[1];
					// set up defaults
					HashMap<String,Object> defaultRegionOptions = new HashMap<String,Object>();
					defaultRegionOptions.put("mobDamageMult", 1.0);
					defaultRegionOptions.put("mobHealthMult", 1.0);
					this.regionConfig.createSection(sectionName, defaultRegionOptions);
					this.saveRegionConfig();
					this.reloadRegionConfig();
					sender.sendMessage("Region "+args[1]+" added");
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
					ArrayList<String> propertyList = new ArrayList<String>();
					propertyList.add("mobHealthMult");
					propertyList.add("mobDamageMult");
					
					if(!propertyList.contains(propName)){
						sender.sendMessage("Unrecognized property.");
						return false;
					}
					
					this.getRegionConfig().set(sectionName+"."+propName, Double.parseDouble(value));
					sender.sendMessage("Set "+args[1]+"'s property "+propName+" to "+value);
					
					this.saveRegionConfig();
				}	
				else if (arg.equalsIgnoreCase("list")){
					if(args.length != 2){
						sender.sendMessage("Usage: /wgd list <regionID>");
						sender.sendMessage("Description: Lists all properties and values for a given region");
						return false;
					}
					if(!sender.hasPermission("wgd.add")){
						sender.sendMessage("Add failed: requires permission" + "wgd.add");
						return false;
					}
					sender.sendMessage("List of properties for region "+args[1]+":");
					Map<String, Object> values = this.getRegionConfig().getConfigurationSection("regions."+args[1]).getValues(false);
					
					if(values.size() == 0){
						sender.sendMessage("Region not found");
						return true;
					}
					
					Iterator<Map.Entry<String, Object>> it = values.entrySet().iterator();
					while (it.hasNext()){
						Map.Entry<String, Object> pairs = it.next();
						sender.sendMessage("Region "+args[1]+" properties: "+pairs.getKey()+" : "+pairs.getValue().toString());
						it.remove();
					}
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
