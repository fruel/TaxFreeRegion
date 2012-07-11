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
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;

import at.lukasf.taxfreeregion.util.Command;

public class Region implements Comparable<Region>{
	
	private String name;	   
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private int z1;
	private int z2;
	private String world;
	private DenyMode deny_dispenser=DenyMode.NONE;
	private DenyMode deny_chest = DenyMode.NONE;
	private DenyMode deny_minecart = DenyMode.NONE;
	private DenyMode border_piston = DenyMode.NONE;
	private DenyMode border_block_drop = DenyMode.NONE;
	private DenyMode item_drops = DenyMode.NONE;
	private DenyMode death_drops = DenyMode.NONE;
	private DenyMode eye_of_ender = DenyMode.NONE;
	private List<InventoryMode> enterI, exitI;
	public final boolean isWorldGuard;
	private List<String> cmdBlacklist, cmdWhitelist;
	private boolean crossPlace;
	private List<InventoryMode> xp, health, hunger, xp_exit, health_exit, hunger_exit;	
	private List<Command> cmdEnter, cmdExit;
	private HashMap<String, Boolean> permissions;
	private String enter="", exit="";
	
	public static Region createDummy(int x1, int x2, int y1, int y2, int z1, int z2, String world)
	{
		return new Region("",world,x1, x2, y1, y2, z1, z2, false);
	}
	
	public Region(String name, String world, int x1, int x2, int y1, int y2, int z1, int z2)
	{
		this(name,world,x1,x2,y1,y2,z1,z2,false);
	}
	public Region(String name, String world, int x1, int x2, int y1, int y2, int z1, int z2, boolean wg)
	{
		isWorldGuard = wg;
		this.world=world;
		this.name = name;
		this.x1=x1;
		this.x2=x2;
		this.y1=y1;
		this.y2=y2;
		this.z1=z1;
		this.z2=z2;
		cmdBlacklist = new ArrayList<String>();
		cmdWhitelist = new ArrayList<String>();
		cmdEnter = new ArrayList<Command>();
		cmdExit = new ArrayList<Command>();
		permissions = new HashMap<String, Boolean>();
	}
	
	public void setInventoryModes(List<InventoryMode> enter, List<InventoryMode> exit)
	{
		enterI=enter;
		exitI=exit;
	}
	
	public void setDeny(DenyMode deny_dispenser, DenyMode deny_chest, DenyMode deny_minecart, DenyMode border_piston, DenyMode border_block_drop, DenyMode item_drops, DenyMode death_drops, DenyMode eye_of_ender)
	{
		this.item_drops = item_drops;
		this.eye_of_ender = eye_of_ender;
		this.death_drops = death_drops;
		this.border_block_drop=border_block_drop;
		this.border_piston=border_piston;
		this.deny_chest = deny_chest;
		this.deny_dispenser = deny_dispenser;
		this.deny_minecart=deny_minecart;
	}
	public void setMessages(String enter, String exit)
	{
		this.enter=enter;
		this.exit=exit;
	}
	public String getWorld()
	{
		return world;
	}
	public DenyMode getDispenserDenyMode()
	{
		return deny_dispenser;
	}
	public DenyMode getChestDenyMode()
	{
		return deny_chest;
	}
	public DenyMode getStorageMinecartDenyMode()
	{
		return deny_minecart;
	}
	public String getName()
    {
     return this.name;
    }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public int getX1() {
     return this.x1;
   }
 
   public void setX1(int x1) {
     this.x1 = x1;
   }
 
   public int getX2()
   {
     return this.x2;
   }
 
   public void setX2(int x2) {
     this.x2 = x2;
   }
 
   public int getY1()
   {
     return this.y1;
   }
 
   public void setY1(int y1) {
     this.y1 = y1;
   }
 
   public int getY2()
   {
     return this.y2;
   }
 
   public void setY2(int y2) {
     this.y2 = y2;
   }
 
   public int getZ1()
   {
     return this.z1;
   }
 
   public void setZ1(int z1) {
     this.z1 = z1;
   }
 
   public int getZ2()
   {
     return this.z2;
   }
 
   public void setZ2(int z2) {
     this.z2 = z2;
   }
 
   public String toString()
   {
     return String.format("[%s] (%d, %d, %d) (%d, %d, %d)", new Object[] { this.name, Integer.valueOf(this.x1), Integer.valueOf(this.y1), Integer.valueOf(this.z1), Integer.valueOf(this.x2), Integer.valueOf(this.y2), Integer.valueOf(this.z2) });
   }
 
