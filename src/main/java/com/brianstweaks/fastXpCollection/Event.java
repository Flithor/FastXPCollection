package com.brianstweaks.fastXpCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import scala.tools.nsc.doc.base.CommentFactoryBase.TagKey;
import scala.tools.nsc.typechecker.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.SpawnerEntityTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent.*;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Event 
{
	public boolean hasSilkTouchEnchant(ItemStack item) {
		NBTTagList enchants = item.getEnchantmentTagList();
		for(int i=0; i<enchants.tagCount(); i++) {
			if(enchants.getStringTagAt(i).contains("id:33")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canHarvestSpawner(ItemStack item) {
		if(item.getUnlocalizedName().equals("item.pickaxeDiamond") && item.isItemEnchanted()) {
			if(hasSilkTouchEnchant(item)) {
				return true;
			}
		}
		return false;
	}
	
	@SubscribeEvent
	public void onPlayerPickupXpEvent(PlayerPickupXpEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		
		if(event.getResult() == Result.DENY) return;
		if(player == null) return;
		if(player.world.isRemote) return;
		
		if(Main.fastXpPickup) {
			AxisAlignedBB aabb = player.getEntityBoundingBox();
			java.util.List<EntityXPOrb> orbs = player.world.getEntitiesWithinAABB(EntityXPOrb.class, aabb);
			if (orbs != null) {
				int xpTotal = 0;
	            for (int i = 0; i < orbs.size(); ++i) {
	                EntityXPOrb orb = orbs.get(i);
	                if (orb != null && !orb.isDead) {
	                    xpTotal += orb.getXpValue();
	                    orb.setDead();
	                }
	            }
	            player.addExperience(xpTotal);
	        }
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onBlockPlace(PlaceEvent event) {
		EntityPlayer player = event.getPlayer();
		
		if(event.getResult() == Result.DENY) return;
		if(player == null) return;
		if(player.world.isRemote) return;
		
		//player.sendMessage(new TextComponentString(player.inventory.getCurrentItem().getDisplayName()));
		if(event.getPlacedBlock().getBlock().getUnlocalizedName().equals("tile.mobSpawner")) {
			if(player.inventory.getCurrentItem().getTagCompound().hasKey("mobSpawnID")) {
				TileEntity entity = player.world.getTileEntity(event.getPos());
				TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)entity;
				
				player.world.setBlockState(event.getPos(), event.getPlacedBlock().getBlock().getDefaultState());
				mobSpawner.getSpawnerBaseLogic().setEntityId(new ResourceLocation(player.inventory.getCurrentItem().getTagCompound().getString("mobSpawnID")));
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tooltipEvent(ItemTooltipEvent event) {
		
		EntityPlayer player = (EntityPlayer)event.getEntityPlayer();
		
		if(event.getResult() == Result.DENY) return;
		if(player == null) return;
		if(!player.world.isRemote) return;
		
		if(event.getItemStack().getUnlocalizedName().equals("tile.mobSpawner") && event.getItemStack().hasTagCompound()) {
			if( event.getItemStack().getTagCompound().hasKey("mobSpawnID")) {
				event.getToolTip().add("");
				event.getToolTip().add("Spawner Type:");
				event.getToolTip().add(event.getItemStack().getTagCompound().getString("mobSpawnID"));
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		EntityPlayer player = event.getPlayer();
		
		if(event.getResult() == Result.DENY) return;
		if(player == null) return;
		if(player.world.isRemote) return;
		
		if(Main.silkSpawners) {
			Block block = event.getState().getBlock();
			if(block.getUnlocalizedName().equals("tile.mobSpawner")) {
				ItemStack item = player.inventory.getCurrentItem();
				if(canHarvestSpawner(item)) {
					TileEntity entity = player.world.getTileEntity(event.getPos());
					TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)entity;
					
					NBTTagCompound tag = mobSpawner.getUpdateTag();
					
					ItemStack newBlock = new ItemStack(block, 1);
					newBlock.setTagInfo("mobSpawnID", new NBTTagString(tag.getCompoundTag("SpawnData").getString("id")));
					
					player.world.spawnEntity(new EntityItem(player.world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), newBlock));
				
					event.setExpToDrop(0);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onHarvestDropsEvent(HarvestDropsEvent event) {
		EntityPlayer player = event.getHarvester();
		
		if(event.getResult() == Result.DENY) return;
		if(player == null) return;
		if(player.world.isRemote) return;
		
		if(Main.opDirt) {
			for(int i=0; i<event.getDrops().size(); i++) {
				ItemStack drop = event.getDrops().get(i);
				if(drop.getDisplayName().compareTo("Dirt") == 0) {
					for(int z=0; z<100; z++)
						player.world.spawnEntity(new EntityXPOrb(player.world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 1600));
				}
			}
		}
		
		if(Main.silkSpawners) {
			Block block = event.getState().getBlock();
			if(block.getUnlocalizedName().equals("tile.mobSpawner")) {
				ItemStack item = player.inventory.getCurrentItem();
				if(canHarvestSpawner(item)) {
					event.getDrops().clear();
				}
			}
		}
	}
}
