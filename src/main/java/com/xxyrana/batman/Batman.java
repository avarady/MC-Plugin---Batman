package com.xxyrana.batman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

/**
 * 
 * @author xxyrana
 *
 */
public class Batman extends JavaPlugin implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");
	private PotionEffect[] batBuffs = {
			PotionEffectType.INCREASE_DAMAGE.createEffect(1200000, 0),
			PotionEffectType.JUMP.createEffect(1200000, 2),
			PotionEffectType.REGENERATION.createEffect(1200000, 0),
			PotionEffectType.SPEED.createEffect(1200000, 2),
			PotionEffectType.DAMAGE_RESISTANCE.createEffect(1200000, 0),
			PotionEffectType.NIGHT_VISION.createEffect(1200000, 0)
			};
	private PotionEffect[] nightBuffs = {
			PotionEffectType.JUMP.createEffect(1200000, 2),
			PotionEffectType.REGENERATION.createEffect(1200000, 0),
			PotionEffectType.SPEED.createEffect(1200000, 2),
			PotionEffectType.DAMAGE_RESISTANCE.createEffect(1200000, 0)
			};

	@Override
	public void onEnable() {
		loadConfiguration();
		getServer().getPluginManager().registerEvents(this, this);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				//Bat spawning
				Player[] list = Bukkit.getOnlinePlayers();
				for(Player p : list){
					if(getConfig().getString(p.getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
						World world = p.getWorld();
						Location location = p.getLocation();
						Entity bat;
						for(int i=0; i<5; i++){
							bat = world.spawnCreature(location, EntityType.BAT);
							((LivingEntity) bat).setRemoveWhenFarAway(true);
						}
					}
				}
			}
		}, 0L, 12000L); //end scheduler
	}

	@Override
	public void onDisable() {
		saveConfig();
		log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}

	//TODO: Night and Bat can climb walls

	//Snowballs do 5 damage if the thrower is Batman
	//Batman's punches do 7 damage and have a 5% chance to stun
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		Entity attacker = event.getDamager();
		//Snowball handler
		if(attacker instanceof Snowball){
			Snowball snowball = (Snowball) event.getDamager();
			Player shooter = (Player) snowball.getShooter();
			if(getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
				event.setDamage(5);
			} else {
				event.setCancelled(true);
			}
		} else if(attacker instanceof Egg){
			Egg egg = (Egg) event.getDamager();
			Player shooter = (Player) egg.getShooter();
			if(getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("nightwing")){
				event.setDamage(5);
			}
		} else if(attacker instanceof Player){
			//Attacker is Batman
			if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				//Punch handler
				if(holding.getTypeId() == 0){
					event.setDamage(7);
					if(event.getEntity() instanceof Player){
						double random = Math.random();
						if(random<0.05){
							Player hit = (Player) event.getEntity();
							PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 100, 10000000);
							hit.addPotionEffect(effect);
							hit.sendMessage("You have been stunned!");
						}
					}
				} else { //Sword handler
					Material inhand = holding.getType();
					int damage = 0;
					if(inhand.equals(Material.DIAMOND_SWORD)){
						damage = 6;
					} else if(inhand.equals(Material.IRON_SWORD)){
						damage = 5;
					} else if(inhand.equals(Material.WOOD_SWORD)){
						damage = 3;
					} else if(inhand.equals(Material.STONE_SWORD)){
						damage = 4;
					} else if(inhand.equals(Material.GOLD_SWORD)){
						damage = 3;
					} else {
						return;
					}
					event.setDamage(damage);
				}
			
			} //end attacker is Batman
			//Attacker is Nightwing
			else if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("nightwing")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				Material inhand = holding.getType();
				if(inhand.equals(Material.BLAZE_ROD)){
					event.setDamage(9);
				}
			} //end attacker is Nightwing

			//Batman has taken damage
			Entity damaged = event.getEntity();
			if(damaged instanceof Player
					&& getConfig().getString(((Player) damaged).getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
				List<Entity> nearby = damaged.getNearbyEntities(15, 15, 15);
				//Find all bats nearby
				for(Entity e : nearby){
					if(e instanceof Bat){
						//Move bats to attacker
						Location bloc = e.getLocation();
						Location ploc = attacker.getLocation();
						Vector delta = ploc.toVector().subtract(bloc.toVector());
						e.setVelocity(delta);
						//Damage the attacker
						((Damageable) attacker).damage(1);
					}
				}
			} //end Batman has taken damage

		}

	}

	//Fishing rods act as grappling hooks
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		if(event.getEntityType().equals(EntityType.FISHING_HOOK)){
			Player shooter = (Player) event.getEntity().getShooter();
			if(getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("batman")
					|| getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("nightwing")){
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
			getConfig().set(p.getName().toLowerCase(), "batman");
			getConfig().saveToString();
			//Remove buffs
			for(int i=0; i<nightBuffs.length; i++){
				if(p.hasPotionEffect(batBuffs[i].getType())){
					p.removePotionEffect(batBuffs[i].getType());
				}
			}
			//Add buffs
			for(int i=0; i<batBuffs.length; i++){
				p.addPotionEffect(batBuffs[i]);
			}
		} else if(isNightwing(p)){
			getConfig().set(p.getName().toLowerCase(), "nightwing");
			getConfig().saveToString();
			//Remove buffs
			for(int i=0; i<batBuffs.length; i++){
				if(p.hasPotionEffect(batBuffs[i].getType())){
					p.removePotionEffect(batBuffs[i].getType());
				}
			}
			//Add buffs
			for(int i=0; i<nightBuffs.length; i++){
				p.addPotionEffect(nightBuffs[i]);
			}
		} else {
			getConfig().set(p.getName().toLowerCase(), "none");
			getConfig().saveToString();
			//Remove buffs
			for(int i=0; i<batBuffs.length; i++){
				if(p.hasPotionEffect(batBuffs[i].getType())){
					p.removePotionEffect(batBuffs[i].getType());
				}
			}
			for(int i=0; i<nightBuffs.length; i++){
				if(p.hasPotionEffect(batBuffs[i].getType())){
					p.removePotionEffect(batBuffs[i].getType());
				}
			}
		}
		saveConfig();
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

	//Checks outfit to see if player is Nightwing
	boolean isNightwing(Player p){
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
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("j3loodking")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 3361970
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 3361970
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 3361970){
			//Make (essentially) unbreakable
			h.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			c.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			l.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			b.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			return true;
		}
		return false;
	}

	public void loadConfiguration(){
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

}
