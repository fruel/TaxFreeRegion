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
 
import at.lukasf.taxfreeregion.inventory.RewardManager;
import at.lukasf.taxfreeregion.region.OfflineRegion;
import at.lukasf.taxfreeregion.region.Region;
import at.lukasf.taxfreeregion.region.RegionManager;
import at.lukasf.taxfreeregion.util.Messages;
import at.lukasf.taxfreeregion.util.SerializedHashMaps;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
 
 public class TaxFreeRegion extends JavaPlugin
 {
   private PlayerListener playerListener;
   public static final Logger log = Logger.getLogger("Minecraft");
   public static Messages messages = new Messages();
   private boolean eventRegistered = false;
   
   private WorldEditPlugin worldEdit;
   private WorldGuardPlugin worldGuard;
      
   private RegionManager region;
   private File inventoryFile, directory, offlineregions;
   
   private RewardManager rewards;
   
   public void onEnable()
   {			  
	  if (!this.isWorldEditSet()) {
	       Plugin worldEdit = this.getServer().getPluginManager().getPlugin("WorldEdit");
	       if ((worldEdit != null) && 
	         (worldEdit.isEnabled())) {
	         this.setWorldEdit((WorldEditPlugin)worldEdit);
	         TaxFreeRegion.log.log(Level.INFO, "[TaxFreeRegion] Successfully linked with WorldEdit.");
	       }
	     }
	     
	     if (!this.isWorldGuardSet()) {
	         Plugin worldGuard = this.getServer().getPluginManager().getPlugin("WorldGuard");
	         if ((worldGuard != null) && 
	           (worldGuard.isEnabled())) {
	           this.setWorldGuard((WorldGuardPlugin)worldGuard);
	           TaxFreeRegion.log.log(Level.INFO, "[TaxFreeRegion] Successfully linked with WorldGuard.");
	         }
	     }
	  
     try
     {
       directory = new File("./plugins/TaxFreeRegion");
       if (!directory.exists()) {
         directory.mkdirs();
       } 
       
       this.inventoryFile = new File(directory, "inventories.ser");
       if ((!this.inventoryFile.exists()) || (!this.inventoryFile.isFile())) {
         this.inventoryFile.createNewFile();
         log.info("[TaxFreeRegion] Created file inventories.ser");
       }        
       
       this.offlineregions = new File(directory, "offlineregion.ser");
       if ((!this.offlineregions.exists()) || (!this.offlineregions.isFile())) {
         this.offlineregions.createNewFile();
         log.info("[TaxFreeRegion] Created file offlineregion.ser");
       } 
       
       File msgFile = new File(directory, "messages.properties");
       if ((!msgFile.exists()) || (!msgFile.isFile())) {
         msgFile.createNewFile();
         writeDefaultMessages();
       } 
       messages.loadMessages(msgFile);
       
       region = new RegionManager(this);
       rewards = new RewardManager(this);
       playerListener = new PlayerListener(this);
     }
     catch (IOException ex) {
       log.log(Level.SEVERE, "[TaxFreeRegion] : Error on Region File");
       return;
     }
 
     if (!this.eventRegistered) {
       PluginManager pm = getServer().getPluginManager();
       pm.registerEvents(playerListener, this);
       
       this.eventRegistered = true;
     }
     
     loadInventories();
     loadOfflineRegions();
     
     log.log(Level.INFO, "[TaxFreeRegion] Version " + getDescription().getVersion() + " is enabled!");
   }
      
   private void writeDefaultMessages() {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("./plugins/TaxFreeRegion/messages.properties"));
			pw.println("noPermission:%red%You don't have the permissions");
			pw.println("noWorldEdit:%red%WorldEdit isn't loaded. Please use WorldEdit to make selections!");
			pw.println("noWorldGuard:%red%WorldGuard not found! You need WorldGuard to use this feature!");
			pw.println("regionOverwriting:%red%There is already a region with that name, overwriting...");
			pw.println("noSelection:%red%You must do a selection with WorldEdit to define a region.");
			pw.println("selectionIncomplete:%red%You have to have two points to have a valid selection!");
			pw.println("noRegion:%red%There is no such region.");
			pw.println("blacklisted:%red%You cannot use that command");
			pw.println("whitelisted:%red%You cannot use that command here");
			pw.println("regionAdded:%gold%Region added. Configure the region in regions.yml then run /tf reload.");
			pw.println("regionDeleted:%gold%Region deleted.");
			pw.println("regionNotFound:%gold%Couldn not find the specified WorldGuard region..");
			pw.println("reload:%gold%Regions reloaded.");
			pw.println("reset:%gold%TaxFreeRegion has been reset.");
			pw.println("blackListReloaded:%gold%Commands Blacklist reloaded.");
			pw.println("reward:%gold%Your reward is waiting for you.");
			pw.println("noReward:%gold%There is no reward for you.");
			pw.flush();
			pw.close();
			log.info("[TaxFreeRegion] Created file messages.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

   public void onDisable()
   {	
	 region.cleanShutdown();
	 
	 saveInventories();
	 saveOfflineRegions();
	 
     rewards.cleanShutdown();
     log.log(Level.INFO, "[TaxFreeRegion] is disabled!");
   }
 
   public WorldEditPlugin getWorldEdit() {
     return this.worldEdit;
   }
 
   public void setWorldEdit(WorldEditPlugin worldEdit) {
     this.worldEdit = worldEdit;
   }
 
   public boolean isWorldEditSet() {
     return this.worldEdit != null;
   }
   public WorldGuardPlugin getWorldGuard() {
     return this.worldGuard;
   }
 
   public void setWorldGuard(WorldGuardPlugin worldGuard) {
     this.worldGuard = worldGuard;
   }
 
   public boolean isWorldGuardSet() {
     return this.worldGuard != null;
   }
   
   private void loadInventories()
   {
     ObjectInputStream ois = null;
     try
     {
       ois = new ObjectInputStream(new FileInputStream(this.inventoryFile));
 
       SerializedHashMaps m = (SerializedHashMaps)ois.readObject();
       region.setInventories(m.inventories);
       region.setHealthValues(m.healthValues);
       region.setHungerValues(m.hungerValues);
       region.setXpValues(m.xpValues);
     }
     catch (EOFException ex) {
 
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex2) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     catch (Exception ex)
     {
       log.log(Level.WARNING, "[TaxFreeRegion] Inventories file error !");
       ex.printStackTrace();
 
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex3) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     finally
     {
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
   }
 
   private void saveInventories()
   {
     ObjectOutputStream oos = null;
     try {
       oos = new ObjectOutputStream(new FileOutputStream(inventoryFile));
 
       oos.writeObject(new SerializedHashMaps(region.getInventories(), region.getHealthValues(), region.getXpValues(), region.getHungerValues()));
     } catch (Exception ex) {
       log.log(Level.SEVERE, "[TaxFreeRegion] Inventory file not saved !");
 
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex2) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     finally
     {
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
   }   
  
   @SuppressWarnings("unchecked")
   private void loadOfflineRegions()
   {
     ObjectInputStream ois = null;
     try
     {
       ois = new ObjectInputStream(new FileInputStream(offlineregions));
 
       region.setOfflinePlayers(((HashMap<String, OfflineRegion>)ois.readObject()));
     }
     catch (EOFException ex) {
       
    	if (ois != null)
         try {
           ois.close();
         } catch (IOException ex2) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     catch (Exception ex)
     {
       log.log(Level.WARNING, "[TaxFreeRegion] Inventories file error !");
       ex.printStackTrace();       
 
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex3) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     finally
     {
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
   }
 
   private void saveOfflineRegions()
   {
     ObjectOutputStream oos = null;
     try {
       oos = new ObjectOutputStream(new FileOutputStream(offlineregions));
 
       oos.writeObject(region.getOfflinePlayers());
     } catch (Exception ex) {
       log.log(Level.SEVERE, "[TaxFreeRegion] Inventory file not saved !");
 
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex2) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
     finally
     {
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex) {
           log.log(Level.WARNING, "[TaxFreeRegion] Inventory file error on close !");
         }
     }
   }   
   
   public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
   {
	   if (args.length < 1) {
		   return false;
	   }

	   Player player=null;

	   if (sender instanceof Player)
	   {
		   player = (Player)sender;		   
	   }

	   boolean allowed = (player != null && player.hasPermission("taxfreeregion.use"))|| sender instanceof ConsoleCommandSender;

	  if (args.length == 1 && args[0].equalsIgnoreCase("clear"))
	   {
		  if(player==null)
		   {
			   sender.sendMessage("You are not a player!");
			   return true;
		   }
		   if (player.hasPermission("taxfreeregion.clear") && region.isPlayerInsideRegion(player))
		   {
			   player.getInventory().clear();
		   }
		   else
		   {
			   player.sendMessage(messages.getMessage("noPermission"));
		   }
	   }
	   else if(args.length == 1 && args[0].equalsIgnoreCase("reward"))
	   {
		   if(player==null)
		   {
			   sender.sendMessage("You are not a player!");
			   return true;
		   }
		   rewards.giveMe(player);
	   }
	   else if ((args.length >= 2) && (args[0].equalsIgnoreCase("add")))
	   {
		   if(player==null)
		   {
			   sender.sendMessage("You are not a player!");
			   return true;
		   }
		   if(player.hasPermission("taxfreeregion.use"))
		   {
			   StringBuilder sb = new StringBuilder(args[1]);
			   for (int i = 2; i < args.length; i++) {
				   sb.append(" ");
				   sb.append(args[i]);
			   }
			   String regionName = sb.toString();

			   if (this.worldEdit == null) {
				   player.sendMessage(messages.getMessage("noWorldEdit"));
				   return true;
			   }

			   if (deleteRegionByName(regionName)) {
				   player.sendMessage(messages.getMessage("regionOverwriting"));
			   }

			   Selection sel = this.worldEdit.getSelection(player);

			   if (sel == null) {
				   player.sendMessage(messages.getMessage("noSelection"));
				   return true;
			   }

			   Location max = sel.getMaximumPoint();
			   Location min = sel.getMinimumPoint();

			   if ((max == null) || (min == null)) {
				   player.sendMessage(messages.getMessage("selectionIncomplete"));
				   return true;
			   }	 

			   region.addRegion(regionName, sel.getWorld().getName(), max.getBlockX(), min.getBlockX(),  max.getBlockY(), min.getBlockY(), max.getBlockZ(), min.getBlockZ());

			   player.sendMessage(messages.getMessage("regionAdded"));
		   }
		   else {
			   player.sendMessage(messages.getMessage("noPermission"));
		   }
	   }   
	   else if ((args.length == 2) && (args[0].equalsIgnoreCase("wgadd")))
	   {
		   if(player==null)
		   {
			   sender.sendMessage("You are not a player!");
			   return true;
		   }
		   if(player.hasPermission("taxfreeregion.use"))
		   {
			   String regionName = args[1];
			   if (this.worldGuard == null) {
				   player.sendMessage(messages.getMessage("noWorldGuard"));
				   return true;
			   }
			   if (deleteRegionByName(regionName)) {
				   player.sendMessage(messages.getMessage("regionOverwriting"));
			   }
			   com.sk89q.worldguard.protection.managers.RegionManager rm = this.worldGuard.getRegionManager(player.getWorld());
			   if(!rm.hasRegion(regionName))
			   {
				   player.sendMessage(messages.getMessage("regionNotFound"));
				   return true;
			   }
			   region.addRegion(regionName, player.getWorld().getName());
			   player.sendMessage(messages.getMessage("regionAdded"));
		   }
		   else {
			   player.sendMessage(messages.getMessage("noPermission"));
		   }
	   }
	   else if ((args.length == 1) && (args[0].equalsIgnoreCase("list")))
	   {
		   if(allowed)
		   {
			   HashMap<String, Region> regions = region.getRegions();
			   if (regions.isEmpty())
			   {
				   sender.sendMessage(messages.getMessage("noRegion"));
				   return true;
			   }

			   for(String s:regions.keySet())
			   {
				   Region r = regions.get(s);
				   sender.sendMessage(ChatColor.GOLD + s + ChatColor.GREEN+" ["+r.getX2() + ", " + r.getY2() + ", "+r.getZ2() + "]["+r.getX1() + ", " + r.getY1() + ", "+r.getZ1() + "] "+ChatColor.DARK_GREEN+"["+regions.get(s).getWorld() + "] "+(regions.get(s).isWorldGuard?ChatColor.RED+"[WorldGuard]":""));
			   }
		   }
		   else {
			   sender.sendMessage(messages.getMessage("noPermission"));
		   }
	   }
	   else if ((args.length > 1) && (args[0].equalsIgnoreCase("delete")))
	   {
		   if(allowed)
		   {
			   StringBuilder sb = new StringBuilder(args[1]);
			   for (int i = 2; i < args.length; i++) 
			   {
				   sb.append(" ");
				   sb.append(args[i]);
			   }
			   String regionName = sb.toString();

			   if (!deleteRegionByName(regionName)) 
			   {
				   sender.sendMessage(messages.getMessage("noRegion"));
				   return true;
			   }

			   sender.sendMessage(messages.getMessage("regionDeleted"));	 
		   }
		   else {
			   sender.sendMessage(messages.getMessage("noPermission"));
		   }	     
	   } 
	   else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {

		   if(allowed)
		   {
			   region.loadConfig();	 		   
			   sender.sendMessage(messages.getMessage("reload"));
		   }
		   else {
			   sender.sendMessage(messages.getMessage("noPermission"));
		   }	
	   }
	   else if ((args.length == 1) && (args[0].equalsIgnoreCase("reset"))) {

		   if(allowed)
		   {
			   inventoryFile.delete();
			   offlineregions.delete();
			   region.reset();
			   region.loadConfig();	
			   rewards.clear();
			   rewards.clean();
			   sender.sendMessage(messages.getMessage("reset"));
		   }
		   else {
			   sender.sendMessage(messages.getMessage("noPermission"));
		   }	
	   } 
	   else{
		   return false;
	   }  
	   
	   return true;
   }    
 
   private boolean deleteRegionByName(String name) {
    Region r = region.getRegionByName(name);
    if(r!=null)
    {
    	region.deleteRegion(r);
    	return true;
    }
     return false;
   }
    
   
   public RegionManager getRegionManager()
   {
	   return region;
   }
   
   public RewardManager getRewardManager()
   {
	   return rewards;
   }
   
   public File getConfigDirectory()
   {
	   return directory;
   }
         
 }
