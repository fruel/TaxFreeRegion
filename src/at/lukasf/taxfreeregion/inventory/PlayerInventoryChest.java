/*
 * Copyright (C) 2011 lishid.  All rights reserved.
 * Modified by lukasf.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.lukasf.taxfreeregion.inventory;
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;


import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;

public class PlayerInventoryChest implements IInventory
{
    public Player Opener;
    String player;
    public Player Target;
    private ItemStack[] items = new ItemStack[36];
    private ItemStack[] armor = new ItemStack[4];
    private ItemStack[] extra = new ItemStack[5];

    public PlayerInventoryChest(SavedInventory inventory, String name)
    {
        player = name;
        
        for (int i = 0; i < inventory.getSize(); i++)
        	items[i] =  inventory.getNewVanillaStackFrom(i);
        
        armor[3] = inventory.getVanillaHelmet();
        armor[2] = inventory.getVanillaChestplate();
        armor[1] = inventory.getVanillaLeggings();
        armor[0] = inventory.getVanillaBoots();
        
        //this.items = inventory.items;
        //this.armor = inventory.armor;
    }

    public ItemStack[] getContents()
    {
        ItemStack[] C = new ItemStack[getSize()];
        System.arraycopy(items, 0, C, 0, items.length);
        System.arraycopy(items, 0, C, items.length, armor.length);
        return C;
    }

    public int getSize()
    {
        return 45;
    }

    public ItemStack getItem(int i)
    {
        ItemStack[] is = this.items;

        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if(is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }

        return is[i];
    }

    public ItemStack splitStack(int i, int j)
    {
        ItemStack[] is = this.items;

        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if(is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }

        if (is[i] != null)
        {
            ItemStack itemstack;

            if (is[i].count <= j)
            {
                itemstack = is[i];
                is[i] = null;
                return itemstack;
            }
            else
            {
                itemstack = is[i].a(j);
                if (is[i].count == 0)
                {
                    is[i] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    public void setItem(int i, ItemStack itemstack)
    {
        ItemStack[] is = this.items;

        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if(is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        //Effects
        if(is == this.extra)
        {
        	if(i == 0)
        	{
        		itemstack.setData(0);
        	}
        }

        is[i] = itemstack;
    }

    private int getReversedItemSlotNum(int i)
    {
        if (i >= 27) return i - 27;
        else return i + 9;
    }

    private int getReversedArmorSlotNum(int i)
    {
        if (i == 0) return 3;
        if (i == 1) return 2;
        if (i == 2) return 1;
        if (i == 3) return 0;
        else return i;
    }

    public String getName()
    {
        if (player.length() > 16) return player.substring(0, 16);
        return player;
    }

    public int getMaxStackSize()
    {
        return 64;
    }

    public boolean a(EntityHuman entityhuman)
    {
        return true;
    }

    public void f() {}

    public void g() {}

    public void update()  { }

	@Override
	public InventoryHolder getOwner() {
		return null;
	}

	@Override
	public List<HumanEntity> getViewers() {
		return null;
	}

	@Override
	public void onClose(CraftHumanEntity arg0) {}

	@Override
	public void onOpen(CraftHumanEntity arg0) {}

	@Override
	public ItemStack splitWithoutUpdate(int arg0) {
		return null;
	}

	@Override
	public void setMaxStackSize(int arg0) {}

	@Override
	public void startOpen() {}

	@Override
	public boolean a_(EntityHuman arg0) {
		return false;
	}
}