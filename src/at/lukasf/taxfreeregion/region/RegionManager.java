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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import at.lukasf.taxfreeregion.TaxFreeRegion;
import at.lukasf.taxfreeregion.inventory.InventoryManager;
import at.lukasf.taxfreeregion.inventory.SavedInventory;
import at.lukasf.taxfreeregion.region.Region.DenyMode;
import at.lukasf.taxfreeregion.region.Region.InventoryMode;
import at.lukasf.taxfreeregion.util.Command;
import at.lukasf.taxfreeregion.util.ConfigUpdater;
import at.lukasf.taxfreeregion.util.IntFloat;
import at.lukasf.taxfreeregion.util.Messages;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class RegionManager {

	private Logger log = TaxFreeRegion.log;
	private YAMLProcessor yaml;
	private File conf;
	private TaxFreeRegion plugin;
	private HashMap<String, Region> regions =  new HashMap<String, Region>();

	private HashMap<PlayerRegion, SavedInventory> inventories = new HashMap<PlayerRegion, SavedInventory>();   
	private HashMap<String, String> playerregion = new HashMap<String, String>();  
	private HashMap<String, ArrayList<PermissionAttachment>> perms = new HashMap<String, ArrayList<PermissionAttachment>>();
	private HashMap<String, OfflineRegion> offline = new HashMap<String, OfflineRegion>();
	
	private HashMap<PlayerRegion, Double> healthValues = new HashMap<PlayerRegion, Double>();
	private HashMap<PlayerRegion, IntFloat> xpValues = new HashMap<PlayerRegion, IntFloat>();
	private HashMap<PlayerRegion, IntFloat> hungerValues = new HashMap<PlayerRegion, IntFloat>();

	public RegionManager(TaxFreeRegion plugin)
	{
		this.plugin=plugin;
		conf=new File(plugin.getConfigDirectory(), "regions.yml");
		filecheck();
		if(ConfigUpdater.updateNecessary())
		{
			log.info("[TaxFreeRegion] Detected old configuration file. Upgrading...");
			ConfigUpdater.updateConfiguration();
		}
		yaml = new 	YAMLProcessor(conf, false, YAMLFormat.EXTENDED);
		loadConfig();
	}
	
	/*  CONFIGURATION MANAGEMENT */
	
	private void filecheck()
	{
		if ((!conf.exists()) || (!conf.isFile())) {
			try {
				conf.createNewFile();
				PrintWriter pw = new PrintWriter(new FileOutputStream(conf));
				pw.print("{}");
				pw.flush();
				pw.close();
				log.info("[TaxFreeRegion] Created file regions.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	public void saveConfig()
	{
		yaml.save();
	}
	public void loadConfig()
	{
		try {
			yaml.load();
			regions = new HashMap<String, Region>();
			List<String> n= yaml.getKeys("regions");
			if(n==null) return;
			for(String s:n)
			{
				boolean wg, wo=false;
				int x1=0,x2=0,y1=0,y2=0,z1=0,z2=0;
				String reg = yaml.getString("regions."+s+".region").trim();					
				if((wg=reg.startsWith("wg|")) && plugin.isWorldGuardSet()){
					ProtectedRegion r =plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(yaml.getString("regions."+s+".world"))).getRegion(reg.substring(reg.indexOf('|')+1));
					if(r!=null){
						x1 = r.getMaximumPoint().getBlockX();
						y1 = r.getMaximumPoint().getBlockY();
						z1 = r.getMaximumPoint().getBlockZ();

						x2 = r.getMinimumPoint().getBlockX();
						y2 = r.getMinimumPoint().getBlockY();
						z2 = r.getMinimumPoint().getBlockZ();}
					else throw new Exception("Could not find the given WorldGuard region! ("+reg.substring(reg.indexOf('|')+1)+")");
				}
				else if(reg.equals("|none|"))
				{
					x1=x2=y1=y2=z1=z2=-999;
					wo=true;
				}
				else
				{
					String[] split = reg.split("\\|");
					if(split.length==2)
					{
						String[] min = split[0].split(",");
						String[] max = split[1].split(",");
						if(min.length==3 && max.length==3)
						{
							x1 = Integer.parseInt(max[0]);
							y1 = Integer.parseInt(max[1]);
							z1 = Integer.parseInt(max[2]);

							x2 = Integer.parseInt(min[0]);
							y2 = Integer.parseInt(min[1]);
							z2 = Integer.parseInt(min[2]);
						}
					}
				}			

				Region r = new Region(s, yaml.getString("regions."+s+".world"),x1,x2,y1,y2,z1,z2,wg,wo);

				r.setCrossPlacing(yaml.getBoolean("regions."+s+".cross_placing"));

				r.setDeny(
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.dispenser").trim().toUpperCase()), 
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.chest").trim().toUpperCase()), 
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.minecart_storage").trim().toUpperCase()),
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.piston").trim().toUpperCase()), 
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.block_drops").trim().toUpperCase()),
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.item_drops").trim().toUpperCase()),
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.death_drops").trim().toUpperCase()),
						DenyMode.valueOf(yaml.getString("regions."+s+".deny.eye_of_ender").trim().toUpperCase()));

				List<String> inv_enter = yaml.getStringList("regions."+s+".enter.inventory", null);
				List<String> inv_exit = yaml.getStringList("regions."+s+".exit.inventory", null);
				List<String> xp_enter = yaml.getStringList("regions."+s+".enter.xp", null);
				List<String> xp_exit = yaml.getStringList("regions."+s+".exit.xp", null);
				List<String> health_enter = yaml.getStringList("regions."+s+".enter.health", null);
				List<String> health_exit = yaml.getStringList("regions."+s+".exit.health", null);
				List<String> hunger_enter = yaml.getStringList("regions."+s+".enter.hunger", null);
				List<String> hunger_exit = yaml.getStringList("regions."+s+".exit.hunger", null);
				
				
				List<InventoryMode> inv_enterI = new ArrayList<InventoryMode>();
				List<InventoryMode> inv_exitI = new ArrayList<InventoryMode>();
				List<InventoryMode> xp_enterI = new ArrayList<InventoryMode>();
				List<InventoryMode> xp_exitI = new ArrayList<InventoryMode>();
				List<InventoryMode> health_enterI = new ArrayList<InventoryMode>();
				List<InventoryMode> health_exitI =new ArrayList<InventoryMode>();
				List<InventoryMode> hunger_enterI = new ArrayList<InventoryMode>();
				List<InventoryMode> hunger_exitI = new ArrayList<InventoryMode>();
				
				try{
					if(inv_enter!=null)
						for(String str: inv_enter) 
							inv_enterI.add(InventoryMode.valueOf(str.toUpperCase()));
					if(inv_exit!=null)
						for(String str: inv_exit)
							inv_exitI.add(InventoryMode.valueOf(str.toUpperCase()));
					
					if(xp_enter!=null)
						for(String str: xp_enter) 
							xp_enterI.add(InventoryMode.valueOf(str.toUpperCase()));
					if(xp_exit!=null)
						for(String str: xp_exit)
							xp_exitI.add(InventoryMode.valueOf(str.toUpperCase()));
					
					if(health_enter!=null)
						for(String str: health_enter) 
							health_enterI.add(InventoryMode.valueOf(str.toUpperCase()));
					if(health_exit!=null)
						for(String str: health_exit)
							health_exitI.add(InventoryMode.valueOf(str.toUpperCase()));
					
					if(hunger_enter!=null)
						for(String str: hunger_enter) 
							hunger_enterI.add(InventoryMode.valueOf(str.toUpperCase()));
					if(hunger_exit!=null)
						for(String str: hunger_exit)
							hunger_exitI.add(InventoryMode.valueOf(str.toUpperCase()));
				}
				catch(Exception ex)
				{
					throw new Exception("Inventory mode format error!");
				}
				r.setInventoryModes(inv_enterI, inv_exitI);
				r.setHungerMode(hunger_enterI);
				r.setHungerExitMode(hunger_exitI);
				r.setHealthMode(health_enterI);
				r.setHealthExitMode(health_exitI);
				r.setXpMode(xp_enterI);
				r.setXpExitMode(xp_exitI);

				r.setMessages(yaml.getString("regions."+s+".messages.enter"), yaml.getString("regions."+s+".messages.exit"));

				List<String> cmd = yaml.getStringList("regions."+s+".cmdBlacklist", null);				
				if(cmd!=null)
				{
					List<String> cmd2 = new ArrayList<String>();
					for(String str: cmd) {
						if(!str.trim().isEmpty()) cmd2.add("/"+str);					
					}
					r.setCommandBlacklist(cmd2);
				}

				cmd = yaml.getStringList("regions."+s+".cmdWhitelist", null);
				if(cmd!=null) 
				{
					List<String> cmd2 = new ArrayList<String>();
					for(String str: cmd) {
						if(!str.trim().isEmpty()) cmd2.add("/"+str);					
					}
					r.setCommandWhitelist(cmd2);
				}

				cmd = yaml.getStringList("regions."+s+".cmdEnter", null);
				if(cmd!=null)
				{
					List<Command> l = new ArrayList<Command>();
					for(String c:cmd)
					{
						if(c.startsWith("d"))
						{
							int delay = Integer.parseInt(c.substring(1, c.indexOf(':')));
							String cc = c.substring(c.indexOf(':')+1);
							l.add(new Command(cc.substring(2), cc.charAt(0)=='c'?true:false,delay));
						}
						else
							l.add(new Command(c.substring(2), c.charAt(0)=='c'?true:false));
					}
					r.setCommandsEnter(l);
				}

				cmd = yaml.getStringList("regions."+s+".cmdExit", null);
				if(cmd!=null)
				{
					List<Command> l = new ArrayList<Command>();
					for(String c:cmd)
					{						
						if(c.startsWith("d"))
						{
							int delay = Integer.parseInt(c.substring(1, c.indexOf(':')));
							String cc = c.substring(c.indexOf(':')+1);
							l.add(new Command(cc.substring(2), cc.charAt(0)=='c'?true:false,delay));
						}
						else
							l.add(new Command(c.substring(2), c.charAt(0)=='c'?true:false));
					}
					r.setCommandsExit(l);
				}

				cmd=yaml.getStringList("regions."+s+".permissions",null);				
				if(cmd!=null)
				{
					HashMap<String, Boolean> perms = new HashMap<String, Boolean>();
					for(String c:cmd)
					{
						c = c.replace('{',' ').replace('}',' ').trim();
						String[] split = c.split("=");						
						if(split.length==2)
							perms.put(split[0], Boolean.parseBoolean(split[1].trim()));
					}
					r.setPermissions(perms);
				}

				regions.put(s, r);
			}


		} catch (Exception e) {
			log.log(Level.SEVERE, "[TaxFreeRegion] : Error loading config file. " + e.getMessage() );
			e.printStackTrace();
		}
	}

	/*  REGION MANAGEMENT */

	public void deleteRegion(Region r)
	{
		regions.remove(r.getName());
		yaml.removeProperty("regions."+r.getName());
		saveConfig();
	}
	public void addRegion(String name, String world, int x1, int x2, int y1, int y2, int z1, int z2)
	{		
		addRegion(name, world, x2+","+y2+","+z2+"|"+x1+","+y1+","+z1);													
	}
	
	public void addRegion(String name, World world)
	{		
		addRegion(name, world.getName(), "|none|");				
	}

	public void addRegionWG(String name, String world, String wg_name)
	{		
		addRegion(name, world, "wg|"+wg_name);				
	}

	private void addRegion(String name, String world, String region)
	{
		YAMLNode n =yaml.addNode("regions."+name);	
		n.setProperty("region", region);
		n.setProperty("world", world);
		n.setProperty("cross_placing", false);
		YAMLNode deny =yaml.addNode("regions."+name+".deny");		
		deny.setProperty("dispenser", "full");
		deny.setProperty("chest", "full");
		deny.setProperty("minecart_storage", "full");
		deny.setProperty("piston", "border");
		deny.setProperty("block_drops", "border");
		deny.setProperty("item_drops", "full");
		deny.setProperty("death_drops", "full");
		deny.setProperty("eye_of_ender", "full");
		List<String> l = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		l.add("store");
		l.add("clear");
		l2.add("restore");
		yaml.setProperty("regions."+name+".enter.inventory", l);
		yaml.setProperty("regions."+name+".exit.inventory", l2);
		yaml.setProperty("regions."+name+".enter.xp", null);
		yaml.setProperty("regions."+name+".exit.xp", null);
		yaml.setProperty("regions."+name+".enter.health", null);
		yaml.setProperty("regions."+name+".exit.health", null);
		yaml.setProperty("regions."+name+".enter.hunger", null);
		yaml.setProperty("regions."+name+".exit.hunger", null);
		YAMLNode messages =yaml.addNode("regions."+name+".messages");		
		messages.setProperty("enter", "%gold%Welcome to %green%"+name+"%gold%.");
		messages.setProperty("exit", "%gold%Bye bye");
		n.setProperty("cmdBlacklist", "");
		n.setProperty("cmdWhitelist", "");
		n.setProperty("cmdEnter", "");
		n.setProperty("cmdExit", "");
		n.setProperty("permissions", "");
		saveConfig();
	}

	/*  REGION CHECK LOGIC */
	
	public void regionCheck(Player player)
	{
		List<Region> list = getRegionsForWorld(player.getLocation().getWorld());

		Iterator<Region> it = list.iterator();

		while (it.hasNext()) {
			Region region = (Region)it.next();

			if (!region.isWorld && region.contains(player.getLocation())) {
				if (!this.isPlayerInsideRegion(player)) {
					enterRegion(region,player);
				} 
				else if(isPlayerInsideRegion(player) && getRegionForPlayer(player) != region)
				{
					exitRegion(player);
					enterRegion(region,player);
				}
				return;
			}
			else if(region.isWorld && !this.isPlayerInsideRegion(player) && region.getWorld().equals(player.getWorld().getName()))
			{
				enterRegion(region,player);
			}

		}

		if(isPlayerInsideRegion(player))
		{
			Region r = getRegionForPlayer(player);
			if(!r.isWorld || (r.isWorld && !player.getWorld().getName().equals(r.getWorld())))
				exitRegion(player);       
		}

	}	

	private void applyPermissions(Region r, Player p)
	{
		if(!perms.containsKey(p.getName()))
			perms.put(p.getName(), new ArrayList<PermissionAttachment>());	   

		for(String perm : r.getPermissions().keySet())
		{
			PermissionAttachment patt = p.addAttachment(plugin, perm, r.getPermissions().get(perm));
			perms.get(p.getName()).add(patt);  		   
		}

		p.recalculatePermissions();
	}


	private void revertPermissions(Player p)
	{
		try{
			for(PermissionAttachment patt : perms.get(p.getName()))
				p.removeAttachment(patt);
		}
		catch(Exception ex)
		{
			log.fine("[TaxFreeRegion] (DEBUG) Exception while reverting permissions.");
		}
		perms.remove(p.getName());
		p.recalculatePermissions();
	}

	private void runCommands(final Player p, List<Command> cmds)
	{
		for(Command c : cmds)
		{

			if(c.getDelay()>0)
			{
				final Command comm = c;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

					@Override
					public void run() {
						String cmd = comm.getCommand().replace("%name%", p.getName());
						Bukkit.getServer().dispatchCommand(comm.isConsoleSender()?Bukkit.getServer().getConsoleSender():p, cmd);

					}}, c.getDelay()*20);
			}
			else
			{
				String cmd = c.getCommand().replace("%name%", p.getName());
				Bukkit.getServer().dispatchCommand(c.isConsoleSender()?Bukkit.getServer().getConsoleSender():p, cmd);
			}
		}
	}


	@SuppressWarnings("deprecation")
	public void restoreInventory(Player player)
	{
		if ((player.hasPermission("taxfreeregion.noclear")))
		{
			return;
		}

		SavedInventory inventory = null;
		String playerName = player.getName();

		inventory = this.inventories.get(playerName);
		if (inventory != null) {
			InventoryManager.setInventoryContent(inventory, player.getInventory());
			this.inventories.remove(playerName);

			player.updateInventory();
		}
	}


	@SuppressWarnings("deprecation")
	public void enterRegion(Region r, Player p)
	{	   
		if(!r.isWorld)
			plugin.getRewardManager().clear(p);
		if(r.getInventoryModesEnter().contains(InventoryMode.STORE))
			inventories.put(new PlayerRegion(p.getName(), null), InventoryManager.getInventoryContent(p.getInventory()));
		else inventories.put(new PlayerRegion(p.getName(), null), InventoryManager.createDummyInventory());

		if(r.getInventoryModesEnter().contains(InventoryMode.REWARD))
			plugin.getRewardManager().queueReward(p);

		if(r.getInventoryModesEnter().contains(InventoryMode.RESTORE))
		{
			InventoryManager.clearInventory(p.getInventory());
			SavedInventory inv = getInventory(p,r);
			if(inv!=null) InventoryManager.setInventoryContent(inv, p.getInventory());
			inventories.remove(new PlayerRegion(p.getName(), r.getName()));
		}
		if(r.getInventoryModesEnter().contains(InventoryMode.CLEAR)) 
			InventoryManager.clearInventory(p.getInventory());

		//---------------------------------------- 
		
		if(r.getXpMode().contains(InventoryMode.STORE))
		{
			IntFloat f = new IntFloat(p.getLevel(), p.getExp());
			xpValues.put(new PlayerRegion(p.getName(),null), f);
		}
		if(r.getXpMode().contains(InventoryMode.RESTORE))
		{			
			IntFloat f = xpValues.remove(new PlayerRegion(p.getName(),r.getName()));
			if(f!=null){
				p.setLevel(f.getIntVal());
				p.setExp(f.getFloatVal());
			}			
		}
		if(r.getXpMode().contains(InventoryMode.CLEAR))
		{
			p.setLevel(0);
			p.setExp(0);
		}
		
		//----------------------------------------
		if(r.getHealthMode().contains(InventoryMode.STORE))
		{
			healthValues.put(new PlayerRegion(p.getName(),null), p.getHealth()==0?1:p.getHealth());
		}
		if(r.getHealthMode().contains(InventoryMode.RESTORE))
		{
			Double d = healthValues.remove(new PlayerRegion(p.getName(),r.getName()));
			if(d!=null){
				p.setHealth(d);				
			}			
		}
		if(r.getHealthMode().contains(InventoryMode.CLEAR))
		{
			p.setHealth(20);
		}
		
		//----------------------------------------
		if(r.getHungerMode().contains(InventoryMode.STORE))
		{
			IntFloat f = new IntFloat(p.getFoodLevel(), p.getSaturation());
			hungerValues.put(new PlayerRegion(p.getName(),null), f);
		}
		if(r.getHungerMode().contains(InventoryMode.RESTORE))
		{
			IntFloat f = hungerValues.remove(new PlayerRegion(p.getName(),r.getName()));
			if(f!=null){
				p.setFoodLevel(f.getIntVal());
				p.setSaturation(f.getFloatVal());
			}		
		}
		if(r.getHungerMode().contains(InventoryMode.CLEAR))
		{
			p.setFoodLevel(20);
			p.setSaturation(20);
		}
		
		//----------------------------------------
		
		p.updateInventory();

		applyPermissions(r,p);
		runCommands(p, r.getCommandsEnter());
		if(!r.getExitMessage().isEmpty())
			p.sendMessage(Messages.replaceColor(r.getEnterMessage()));
		playerregion.put(p.getName(), r.getName());
	}



	@SuppressWarnings("deprecation")
	public void exitRegion(Player p)
	{	   
		Region r = getRegionByName(playerregion.get(p.getName()));
		if(r!=null){
			plugin.getRewardManager().clear(p);
			if(r.getInventoryModesExit().contains(InventoryMode.STORE))
				this.inventories.put(new PlayerRegion(p.getName(), r.getName()), InventoryManager.getInventoryContent(p.getInventory()));

			if(r.getInventoryModesExit().contains(InventoryMode.REWARD))
				plugin.getRewardManager().queueReward(p);

			if(r.getInventoryModesExit().contains(InventoryMode.RESTORE))
			{
				InventoryManager.clearInventory(p.getInventory());
				SavedInventory inv = getInventory(p,null);

				if(inv!=null) InventoryManager.setInventoryContent(inv, p.getInventory());
				inventories.remove(new PlayerRegion(p.getName(), null));
			}
			if(r.getInventoryModesExit().contains(InventoryMode.CLEAR)) 
				InventoryManager.clearInventory(p.getInventory());

			//---------------------------------------- 
			
			if(r.getXpExitMode().contains(InventoryMode.STORE))
			{
				IntFloat f = new IntFloat(p.getLevel(), p.getExp());
				xpValues.put(new PlayerRegion(p.getName(),r.getName()), f);
			}
			if(r.getXpExitMode().contains(InventoryMode.RESTORE))
			{			
				IntFloat f = xpValues.remove(new PlayerRegion(p.getName(),null));
				if(f!=null){
					p.setLevel(f.getIntVal());
					p.setExp(f.getFloatVal());
				}			
			}
			if(r.getXpExitMode().contains(InventoryMode.CLEAR))
			{
				p.setLevel(0);
				p.setExp(0);
			}
			
			//----------------------------------------
			if(r.getHealthExitMode().contains(InventoryMode.STORE))
			{
				healthValues.put(new PlayerRegion(p.getName(),r.getName()), p.getHealth()==0?1:p.getHealth());
			}
			if(r.getHealthExitMode().contains(InventoryMode.RESTORE))
			{
				Double d = healthValues.remove(new PlayerRegion(p.getName(),null));
				if(d!=null){
					p.setHealth(d);						
				}			
			}
			if(r.getHealthExitMode().contains(InventoryMode.CLEAR))
			{
				p.setHealth(20);
			}
			
			//----------------------------------------
			if(r.getHungerExitModet().contains(InventoryMode.STORE))
			{
				IntFloat f = new IntFloat(p.getFoodLevel(), p.getSaturation());
				hungerValues.put(new PlayerRegion(p.getName(),r.getName()), f);
			}
			if(r.getHungerExitModet().contains(InventoryMode.RESTORE))
			{
				IntFloat f = hungerValues.remove(new PlayerRegion(p.getName(),null));
				if(f!=null){
					p.setFoodLevel(f.getIntVal());
					p.setSaturation(f.getFloatVal());
				}		
			}
			if(r.getHungerExitModet().contains(InventoryMode.CLEAR))
			{
				p.setFoodLevel(20);
				p.setSaturation(20);
			}
			
			//----------------------------------------

			p.updateInventory();

			if(!r.getExitMessage().isEmpty())
				p.sendMessage(Messages.replaceColor(r.getExitMessage()));
			runCommands(p, r.getCommandsExit());
			revertPermissions(p);
			playerregion.remove(p.getName());
		}
	}

	public void disconnectPlayer(Player p)
	{
		Region r = getRegionByName(playerregion.get(p.getName()));
		if(r!=null){
			SavedInventory current = InventoryManager.getInventoryContent(p.getInventory());
			InventoryManager.clearInventory(p.getInventory());

			offline.put(p.getName(), new OfflineRegion(r.getName(), current));

			SavedInventory def = inventories.get(new PlayerRegion(p.getName(), null));
			if(def!=null) InventoryManager.setInventoryContent(def, p.getInventory());
			revertPermissions(p);
			playerregion.remove(p.getName());
		}
	}

	public void reconnectPlayer(Player p)
	{
		if(offline.containsKey(p.getName()))
		{
			OfflineRegion or = offline.get(p.getName());

			Region r = getRegionByName(or.getRegion());

			if(r!=null){		   
				InventoryManager.clearInventory(p.getInventory());
				if(or.getInventory()!=null)
					InventoryManager.setInventoryContent(or.getInventory(), p.getInventory());

				applyPermissions(r,p);

				playerregion.put(p.getName(),r.getName());
				offline.remove(p.getName());
				log.info("[TaxFreeRegion] "+p.getName()+" joined into region " + r.getName());
			}
			else{
				log.severe("[TaxFreeRegion] A player wants to join a region which is not present in regions.yml. Probably he lost his inventory now.");
			}

		}
		else regionCheck(p);
	}

	/* CLEANUP CODE */
	
	public void cleanShutdown() {
		Set<String> ps = new HashSet<String>(playerregion.keySet());
		for(String player : ps)
		{
			Player p = Bukkit.getServer().getPlayer(player);
			disconnectPlayer(p);
		}
		saveConfig();
	}
	
	public void reset()
	{
		inventories.clear();
		playerregion.clear();
		perms.clear();
		offline.clear();
		xpValues.clear();
		healthValues.clear();
		hungerValues.clear();
	}
	
	/* GETTER/SETTER */
	
	public HashMap<PlayerRegion, SavedInventory> getInventories() {
		return inventories;
	}
	
	public void setInventories(HashMap<PlayerRegion, SavedInventory> inventories) {
		this.inventories = inventories;
	}
	
	public HashMap<String, OfflineRegion> getOfflinePlayers() {
		return offline;
	}
	
	public void setOfflinePlayers(HashMap<String, OfflineRegion> offline) {
		this.offline = offline;
	}
	
	public List<Region> getRegionsForWorld(World world)
	{
		ArrayList<Region> regs = new ArrayList<Region>();
		for(String s:regions.keySet())
		{
			if(regions.get(s).getWorld().equals(world.getName()))
				regs.add(regions.get(s));
		}
		return regs;
	}	

	public Region getRegionByName(String name)
	{
		return regions.get(name);
	}
	
	public HashMap<String, Region> getRegions()
	{
		return regions;
	}

	public boolean isPlayerInsideRegion(Player player) {
		return playerregion.containsKey(player.getName());
	}
	
	public Region getRegionForPlayer(Player p)
	{
		return getRegionByName(playerregion.get(p.getName()));
	}
	
	public SavedInventory getInventory(Player p, Region r)
	{
		return inventories.get(new PlayerRegion(p.getName(), r==null?null:r.getName()));
	}

	public Region getRegionForLocation(Location l)
	{
		for(Region reg:getRegions().values())
			if(reg.contains(l))
			{
				return reg;
			}
				
		return getRegionForWorld(l.getWorld());
	}
	
	public Region getRegionForWorld(World w)
	{
		for(Region reg:getRegions().values())
			if(reg.isWorld && reg.getWorld().equals(w.getName()))
			{
				return reg;
			}
		return null;
	}

	public HashMap<PlayerRegion, Double> getHealthValues() {
		return healthValues;
	}

	public HashMap<PlayerRegion, IntFloat> getXpValues() {
		return xpValues;
	}

	public HashMap<PlayerRegion, IntFloat> getHungerValues() {
		return hungerValues;
	}

	public void setHealthValues(HashMap<PlayerRegion, Double> healthValues) {
		this.healthValues = healthValues;
	}

	public void setXpValues(HashMap<PlayerRegion, IntFloat> xpValues) {
		this.xpValues = xpValues;
	}

	public void setHungerValues(HashMap<PlayerRegion, IntFloat> hungerValues) {
		this.hungerValues = hungerValues;
	}
}
