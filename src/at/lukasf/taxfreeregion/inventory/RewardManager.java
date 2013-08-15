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

package at.lukasf.taxfreeregion.inventory;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import at.lukasf.taxfreeregion.TaxFreeRegion;


public class RewardManager {
	
	private HashMap<String, SavedInventory> rewards = new HashMap<String, SavedInventory>();;
	private TaxFreeRegion plugin;
	
	public RewardManager(TaxFreeRegion plugin)
	{
		this.plugin = plugin;
	}
	
	public void queueReward(Player p)
	{	
		rewards.put(p.getName(), new SavedInventory(p.getInventory()));		
		p.sendMessage(plugin.getMessages().getMessage("reward"));		
	}
	
	public void giveMe(Player player) {
		if(rewards.containsKey(player.getName()))
		{
			Inventory inv = Bukkit.getServer().createInventory(player, 45, player.getName());
			SavedInventory si= rewards.get(player.getName());
			
			for (int i = 0; i < si.getSize(); i++){
				ItemStack is = si.getNewStackFrom(i);
				if(is != null)
					inv.addItem(is);				
			}	        	
			
			
			ItemStack armor;
			
			armor = si.getBoots();
			if(armor != null)
				inv.addItem(armor);
			
			armor = si.getChestplate();
			if(armor != null)
				inv.addItem(armor);
			
			armor = si.getHelmet();
			if(armor != null)
				inv.addItem(armor);
			
			armor = si.getLeggings();
			if(armor != null)
				inv.addItem(armor);
						
			player.openInventory(inv);
			rewards.remove(player.getName());
		}
		else player.sendMessage(plugin.getMessages().getMessage("noReward"));
	}
		
	public void clear()
	{
		rewards.clear();
	}
	
	public void clear(Player p)
	{
		rewards.remove(p.getName());
	}
	
	public HashMap<String, SavedInventory> getRewards() {
		return rewards;
	}

	public void setRewards(HashMap<String, SavedInventory> rewards) {
		if(rewards == null){
			rewards = new HashMap<String, SavedInventory>();
		}
		else {
			this.rewards = rewards;
		}
	}
}
