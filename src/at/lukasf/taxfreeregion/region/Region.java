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
package at.lukasf.taxfreeregion.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import at.lukasf.taxfreeregion.util.Command;

public class Region {

	public static enum DenyMode { NONE, FULL, BORDER };
	public static enum InventoryMode { CLEAR, STORE, RESTORE, REWARD };

	public final boolean isWorldGuard, isWorld;

	private BlockVector start, end;

	private String name, world;
	private String enterMessage, exitMessage;

	private boolean crossPlace;
	private DenyMode denyBlockDrops, denyItemDrops, denyDeathDrops;

	private HashMap<Integer, DenyMode> deniedUse, deniedPlace, deniedRemove;
	private HashMap<String, Boolean> permissions;

	private List<InventoryMode> inventory, xp, health, hunger;
	private List<InventoryMode> inventory_exit, xp_exit, health_exit, hunger_exit;

	private List<Command> cmdEnter, cmdExit;
	private List<String> cmdBlacklist, cmdWhitelist;

	public static Region createDummy(BlockVector point1, BlockVector point2, String world) {
		return new Region("", world, point1, point2, false);
	}

	public Region(String name, String world, BlockVector point1, BlockVector point2) {
		this(name, world, point1, point2, false);
	}

	public Region(String name, String world, BlockVector point1, BlockVector point2, boolean wg) {
		this(name, world, point1, point2, wg, false);
	}

	public Region(String name, String world, BlockVector point1, BlockVector point2, boolean wg, boolean isworld) {
		this.isWorldGuard = wg;
		this.isWorld = isworld;
		this.world = world;
		this.name = name;

		crossPlace = false;
		denyBlockDrops = DenyMode.BORDER;
		denyItemDrops = DenyMode.FULL;
		denyDeathDrops = DenyMode.FULL;
		
		enterMessage = "";
		exitMessage = "";

		start = BlockVector.getMinimum(point1, point2).toBlockVector();
		end = BlockVector.getMaximum(point1, point2).toBlockVector();
		
		cmdBlacklist = new ArrayList<String>();
		cmdWhitelist = new ArrayList<String>();
		cmdEnter = new ArrayList<Command>();
		cmdExit = new ArrayList<Command>();
		permissions = new HashMap<String, Boolean>();
		deniedUse = new HashMap<Integer, DenyMode>();
		deniedPlace = new HashMap<Integer, DenyMode>();
		deniedRemove = new HashMap<Integer, DenyMode>();

		inventory = new ArrayList<InventoryMode>();
		xp = new ArrayList<InventoryMode>();
		health = new ArrayList<InventoryMode>();
		hunger = new ArrayList<InventoryMode>();

		inventory_exit = new ArrayList<InventoryMode>();
		xp_exit = new ArrayList<InventoryMode>();
		health_exit = new ArrayList<InventoryMode>();
		hunger_exit = new ArrayList<InventoryMode>();
	}
	
	public boolean contains(Location loc) {
		return contains(loc.toVector()) && loc.getWorld().getName().equals(world);
	}

	public boolean contains(Vector v) {
		return v.isInAABB(start, end);
	}
	
	public boolean contains(int x, int y, int z) {
		return new Vector(x, y, z).isInAABB(start, end);
	}

	public boolean isCommandBlackListed(String command) {
		for (String cmd : cmdBlacklist) {
			if (command.startsWith(cmd + " ") || command.equals(cmd))
				return true;
		}
		return false;
	}

	public boolean isCommandWhiteListed(String command) {
		for (String cmd : cmdWhitelist) {
			if (command.startsWith(cmd + " ") || command.equals(cmd))
				return true;
		}
		return false;
	}	
	
	/* SETTER */
	
	public void setDenyBlockDrops(DenyMode denyBlockDrops) {
		this.denyBlockDrops = denyBlockDrops;
	}

	public void setDenyItemDrops(DenyMode denyItemDrops) {
		this.denyItemDrops = denyItemDrops;
	}	

