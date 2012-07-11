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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;

import at.lukasf.taxfreeregion.TaxFreeRegion;


public class RewardManager {
	
	private HashMap<String, SavedInventory> rewards;
	private File rewardFile;
	private Logger log = TaxFreeRegion.log;
	
	public RewardManager(TaxFreeRegion plugin)
	{
		rewardFile = new File(plugin.getConfigDirectory(), "rewards.ser");
	    if ((!rewardFile.exists()) || (!rewardFile.isFile())) {
	        try {
				rewardFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        log.info("[TaxFreeRegion] Created file inventories.ser");
	    } 
	    loadRewards();
	}
	
	public void queueReward(Player p)
	{	
		rewards.put(p.getName(), InventoryManager.getInventoryContent(p.getInventory()));		
		p.sendMessage(TaxFreeRegion.messages.getMessage("reward"));		
	}
	
	public void giveMe(Player player) {
		if(rewards.containsKey(player.getName()))
		{
			PlayerInventoryChest inv = new PlayerInventoryChest(rewards.get(player.getName()), player.getName());
			((CraftPlayer)player).openInventory(new CraftInventory(inv));
			rewards.remove(player.getName());
		}
		else player.sendMessage(TaxFreeRegion.messages.getMessage("noReward"));
	}
	public void cleanShutdown()
	{
		saveRewards();
	}
	
	public void clear()
	{
		rewards.clear();
	}
	public void clean()
	{
		rewardFile.delete();
	}
	public void clear(Player p)
	{
		rewards.remove(p.getName());
	}
	
	@SuppressWarnings("unchecked")
   private void loadRewards()
   {
     ObjectInputStream ois = null;
     try
     {
       ois = new ObjectInputStream(new FileInputStream(rewardFile));
 
       this.rewards = ((HashMap<String, SavedInventory>)ois.readObject());
     }
     catch (EOFException ex) {
       this.rewards = new HashMap<String, SavedInventory>();
 
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex2) {
           TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Reward file error on close !");
         }
     }
     catch (Exception ex)
     {
    	 TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Rewards file error !");
       this.rewards = new HashMap<String, SavedInventory>();
 
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex3) {
        	 TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Reward file error on close !");
         }
     }
     finally
     {
       if (ois != null)
         try {
           ois.close();
         } catch (IOException ex) {
        	 TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Reward file error on close !");
         }
     }
   }
	
   private void saveRewards()
   {
     ObjectOutputStream oos = null;
     try {
       oos = new ObjectOutputStream(new FileOutputStream(rewardFile));
 
       oos.writeObject(this.rewards);
     } catch (Exception ex) {
    	 TaxFreeRegion.log.log(Level.SEVERE, "[TaxFreeRegion] Reward file not saved !");
 
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex2) {
        	 TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Reward file error on close !");
         }
     }
     finally
     {
       if (oos != null)
         try {
           oos.close();
         } catch (IOException ex) {
        	 TaxFreeRegion.log.log(Level.WARNING, "[TaxFreeRegion] Reward file error on close !");
         }
     }
   }

   
}
