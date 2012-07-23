package com.coffeejawa.WorldguardDifficulty;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Entity;

public class EntityRegistry {

    private HashMap<Entity,ArrayList<String>> entityRegistry;
    
    public EntityRegistry(){
        entityRegistry = new HashMap<Entity, ArrayList<String>>();
    }
    
    public void add(Entity entity, ArrayList<String> regionNames){
        if(!entityRegistry.containsKey(entity)){
            entityRegistry.put(entity, regionNames);
        }
    }
    
    public void remove(Entity entity){
        if(entityRegistry.containsKey(entity)){
            entityRegistry.remove(entity);
        }
    }
    public ArrayList<String> getRegions(Entity entity){
        if(entityRegistry.containsKey(entity)){
            return entityRegistry.get(entity);
        }
        return new ArrayList<String>();
    }
}
