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
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.util.Vector;

import at.lukasf.taxfreeregion.region.Region;
import at.lukasf.taxfreeregion.region.Region.DenyMode;
import at.lukasf.taxfreeregion.region.RegionManager;

public class PlayerListener implements Listener
{
	private static final HashMap<Integer, Integer> entityItemMapping;
	
	static {
		entityItemMapping = new HashMap<Integer, Integer>();
		entityItemMapping.put(9, 321); //painting
		entityItemMapping.put(10, 262); //arrow
		entityItemMapping.put(11, 332); //snowball
		entityItemMapping.put(13, 385); //small-fireball
		entityItemMapping.put(14, 368); //ender pearl
		entityItemMapping.put(15, 381); //ender signal
		entityItemMapping.put(17, 384); //xp bottle
		entityItemMapping.put(18, 389); //item frame
		entityItemMapping.put(20, 46); //tnt
		entityItemMapping.put(22, 401); //firework
		entityItemMapping.put(41, 333); //boat
		entityItemMapping.put(42, 328); //minecart
		entityItemMapping.put(43, 342); //minecart chest
		entityItemMapping.put(44, 343); //minecart furnance
		entityItemMapping.put(45, 407); //minecart tnt
		entityItemMapping.put(46, 408); //minecart hopper
	}	
	
	private RegionManager regionManager;
	private TaxFreeRegion plugin;
	
	//block players from doing anything after respawn until they move 
	//ensures that all region restrictions are applied correctly
	private HashSet<String> lockedPlayers = new HashSet<String>();

