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
import at.lukasf.taxfreeregion.inventory.SavedInventory;
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
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
 
public class TaxFreeRegion extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final File baseDirectory = new File("./plugins/TaxFreeRegion");
		
	private PlayerListener playerListener;
	private boolean eventRegistered = false;

	private WorldEditPlugin worldEdit;
	private WorldGuardPlugin worldGuard;

	private RegionManager region;
	private RewardManager rewards;
	
	private Messages messages;
	
	private File inventoryFile, offlineregions, rewardFile;

	public void onEnable() {
		if (!this.isWorldEditSet()) {
			Plugin worldEdit = this.getServer().getPluginManager().getPlugin("WorldEdit");
			
			if (worldEdit != null && worldEdit.isEnabled()) {
				this.worldEdit = (WorldEditPlugin) worldEdit;
				log.info("[TaxFreeRegion] Successfully linked with WorldEdit.");
			}
		}

		if (!this.isWorldGuardSet()) {
			Plugin worldGuard = this.getServer().getPluginManager().getPlugin("WorldGuard");
			
			if (worldGuard != null && worldGuard.isEnabled()) {
				this.worldGuard = (WorldGuardPlugin) worldGuard;
				log.info("[TaxFreeRegion] Successfully linked with WorldGuard.");
			}
		}
		
		if (!baseDirectory.exists()) {
			baseDirectory.mkdirs();
		}
		
		messages = new Messages("messages.properties");

		region = new RegionManager(this);
		rewards = new RewardManager(this);

		try {
			this.inventoryFile = new File(baseDirectory, "inventories.ser");
			if (!this.inventoryFile.exists() || !this.inventoryFile.isFile()) {
				this.inventoryFile.createNewFile();
				log.info("[TaxFreeRegion] Created file inventories.ser");
			}

			this.offlineregions = new File(baseDirectory, "offlineregion.ser");
			if (!this.offlineregions.exists() || !this.offlineregions.isFile()) {
				this.offlineregions.createNewFile();
				log.info("[TaxFreeRegion] Created file offlineregion.ser");
			}
			
			this.rewardFile = new File(baseDirectory, "rewards.ser");
			if (!this.rewardFile.exists() || !this.rewardFile.isFile()) {
				this.rewardFile.createNewFile();
				log.info("[TaxFreeRegion] Created file rewards.ser");
			}
			
			loadInventories();
			loadOfflineRegions();
			loadRewards();
			
		} catch (IOException ex) {
			log.severe("[TaxFreeRegion] Error in data files.");
			return;
		}
		
		playerListener = new PlayerListener(this);

		if (!this.eventRegistered) {
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvents(playerListener, this);

			this.eventRegistered = true;
		}
		
		log.info("[TaxFreeRegion] Version " + getDescription().getVersion() + " is enabled!");
	}

	public void onDisable() {
		region.cleanShutdown();

		saveInventories();
		saveOfflineRegions();
		saveRewards();
		
		log.info("[TaxFreeRegion] is disabled!");
	}

	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
		}

		boolean allowed = (player != null && player.hasPermission("taxfreeregion.use"))	|| sender instanceof ConsoleCommandSender;

		if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
			if (player == null) {
				sender.sendMessage("You are not a player!");
				return true;
			}
			if (player.hasPermission("taxfreeregion.clear")	&& region.isPlayerInsideRegion(player)) {
				player.getInventory().clear();
			} else {
				player.sendMessage(messages.getMessage("noPermission"));
			}
		} 
		else if (args.length == 1 && args[0].equalsIgnoreCase("reward")) {
			if (player == null) {
				sender.sendMessage("You are not a player!");
				return true;
			}
			rewards.giveMe(player);
		} 
		else if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
			if (player == null) {
				sender.sendMessage("You are not a player!");
				return true;
			}
			if (player.hasPermission("taxfreeregion.use")) {
				if (args.length < 2 || args.length > 3) {
					player.sendMessage(messages.getMessage("error"));
					return true;
				}

				String name = args[1];

				if (deleteRegionByName(name)) {
					player.sendMessage(messages.getMessage("regionOverwriting"));
				}

				if (args.length == 2) {
					if (!createRegion(player, name)) {
						player.sendMessage(messages.getMessage("error"));
					}
					return true;
				}

				if (!createRegion(player, name, args[2])) {
					player.sendMessage(messages.getMessage("error"));
				}
				return true;
			} else {
				player.sendMessage(messages.getMessage("noPermission"));
			}
		} 
		else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			if (allowed) {
				HashMap<String, Region> regions = region.getRegions();
				if (regions.isEmpty()) {
					sender.sendMessage(messages.getMessage("noRegion"));
					return true;
				}

				for (String s : regions.keySet()) {
					Region r = regions.get(s);
					
					BlockVector min = r.getLowerEdge();
					BlockVector max = r.getUpperEdge();
					
					sender.sendMessage(ChatColor.GOLD + s + ChatColor.GREEN + " [" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + "][" 
									+ max.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + "] " + ChatColor.DARK_GREEN + "[" + regions.get(s).getWorld() + "] "
									+ (regions.get(s).isWorldGuard ? ChatColor.RED
											+ "[WorldGuard]" : ""));
				}
			} else {
				sender.sendMessage(messages.getMessage("noPermission"));
			}
		} 
		else if (args.length > 1 && args[0].equalsIgnoreCase("delete")) {
			if (allowed) {
				StringBuilder sb = new StringBuilder(args[1]);
				for (int i = 2; i < args.length; i++) {
					sb.append(" ");
					sb.append(args[i]);
				}
				String regionName = sb.toString();

				if (!deleteRegionByName(regionName)) {
					sender.sendMessage(messages.getMessage("noRegion"));
					return true;
				}

				sender.sendMessage(messages.getMessage("regionDeleted"));
			} else {
				sender.sendMessage(messages.getMessage("noPermission"));
			}
		} 
		else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

			if (allowed) {
				region.loadConfig();
				sender.sendMessage(messages.getMessage("reload"));
			} else {
				sender.sendMessage(messages.getMessage("noPermission"));
			}
		} 
		else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {

			if (allowed) {
				inventoryFile.delete();
				offlineregions.delete();
				rewardFile.delete();
				region.reset();
				region.loadConfig();
				rewards.clear();
				sender.sendMessage(messages.getMessage("reset"));
			} else {
				sender.sendMessage(messages.getMessage("noPermission"));
			}
		} else {
			return false;
		}

		return true;
	}

	public boolean createRegion(Player player, String name) {
		if (this.worldEdit == null) {
			player.sendMessage(messages.getMessage("noWorldEdit"));
			return false;
		}

		Selection sel = this.worldEdit.getSelection(player);

		if (sel == null) {
			player.sendMessage(messages.getMessage("noSelection"));
			return false;
		}

		Location max = sel.getMaximumPoint();
		Location min = sel.getMinimumPoint();

		if ((max == null) || (min == null)) {
			player.sendMessage(messages.getMessage("selectionIncomplete"));
			return false;
		}

		region.addRegion(name, sel.getWorld().getName(), min.toVector().toBlockVector(), max.toVector().toBlockVector());

		player.sendMessage(messages.getMessage("regionAdded"));

		return true;
	}

	public boolean createRegion(Player player, String name, String wg) {
		if (this.worldGuard == null) {
			player.sendMessage(messages.getMessage("noWorldGuard"));
			return false;
		}

		com.sk89q.worldguard.protection.managers.RegionManager rm = this.worldGuard.getRegionManager(player.getWorld());
		if (!rm.hasRegion(wg)) {
			World w = Bukkit.getServer().getWorld(wg);
			if (w != null) {
				return createRegion(player, name, w);
			} else {
				player.sendMessage(messages.getMessage("regionNotFound"));
				return false;
			}
		}
		region.addRegionWG(name, player.getWorld().getName(), wg);
		player.sendMessage(messages.getMessage("regionAdded"));

		return true;
	}
	
	private boolean deleteRegionByName(String name) {
		Region r = region.getRegionByName(name);
		if (r != null) {
			region.deleteRegion(r);
			return true;
		}
		return false;
	}

	public boolean createRegion(Player p, String name, World w) {
		region.addRegion(name, w);
		p.sendMessage(messages.getMessage("regionAdded"));
		return true;
	}
	
	public RegionManager getRegionManager() {
		return region;
	}

	public RewardManager getRewardManager() {
		return rewards;
	}

	public Messages getMessages(){
		return messages;
	}
	
	public WorldEditPlugin getWorldEdit() {
		return this.worldEdit;
	}
	
	public WorldGuardPlugin getWorldGuard() {
		return this.worldGuard;
	}

	public boolean isWorldEditSet() {
		return this.worldEdit != null;
	}
	
	public boolean isWorldGuardSet() {
		return this.worldGuard != null;
	}
	
	private void loadInventories() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(this.inventoryFile));

			SerializedHashMaps m = (SerializedHashMaps) ois.readObject();
			region.setInventories(m.inventories);
			region.setHealthValues(m.healthValues);
			region.setHungerValues(m.hungerValues);
			region.setXpValues(m.xpValues);
		} 
		catch(EOFException ex) {}
		catch (Exception ex) {
			log.warning("[TaxFreeRegion] Error while reading inventory file!");
			ex.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close inventories file after reading!");
				}
			}
		}
	}
	
	private void saveInventories() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(inventoryFile));

			oos.writeObject(new SerializedHashMaps(region.getInventories(), region.getHealthValues(), region.getXpValues(), region.getHungerValues()));
		} catch (Exception ex) {
			log.severe("[TaxFreeRegion] Error while saving inventory file!");
			ex.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close inventories file after saving!");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadOfflineRegions() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(offlineregions));

			region.setOfflinePlayers(((HashMap<String, OfflineRegion>) ois.readObject()));
		} 
		catch(EOFException ex) {}
		catch (Exception ex) {
			log.warning("[TaxFreeRegion] Error while reading offline region information!");
			ex.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close offline-region file after reading!");
				}
			}
		}
	}

	private void saveOfflineRegions() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(offlineregions));

			oos.writeObject(region.getOfflinePlayers());
		} catch (Exception ex) {
			log.severe("[TaxFreeRegion] Error while saving offline region information!");
			ex.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close offline-region file after saving!");
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadRewards() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(rewardFile));

			rewards.setRewards((HashMap<String, SavedInventory>) ois.readObject());
		} 
		catch(EOFException ex) {}
		catch (Exception ex) {
			log.warning("[TaxFreeRegion] Error while reading rewards file!");
			ex.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close rewards file after reading!");
				}
			}
		}
	}

	private void saveRewards() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(rewardFile));

			oos.writeObject(this.rewards.getRewards());
		} catch (Exception ex) {
			log.severe("[TaxFreeRegion] Error while saving rewards file!");
			ex.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException ex) {
					log.warning("[TaxFreeRegion] Could not close rewards file after saving!");
				}
		}
	}
}
