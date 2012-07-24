package com.coffeejawa.WorldguardDifficulty;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class WgdZombieListener implements Listener {
    private WorldguardDifficulty plugin;
    
    WgdZombieListener(WorldguardDifficulty plugin){
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCreatureSpawn (CreatureSpawnEvent event) {
        
        Location location = event.getLocation();
        Entity entity = event.getEntity();
        EntityType entityType = event.getEntityType();
 
        net.minecraft.server.World mcWorld = ((CraftWorld) (event.getLocation().getWorld())).getHandle();
        net.minecraft.server.Entity mcEntity = (((CraftEntity) entity).getHandle());
 
        if (entityType == EntityType.ZOMBIE && mcEntity instanceof WgdZombie == false){
            WgdZombie wgdZombie = new WgdZombie(mcWorld);
 
            wgdZombie.setPosition(location.getX(), location.getY(), location.getZ());
            
            mcWorld.removeEntity((net.minecraft.server.EntityZombie) mcEntity);
            mcWorld.addEntity(wgdZombie, SpawnReason.CUSTOM);
 
            return;
        }
    }
}

//public SuperDuperFastZombie(World world) {
//super(world);
//this.bb = 10.0F;
//this.goalSelector.a(0, new PathfinderGoalFloat(this));
//this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
//this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bb, false));
//this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityVillager.class, this.bb, true));
//this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bb));
//this.goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, this.bb, false));
//this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bb));
//this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
//this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
//this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
//this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
//this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, 16.0F, 0, false));
//}