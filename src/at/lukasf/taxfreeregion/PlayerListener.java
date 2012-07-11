/*
 * TaxFreeRegion
 * Copyright (C) 2012 lukasf, adreide, tickleman and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package at.lukasf.taxfreeregion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import at.lukasf.taxfreeregion.region.Region;
import at.lukasf.taxfreeregion.region.Region.DenyMode;
import at.lukasf.taxfreeregion.region.RegionManager;


public class PlayerListener implements Listener
{
	private RegionManager regionManager;
	private HashSet<String> lockedPlayers = new HashSet<String>();

	public PlayerListener(TaxFreeRegion plugin)
	{
		regionManager = plugin.getRegionManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(final PlayerInteractEvent event)
	{
		if(lockedPlayers.contains(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			return;
		}
		if (event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		if (regionManager.isPlayerInsideRegion(event.getPlayer()))
		{
			Region r = regionManager.getRegionForPlayer(event.getPlayer());
			
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)  || event.getAction().equals(Action.RIGHT_CLICK_AIR))
			{
				if(event.getMaterial() == Material.EYE_OF_ENDER && checkDenyMode(r.getEyeOfEnderDenyMode(), event.getPlayer().getLocation(), r,18))
				{
					event.setCancelled(true);
					return;
				}
			}
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				Block block = event.getClickedBlock();
				if((block.getType().equals(Material.CHEST) || block.getType().equals(Material.FURNACE)) && checkDenyMode(r.getChestDenyMode(), block.getLocation(), r,5))
				{					
					event.setCancelled(true);
					event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));	
					return;
				}
				if(block.getType().equals(Material.DISPENSER) && checkDenyMode(r.getDispenserDenyMode(),block.getLocation(), r,7))
				{					
					event.setCancelled(true);
					event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
					return;
				}
			}
		}
	}
		
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event)
	{
		if (event.isCancelled()|| event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		if(lockedPlayers.contains(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		if(lockedPlayers.contains(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			return;
		}
		
		if (event.isCancelled()|| event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		if (regionManager.isPlayerInsideRegion(event.getPlayer()))
		{
			Region r = regionManager.getRegionForPlayer(event.getPlayer());
			if(checkDenyMode(r.getStorageMinecartDenyMode(), event.getRightClicked().getLocation(), r,5)) {		  
				Entity entity = event.getRightClicked();

				if ((entity.getClass().getName().contains("StorageMinecart")))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(final PlayerMoveEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) {
			return;
		}
		try{
			regionManager.regionCheck(event.getPlayer());
			if(lockedPlayers.contains(event.getPlayer().getName()))
				lockedPlayers.remove(event.getPlayer().getName());
		}
		catch(Exception ex)
		{
			TaxFreeRegion.log.log(Level.SEVERE, "[TaxFreeRegion] onPlayerMove Exception: " + ex.getMessage());
			try{
				event.getPlayer().sendMessage("TaxFreeRegion Error: " + ex.getMessage());}catch(Exception e){}
			ex.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		if (event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
			
		regionManager.reconnectPlayer(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) {
			return;
		}

		Player player = event.getPlayer();
		String msg = event.getMessage();

		if (regionManager.isPlayerInsideRegion(event.getPlayer())) {
			Region r = regionManager.getRegionForPlayer(event.getPlayer());

			if(r.isCommandWhiteListed(msg)) return;

			if (r.isCommandBlackListed(msg)) {
				player.sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}

			for(Region reg : regionManager.getRegions().values()){
				if(reg.equals(r)) continue;
				if (reg.isCommandWhiteListed(msg)) {
					player.sendMessage(TaxFreeRegion.messages.getMessage("whitelisted"));
					event.setCancelled(true);
					break;
				}
			}

		}     
		else
		{
			for(Region reg : regionManager.getRegions().values()){
				if (reg.isCommandWhiteListed(msg)) {
					player.sendMessage(TaxFreeRegion.messages.getMessage("whitelisted"));
					event.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(final PlayerDropItemEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		if (regionManager.isPlayerInsideRegion(event.getPlayer()))
		{
			Region r = regionManager.getRegionForPlayer(event.getPlayer());
			if(checkDenyMode(r.getItemDropsDenyMode(), event.getPlayer().getLocation(), r,7))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit (final PlayerQuitEvent event)
	{
		if (event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		try{
			if (regionManager.isPlayerInsideRegion(event.getPlayer()))
				regionManager.disconnectPlayer(event.getPlayer());
		}
		catch(Exception ex)
		{
			TaxFreeRegion.log.log(Level.SEVERE, "[TaxFreeRegion] onPlayerQuit Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(final EntityDeathEvent event) {

		if(event instanceof PlayerDeathEvent)
		{
			PlayerDeathEvent ev = (PlayerDeathEvent)event;
			
			if (((Player)ev.getEntity()).hasPermission("taxfreeregion.passthrough")) return;
			
			if(regionManager.isPlayerInsideRegion((Player)ev.getEntity())){	
				Region r = regionManager.getRegionForPlayer((Player)event.getEntity());
				if(checkDenyMode(r.getDeathDropsDenyMode(), event.getEntity().getLocation(), r,10))
					ev.getDrops().clear();
			}
		}
	}  
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		if (event.isCancelled()) return;

		Region r = regionManager.getRegionForLocation(event.getBlock().getLocation());

		if(r!=null){
			if(r.getPistonDenyMode()==DenyMode.BORDER){

				ArrayList<Block> blocks = new ArrayList<Block>();
				blocks.addAll(event.getBlocks());
				if(event.getBlocks().size() > 0)
					blocks.add(event.getBlocks().get(event.getBlocks().size()-1).getRelative(event.getDirection()));

				for(Block b:blocks)
				{
					if(!r.contains(b.getLocation())){ 
						event.setCancelled(true);
						break;
					}
				}
			}
			else if(r.getPistonDenyMode() == DenyMode.FULL) event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		if(lockedPlayers.contains(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			return;
		}
		
		Region r = regionManager.getRegionForLocation(event.getBlock().getLocation());

		if(r!=null)
		{
			if((!regionManager.isPlayerInsideRegion(event.getPlayer()) && r.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			Region other = regionManager.getRegionForPlayer(event.getPlayer());
			if (regionManager.isPlayerInsideRegion(event.getPlayer()) && !other.equals(r) && (r.isCrossPlacingDenyed() || other.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			if(checkDenyMode(r.getBlockDropDenyMode(), event.getBlock().getLocation(), r,5))
				event.getBlock().setTypeId(0);
		}
		else if(regionManager.isPlayerInsideRegion(event.getPlayer()) && regionManager.getRegionForPlayer(event.getPlayer()).isCrossPlacingDenyed())
		{
			event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
			event.setCancelled(true);
			return;
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		Region r = regionManager.getRegionForLocation(event.getBlock().getLocation());

		if(r!=null)
		{
			if((!regionManager.isPlayerInsideRegion(event.getPlayer()) && r.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			Region other = regionManager.getRegionForPlayer(event.getPlayer());
			if (regionManager.isPlayerInsideRegion(event.getPlayer()) && !other.equals(r) && (r.isCrossPlacingDenyed() || other.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
		}
		else if(regionManager.isPlayerInsideRegion(event.getPlayer()) && regionManager.getRegionForPlayer(event.getPlayer()).isCrossPlacingDenyed())
		{
			event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		lockedPlayers.add(event.getPlayer().getName());
	}
	 

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		try{
			if (regionManager.isPlayerInsideRegion(event.getPlayer()))
			{
				Region r = regionManager.getRegionForPlayer(event.getPlayer());
				if(!r.contains(event.getTo()))
					regionManager.exitRegion(event.getPlayer());
			}
			Region reg = regionManager.getRegionForLocation(event.getTo());
			if(reg!=null)
			{
				Region current = regionManager.getRegionForPlayer(event.getPlayer());
				if(!reg.equals(current))
				{
					if(current!=null) regionManager.exitRegion(event.getPlayer());
					regionManager.enterRegion(reg, event.getPlayer());
				}		
			}
		}
		catch(Exception ex)
		{
			TaxFreeRegion.log.log(Level.SEVERE, "[TaxFreeRegion] onPlayerTeleport Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		Region src = regionManager.getRegionForPlayer(event.getPlayer());
		Region target = regionManager.getRegionForLocation(event.getBlockClicked().getLocation());
		
		if(src!=target)
		{
			if((src != null && src.isCrossPlacingDenyed()) || (target != null && target.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
		}			
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;
		
		Region src = regionManager.getRegionForPlayer(event.getPlayer());
		Region target = regionManager.getRegionForLocation(event.getBlockClicked().getLocation());
		
		if(src!=target)
		{
			if((src != null && src.isCrossPlacingDenyed()) || (target != null && target.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(TaxFreeRegion.messages.getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
		}			
		
	}

	private boolean checkDenyMode(DenyMode d, Location pos, Region r, int border)
	{
		switch(d)
		{
		case NONE: return false;
		case FULL: return true;
		case BORDER:
			Region reg = Region.createDummy(r.getX1()-border, r.getX2()+border, r.getY1()-border, r.getY2()+border, r.getZ1()-border, r.getZ2()+border, r.getWorld());
			//TaxFreeRegion.log.info("BORDER CHECK: " + r.contains(pos) + " " + reg.contains(pos) +  " " + pos + " *  " + r + " * " + reg);
			return r.contains(pos) && !reg.contains(pos);
		}
		return true;
	}
}
