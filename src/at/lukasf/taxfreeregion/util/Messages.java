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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.bukkit.ChatColor;

import at.lukasf.taxfreeregion.TaxFreeRegion;

public class Messages {
	private Properties messages;
	
	public Messages(String filename){
		try {
			File msgFile = new File(TaxFreeRegion.baseDirectory, filename);
			
			if (!msgFile.exists() || !msgFile.isFile()) {			
				msgFile.createNewFile();			
				writeDefaultMessages();
			}
			
			loadMessages(msgFile);		
		} catch (IOException e) {
			TaxFreeRegion.log.warning("[TaxFreeRegion] Could not load messages file.");
		}
	}

	public String getMessage(String key) {
		if (messages == null) {
			return null;
		}
		return messages.getProperty(key, key + " message not set");
	}
	
	public static String replaceColors(String input) {
		input = input.replaceAll("%aqua%", ChatColor.AQUA.toString());
		input = input.replaceAll("%black%", ChatColor.BLACK.toString());
		input = input.replaceAll("%blue%", ChatColor.BLUE.toString());
		input = input.replaceAll("%darkaqua%", ChatColor.DARK_AQUA.toString());
		input = input.replaceAll("%darkblue%", ChatColor.DARK_BLUE.toString());
		input = input.replaceAll("%darkgray%", ChatColor.DARK_GRAY.toString());
		input = input.replaceAll("%darkgreen%", ChatColor.DARK_GREEN.toString());
		input = input.replaceAll("%darkpurple%", ChatColor.DARK_PURPLE.toString());
		input = input.replaceAll("%darkred%", ChatColor.DARK_RED.toString());
		input = input.replaceAll("%gold%", ChatColor.GOLD.toString());
		input = input.replaceAll("%gray%", ChatColor.GRAY.toString());
		input = input.replaceAll("%green%", ChatColor.GREEN.toString());
		input = input.replaceAll("%lightpurple%", ChatColor.LIGHT_PURPLE.toString());
		input = input.replaceAll("%red%", ChatColor.RED.toString());
		input = input.replaceAll("%white%", ChatColor.WHITE.toString());
		input = input.replaceAll("%yellow%", ChatColor.YELLOW.toString());

		return input;
	}
	
	private void loadMessages(File propertyFile) throws IOException {
		messages = new Properties();		
		messages.load(new FileInputStream(propertyFile));

		Iterator<Map.Entry<Object, Object>> it = messages.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) it.next();
			entry.setValue(replaceColors((String) entry.getValue()));
		}
	}

	private static void writeDefaultMessages() {
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
			pw.println("error:%gold%Could not create region.");
			pw.flush();
			pw.close();

			TaxFreeRegion.log.info("[TaxFreeRegion] Created file messages.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}