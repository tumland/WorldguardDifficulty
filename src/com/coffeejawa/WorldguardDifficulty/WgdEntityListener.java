package com.coffeejawa.WorldguardDifficulty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.EntityCreature;
import net.minecraft.server.Navigation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WgdEntityListener implements Listener 
{
	private final WorldguardDifficulty _plugin;
    private EntityRegistry entityRegistry;
	
		
	public WgdEntityListener(WorldguardDifficulty plugin) {
		super();

		_plugin = plugin;
		entityRegistry = new EntityRegistry();
	}
    
	@EventHandler
	public void onEntityDamage	( EntityDamageEvent event )	
	{
		// Damage cause by player
		if( event instanceof EntityDamageByEntityEvent ){
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
			Player playerDamager = null; 
			
			// Direct damage
			if( damageEvent.getDamager().getType() == EntityType.PLAYER )
			{
				playerDamager = (Player) damageEvent.getDamager();
			}
			// Projectile damage
			else if(damageEvent.getDamager() instanceof Projectile ) {
				Projectile projectile = (Projectile) damageEvent.getDamager();
				if (projectile.getShooter().getType() == EntityType.PLAYER ) {
					playerDamager = (Player) projectile.getShooter();
				}
			}

			if(playerDamager == null){
			    return;
			}
						
			double healthFactor = getModifiedEntityDamageFactor(event.getEntity());
			double damage = Math.ceil(event.getDamage() / healthFactor);
						
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("YOU DAMAGED AN ENTITY: AMT %d", event.getDamage()));	
				_plugin.logger.info(String.format("maxMobHealthMult %f", healthFactor));
				_plugin.logger.info(String.format("Adjusted Damage: AMT %f", damage));
				playerDamager.sendMessage(String.format("Adjusted Damage: AMT %f", damage));
			}
			
			event.setDamage((int) damage);
		}
		
		// Player being damaged
		if( event instanceof EntityDamageByEntityEvent && event.getEntityType() == EntityType.PLAYER ){
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
			Entity damager = null;
            Player player = (Player) event.getEntity();
			
			// Direct damage
			if( damageEvent.getEntity().getType() == EntityType.PLAYER )
			{
			    damager = damageEvent.getEntity();
			}
	        // Projectile damage
			else if(damageEvent.getDamager() instanceof Projectile ) {
                Projectile projectile = (Projectile) damageEvent.getDamager();
                damager = projectile.getShooter();
            }

	        if(damager == null){
	            return;
	        }
		    
		    double damageFactor = getModifiedEntityDamageFactor(damager);
		    
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("you were hit by an entity: amt %d", event.getDamage()));	
				_plugin.logger.info(String.format("mobDifficultyLevel %f", damageFactor));
			}
			
			double damage = Math.ceil(event.getDamage() * damageFactor);
						
			if(_plugin.getConfig().getBoolean("debug")){
				_plugin.logger.info(String.format("Adjusted Damage: AMT %f", damage));
				player.sendMessage(String.format("Adjusted Damage: AMT %f", damage));
			}
			
			event.setDamage((int) damage);
		}
		
		
	}
	
	private double getModifiedEntityHealthFactor(Entity entity)
	{
        if (_plugin.worldguard == null || !_plugin.bWorldGuardEnabled){
            return 1.0;
        }
	
        double worldMobHealthMult = _plugin.getConfig().getDouble("mobHealthMult");
	    

		ArrayList<String> regionNames = entityRegistry.getRegions(entity);
		
		double maxMobHealthMult = worldMobHealthMult;
		
		// Look in our config for regions
		for(String regionName : regionNames) {
			if(_plugin.getRegionConfig() != null) {
				if(_plugin.getRegionConfig().getDouble("regions."+regionName+".mobHealthMult") != 0){
						 
					double mobHealthMult = _plugin.getRegionConfig().getDouble("regions."+regionName+".mobHealthMult");
					
					if(mobHealthMult > maxMobHealthMult){
						maxMobHealthMult = mobHealthMult;
					}
				}
			}
					
		}
	
		return maxMobHealthMult;
	}
	private double getModifiedEntityDamageFactor(Entity entity){
        if (_plugin.worldguard == null || !_plugin.bWorldGuardEnabled){
            return 1.0;
        }

        double worldMobDamageMult = _plugin.getConfig().getDouble("mobDamageMult");

        ArrayList<String> regionNames = entityRegistry.getRegions(entity);

        double maxMobDamageMult = worldMobDamageMult;
        // Look in our config for regions
        for(String regionName : regionNames) {
            if(_plugin.getRegionConfig() != null) {
                if(_plugin.getRegionConfig().getDouble("regions."+regionName+".mobDamageMult") != 0){
                    double mobDamageMult = _plugin.getRegionConfig().getDouble("regions."+regionName+".mobDamageMult");

                    if(mobDamageMult > maxMobDamageMult){
                        maxMobDamageMult = mobDamageMult;
                    }
                }
            }                    
        }
        return maxMobDamageMult; 
	}
    public double getMaxZombieSpeed(Entity entity){
        double worldZombieSpeed = _plugin.getConfig().getDouble("zombieSpeed");
        
        if (_plugin.worldguard == null || !_plugin.bWorldGuardEnabled){
            return worldZombieSpeed;
        }

        ArrayList<String> regionNames = entityRegistry.getRegions(entity);

        double maxZombieSpeed = worldZombieSpeed;
        // Look in our config for regions
        for(String regionName : regionNames) {
            if(_plugin.getRegionConfig() != null) {
                if(_plugin.getRegionConfig().getDouble("regions."+regionName+".zombieSpeed") != 0){
                    double regionZombieSpeed = _plugin.getRegionConfig().getDouble("regions."+regionName+".zombieSpeed");

                    if(regionZombieSpeed > maxZombieSpeed){
                        maxZombieSpeed = regionZombieSpeed;
                    }
                }
            }                    
        }
        return maxZombieSpeed; 
    }
    private double getMinZombieActivationRange(Entity entity) {
        // TODO Auto-generated method stub
        double worldZombieActivationRange = _plugin.getConfig().getDouble("zombieActivationRange");
        
        if (_plugin.worldguard == null || !_plugin.bWorldGuardEnabled){
            return worldZombieActivationRange;
        }
        
        ArrayList<String> regionNames = entityRegistry.getRegions(entity);

        double minZombieActivationRange = worldZombieActivationRange;
        // Look in our config for regions
        for(String regionName : regionNames) {
            if(_plugin.getRegionConfig() != null) {
                if(_plugin.getRegionConfig().getDouble("regions."+regionName+".zombieActivationRange") != 0){
                    double regionZombieActivationRange = _plugin.getRegionConfig().getDouble("regions."+regionName+".zombieActivationRange");

                    if(regionZombieActivationRange < minZombieActivationRange){
                        minZombieActivationRange = regionZombieActivationRange;
                    }
                }
            }                    
        }
        return minZombieActivationRange; 
    }

	private ArrayList<String> locationInRegionsNamed(Location location)
	{
	    
		Map<String,ProtectedRegion> regionMap = getRegions(location);
		ArrayList<String> regionNames = new ArrayList<String>();
		
        for(ProtectedRegion pr : regionMap.values()) {
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
	
	
    private Map<String,ProtectedRegion> getRegions(Location location) {
    	WorldGuardPlugin wg = (WorldGuardPlugin) _plugin.getServer().getPluginManager()
				.getPlugin("WorldGuard");
        
    	Map<String,ProtectedRegion> regionsMap = new HashMap<String,ProtectedRegion>();
        if(wg == null) {
            if(_plugin.getConfig().getBoolean("debug")){
                _plugin.logger.info(String.format("Worldguard == null"));
            }
            return regionsMap;
        }
        RegionManager rm = wg.getRegionManager(location.getWorld()); 
        if(rm == null) {
            if(_plugin.getConfig().getBoolean("debug")){
                _plugin.logger.info(String.format("RegionManager == null"));
            }
            return regionsMap;
        }
            
      return rm.getRegions();
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event){
        Location loc = event.getLocation();
        ArrayList<String> regionNames = locationInRegionsNamed(loc);

        if(regionNames.size() == 0){
            return;
        }
        
        // add to entity registry
        entityRegistry.add(event.getEntity(), regionNames);
        if(_plugin.getConfig().getBoolean("debug")){
            _plugin.logger.info(String.format("Added to entity registry: %s",event.getEntity().getType().toString()));
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        entityRegistry.remove(event.getEntity());
        if(_plugin.getConfig().getBoolean("debug")){
            _plugin.logger.info(String.format("Removed from entity registry: %s",event.getEntity().getType().toString()));
        }
    }
    
//    @EventHandler
//    public void onChunkUnload(ChunkUnloadEvent event){
//        Entity[] entities = event.getChunk().getEntities();
//        
//        for( Entity entity : entities ){
//            entityRegistry.remove(entity);
//            if(_plugin.getConfig().getBoolean("debug")){
//                _plugin.logger.info(String.format("Removed from entity registry: %s",entity.getType().toString()));
//            }
//        }
//    }
//    
//    @EventHandler
//    public void onChunkLoad(ChunkLoadEvent event){
//        Entity[] entities = event.getChunk().getEntities();
//        
//        for( Entity entity : entities ){
//            Location loc = entity.getLocation();
//            ArrayList<String> regionNames = locationInRegionsNamed(loc);
//
//            if(regionNames.size() == 0){
//                return;
//            }
//            
//            // add to entity registry
//            entityRegistry.add(entity, regionNames);
//            if(_plugin.getConfig().getBoolean("debug")){
//                _plugin.logger.info(String.format("Added to entity registry: %s",entity.getType().toString()));
//            }
//        }
//    }
    
    @EventHandler
    public void onZombieMove(ZombieMoveEvent event) {
        Zombie zombie = event.getZombie();
        double zombieSpeed = this.getMaxZombieSpeed(event.getEntity());
        double zombieActivationRange = this.getMinZombieActivationRange(event.getEntity());
      
        
        for (Player player : Bukkit.getOnlinePlayers()){
            if(player.getLocation().distance(zombie.getLocation()) < zombieActivationRange){
                // set zombie speed
                EntityCreature ec = ((CraftCreature)zombie).getHandle();
                Navigation nav = ec.al();
                  nav.a((float)zombieSpeed);
                  
                  if(_plugin.getConfig().getBoolean("debug")){
                      _plugin.logger.info(String.format("Zombie lunge activated!"));
                  }
                break;
            }
        }
    }


}



