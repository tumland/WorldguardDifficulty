package com.coffeejawa.WorldguardDifficulty;

import net.minecraft.server.Navigation;

import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

class ZombieMoveEvent extends EntityEvent{
    
    private static final HandlerList handlers = new HandlerList();
    private Zombie zombie;
    public Zombie getZombie() {
        return zombie;
    }

    public void setZombie(Zombie zombie) {
        this.zombie = zombie;
    }

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public Navigation getNav() {
        return nav;
    }

    public void setNav(Navigation nav) {
        this.nav = nav;
    }

    private Location from;
    private Location to;
    private Navigation nav;
    
    public ZombieMoveEvent(Zombie zombie, Location from, Location to, Navigation nav) {
        // TODO Auto-generated constructor stub
        super(zombie);
        this.zombie = zombie;
        this.from = from;
        this.to = to;
        this.nav = nav;
    }

    public boolean isCancelled() {
        // TODO Auto-generated method stub
        if(zombie.isDead())
            return true;
        return false;
    }

    public HandlerList getHandlers() {
        // TODO Auto-generated method stub
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}