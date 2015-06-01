package com.xxyrana.batman;

import java.util.logging.Logger;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author xxyrana
 *
 */
public class Batman extends JavaPlugin implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("test")) { // If the player typed /basic then do the following...
			if(sender instanceof Player){
				System.out.println(this.getConfig().getBoolean(((Player) sender).getName().toLowerCase(), true));
			}
		}
		return false; 
	}
	
	//TODO: Snowball throw event (5 dmg), fishing rod throw event (grappling hook)
	
	//TODO: Bat spawning (every 5-10 mins)
	
	//TODO: On punch (7 dmg + 5% chance to stun)
	
	//TODO: On sword hit (20% weaker)
	
	
	//Updates Batman status in config whenever inventory is closed
	//Updates buffs accordingly and makes armor set unbreakable
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player p =(Player) event.getPlayer();
		if(isBatman(p)){
			this.getConfig().set(p.getName().toLowerCase(), true);
		} else {
			this.getConfig().set(p.getName().toLowerCase(), false);
		}
		
		//TODO: Buffs
		
		//TODO: Unbreakable
	}
	
	//Checks outfit to see if player is Batman
	boolean isBatman(Player p){
		//Get outfit
		PlayerInventory inv = p.getInventory();
		ItemStack h = inv.getHelmet();
		ItemStack c = inv.getChestplate();
		ItemStack l = inv.getLeggings();
		ItemStack b = inv.getBoots();
		if (h == null || c == null || l == null || b == null){
			return false;
		}

		//Check outfit
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("redrocketjj")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 1644825
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 1644825
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 1644825){
			return true;
		}
		return false;
	}
	
}
