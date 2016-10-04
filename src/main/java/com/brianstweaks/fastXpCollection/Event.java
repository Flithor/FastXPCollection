package com.brianstweaks.fastXpCollection;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public class Event 
{
	@SubscribeEvent
	public void onPlayerPickupXpEvent(PlayerPickupXpEvent event)
	{
		if(Main.fastXpPickup)
		{
			EntityPlayer player = event.getEntityPlayer();
			if(player != null)
			{
				if(!player.worldObj.isRemote)
				{
					AxisAlignedBB aabb = player.getEntityBoundingBox();
					java.util.List<EntityXPOrb> orbs = player.worldObj.getEntitiesWithinAABB(EntityXPOrb.class, aabb);
					if (orbs != null)
				    {
						int xpTotal = 0;
			            for (int i = 0; i < orbs.size(); ++i)
			            {
			                EntityXPOrb orb = orbs.get(i);
			                if (orb != null && !orb.isDead)
			                {
			                    xpTotal += orb.getXpValue();
			                    orb.setDead();
			                }
			            }
			            player.addExperience(xpTotal);
			        }
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onHarvestDropsEvent(HarvestDropsEvent event)
	{
		if(Main.opDirt)
		{
			EntityPlayer player = event.getHarvester();
			if(player != null && !player.worldObj.isRemote)
			{
				for(int i=0; i<event.getDrops().size(); i++)
				{
					ItemStack drop = event.getDrops().get(i);
					if(drop.getDisplayName().compareTo("Dirt") == 0)
					{
						for(int z=0; z<100; z++)
							player.worldObj.spawnEntityInWorld(new EntityXPOrb(player.worldObj, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 1600));
					}
				}
			}
		}
	}
	
}
