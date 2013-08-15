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

public class Command {
	private String command;
	private boolean consoleSender = true;
	private int delay = 0;

	public Command(String command, boolean consoleSender, int delay) {
		this.command = command;
		this.consoleSender = consoleSender;
		this.delay = delay;
	}

	public Command(String command, boolean consoleSender) {
		this(command, consoleSender, 0);
	}

	public String getCommand() {
		return command;
	}

	public boolean isConsoleSender() {
		return consoleSender;
	}

	public int getDelay() {
		return delay;
	}
}