	public PlayerListener(TaxFreeRegion plugin)
	{
		this.plugin = plugin;
		this.regionManager = plugin.getRegionManager();
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
				DenyMode mode;
				if(!event.getMaterial().isBlock() && (mode = r.getUsageDenyMode(event.getMaterial().getId())) != DenyMode.NONE){
					if(mode == DenyMode.FULL || (mode == DenyMode.BORDER && isInBorder(event.getPlayer().getLocation(), r, 20))) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
						return;
					}
				}				
			}
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				DenyMode mode;
				if((mode = r.getUsageDenyMode(event.getClickedBlock().getType().getId())) != DenyMode.NONE){
					if(mode == DenyMode.FULL || (mode == DenyMode.BORDER && isInBorder(event.getPlayer().getLocation(), r, 10))) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
						return;
					}
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
		
		if (event.isCancelled() || event.getPlayer().hasPermission("taxfreeregion.passthrough")) return;

		if (regionManager.isPlayerInsideRegion(event.getPlayer()))
		{
			int entitiyId = event.getRightClicked().getType().getTypeId();
			
			if(entityItemMapping.containsKey(entitiyId)) {
				Region r = regionManager.getRegionForPlayer(event.getPlayer());
				int item = entityItemMapping.get(entitiyId);
			
				DenyMode mode;
				if((mode = r.getUsageDenyMode(item)) != DenyMode.NONE){
					if(mode == DenyMode.FULL || (mode == DenyMode.BORDER && isInBorder(event.getPlayer().getLocation(), r, 5))) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
						return;
					}
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
			TaxFreeRegion.log.severe("[TaxFreeRegion] onPlayerMove Exception: " + ex.getMessage());	
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
				player.sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}

			for(Region reg : regionManager.getRegions().values()){
				if(reg.equals(r)) continue;
				if (reg.isCommandWhiteListed(msg)) {
					player.sendMessage(plugin.getMessages().getMessage("whitelisted"));
					event.setCancelled(true);
					break;
				}
			}

		}     
		else
		{
			for(Region reg : regionManager.getRegions().values()){
				if (reg.isCommandWhiteListed(msg)) {
					player.sendMessage(plugin.getMessages().getMessage("whitelisted"));
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
			if(r.isItemDropsDenied() == DenyMode.FULL || (r.isItemDropsDenied() == DenyMode.BORDER && isInBorder(event.getPlayer().getLocation(), r,7)))
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
			TaxFreeRegion.log.severe("[TaxFreeRegion] onPlayerQuit Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(final EntityDeathEvent event) {

		if(event instanceof PlayerDeathEvent)
		{
			Player p = (Player)((PlayerDeathEvent)event).getEntity();
			
			if (p.hasPermission("taxfreeregion.passthrough")) return;
			
			if(regionManager.isPlayerInsideRegion(p)){	
				Region r = regionManager.getRegionForPlayer(p);				
				if(r.isDeathDropsDenied() == DenyMode.FULL || (r.isDeathDropsDenied() == DenyMode.BORDER && isInBorder(p.getLocation(), r,10)))
					event.getDrops().clear();
			}
		}
		else {
			Region r = regionManager.getRegionForLocation(event.getEntity().getLocation());
			if(r != null){
				if(r.isItemDropsDenied() == DenyMode.FULL || (r.isItemDropsDenied() == DenyMode.BORDER && isInBorder(event.getEntity().getLocation(), r,10)))
					event.getDrops().clear();
			}
		}
	}  
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleDamage(final VehicleDamageEvent event)
	{
		if (event.isCancelled()) return;
		
		if(event.getAttacker() instanceof Player){
			Player p = (Player) event.getAttacker();
			
			if (p.hasPermission("taxfreeregion.passthrough")) return;

			int entitiyId = event.getVehicle().getType().getTypeId();
			
			if(entityItemMapping.containsKey(entitiyId)) {
				Region r = regionManager.getRegionForPlayer(p);
				int item = entityItemMapping.get(entitiyId);
			
				DenyMode mode;
				if((mode = r.getRemoveDenyMode(item)) != DenyMode.NONE){
					if(mode == DenyMode.FULL || (mode == DenyMode.BORDER && isInBorder(p.getLocation(), r, 5))) {
						event.setCancelled(true);
						p.sendMessage(plugin.getMessages().getMessage("blacklisted"));
						return;
					}
				}
			}			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		if (event.isCancelled()) return;

		Region r = regionManager.getRegionForLocation(event.getBlock().getLocation());

		if(r != null){			
			if(r.getUsageDenyMode(33) == DenyMode.BORDER || (event.isSticky() && r.getUsageDenyMode(29) == DenyMode.BORDER)){

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
			else if(r.getUsageDenyMode(33) == DenyMode.FULL || (event.isSticky() && r.getUsageDenyMode(29) == DenyMode.FULL)) {
				event.setCancelled(true);
			}
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

		if(r != null)
		{
			if((!regionManager.isPlayerInsideRegion(event.getPlayer()) && r.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			Region other = regionManager.getRegionForPlayer(event.getPlayer());
			if (regionManager.isPlayerInsideRegion(event.getPlayer()) && !other.equals(r) && (r.isCrossPlacingDenyed() || other.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			
			int id = event.getBlock().getType().getId();
			if(r.getRemoveDenyMode(id) == DenyMode.FULL || (r.getRemoveDenyMode(id) == DenyMode.BORDER && isInBorder(event.getBlock().getLocation(), r, 5))) {
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			
			if(r.isBlockDropsDenied() == DenyMode.FULL || (r.isBlockDropsDenied() == DenyMode.BORDER && isInBorder(event.getBlock().getLocation(), r, 5))) {
				event.getBlock().setTypeId(0);
			}
		}
		else if(regionManager.isPlayerInsideRegion(event.getPlayer()) && regionManager.getRegionForPlayer(event.getPlayer()).isCrossPlacingDenyed())
		{
			event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
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
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			Region other = regionManager.getRegionForPlayer(event.getPlayer());
			if (regionManager.isPlayerInsideRegion(event.getPlayer()) && !other.equals(r) && (r.isCrossPlacingDenyed() || other.isCrossPlacingDenyed()))
			{
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
			
			int id = event.getBlock().getType().getId();
			if(r.getPlaceDenyMode(id) == DenyMode.FULL || (r.getPlaceDenyMode(id) == DenyMode.BORDER && isInBorder(event.getBlock().getLocation(), r, 5))) {
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
		}
		else if(regionManager.isPlayerInsideRegion(event.getPlayer()) && regionManager.getRegionForPlayer(event.getPlayer()).isCrossPlacingDenyed())
		{
			event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
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
				if((!r.isWorld && !r.contains(event.getTo()) || (r.isWorld && !r.getWorld().equals(event.getTo().getWorld().getName()))))
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
			TaxFreeRegion.log.severe("[TaxFreeRegion] onPlayerTeleport Exception: " + ex.getMessage());
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
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
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
				event.getPlayer().sendMessage(plugin.getMessages().getMessage("blacklisted"));
				event.setCancelled(true);
				return;
			}
		}			
		
	}

	private boolean isInBorder(Location pos, Region r, int border)
	{
		return r.contains(pos) && !pos.toVector().isInAABB(r.getLowerEdge().add(new Vector(border, border, border)), r.getUpperEdge().subtract(new Vector(border, border, border)));
	}
}
