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
 
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import at.lukasf.taxfreeregion.TaxFreeRegion;
 
 public class SavedInventory implements Serializable
 {
   private static final long serialVersionUID = -4000934373645689393L;
   
   private int[] typeIds;
   private short[] damageIds;
   private int[] quantities;
   private int size;
   
   private int[] armorIds;
   private short[] armorDmg;
   
   private int[] bootsEnch;
   private int[] bootsEnchData;
   
   private int[] chestEnch;
   private int[] chestEnchData;
   
   private int[] legEnch;
   private int[] legEnchData;
   
   private int[] headEnch;
   private int[] headEnchData;   
   
   private HashMap<Integer, HashMap<Integer, Integer>> ench;
   
   public SavedInventory(int size)
   {
     this.size = size;
     this.typeIds = new int[size];
     this.damageIds = new short[size];
     this.quantities = new int[size];
     
     ench = new HashMap<Integer, HashMap<Integer, Integer>>();
     armorIds = new int[]{-1,-1,-1,-1};
     armorDmg = new short[]{0,0,0,0};
   }
 
   public void setBoots(ItemStack is)
   {
	   if(is==null)return;
	   armorIds[0] = is.getTypeId();
	   armorDmg[0] = is.getDurability();
	   
	   Map<Enchantment, Integer> en = is.getEnchantments();
	   if(en.size()>0){
		   bootsEnch = new int[en.size()];
		   bootsEnchData = new int[en.size()];
		   
		   Iterator<Enchantment> it = en.keySet().iterator();
		   for(int i = 0; i< en.size(); i++)
		   {
			   Enchantment ent= it.next();
			   bootsEnch[i] = ent.getId();
			   bootsEnchData[i] = en.get(ent);
		   }
	   }
   }
   public void setChestplate(ItemStack is)
   {
	   if(is==null)return;
	   armorIds[1] = is.getTypeId();
	   armorDmg[1] = is.getDurability();
	   
	   Map<Enchantment, Integer> en = is.getEnchantments();
	   if(en.size()>0){
		   chestEnch = new int[en.size()];
		   chestEnchData = new int[en.size()];
	   
		   Iterator<Enchantment> it = en.keySet().iterator();
		   for(int i = 0; i< en.size(); i++)
		   {
			   Enchantment ent= it.next();
			   chestEnch[i] = ent.getId();
			   chestEnchData[i] = en.get(ent);
		   }
	   }
   }
   public void setHelmet(ItemStack is)
   {
	   if(is==null)return;
	   armorIds[2] = is.getTypeId();
	   armorDmg[2] = is.getDurability();
	   
	   Map<Enchantment, Integer> en = is.getEnchantments();
	   if(en.size()>0){
		   headEnch = new int[en.size()];
		   headEnchData = new int[en.size()];
	   
		   Iterator<Enchantment> it = en.keySet().iterator();
		   for(int i = 0; i< en.size(); i++)
		   {
			   Enchantment ent= it.next();
			   headEnch[i] = ent.getId();
			   headEnchData[i] = en.get(ent);
		   } 
	   }
   }
   public void setLeggings(ItemStack is)
   {
	   if(is==null)return;
	   armorIds[3] = is.getTypeId();
	   armorDmg[3] = is.getDurability();
	   
	   Map<Enchantment, Integer> en = is.getEnchantments();
	   if(en.size()>0){
		   legEnch = new int[en.size()];
		   legEnchData = new int[en.size()];
	   
		   Iterator<Enchantment> it = en.keySet().iterator();
		   for(int i = 0; i< en.size(); i++)
		   {
			   Enchantment ent= it.next();
			   legEnch[i] = ent.getId();
			   legEnchData[i] = en.get(ent);
		   } 
	   }
   }
   
   public ItemStack getBoots()
   {
	   if(armorIds[0]>0){		   
		   ItemStack is = new ItemStack(armorIds[0],1,armorDmg[0]);
		   if(bootsEnch != null && bootsEnchData != null)
		   {
			   for(int i = 0; i<bootsEnch.length;i++)
			   {
				   is.addUnsafeEnchantment(Enchantment.getById(bootsEnch[i]), bootsEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public ItemStack getChestplate()
   {
	   if(armorIds[1]>0)
	   {
		   ItemStack is = new ItemStack(armorIds[1],1,armorDmg[1]);
		   if(chestEnch != null && chestEnchData != null)
		   {
			   for(int i = 0; i<chestEnch.length;i++)
			   {
				   is.addUnsafeEnchantment(Enchantment.getById(chestEnch[i]), chestEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public ItemStack getHelmet()
   {
	   if(armorIds[2]>0)
	   {
		   ItemStack is = new ItemStack(armorIds[2],1,armorDmg[2]);
		   if(headEnch != null && headEnchData != null)
		   {
			   for(int i = 0; i<headEnch.length;i++)
			   {
				   is.addUnsafeEnchantment(Enchantment.getById(headEnch[i]), headEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public ItemStack getLeggings()
   {
	   if(armorIds[3]>0)
	   {
		   ItemStack is = new ItemStack(armorIds[3],1,armorDmg[3]);
		   if(legEnch != null && legEnchData != null)
		   {
			   for(int i = 0; i<legEnch.length;i++)
			   {
				   is.addUnsafeEnchantment(Enchantment.getById(legEnch[i]), legEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   
   public void setItem(int index, ItemStack is) {
     if (is != null) {
       this.typeIds[index] = is.getTypeId();
       this.damageIds[index] = is.getDurability();
       this.quantities[index] = is.getAmount();
       
       Map<Enchantment, Integer> en = is.getEnchantments();
	   if(en.size()>0)
	   {	
		   HashMap<Integer, Integer> data = new HashMap<Integer, Integer>();	   
		   Iterator<Enchantment> it = en.keySet().iterator();
		   while(it.hasNext())
		   {
			   Enchantment ent= it.next();
			   data.put(ent.getId(), en.get(ent));			  
		   } 
		   
		   ench.put(index,data);
	   }
       
       
     } else {
       this.typeIds[index] = -1;
     }
   }
 
   public ItemStack getNewStackFrom(int index) {
     if ((this.typeIds[index] == -1) || (this.typeIds[index] == 0)) {
       return null;
     }
     ItemStack is = new ItemStack(this.typeIds[index], this.quantities[index], this.damageIds[index]);
     try{
     if(ench.containsKey(index))
     {
    	 for(Integer id : ench.get(index).keySet())
    	 {
    		 is.addUnsafeEnchantment(Enchantment.getById(id), ench.get(index).get(id));
    	 }
     }
     }catch(Exception ex)
     {
    	 TaxFreeRegion.log.log(Level.WARNING, "Enchantment exception: " + ex.getMessage());
    	 ex.printStackTrace();
     }     
     return is; 
   }
   //CraftItemStack.createNMSItemStack
   public net.minecraft.server.ItemStack getNewVanillaStackFrom(int index) {
	     if ((this.typeIds[index] == -1) || (this.typeIds[index] == 0)) {
	       return null;
	     }
	     net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(this.typeIds[index], this.quantities[index], this.damageIds[index]);
	     try{
	     if(ench.containsKey(index))
	     {
	    	 for(Integer id : ench.get(index).keySet())
	    	 {	    		 
	    		 is.addEnchantment(net.minecraft.server.Enchantment.byId[id], ench.get(index).get(id).intValue());
	    	 }
	     }
	     }catch(Exception ex)
	     {
	    	 TaxFreeRegion.log.log(Level.WARNING, "Enchantment exception: " + ex.getMessage());
	    	 ex.printStackTrace();
	     }
	     return is; 
	   }
 
   public int getSize()
   {
     return this.size;
   }
 
   public short[] getDamageIds() {
     return this.damageIds;
   }
 
   public void setDamageIds(short[] damageIds) {
     this.damageIds = damageIds;
   }
 
   public int[] getQuantities() {
     return this.quantities;
   }
 
   public void setQuantities(int[] quantities) {
     this.quantities = quantities;
   }
 
   public int[] getTypeIds() {
     return this.typeIds;
   }
 
   public void setTypeIds(int[] typeIds) {
     this.typeIds = typeIds;
   }
   
   public net.minecraft.server.ItemStack getVanillaBoots()
   {
	   if(armorIds[0]>0){		   
		   net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(armorIds[0],1,armorDmg[0]);
		   if(bootsEnch != null && bootsEnchData != null)
		   {
			   for(int i = 0; i<bootsEnch.length;i++)
			   {
				   is.addEnchantment(net.minecraft.server.Enchantment.byId[bootsEnch[i]], bootsEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public net.minecraft.server.ItemStack getVanillaChestplate()
   {
	   if(armorIds[1]>0)
	   {
		   net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(armorIds[1],1,armorDmg[1]);
		   if(chestEnch != null && chestEnchData != null)
		   {
			   for(int i = 0; i<chestEnch.length;i++)
			   {
				   is.addEnchantment(net.minecraft.server.Enchantment.byId[chestEnch[i]], chestEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public net.minecraft.server.ItemStack getVanillaHelmet()
   {
	   if(armorIds[2]>0)
	   {
		   net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(armorIds[2],1,armorDmg[2]);
		   if(headEnch != null && headEnchData != null)
		   {
			   for(int i = 0; i<headEnch.length;i++)
			   {
				   is.addEnchantment(net.minecraft.server.Enchantment.byId[headEnch[i]], headEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
   public net.minecraft.server.ItemStack getVanillaLeggings()
   {
	   if(armorIds[3]>0)
	   {
		   net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(armorIds[3],1,armorDmg[3]);
		   if(legEnch != null && legEnchData != null)
		   {
			   for(int i = 0; i<legEnch.length;i++)
			   {
				   is.addEnchantment(net.minecraft.server.Enchantment.byId[legEnch[i]], legEnchData[i]);
			   }
		   }
		   return is;
	   }
	   else return null;
   }
 }
