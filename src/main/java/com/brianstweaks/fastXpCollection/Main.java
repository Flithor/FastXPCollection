package com.brianstweaks.fastXpCollection;

import java.io.File;

import net.minecraftforge.common.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main
{
    public static final String MODID = "fastxpcollection";
    public static final String VERSION = "1.0";
    
    public static SimpleNetworkWrapper network;
    
    public static boolean fastXpPickup;
    public static boolean opDirt;
    public static boolean silkSpawners;
    
    @EventHandler
	public void preInit(FMLPreInitializationEvent event)  
    {
        MinecraftForge.EVENT_BUS.register(new Event());
    }
	
    public void initConfiguration(FMLInitializationEvent event)
    {
    	Configuration config = new Configuration(new File("config/" + MODID + ".cfg"));
    	config.load();
    	
    	fastXpPickup = config.getBoolean("Fast XP Collection", "World", true, "Causes XP orbs to be instantly absorbed by players, to pick up XP at vanilla speed set to false.");
    	silkSpawners = config.getBoolean("Silk Spawners", "World", true, "Allows you to harvest mob spawners using a diamond silk touch pickaxe.");
    	opDirt = config.getBoolean("OP Dirt", "World", false, "You probably don't want this enabled.");
    	
    	config.save();
    	
    	System.out.println("Configuration file loaded.");
    }
    
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		initConfiguration(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)  
	{
		
	}
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    //    // some example code
    //    System.out.println("DIRT BLOCK >> "+Blocks.DIRT.getUnlocalizedName());
    }
}
