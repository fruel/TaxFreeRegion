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

import java.io.Serializable;
import java.util.HashMap;

import at.lukasf.taxfreeregion.inventory.SavedInventory;
import at.lukasf.taxfreeregion.region.PlayerRegion;

public class SerializedHashMaps implements Serializable{

	private static final long serialVersionUID = -829296935480224825L;
	
	public HashMap<PlayerRegion, SavedInventory> inventories = new HashMap<PlayerRegion, SavedInventory>();
	public HashMap<PlayerRegion, Double> healthValues = new HashMap<PlayerRegion, Double>();
	public HashMap<PlayerRegion, IntFloat> xpValues = new HashMap<PlayerRegion, IntFloat>();
	public HashMap<PlayerRegion, IntFloat> hungerValues = new HashMap<PlayerRegion, IntFloat>();
	
	public SerializedHashMaps(HashMap<PlayerRegion, SavedInventory> inventories,
			HashMap<PlayerRegion, Double> healthValues,
			HashMap<PlayerRegion, IntFloat> xpValues,
			HashMap<PlayerRegion, IntFloat> hungerValues) {
		this.inventories = inventories;
		this.healthValues = healthValues;
		this.xpValues = xpValues;
		this.hungerValues = hungerValues;
	}

}