	public void setDenyDeathDrops(DenyMode denyDeathDrops) {
		this.denyDeathDrops = denyDeathDrops;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEnterMessage(String enter) {
		enterMessage = enter;
	}

	public void setExitMessage(String exit) {
		exitMessage = exit;
	}
	
	public void setCrossPlacing(boolean crossPlace) {
		this.crossPlace = crossPlace;
	}
	
	/* ADD-Methods */
	
	public void addPermission(String permission, boolean value) {
		permissions.put(permission, value);
	}
	
	public void addBlacklistedCommand(String cmd) {
		cmdBlacklist.add(cmd);
	}
	
	public void addWhitelistedCommand(String cmd) {
		cmdWhitelist.add(cmd);
	}

	public void addEnterInventoryMode(InventoryMode mode) {
		if (!inventory.contains(mode)) {
			inventory.add(mode);
		}
	}

	public void addExitInventoryMode(InventoryMode mode) {
		if (!inventory_exit.contains(mode)) {
			inventory_exit.add(mode);
		}
	}

	public void addUsageDeniedItem(int blockid, DenyMode mode) {
		deniedUse.put(blockid, mode);
	}
	
	public void addPlaceDeniedItem(int blockid, DenyMode mode) {
		deniedPlace.put(blockid, mode);
	}
	
	public void addRemoveDeniedItem(int blockid, DenyMode mode) {
		deniedRemove.put(blockid, mode);
	}
	
	public void addEnterCommand(Command cmd) {
		cmdEnter.add(cmd);
	}
	
	public void addExitCommand(Command cmd) {
		cmdExit.add(cmd);
	}
	
	public void addEnterXpMode(InventoryMode mode) {
		xp.add(mode);
	}
	
	public void addEnterHealthMode(InventoryMode mode) {
		health.add(mode);
	}
	
	public void addEnterHungerMode(InventoryMode mode) {
		hunger.add(mode);
	}	
	
	public void addExitXpMode(InventoryMode mode) {
		xp_exit.add(mode);
	}
	
	public void addExitHealthMode(InventoryMode mode) {
		health_exit.add(mode);
	}
	
	public void addExitHungerMode(InventoryMode mode) {
		hunger_exit.add(mode);
	}
	
	/* GETTER */
	
	public boolean isCrossPlacingDenyed() {
		return !crossPlace;
	}
	
	public DenyMode isBlockDropsDenied() {
		return denyBlockDrops;
	}
	
	public DenyMode isItemDropsDenied() {
		return denyItemDrops;
	}
	
	public DenyMode isDeathDropsDenied() {
		return denyDeathDrops;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getWorld() {
		return world;
	}

	public DenyMode getUsageDenyMode(int blockid) {
		if (deniedUse.containsKey(blockid)) {
			return deniedUse.get(blockid);
		}

		return DenyMode.NONE;
	}
	
	public DenyMode getPlaceDenyMode(int blockid) {
		if (deniedPlace.containsKey(blockid)) {
			return deniedPlace.get(blockid);
		}

		return DenyMode.NONE;
	}
	
	public DenyMode getRemoveDenyMode(int blockid) {
		if (deniedRemove.containsKey(blockid)) {
			return deniedRemove.get(blockid);
		}

		return DenyMode.NONE;
	}

	public BlockVector getLowerEdge() {
		return start.clone();
	}

	public BlockVector getUpperEdge() {
		return end.clone();
	}
	
	public List<Command> getEnterCommands() {
		return cmdEnter;
	}
	
	public List<Command> getExitCommands() {
		return cmdEnter;
	}

	public HashMap<String, Boolean> getPermissions() {
		return permissions;
	}
	
	public String getEnterMessage() {
		return enterMessage;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public boolean hasInventoryEnterMode(InventoryMode i) {
		return inventory.contains(i);
	}

	public boolean hasInventoryExitMode(InventoryMode i) {
		return inventory_exit.contains(i);
	}

	public boolean hasXpEnterMode(InventoryMode i) {
		return xp.contains(i);
	}
	
	public boolean hasHealthEnterMode(InventoryMode i) {
		return health.contains(i);
	}
	
	public boolean hasHungerEnterMode(InventoryMode i) {
		return hunger.contains(i);
	}
	
	public boolean hasXpExitMode(InventoryMode i) {
		return xp_exit.contains(i);
	}
	
	public boolean hasHealthExitMode(InventoryMode i) {
		return health_exit.contains(i);
	}
	
	public boolean hasHungerExitMode(InventoryMode i) {
		return hunger_exit.contains(i);
	}

	/* OVERRIDES */
	
	@Override
	public String toString() {
		return String.format("[%s] (%d, %d, %d) (%d, %d, %d)", name, start.getBlockX(), start.getBlockY(), start.getBlockZ(), end.getBlockX(), end.getBlockY(), end.getBlockZ());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Region))
			return false;
		Region other = (Region) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
