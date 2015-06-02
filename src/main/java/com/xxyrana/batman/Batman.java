package com.xxyrana.batman;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 
 * @author xxyrana
 *
 */
public class Batman extends JavaPlugin implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");
	private PotionEffectType[] buffs = {PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.JUMP,
			PotionEffectType.REGENERATION,
			PotionEffectType.SPEED,
			PotionEffectType.DAMAGE_RESISTANCE};

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
		if (cmd.getName().equalsIgnoreCase("test")){
			if(sender instanceof Player){
				System.out.println(this.getConfig().getBoolean(((Player) sender).getName().toLowerCase(), false));
			}
		}
		return false; 
	}

	//TODO: Bat spawning (every 5-10 mins)
	//TODO: On sword hit (20% weaker)

	@SuppressWarnings("deprecation")
	//Snowballs do 5 damage if the thrower is Batman
	//Batman's punches do 7 damage and have a 5% chance to stun
	@EventHandler
	public void onEntityDamgeByEntity(EntityDamageByEntityEvent event){
		Entity attacker = event.getDamager();
		//Snowball handler
		if(attacker instanceof Snowball){
			Snowball snowball = (Snowball) event.getDamager();
			Player shooter = (Player) snowball.getShooter();
			if(this.getConfig().getBoolean(shooter.getName().toLowerCase(), false)){
				event.setDamage(5);
			}
		} else if(attacker instanceof Player){
			Player damager = (Player) event.getDamager();
			ItemStack holding = damager.getItemInHand();
			if(this.getConfig().getBoolean(damager.getName().toLowerCase(), false)){
				//Punch handler
				if(holding.getTypeId() == 0){
					event.setDamage(7);
					double random = Math.random();
					if(random<0.05){
						Player hit = (Player) event.getEntity();
						PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 100, 10000000);
						hit.addPotionEffect(effect);
						hit.sendMessage("You have been stunned!");
					}
				}
				//TODO: Add sword handling
			}
		}
	}

	//Fishing rods act as grappling hooks
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		if(event.getEntityType().equals(EntityType.FISHING_HOOK)){
			Player shooter = (Player) event.getEntity().getShooter();
			if(this.getConfig().getBoolean(shooter.getName().toLowerCase(), false)){
				Location tp = event.getEntity().getLocation();
				shooter.teleport(tp);
				event.getEntity().remove();
			}
		}
	}	

	//Updates Batman status in config whenever inventory is closed
	//Updates buffs accordingly and makes armor set unbreakable
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player p =(Player) event.getPlayer();
		if(isBatman(p)){
			this.getConfig().set(p.getName().toLowerCase(), true);
			//Add buffs
			for(int i=0; i<buffs.length; i++){
				p.addPotionEffect(buffs[i].createEffect(1200000, 0));
			}
		} else {
			this.getConfig().set(p.getName().toLowerCase(), false);
			//Remove buffs
			for(int i=0; i<buffs.length; i++){
				if(p.hasPotionEffect(buffs[i])){
					p.removePotionEffect(buffs[i]);
				}
			}
		}
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
			//Make (essentially) unbreakable
			h.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			c.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			l.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			b.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			return true;
		}
		return false;
	}

}
