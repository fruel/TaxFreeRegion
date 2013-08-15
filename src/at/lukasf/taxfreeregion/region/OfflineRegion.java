package at.lukasf.taxfreeregion.region;

import java.io.Serializable;

import at.lukasf.taxfreeregion.inventory.SavedInventory;

public class OfflineRegion implements Serializable {

	private static final long serialVersionUID = -4305050441345939305L;

	SavedInventory inventory;
	String region;

	public OfflineRegion(String region, SavedInventory inv) {
		this.inventory = inv;
		this.region = region;
	}

	public SavedInventory getInventory() {
		return inventory;
	}

	public String getRegion() {
		return region;
	}
}
