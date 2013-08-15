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
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SavedInventory implements Serializable {
	private static final long serialVersionUID = -4000934373645689393L;

	private ArrayList<Map<String, Object>> items;
	private Map<String, Object> chest = null;
	private Map<String, Object> boots = null;
	private Map<String, Object> leggins = null;
	private Map<String, Object> head = null;

	public SavedInventory(int size) {
		this.items = new ArrayList<Map<String, Object>>();
		this.items.ensureCapacity(size);
		
		while(this.items.size() < size) this.items.add(null);
	}
	
	public SavedInventory(PlayerInventory inv) {
		this(inv.getSize());
		
		for (int i = 0; i < inv.getSize(); i++) {
			setItem(i, inv.getItem(i));
		}

		setHelmet(inv.getHelmet());
		setChestplate(inv.getChestplate());
		setLeggings(inv.getLeggings());
		setBoots(inv.getBoots());
	}

	public void setBoots(ItemStack is) {
		if (is == null)	return;
		
		boots = is.serialize();
	}

	public void setChestplate(ItemStack is) {
		if (is == null) return;
		
		chest = is.serialize();
	}

	public void setHelmet(ItemStack is) {
		if (is == null)	return;

		head = is.serialize();
	}

	public void setLeggings(ItemStack is) {
		if (is == null)	return;

		leggins = is.serialize();
	}

	public ItemStack getBoots() {
		if (boots != null) {
			return ItemStack.deserialize(boots);
		} else
			return null;
	}

	public ItemStack getChestplate() {
		if (chest != null) {
			return ItemStack.deserialize(chest);
		} else
			return null;
	}

	public ItemStack getHelmet() {
		if (head != null) {
			return ItemStack.deserialize(head);
		} else
			return null;
	}

	public ItemStack getLeggings() {
		if (leggins != null) {
			return ItemStack.deserialize(leggins);
		} else
			return null;
	}

	public void setItem(int index, ItemStack is) {
		if (is != null) {
			items.set(index, is.serialize());
		} else {
			items.set(index, null);
		}
	}

	public ItemStack getNewStackFrom(int index) {
		if (items.get(index) == null) {
			return null;
		}
		else {
			return ItemStack.deserialize(items.get(index));
		}
	}
	
	public int getSize(){
		return items.size();
	}

	public void setInventoryContent(PlayerInventory inv) {
		int imax = Math.min(getSize(), inv.getSize());
		for (int i = 0; i < imax; i++)
			inv.setItem(i, getNewStackFrom(i));

		inv.setBoots(getBoots());
		inv.setChestplate(getChestplate());
		inv.setHelmet(getHelmet());
		inv.setLeggings(getLeggings());
	}
}
