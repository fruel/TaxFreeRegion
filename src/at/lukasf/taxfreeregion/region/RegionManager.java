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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.BlockVector;

import at.lukasf.taxfreeregion.TaxFreeRegion;
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

	private YAMLProcessor yaml;
	private File conf;
	private TaxFreeRegion plugin;

	private HashMap<String, Region> regions = new HashMap<String, Region>();

	private HashMap<PlayerRegion, SavedInventory> inventories = new HashMap<PlayerRegion, SavedInventory>();
	private HashMap<String, String> playerregion = new HashMap<String, String>();
	private HashMap<String, ArrayList<PermissionAttachment>> perms = new HashMap<String, ArrayList<PermissionAttachment>>();
	private HashMap<String, OfflineRegion> offline = new HashMap<String, OfflineRegion>();

	private HashMap<PlayerRegion, Double> healthValues = new HashMap<PlayerRegion, Double>();
	private HashMap<PlayerRegion, IntFloat> xpValues = new HashMap<PlayerRegion, IntFloat>();
	private HashMap<PlayerRegion, IntFloat> hungerValues = new HashMap<PlayerRegion, IntFloat>();

	public RegionManager(TaxFreeRegion plugin) {
		this.plugin = plugin;

		this.conf = new File(TaxFreeRegion.baseDirectory, "regions.yml");

		filecheck();

		if (ConfigUpdater.updateNecessary()) {
			TaxFreeRegion.log.info("[TaxFreeRegion] Detected old configuration file. Upgrading...");
			ConfigUpdater.updateConfiguration();
		}

		yaml = new YAMLProcessor(conf, false, YAMLFormat.EXTENDED);

		loadConfig();
	}

	/* CONFIGURATION MANAGEMENT */

	private void filecheck() {
		if (!conf.exists() || !conf.isFile()) {
			try {
				conf.createNewFile();
				PrintWriter pw = new PrintWriter(new FileOutputStream(conf));
				pw.print("{}");
				pw.flush();
				pw.close();
				TaxFreeRegion.log.fine("[TaxFreeRegion] Created file regions.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveConfig() {
		yaml.save();
	}

	public void loadConfig() {
		try {
			yaml.load();
			regions = new HashMap<String, Region>();
			List<String> node = yaml.getKeys("regions");

			if (node == null)
				return;

			for (String regionName : node) {
				YAMLNode regionNode = yaml.getNode("regions." + regionName);

				boolean wordlGuardRegion = false, fullWorldRegion = false;
				BlockVector point1, point2;

				String regionDefinition = regionNode.getString("region").trim();

				if ((wordlGuardRegion = regionDefinition.startsWith("wg|")) && plugin.isWorldGuardSet()) {

					if (!plugin.isWorldGuardSet()) throw new Exception("Found a WorldGuard region bot not the WorldGuard plugin!");

					String worldGuardName = regionDefinition.substring(regionDefinition.indexOf('|') + 1);

					ProtectedRegion r = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(regionNode.getString("world"))).getRegion(worldGuardName);
					if (r != null) {

						com.sk89q.worldedit.BlockVector min = r.getMinimumPoint();
						com.sk89q.worldedit.BlockVector max = r.getMaximumPoint();

						point1 = new BlockVector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
						point2 = new BlockVector(max.getBlockX(), max.getBlockY(), max.getBlockZ());
					} 
					else throw new Exception("Could not find the given WorldGuard region! (" + worldGuardName + ")");
				} 
				else if (regionDefinition.equals("|none|")) {
					point1 = new BlockVector(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
					point2 = new BlockVector(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
					fullWorldRegion = true;
				} 
				else {
					String[] split = regionDefinition.split("\\|");

					if (split.length == 2) {
						String[] min = split[0].split(",");
						String[] max = split[1].split(",");

						if (min.length == 3 && max.length == 3) {
							point1 = new BlockVector(Integer.parseInt(min[0]), Integer.parseInt(min[1]), Integer.parseInt(min[2]));
							point2 = new BlockVector(Integer.parseInt(max[0]), Integer.parseInt(max[1]), Integer.parseInt(max[2]));
						} 
						else throw new Exception("Invalid region definition (" + regionDefinition + ")");
					} 
					else throw new Exception("Invalid region definition (" + regionDefinition + ")");
				}

				Region r = new Region(regionName, regionNode.getString("world"), point1, point2, wordlGuardRegion, fullWorldRegion);

				r.setCrossPlacing(regionNode.getBoolean("cross_placing"));
				r.setEnterMessage(regionNode.getString("messages.enter"));
				r.setExitMessage(regionNode.getString("messages.exit"));

				List<String> denyList = regionNode.getKeys("deny_usage");

				if (denyList != null) {
					for (String key : denyList) {
						try {
							int id = Integer.parseInt(key);

							r.addUsageDeniedItem(id, DenyMode.valueOf(regionNode.getString("deny_usage." + key).trim().toUpperCase()));
						} catch (Exception ex) {
							TaxFreeRegion.log.warning("[TaxFreeRegion] Invalid Item/Block-ID in deny list for region " + regionName);
						}
					}
				}
				
				denyList = regionNode.getKeys("deny_place");

				if (denyList != null) {
					for (String key : denyList) {
						try {
							int id = Integer.parseInt(key);

							r.addPlaceDeniedItem(id, DenyMode.valueOf(regionNode.getString("deny_place." + key).trim().toUpperCase()));
						} catch (Exception ex) {
							TaxFreeRegion.log.warning("[TaxFreeRegion] Invalid Item/Block-ID in deny list for region " + regionName);
						}
					}
				}
				
				denyList = regionNode.getKeys("deny_remove");

				if (denyList != null) {
					for (String key : denyList) {
						try {
							int id = Integer.parseInt(key);

							r.addRemoveDeniedItem(id, DenyMode.valueOf(regionNode.getString("deny_remove." + key).trim().toUpperCase()));
						} catch (Exception ex) {
							TaxFreeRegion.log.warning("[TaxFreeRegion] Invalid Item/Block-ID in deny list for region " + regionName);
						}
					}
				}

				try {
					List<String> inv_enter = regionNode.getStringList("enter.inventory", null);
					if (inv_enter != null) {
						for (String str : inv_enter) {
							r.addEnterInventoryMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> inv_exit = regionNode.getStringList("exit.inventory", null);
					if (inv_exit != null) {
						for (String str : inv_exit) {
							r.addExitInventoryMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> xp_enter = regionNode.getStringList("enter.xp", null);
					if (xp_enter != null) {
						for (String str : xp_enter) {
							r.addEnterXpMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> xp_exit = regionNode.getStringList("exit.xp", null);
					if (xp_exit != null) {
						for (String str : xp_exit) {
							r.addExitXpMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> health_enter = regionNode.getStringList("enter.health", null);
					if (health_enter != null) {
						for (String str : health_enter) {
							r.addEnterHealthMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> health_exit = regionNode.getStringList("exit.health", null);
					if (health_exit != null) {
						for (String str : health_exit) {
							r.addExitHealthMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> hunger_enter = regionNode.getStringList("enter.hunger", null);
					if (hunger_enter != null) {
						for (String str : hunger_enter) {
							r.addEnterHungerMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}

					List<String> hunger_exit = regionNode.getStringList("exit.hunger", null);
					if (hunger_exit != null) {
						for (String str : hunger_exit) {
							r.addExitHungerMode(InventoryMode.valueOf(str.toUpperCase()));
						}
					}
				} catch (Exception ex) {
					throw new Exception("Inventory mode format error in region " + regionName);
				}

				List<String> cmd = regionNode.getStringList("cmdBlacklist", null);
				if (cmd != null) {
					for (String str : cmd) {
						String clean = str.trim();
						if (!clean.isEmpty())
							r.addBlacklistedCommand("/" + clean);
					}
				}

				cmd = regionNode.getStringList("cmdWhitelist", null);
				if (cmd != null) {
					for (String str : cmd) {
						String clean = str.trim();
						if (!clean.isEmpty())
							r.addWhitelistedCommand("/" + clean);
					}
				}

				cmd = regionNode.getStringList("cmdEnter", null);
				if (cmd != null) {
					for (String c : cmd) {
						if (c.startsWith("d")) {
							int delay = Integer.parseInt(c.substring(1, c.indexOf(':')));
							String cc = c.substring(c.indexOf(':') + 1);
							r.addEnterCommand(new Command(cc.substring(2), cc.charAt(0) == 'c' ? true : false, delay));
						} else
							r.addEnterCommand(new Command(c.substring(2), c.charAt(0) == 'c' ? true : false));
					}
				}

				cmd = regionNode.getStringList("cmdExit", null);
				if (cmd != null) {
					for (String c : cmd) {
						if (c.startsWith("d")) {
							int delay = Integer.parseInt(c.substring(1, c.indexOf(':')));
							String cc = c.substring(c.indexOf(':') + 1);
							r.addExitCommand(new Command(cc.substring(2), cc.charAt(0) == 'c' ? true : false, delay));
						} else
							r.addExitCommand(new Command(c.substring(2), c.charAt(0) == 'c' ? true : false));
					}
				}

				cmd = regionNode.getStringList("permissions", null);
				if (cmd != null) {
					for (String c : cmd) {
						c = c.replace('{', ' ').replace('}', ' ').trim();
						String[] split = c.split("=");

						if (split.length == 2)
							r.addPermission(split[0], Boolean.parseBoolean(split[1].trim()));
					}
				}

				regions.put(regionName, r);
			}

		} catch (Exception e) {
			TaxFreeRegion.log.severe("[TaxFreeRegion] Error loading config file. " + e.getMessage());
			e.printStackTrace();
		}
	}

	/* REGION MANAGEMENT */

	public void deleteRegion(Region r) {
		regions.remove(r.getName());

		for (String player : playerregion.keySet()) {
			if (playerregion.get(player).equals(r.getName())) {
				exitRegion(Bukkit.getPlayer(player));
			}
		}

		yaml.removeProperty("regions." + r.getName());
		saveConfig();
	}

	public void addRegion(String name, String world, BlockVector point1, BlockVector point2) {
		BlockVector min = BlockVector.getMinimum(point1, point2).toBlockVector();
		BlockVector max = BlockVector.getMaximum(point1, point2).toBlockVector();

		addRegion(name, world, min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + "|" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ());
	}

	public void addRegion(String name, World world) {
		addRegion(name, world.getName(), "|none|");
	}

	public void addRegionWG(String name, String world, String wg_name) {
		addRegion(name, world, "wg|" + wg_name);
	}

	private void addRegion(String name, String world, String region) {
		YAMLNode n = yaml.addNode("regions." + name);
			n.setProperty("region", region);
			n.setProperty("world", world);
			n.setProperty("cross_placing", false);
			n.setProperty("deny_block_drops", "border");
			n.setProperty("deny_item_drops", "full");
			n.setProperty("deny_death_drops", "full");
			YAMLNode deny = n.addNode("deny_usage");
				deny.setProperty("381", "full");
				deny.setProperty("408", "full");
				deny.setProperty("154", "full");
				deny.setProperty("23", "full");
				deny.setProperty("158", "full");
				deny.setProperty("29", "border");
				deny.setProperty("33", "border");
				deny.setProperty("342", "full");
				deny.setProperty("343", "full");
				deny.setProperty("407", "full");
				deny.setProperty("54", "full");
				deny.setProperty("61", "full");
				deny.setProperty("146", "full");
				deny.setProperty("130", "full");
				deny.setProperty("379", "full");
				deny.setProperty("138", "full");
				
			deny = n.addNode("deny_place");
				deny.setProperty("408", "full");
				deny.setProperty("154", "full");
				
			n.addNode("deny_remove");
	
			List<String> l = new ArrayList<String>();
			List<String> l2 = new ArrayList<String>();
			l.add("store");
			l.add("clear");
			l2.add("restore");
			n.setProperty("enter.inventory", l);
			n.setProperty("exit.inventory", l2);
			n.setProperty("enter.xp", null);
			n.setProperty("exit.xp", null);
			n.setProperty("enter.health", null);
			n.setProperty("exit.health", null);
			n.setProperty("enter.hunger", null);
			n.setProperty("exit.hunger", null);
	
			YAMLNode messages = n.addNode("messages");
				messages.setProperty("enter", "%gold%Welcome to %green%" + name + "%gold%.");
				messages.setProperty("exit", "%gold%Bye bye");
	
			n.setProperty("cmdBlacklist", null);
			n.setProperty("cmdWhitelist", null);
			n.setProperty("cmdEnter", null);
			n.setProperty("cmdExit", null);
			n.setProperty("permissions", null);
		saveConfig();
	}

	/* REGION CHECK LOGIC */

	public void regionCheck(Player player) {
		List<Region> list = getRegionsForWorld(player.getLocation().getWorld());

		Iterator<Region> it = list.iterator();

		while (it.hasNext()) {
			Region region = (Region) it.next();

			if (!region.isWorld && region.contains(player.getLocation())) {
				if (!this.isPlayerInsideRegion(player)) {
					enterRegion(region, player);
				} else if (isPlayerInsideRegion(player) && getRegionForPlayer(player) != region) {
					exitRegion(player);
					enterRegion(region, player);
				}
				return;
			} else if (region.isWorld && !this.isPlayerInsideRegion(player) && region.getWorld().equals(player.getWorld().getName())) {
				enterRegion(region, player);
			}

		}

		if (isPlayerInsideRegion(player)) {
			Region r = getRegionForPlayer(player);
			if (!r.isWorld || (r.isWorld && !player.getWorld().getName().equals(r.getWorld())))
				exitRegion(player);
		}

	}

	private void applyPermissions(Region r, Player p) {
		if (!perms.containsKey(p.getName()))
			perms.put(p.getName(), new ArrayList<PermissionAttachment>());

		for (String perm : r.getPermissions().keySet()) {
			PermissionAttachment patt = p.addAttachment(plugin, perm, r.getPermissions().get(perm));
			perms.get(p.getName()).add(patt);
		}

		p.recalculatePermissions();
	}

	private void revertPermissions(Player p) {
		try {
			for (PermissionAttachment patt : perms.get(p.getName()))
				p.removeAttachment(patt);
		} catch (Exception ex) {
			TaxFreeRegion.log.fine("[TaxFreeRegion] (DEBUG) Exception while reverting permissions.");
		}
		perms.remove(p.getName());
		p.recalculatePermissions();
	}

	private void runCommands(final Player p, List<Command> cmds) {
		for (Command c : cmds) {
			if (c.getDelay() > 0) {
				final String cmd = c.getCommand().replace("%name%", p.getName());
				final boolean console = c.isConsoleSender();
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						Bukkit.getServer().dispatchCommand(console ? Bukkit.getServer().getConsoleSender() : p, cmd);

					}
				}, c.getDelay() * 20);
			} else {
				String cmd = c.getCommand().replace("%name%", p.getName());
				Bukkit.getServer().dispatchCommand(c.isConsoleSender() ? Bukkit.getServer().getConsoleSender() : p, cmd);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void restoreInventory(Player player) {
		if (player.hasPermission("taxfreeregion.noclear"))
			return;

		SavedInventory inventory = null;
		String playerName = player.getName();

		inventory = this.inventories.get(playerName);
		if (inventory != null) {
			inventory.setInventoryContent(player.getInventory());
			this.inventories.remove(playerName);

			player.updateInventory();
		}
	}

	@SuppressWarnings("deprecation")
	public void enterRegion(Region r, Player p) {
		if (!r.isWorld)
			plugin.getRewardManager().clear(p);

		if (r.hasInventoryEnterMode(InventoryMode.STORE))
			inventories.put(new PlayerRegion(p.getName(), null), new SavedInventory(p.getInventory()));
		else
			inventories.put(new PlayerRegion(p.getName(), null), null);

		if (r.hasInventoryEnterMode(InventoryMode.REWARD))
			plugin.getRewardManager().queueReward(p);

		if (r.hasInventoryEnterMode(InventoryMode.RESTORE)) {
			clearInventory(p);
			SavedInventory inv = getInventory(p, r);

			if (inv != null)
				inv.setInventoryContent(p.getInventory());

			inventories.remove(new PlayerRegion(p.getName(), r.getName()));
		}
		if (r.hasInventoryEnterMode(InventoryMode.CLEAR))
			clearInventory(p);

		// ----------------------------------------

		if (r.hasXpEnterMode(InventoryMode.STORE)) {
			IntFloat f = new IntFloat(p.getLevel(), p.getExp());
			xpValues.put(new PlayerRegion(p.getName(), null), f);
		}
		if (r.hasXpEnterMode(InventoryMode.RESTORE)) {
			IntFloat f = xpValues.remove(new PlayerRegion(p.getName(), r.getName()));
			if (f != null) {
				p.setLevel(f.getIntVal());
				p.setExp(f.getFloatVal());
			}
		}
		if (r.hasXpEnterMode(InventoryMode.CLEAR)) {
			p.setLevel(0);
			p.setExp(0);
		}

		// ----------------------------------------
		if (r.hasHealthEnterMode(InventoryMode.STORE)) {
			healthValues.put(new PlayerRegion(p.getName(), null), p.getHealth() == 0.0 ? 1 : p.getHealth());
		}
		if (r.hasHealthEnterMode(InventoryMode.RESTORE)) {
			Double d = healthValues.remove(new PlayerRegion(p.getName(), r.getName()));
			if (d != null) {
				p.setHealth(d);
			}
		}
		if (r.hasHealthEnterMode(InventoryMode.CLEAR)) {
			p.setHealth(20);
		}

		// ----------------------------------------
		if (r.hasHungerEnterMode(InventoryMode.STORE)) {
			IntFloat f = new IntFloat(p.getFoodLevel(), p.getSaturation());
			hungerValues.put(new PlayerRegion(p.getName(), null), f);
		}
		if (r.hasHungerEnterMode(InventoryMode.RESTORE)) {
			IntFloat f = hungerValues.remove(new PlayerRegion(p.getName(), r.getName()));
			if (f != null) {
				p.setFoodLevel(f.getIntVal());
				p.setSaturation(f.getFloatVal());
			}
		}
		if (r.hasHungerEnterMode(InventoryMode.CLEAR)) {
			p.setFoodLevel(20);
			p.setSaturation(20);
		}

		// ----------------------------------------

		p.updateInventory();

		applyPermissions(r, p);
		runCommands(p, r.getEnterCommands());

		if (!r.getExitMessage().isEmpty())
			p.sendMessage(Messages.replaceColors(r.getEnterMessage()));

		playerregion.put(p.getName(), r.getName());
	}

	@SuppressWarnings("deprecation")
	public void exitRegion(Player p) {
		Region r = getRegionByName(playerregion.get(p.getName()));
		if (r != null) {
			plugin.getRewardManager().clear(p);
			if (r.hasInventoryExitMode(InventoryMode.STORE))
				this.inventories.put(new PlayerRegion(p.getName(), r.getName()), new SavedInventory(p.getInventory()));

			if (r.hasInventoryExitMode(InventoryMode.REWARD))
				plugin.getRewardManager().queueReward(p);

			if (r.hasInventoryExitMode(InventoryMode.RESTORE)) {
				clearInventory(p);
				SavedInventory inv = getInventory(p, null);

				if (inv != null)
					inv.setInventoryContent(p.getInventory());
				
				inventories.remove(new PlayerRegion(p.getName(), null));
			}
			if (r.hasInventoryExitMode(InventoryMode.CLEAR))
				clearInventory(p);

			// ----------------------------------------

			if (r.hasXpExitMode(InventoryMode.STORE)) {
				IntFloat f = new IntFloat(p.getLevel(), p.getExp());
				xpValues.put(new PlayerRegion(p.getName(), r.getName()), f);
			}
			if (r.hasXpExitMode(InventoryMode.RESTORE)) {
				IntFloat f = xpValues.remove(new PlayerRegion(p.getName(), null));
				if (f != null) {
					p.setLevel(f.getIntVal());
					p.setExp(f.getFloatVal());
				}
			}
			if (r.hasXpExitMode(InventoryMode.CLEAR)) {
				p.setLevel(0);
				p.setExp(0);
			}

			// ----------------------------------------
			if (r.hasHealthExitMode(InventoryMode.STORE)) {
				healthValues.put(new PlayerRegion(p.getName(), r.getName()), p.getHealth() == 0 ? 1 : p.getHealth());
			}
			if (r.hasHealthExitMode(InventoryMode.RESTORE)) {
				Double d = healthValues.remove(new PlayerRegion(p.getName(), null));
				if (d != null) {
					p.setHealth(d);
				}
			}
			if (r.hasHealthExitMode(InventoryMode.CLEAR)) {
				p.setHealth(20);
			}

			// ----------------------------------------
			if (r.hasHungerExitMode(InventoryMode.STORE)) {
				IntFloat f = new IntFloat(p.getFoodLevel(), p.getSaturation());
				hungerValues.put(new PlayerRegion(p.getName(), r.getName()), f);
			}
			if (r.hasHungerExitMode(InventoryMode.RESTORE)) {
				IntFloat f = hungerValues.remove(new PlayerRegion(p.getName(), null));
				if (f != null) {
					p.setFoodLevel(f.getIntVal());
					p.setSaturation(f.getFloatVal());
				}
			}
			if (r.hasHungerExitMode(InventoryMode.CLEAR)) {
				p.setFoodLevel(20);
				p.setSaturation(20);
			}

			// ----------------------------------------

			p.updateInventory();

			if (!r.getExitMessage().isEmpty())
				p.sendMessage(Messages.replaceColors(r.getExitMessage()));
			
			runCommands(p, r.getExitCommands());
			revertPermissions(p);
			
			playerregion.remove(p.getName());
		}
	}

	public void disconnectPlayer(Player p) {
		Region r = getRegionByName(playerregion.get(p.getName()));
		if (r != null) {
			SavedInventory current = new SavedInventory(p.getInventory());
			clearInventory(p);

			offline.put(p.getName(), new OfflineRegion(r.getName(), current));

			SavedInventory def = inventories.get(new PlayerRegion(p.getName(), null));
			if (def != null)
				def.setInventoryContent(p.getInventory());
			
			revertPermissions(p);
			playerregion.remove(p.getName());
		}
	}

	public void reconnectPlayer(Player p) {
		if (offline.containsKey(p.getName())) {
			OfflineRegion or = offline.get(p.getName());

			Region r = getRegionByName(or.getRegion());

			if (r != null) {
				clearInventory(p);
				if (or.getInventory() != null)
					or.getInventory().setInventoryContent(p.getInventory());

				applyPermissions(r, p);

				playerregion.put(p.getName(), r.getName());
				offline.remove(p.getName());
				TaxFreeRegion.log.fine("[TaxFreeRegion] " + p.getName() + " joined into region " + r.getName());
			} else {
				TaxFreeRegion.log.severe("[TaxFreeRegion] A player wants to join into a region which is not present in regions.yml. Probably he lost his inventory now.");
			}

		} else {
			regionCheck(p);
		}
	}

	/* CLEANUP CODE */

	public void cleanShutdown() {
		Set<String> ps = new HashSet<String>(playerregion.keySet());
		for (String player : ps) {
			Player p = Bukkit.getServer().getPlayer(player);
			disconnectPlayer(p);
		}
		saveConfig();
	}

	public void reset() {
		inventories.clear();
		playerregion.clear();
		perms.clear();
		offline.clear();
		xpValues.clear();
		healthValues.clear();
		hungerValues.clear();
	}

	/* GETTER/SETTER */

	public List<Region> getRegionsForWorld(World world) {
		ArrayList<Region> regs = new ArrayList<Region>();
		for (String s : regions.keySet()) {
			if (regions.get(s).getWorld().equals(world.getName()))
				regs.add(regions.get(s));
		}
		return regs;
	}

	public Region getRegionByName(String name) {
		return regions.get(name);
	}

	public HashMap<String, Region> getRegions() {
		return regions;
	}

	public boolean isPlayerInsideRegion(Player player) {
		return playerregion.containsKey(player.getName());
	}

	public Region getRegionForPlayer(Player p) {
		return getRegionByName(playerregion.get(p.getName()));
	}

	public SavedInventory getInventory(Player p, Region r) {
		return inventories.get(new PlayerRegion(p.getName(), r == null ? null : r.getName()));
	}

	public Region getRegionForLocation(Location l) {
		for (Region reg : getRegions().values())
			if (reg.contains(l)) {
				return reg;
			}

		return getRegionForWorld(l.getWorld());
	}

	public Region getRegionForWorld(World w) {
		for (Region reg : getRegions().values())
			if (reg.isWorld && reg.getWorld().equals(w.getName())) {
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
	
	public HashMap<PlayerRegion, SavedInventory> getInventories() {
		return inventories;
	}
	
	public HashMap<String, OfflineRegion> getOfflinePlayers() {
		return offline;
	}
	
	public void setInventories(HashMap<PlayerRegion, SavedInventory> inventories) {
		if(inventories == null){
			this.inventories = new HashMap<PlayerRegion, SavedInventory>();
		} else {
			this.inventories = inventories;
		}		
	}
	
	public void setOfflinePlayers(HashMap<String, OfflineRegion> offline) {
		if(offline == null){
			this.offline = new HashMap<String, OfflineRegion>();
		} else {
			this.offline = offline;
		}	
	}

	public void setHealthValues(HashMap<PlayerRegion, Double> healthValues) {
		if(healthValues == null){
			this.healthValues = new HashMap<PlayerRegion, Double>();
		} else {
			this.healthValues = healthValues;
		}	
	}

	public void setXpValues(HashMap<PlayerRegion, IntFloat> xpValues) {
		if(xpValues == null){
			this.xpValues = new HashMap<PlayerRegion, IntFloat>();
		} else {
			this.xpValues = xpValues;
		}	
	}

	public void setHungerValues(HashMap<PlayerRegion, IntFloat> hungerValues) {
		if(hungerValues == null){
			this.hungerValues = new HashMap<PlayerRegion, IntFloat>();
		} else {
			this.hungerValues = hungerValues;
		}	
	}
	
	private void clearInventory(Player p) {
		PlayerInventory inv = p.getInventory();
		inv.setBoots(null);
		inv.setChestplate(null);
		inv.setHelmet(null);
		inv.setLeggings(null);
		inv.clear();
	}
}
