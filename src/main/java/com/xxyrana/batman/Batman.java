package com.xxyrana.batman;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
	private PotionEffect[] superBuffs = {
			PotionEffectType.INCREASE_DAMAGE.createEffect(1200000, 1),
			PotionEffectType.JUMP.createEffect(1200000, 2),
			PotionEffectType.REGENERATION.createEffect(1200000, 1),
			PotionEffectType.SPEED.createEffect(1200000, 3),
			PotionEffectType.DAMAGE_RESISTANCE.createEffect(1200000, 0)
	};
	private PotionEffect[] greenBuffs = {
			PotionEffectType.INCREASE_DAMAGE.createEffect(1200000, 0),
			PotionEffectType.JUMP.createEffect(1200000, 3),
			PotionEffectType.REGENERATION.createEffect(1200000, 0),
			PotionEffectType.SPEED.createEffect(1200000, 0),
			PotionEffectType.DAMAGE_RESISTANCE.createEffect(1200000, 0)
	};
	private PotionEffect[] jokerBuffs = {
			PotionEffectType.INCREASE_DAMAGE.createEffect(1200000, 0),
			PotionEffectType.JUMP.createEffect(1200000, 3),
			PotionEffectType.REGENERATION.createEffect(1200000, 1),
			PotionEffectType.SPEED.createEffect(1200000, 3),
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
	//TODO: Superman flying
	//TODO: Green Lantern can fly while holding emeralds
	//TODO: Green Lantern has 5 more blocks of attack range
	//TODO: Green Lantern has weakness 2 when low on health (2 hearts)
	//TODO: Kryptonite - Nearby = slow 50, blind 50, weak 10, poison 3; in inventory = death


	//-------------------//
	//-------------------//
	//                   //
	//   DAMAGE EVENTS   //
	//                   //
	//-------------------//
	//-------------------//

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		Entity attacker = event.getDamager();
		if(attacker instanceof Snowball){ //Snowball has been thrown
			//---------------------------//
			//   WEAPONIZED: SNOWBALLS   //
			//---------------------------//
			//Snowballs do 5 damage if the thrower is Batman
			Snowball snowball = (Snowball) event.getDamager();
			Player shooter = (Player) snowball.getShooter();
			if(getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
				event.setDamage(5);
			} else {
				event.setCancelled(true);
			}
		} else if(attacker instanceof Egg){ //Egg has been thrown
			//----------------------//
			//   WEAPONIZED: EGGS   //
			//----------------------//
			//Eggs do 5 damage if the thrower is Nightwing
			Egg egg = (Egg) event.getDamager();
			Player shooter = (Player) egg.getShooter();
			if(getConfig().getString(shooter.getName().toLowerCase(), "none").equalsIgnoreCase("nightwing")){
				event.setDamage(5);
			}
		} else if(attacker instanceof Player){

			//----------------------//
			//   ATTACKER: BATMAN   //
			//----------------------//
			//Attacker is Batman
			//TODO: Batman attacking Superman = +20% damage
			if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				//Punch handler
				//Batman's punches do 7 damage and have a 5% chance to stun
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
				} //end sword handler
			} //end attacker is Batman

			//-------------------------//
			//   ATTACKER: NIGHTWING   //
			//-------------------------//
			//Attacker is Nightwing
			else if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("nightwing")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				Material inhand = holding.getType();
				if(inhand.equals(Material.BLAZE_ROD)){
					event.setDamage(9);
				}
			} //end attacker is Nightwing

			//------------------------//
			//   ATTACKER: SUPERMAN   //
			//------------------------//
			//Attacker is Superman
			else if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("superman")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				Material inhand = holding.getType();
				//TODO: Superman can explode things 20 blocks away but takes away 2 hearts (heat vision)
				//TODO: Superman - each hit slows mobs
				//TODO: All weapons 60% weaker
			} //end attacker is Superman

			//-----------------------------//
			//   ATTACKER: GREEN LANTERN   //
			//-----------------------------//
			//Attacker is Green Lantern
			else if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("greenlantern")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				Material inhand = holding.getType();
				//TODO: Green lantern can shoot green particles from emeralds (consuming them) to do 7 damage
			} //end attacker is Green Lantern

			//-------------------------//
			//   ATTACKER: THE JOKER   //
			//-------------------------//
			//Attacker is The Joker
			else if(getConfig().getString(((Player) event.getDamager()).getName().toLowerCase(), "none").equalsIgnoreCase("thejoker")){
				Player damager = (Player) event.getDamager();
				ItemStack holding = damager.getItemInHand();
				Material inhand = holding.getType();
				//TODO: All weapons do 60% more damage
				//TODO: Guns in Downtown do 20% more damage
				//TODO: Fists do 5 damage
				//TODO: Attacking something gives speed 3 for 5 seconds
			} //end attacker is The Joker

			Entity damaged = event.getEntity();
			if(damaged instanceof Player){
				
				//---------------------//
				//   DAMAGED: BATMAN   //
				//---------------------//
				//Batman has taken damage
				if(getConfig().getString(((Player) damaged).getName().toLowerCase(), "none").equalsIgnoreCase("batman")){
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
				
				//------------------------//
				//   DAMAGED: THE JOKER   //
				//------------------------//
				//The Joker has taken damage
				if(getConfig().getString(((Player) damaged).getName().toLowerCase(), "none").equalsIgnoreCase("thejoker")){
					//TODO: Check to make sure the damage was > 5 times in a row
					//TODO: Spawn in 3 Zombies and 3 Skeletons as The Joker's henchmen
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
				} //end The Joker has taken damage
				
			}
		}

	}


	//----------------------//
	//----------------------//
	//                      //
	//   MISC HERO EVENTS   //
	//                      //
	//----------------------//
	//----------------------//

	//Fishing rods act as grappling hooks
	//Applies to: Batman, Nightwing
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


	//-------------------------//
	//-------------------------//
	//                         //
	//   HERO UPDATING EVENT   //
	//                         //
	//-------------------------//
	//-------------------------//

	//Updates hero status in config whenever inventory is closed
	//Updates buffs accordingly and makes armor set unbreakable
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player p =(Player) event.getPlayer();
		//Get outfit
		PlayerInventory inv = p.getInventory();
		ItemStack h = inv.getHelmet();
		ItemStack c = inv.getChestplate();
		ItemStack l = inv.getLeggings();
		ItemStack b = inv.getBoots();

		//Initial check to save time
		if (h == null || c == null || l == null || b == null){
			//Remove hero buffs
			if(!getConfig().getString(p.getName().toLowerCase()).equalsIgnoreCase("none")){
				//Remove buffs
				for (PotionEffect effect : p.getActivePotionEffects()){
					p.removePotionEffect(effect.getType());
				}
			}
			getConfig().set(p.getName().toLowerCase(), "none");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			saveConfig();
			return;
		}

		//Hero checks
		if(isBatman(h, c, l, b)){
			getConfig().set(p.getName().toLowerCase(), "batman");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			//Add buffs
			for(int i=0; i<batBuffs.length; i++){
				p.addPotionEffect(batBuffs[i]);
			}
		} else if(isNightwing(h, c, l, b)){
			getConfig().set(p.getName().toLowerCase(), "nightwing");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			//Add buffs
			for(int i=0; i<nightBuffs.length; i++){
				p.addPotionEffect(nightBuffs[i]);
			}
		} else if(isSuperman(h, c, l, b)){
			getConfig().set(p.getName().toLowerCase(), "superman");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			//Add buffs
			for(int i=0; i<superBuffs.length; i++){
				p.addPotionEffect(superBuffs[i]);
			}
		} else if(isGreenlantern(h, c, l, b)){
			getConfig().set(p.getName().toLowerCase(), "greenlantern");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			//Add buffs
			for(int i=0; i<greenBuffs.length; i++){
				p.addPotionEffect(greenBuffs[i]);
			}
		} else if(isThejoker(h, c, l, b)){
			getConfig().set(p.getName().toLowerCase(), "thejoker");
			getConfig().saveToString();
			//Remove buffs
			for (PotionEffect effect : p.getActivePotionEffects()){
				p.removePotionEffect(effect.getType());
			}
			//Add buffs
			for(int i=0; i<jokerBuffs.length; i++){
				p.addPotionEffect(jokerBuffs[i]);
			}
		} else {
			//Remove hero buffs
			if(!getConfig().getString(p.getName().toLowerCase()).equalsIgnoreCase("none")){
				//Remove buffs
				for (PotionEffect effect : p.getActivePotionEffects()){
					p.removePotionEffect(effect.getType());
				}
			}
			getConfig().set(p.getName().toLowerCase(), "none");
			getConfig().saveToString();
			saveConfig();
			return;
		}
		saveConfig();
		//Make armor (essentially) unbreakable
		h.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		c.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		l.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		b.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
	}


	//-------------------//
	//-------------------//
	//                   //
	//   OUTFIT CHECKS   //
	//                   //
	//-------------------//
	//-------------------//

	//--------------------//
	//   OUTFIT: BATMAN   //
	//--------------------//
	boolean isBatman(ItemStack h, ItemStack c, ItemStack l, ItemStack b){
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("redrocketjj")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 1644825 //ink sac
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 1644825
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 1644825){
			return true;
		}
		return false;
	}

	//-----------------------//
	//   OUTFIT: NIGHTWING   //
	//-----------------------//
	boolean isNightwing(ItemStack h, ItemStack c, ItemStack l, ItemStack b){
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("j3loodking")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 3361970 //lapis
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 3361970
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 3361970){
			return true;
		}
		return false;
	}

	//----------------------//
	//   OUTFIT: SUPERMAN   //
	//----------------------//
	boolean isSuperman(ItemStack h, ItemStack c, ItemStack l, ItemStack b){
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("superman")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 3361970 //lapis
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 10040115 //rosered
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 10040115){
			return true;
		}
		return false;
	}

	//--------------------------//
	//   OUTFIT: GREENLANTERN   //
	//--------------------------//
	boolean isGreenlantern(ItemStack h, ItemStack c, ItemStack l, ItemStack b){
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("greenlantern")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 8375321 //lime
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 8375321
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 8375321){
			return true;
		}
		return false;
	}

	//----------------------//
	//   OUTFIT: THEJOKER   //
	//----------------------//
	boolean isThejoker(ItemStack h, ItemStack c, ItemStack l, ItemStack b){
		if(h.getType().equals(Material.SKULL_ITEM)
				&& h.getItemMeta().getDisplayName().substring(11).equalsIgnoreCase("the_killing_joke")
				&& c.getType().equals(Material.LEATHER_CHESTPLATE)
				&& l.getType().equals(Material.LEATHER_LEGGINGS)
				&& b.getType().equals(Material.LEATHER_BOOTS)
				&& ((LeatherArmorMeta) c.getItemMeta()).getColor().asRGB() == 8339378 //purple
				&& ((LeatherArmorMeta) l.getItemMeta()).getColor().asRGB() == 8339378
				&& ((LeatherArmorMeta) b.getItemMeta()).getColor().asRGB() == 8339378){
			return true;
		}
		return false;
	}

	public void loadConfiguration(){
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

}
