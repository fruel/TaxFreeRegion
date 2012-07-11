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
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import org.bukkit.ChatColor;
 
 public class Messages
 {
   private Properties msgs;
 
   public static String replaceColor(String input)
   {
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
 
   public static String setField(String input, String field, String value) {
     return input.replaceAll(field, value);
   }
 
   public Messages() {
     this.msgs = new Properties();
   }
 
   public void loadMessages(File propertyFile) throws IOException
   {
     this.msgs.load(new FileInputStream(propertyFile));
 
     Iterator<Map.Entry<Object, Object>> it = this.msgs.entrySet().iterator();
     while (it.hasNext()) {
       Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>)it.next();
 
       entry.setValue(replaceColor((String)entry.getValue()));
     }
   }
 
   public String getMessage(String key) {
     if (this.msgs == null) {
       return null;
     }
     return this.msgs.getProperty(key, key + " message not set");
   }
 }