package com.coffeejawa.WorldguardDifficulty;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WgdDamageListener implements Listener 
{
	private double worldMobHealthMult;
	private double worldMobDamageMult;
	private final WorldguardDifficulty _plugin;
	
    private Map<String,ProtectedRegion> AllProtectedRegions;
		
	public WgdDamageListener(WorldguardDifficulty plugin) {
		super();
		this.worldMobHealthMult = plugin.getConfig().getDouble("mobHealthMult");
		this.worldMobDamageMult = plugin.getConfig().getDouble("mobDamageMult");
		_plugin = plugin;
	}
    
	@EventHandler
	public 
	void onEntityDamage	( EntityDamageEvent event )	
	{
		//TODO : scale mob damage in addition to player damage
		
		Player playerDamager = null; 
		
		if( event instanceof EntityDamageByEntityEvent ){
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
			// Direct damage
			if( damageEvent.getDamager().getType() == EntityType.PLAYER )
			{
				playerDamager = (Player) damageEvent.getDamager();
			}
			// Projectile damage
			else if(damageEvent.getDamager().getType() == EntityType.ARROW ) {
				Projectile arrow = (Projectile) damageEvent.getDamager();
				if (arrow.getShooter().getType() == EntityType.PLAYER ) {
					playerDamager = (Player) arrow.getShooter();
				}
			}
			// TODO : add handling for potions, eggs.

		}
		
		if (playerDamager != null){
						
			updateHighestRegionDifficulty(playerDamager);
			double localDifficultyLevel = Math.max(this.maxMobHealthMult, this.worldMobHealthMult);
			double damage = Math.ceil(event.getDamage() / localDifficultyLevel);
						
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("YOU DAMAGED AN ENTITY: AMT %d", event.getDamage()));	
				_plugin.logger.info(String.format("maxMobHealthMult %f", this.maxMobDamageMult));
				_plugin.logger.info(String.format("Adjusted Damage: AMT %f", damage));
				playerDamager.sendMessage(String.format("Adjusted Damage: AMT %f", damage));
			}
			
			event.setDamage((int) damage);
			
			LivingEntity targetEntity = null;
			if(!event.getEntity().isDead()){
				targetEntity = (LivingEntity) event.getEntity();
			}
			
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("Your %s target how has %d health", targetEntity.getType().name(), targetEntity.getHealth()-event.getDamage()));
			}
		}
		
		// Player being damaged
		Player damagedPlayer = null;
		if( event instanceof EntityDamageByEntityEvent ){
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
			// Direct damage
			if( damageEvent.getEntity().getType() == EntityType.PLAYER )
			{
				damagedPlayer = (Player) damageEvent.getEntity();
			}
		}
		if(damagedPlayer != null){
			// increase damage based on difficulty
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("you were hit by an entity: amt %d", event.getDamage()));	
				_plugin.logger.info(String.format("mobDifficultyLevel %f", this.maxMobDamageMult));
			}
			
			double damage = Math.ceil(event.getDamage() * this.maxMobDamageMult);
						
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("Adjusted Damage: AMT %f", damage));
				damagedPlayer.sendMessage(String.format("Adjusted Damage: AMT %f", damage));
			}
			
			event.setDamage((int) damage);
		}
		
		
	}
	private double maxMobHealthMult = 1.0;
	private double maxMobDamageMult = 1.0;
	
	private void updateHighestRegionDifficulty(Player damager)
	{
		
		if (_plugin.worldguard == null || !_plugin.bWorldGuardEnabled){
			return;
		}

		ArrayList<String> regionNames = locationInRegionsNamed(damager.getLocation());
		
		double maxMobHealthMult = this.worldMobHealthMult;
		double maxMobDamageMult = this.worldMobDamageMult;
		
		// Look in our config for regions
		for(String regionName : regionNames) {
			if(_plugin.getRegionConfig() != null) {
				if(_plugin.getRegionConfig().getDouble("regions."+regionName+".mobHealthMult") != 0 &&
						_plugin.getRegionConfig().getDouble("regions."+regionName+".mobDamageMult") != 0){
						 
					double mobHealthMult = _plugin.getRegionConfig().getDouble("regions."+regionName+".mobHealthMult");
					double mobDamageMult = _plugin.getRegionConfig().getDouble("regions."+regionName+".mobDamageMult");
					
					if(mobHealthMult > maxMobHealthMult){
						maxMobHealthMult = mobHealthMult;
					}
					if(mobDamageMult > maxMobDamageMult){
						maxMobDamageMult = mobDamageMult;
					}
				}
			}
					
		}
	
		this.maxMobDamageMult = maxMobDamageMult;
		this.maxMobHealthMult = maxMobHealthMult;
	}

	private ArrayList<String> locationInRegionsNamed(Location location)
	{
		updateRegions();
		ArrayList<String> regionNames = new ArrayList<String>();
		
        for(ProtectedRegion pr : this.AllProtectedRegions.values()) {
            int depth = 1;
            ProtectedRegion p = pr;
            while(p.getParent() != null) {
                depth++;
                p = p.getParent();
            }
            if(depth > 16)
                continue;
            if( isLocationInRegion(location, pr) ){
                String name = pr.getId();
                regionNames.add(name);
            }
		
        }
        return regionNames;
	}

	private boolean isLocationInRegion(Location location, ProtectedRegion region) {

        String tn = region.getTypeName();
        com.sk89q.worldedit.BlockVector l0 = region.getMinimumPoint();      
        com.sk89q.worldedit.BlockVector l1 = region.getMaximumPoint();

        if(tn.equalsIgnoreCase("cuboid")) { 
  
            Location l = location;
            
            // Is player's X within the region?
            if((l0.getBlockX() < l.getBlockX() && l.getBlockX() < l1.getBlockX()) || (l1.getBlockX() < l.getBlockX() && l.getBlockX() < l0.getBlockX())) {
                // Is player's Y within the region?
            	if((l0.getBlockY() < l.getBlockY() && l.getBlockY() < l1.getBlockY()) || (l1.getBlockY() < l.getBlockY() && l.getBlockY() < l0.getBlockY())){
            		return true;
            	}
            }
            	
                    }
//        else if(tn.equalsIgnoreCase("polygon")) {
//TODO: add polygon support

        else {  
            return false;
        }
		return false;
    }
    
    private void updateRegions() {
    	WorldGuardPlugin wg = (WorldGuardPlugin) _plugin.getServer().getPluginManager()
				.getPlugin("WorldGuard");
        if(wg == null) {
            return;
        }
    	
        Map<String,ProtectedRegion> wRegions;
        for(World w : _plugin.getServer().getWorlds()) {
            RegionManager rm = wg.getRegionManager(w); 
            if(rm == null) continue;
            
            wRegions = rm.getRegions();
            if(wRegions != null && wRegions.size() > 0){
            	if(this.AllProtectedRegions == null) {
            		this.AllProtectedRegions = wRegions;
            	}
            	else {
            		this.AllProtectedRegions.putAll(wRegions);  
            	}
            }
        }
    }
}