   public boolean contains(Location loc)
   {
     int x = loc.getBlockX();
     int y = loc.getBlockY();
     int z = loc.getBlockZ();
 
     return contains(x,y,z) && loc.getWorld().getName().equals(world);
   }
 
   public boolean contains(int x, int y, int z) {
     return (x >= this.x2) && (x <= this.x1) && (z >= this.z2) && (z <= this.z1) && (y >= this.y2) && (y <= this.y1);
   }
 
   public int compareTo(Region o)
   {
     if (o.x1 > this.x1)
       return 1;
     if (o.x1 == this.x1)
     {
       if (o.z1 > this.z1)
         return 1;
       if (o.z1 == this.z1)
       {
         if (o.y1 > this.y1)
           return 1;
         if (o.y1 == this.y1) {
           return 0;
         }
       }
     }
 
     return -1;
   }
	public List<String> getCommandBlacklist() {
		return cmdBlacklist;
	}
	public void setCommandBlacklist(List<String> cmdBlacklist) {
		this.cmdBlacklist = cmdBlacklist;
	}
	public List<String> getCommandWhitelist() {
		return cmdWhitelist;
	}
	public void setCommandWhitelist(List<String> cmdWhitelist) {
		this.cmdWhitelist = cmdWhitelist;
	}
	public List<Command> getCommandsEnter() {
		return cmdEnter;
	}
	public void setCommandsEnter(List<Command> cmdEnter) {
		this.cmdEnter = cmdEnter;
	}
	public List<Command> getCommandsExit() {
		return cmdExit;
	}
	public void setCommandsExit(List<Command> cmdExit) {
		this.cmdExit = cmdExit;
	}
	public HashMap<String, Boolean> getPermissions() {
		return permissions;
	}
	public void setPermissions(HashMap<String, Boolean> permissions) {
		this.permissions = permissions;
	}
	public boolean isCommandBlackListed(String command)
   {
     for(String cmd : cmdBlacklist)
     {
    	 if(command.startsWith(cmd)) return true;
     }
     return false;
   }
   public boolean isCommandWhiteListed(String command)
   {
     Iterator<String> it = this.cmdWhitelist.iterator();
 
     while (it.hasNext()) {
       String itEquals = (String)it.next();
       String itWithParams = itEquals + " ";
       if ((command.startsWith(itWithParams)) || (command.equals(itEquals))) {
         return true;
       }
     }
     return false;
   }
	
	public DenyMode getPistonDenyMode() {
		return border_piston;
	}
	public DenyMode getBlockDropDenyMode() {
		return border_block_drop;
	}
	public DenyMode getItemDropsDenyMode() {
		return item_drops;
	}
	public DenyMode getDeathDropsDenyMode() {
		return death_drops;
	}
	public DenyMode getEyeOfEnderDenyMode() {
		return eye_of_ender;
	}
	public String getEnterMessage() {
		return enter;
	}
	public String getExitMessage() {
		return exit;
	}
	
	public List<InventoryMode> getInventoryModesEnter() {
		return enterI;
	}
	public List<InventoryMode> getInventoryModesExit() {
		return exitI;
	}

	public List<InventoryMode> getXpMode() {
		return xp;
	}

	public void setXpMode(List<InventoryMode> xp) {
		this.xp = xp;
	}

	public List<InventoryMode> getHealthMode() {
		return health;
	}

	public void setHealthMode(List<InventoryMode> health) {
		this.health = health;
	}

	public List<InventoryMode> getHungerMode() {
		return hunger;
	}

	public void setHungerMode(List<InventoryMode> hunger) {
		this.hunger = hunger;
	}

	public boolean isCrossPlacingDenyed() {
		return !crossPlace;
	}

	public void setCrossPlacing(boolean crossPlace) {
		this.crossPlace = crossPlace;
	}
	
	public List<InventoryMode> getXpExitMode() {
		return xp_exit;
	}

	public void setXpExitMode(List<InventoryMode> xp_exit) {
		this.xp_exit = xp_exit;
	}

	public List<InventoryMode> getHealthExitMode() {
		return health_exit;
	}

	public void setHealthExitMode(List<InventoryMode> health_exit) {
		this.health_exit = health_exit;
	}

	public List<InventoryMode> getHungerExitModet() {
		return hunger_exit;
	}

	public void setHungerExitMode(List<InventoryMode> hunger_exit) {
		this.hunger_exit = hunger_exit;
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

	public static enum DenyMode { NONE, FULL, BORDER};
    public static enum InventoryMode {CLEAR, STORE, RESTORE, REWARD};
}
