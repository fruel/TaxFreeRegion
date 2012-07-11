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

import org.bukkit.inventory.PlayerInventory;
 
 public class InventoryManager
 {   
 
   public static void clearInventory(PlayerInventory inv) {
			
	 inv.setBoots(null);
	 inv.setChestplate(null);
	 inv.setHelmet(null);
	 inv.setLeggings(null);
     inv.clear();
   }
 
   public static SavedInventory getInventoryContent(PlayerInventory inv)
   {
     int imax = inv.getSize();
     SavedInventory save = new SavedInventory(imax);
     for (int i = 0; i < imax; i++) {
       save.setItem(i, inv.getItem(i));
     }
 
     save.setHelmet(inv.getHelmet());
     save.setChestplate(inv.getChestplate());
     save.setLeggings(inv.getLeggings());
    save.setBoots(inv.getBoots());
    
     return save;
   }
 
   public static void setInventoryContent(SavedInventory save, PlayerInventory inv)
   {
     int imax = Math.min(save.getSize(), inv.getSize());
     for (int i = 0; i < imax; i++)   	
    		 inv.setItem(i, save.getNewStackFrom(i));    
     
     inv.setBoots(save.getBoots());
     inv.setChestplate(save.getChestplate());
     inv.setHelmet(save.getHelmet());
     inv.setLeggings(save.getLeggings());
   }
 
   public static SavedInventory createDummyInventory()
   {
	   return new SavedInventory(0);
   }
 }