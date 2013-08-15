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

package at.lukasf.taxfreeregion.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.lukasf.taxfreeregion.TaxFreeRegion;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;

public class ConfigUpdater {

	private static final int VERSION = 5;

	private static int detectedVersion = -1;

	public static boolean updateNecessary() {
		File f = new File("./plugins/TaxFreeRegion/regions.yml");
		if (!f.exists())
			return false;

		detectedVersion = detectCurrentConfigVersion();

		if (detectedVersion != -1 && detectedVersion < VERSION)
			return true;
		return false;
	}

	private static int detectCurrentConfigVersion() {
		try {
			YAMLProcessor yaml = new YAMLProcessor(new File("./plugins/TaxFreeRegion/regions.yml"), false, YAMLFormat.EXTENDED);
			yaml.load();
			List<String> n = yaml.getKeys("regions");
			if (n != null && n.size() > 0) {
				
				List<String> keys = yaml.getKeys("regions." + n.get(0) + ".deny");
				if (keys != null && keys.size() > 0) {
					if (keys.contains("border_piston"))
						return 1;
				}
				
				String v2 = yaml.getString("regions." + n.get(0) + ".inventory.enter");
				String v2exit = yaml.getString("regions." + n.get(0) + ".inventory.exit");
				
				if (v2 != null || v2exit != null) {
					int cEnter = -1, cExit = -1;
					
					if (v2 != null)	cEnter = yaml.getStringList("regions." + n.get(0) + ".inventory.enter", null).size();
					if (v2exit != null)	cExit = yaml.getStringList("regions." + n.get(0) + ".inventory.exit", null).size();
					
					if (cEnter == 0 || cExit == 0)
						return 2;
					else return 3;
				}					
				else {
					
					keys = yaml.getKeys("regions." + n.get(0) + ".deny");
					if (keys != null && keys.size() > 0) {
						if (keys.contains("minecart_storage"))
							return 4;
					}
					
					return 5;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			TaxFreeRegion.log.severe("[TaxFreeRegion] Could not detect the version of the current configuration file (regions.yml). Is the file corrupted?");
			return -1;
		}

		TaxFreeRegion.log.info("[TaxFreeRegion] Could not detect the version of the current configuration file (regions.yml). The file is empty.");
		return VERSION;
	}

	public static void updateConfiguration() {
		TaxFreeRegion.log.info("[TaxFreeRegion] Trying to upgrade the configuration file. Current version: " + detectedVersion + " - Target version: " + VERSION);
		try {
			File fNew = new File("./plugins/TaxFreeRegion/regions.yml");

			if (detectedVersion == 1) {
				YAMLProcessor yaml = new YAMLProcessor(fNew, false, YAMLFormat.EXTENDED);
				YAMLProcessor new_yaml = new YAMLProcessor(fNew, false, YAMLFormat.EXTENDED);
				yaml.load();

				YAMLNode root = new_yaml.addNode("regions");
				List<String> n = yaml.getKeys("regions");
				if (n != null) {
					for (String r : n) {
						YAMLNode region = root.addNode(r);
						YAMLNode old_region = yaml.getNode("regions." + r);
						region.setProperty("region", old_region.getProperty("region"));
						region.setProperty("world", old_region.getProperty("world"));
						region.setProperty("cross_placing", true);
						YAMLNode deny = region.addNode("deny");
						deny.setProperty("dispenser", (old_region.getBoolean("deny.dispenser") ? "full" : "none"));
						deny.setProperty("chest", (old_region.getBoolean("deny.chest") ? "full" : "none"));
						deny.setProperty("minecart_storage", (old_region.getBoolean("deny.minecart_storage") ? "full" : "none"));
						deny.setProperty("piston", (old_region.getBoolean("deny.border_piston") ? "border" : "none"));
						deny.setProperty("block_drops", (old_region.getBoolean("deny.border_block_drop") ? "border" : "none"));
						deny.setProperty("item_drops", (old_region.getBoolean("deny.item_drop") ? "full" : "none"));
						deny.setProperty("death_drops", (old_region.getBoolean("deny.death_drops") ? "full" : "none"));
						deny.setProperty("eye_of_ender", (old_region.getBoolean("deny.eye_of_ender") ? "full" : "none"));

						YAMLNode inv = region.addNode("inventory");
						inv.setProperty("enter", "store");
						inv.setProperty("exit", "restore");

						YAMLNode msg = region.addNode("messages");
						msg.setProperty("enter", old_region.getString("messages.enter"));
						msg.setProperty("exit", old_region.getString("messages.exit"));

						region.setProperty("cmdBlacklist", old_region.getProperty("cmdBlacklist"));
						region.setProperty("cmdWhitelist", old_region.getProperty("cmdWhitelist"));
						region.setProperty("cmdEnter", old_region.getProperty("cmdEnter"));
						region.setProperty("cmdExit", old_region.getProperty("cmdExit"));
						region.setProperty("permissions", old_region.getProperty("permissions"));
					}
				}
				new_yaml.save();
				detectedVersion++;
				TaxFreeRegion.log.info("[TaxFreeRegion] Upgrade to version " + detectedVersion + " successful");
			}
			if (detectedVersion == 2) {
				YAMLProcessor yaml = new YAMLProcessor(fNew, false, YAMLFormat.EXTENDED);
				yaml.load();

				List<String> n = yaml.getKeys("regions");
				if (n != null) {
					for (String r : n) {
						String enter = yaml.getString("regions." + r + ".inventory.enter");
						if (enter != null) {
							if (yaml.getStringList("regions." + r + ".inventory.enter", null).size() == 0) {
								List<String> l = new ArrayList<String>();
								l.add(enter);

								yaml.setProperty("regions." + r + ".inventory.enter", l);
							}
						}

						String exit = yaml.getString("regions." + r + ".inventory.exit");
						if (exit != null) {
							if (yaml.getStringList("regions." + r + ".inventory.exit", null).size() == 0) {
								List<String> l = new ArrayList<String>();
								l.add(exit);

								yaml.setProperty("regions." + r + ".inventory.exit", l);
							}
						}

					}
				}
				yaml.save();
				detectedVersion++;
				TaxFreeRegion.log.info("[TaxFreeRegion] Upgrade to version " + detectedVersion + " successful");
			}
			if (detectedVersion == 3) {
				YAMLProcessor yaml = new YAMLProcessor(fNew, false, YAMLFormat.EXTENDED);
				yaml.load();

				List<String> n = yaml.getKeys("regions");
				if (n != null) {
					for (String r : n) {
						List<String> enter = yaml.getStringList("regions." + r + ".inventory.enter", null);
						List<String> exit = yaml.getStringList("regions." + r + ".inventory.exit", null);

						yaml.removeProperty("regions." + r + ".inventory");

						yaml.setProperty("regions." + r + ".enter.health", null);
						yaml.setProperty("regions." + r + ".enter.xp", null);
						yaml.setProperty("regions." + r + ".enter.hunger", null);
						yaml.setProperty("regions." + r + ".enter.inventory", enter);

						yaml.setProperty("regions." + r + ".exit.health", null);
						yaml.setProperty("regions." + r + ".exit.xp", null);
						yaml.setProperty("regions." + r + ".exit.hunger", null);
						yaml.setProperty("regions." + r + ".exit.inventory", exit);
					}
				}
				yaml.save();
				detectedVersion++;
				TaxFreeRegion.log.info("[TaxFreeRegion] Upgrade to version " + detectedVersion + " successful");
			}
			if (detectedVersion == 4) {
				YAMLProcessor yaml = new YAMLProcessor(fNew, false, YAMLFormat.EXTENDED);
				yaml.load();

				List<String> n = yaml.getKeys("regions");
				if (n != null) {
					for (String r : n) {
						yaml.setProperty("regions." + r + ".deny_block_drops", yaml.getProperty("regions." + r + ".deny.block_drops"));
						yaml.setProperty("regions." + r + ".deny_item_drops", yaml.getProperty("regions." + r + ".deny.item_drops"));
						yaml.setProperty("regions." + r + ".deny_death_drops", yaml.getProperty("regions." + r + ".deny.death_drops"));
						
						String chest = yaml.getString("regions." + r + ".deny.chest");
						String dispenser = yaml.getString("regions." + r + ".deny.dispenser");
						String minecart_storage = yaml.getString("regions." + r + ".deny.minecart_storage");
						String piston = yaml.getString("regions." + r + ".deny.piston");
						String eye_of_ender = yaml.getString("regions." + r + ".deny.eye_of_ender");
						
						yaml.removeProperty("regions." + r + ".deny");
						
						yaml.setProperty("regions." + r + ".deny_usage.381", eye_of_ender);
						yaml.setProperty("regions." + r + ".deny_usage.408", dispenser);
						yaml.setProperty("regions." + r + ".deny_usage.154", dispenser);
						yaml.setProperty("regions." + r + ".deny_usage.23", dispenser);
						yaml.setProperty("regions." + r + ".deny_usage.158", dispenser);
						yaml.setProperty("regions." + r + ".deny_usage.29", piston);
						yaml.setProperty("regions." + r + ".deny_usage.33", piston);
						yaml.setProperty("regions." + r + ".deny_usage.342", minecart_storage);
						yaml.setProperty("regions." + r + ".deny_usage.343", minecart_storage);
						yaml.setProperty("regions." + r + ".deny_usage.407", minecart_storage);
						yaml.setProperty("regions." + r + ".deny_usage.54", chest);
						yaml.setProperty("regions." + r + ".deny_usage.61", chest);
						yaml.setProperty("regions." + r + ".deny_usage.146", chest);
						yaml.setProperty("regions." + r + ".deny_usage.130", chest);
						yaml.setProperty("regions." + r + ".deny_usage.379", chest);
						yaml.setProperty("regions." + r + ".deny_usage.138", chest);
						
						yaml.setProperty("regions." + r + ".deny_place.408", chest);
						yaml.setProperty("regions." + r + ".deny_place.154", chest);
						
						yaml.setProperty("regions." + r + ".deny_remove", null);
					}
				}
				yaml.save();
				detectedVersion++;
				TaxFreeRegion.log.info("[TaxFreeRegion] Upgrade to version " + detectedVersion + " successful");
			}
			TaxFreeRegion.log.info("[TaxFreeRegion] Configuration successfully upgraded to version " + detectedVersion);

		} catch (Exception ex) {
			TaxFreeRegion.log.severe("[TaxFreeRegion] Could not complete the configuration upgrade. Please check the configuration file.");
			ex.printStackTrace();
		}
	}
}
