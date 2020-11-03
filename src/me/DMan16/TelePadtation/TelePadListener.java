package me.DMan16.TelePadtation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;

public class TelePadListener implements Listener {
	private static TelePadsManager manager = TelePadtation.TelePadsManager;
	
	@EventHandler(ignoreCancelled = true)
	void onPlaceEvent(BlockPlaceEvent event) {
		org.bukkit.Location origin = event.getBlock().getLocation();
		Location location = new Location(origin);
		if (checkTelePad(location) && !Tag.CARPETS.isTagged(event.getBlock().getType())) {
			event.setCancelled(true);
			return;
		}
		ItemStack item = event.getItemInHand();
		TelePad telePad = new TelePad(event.getPlayer().getUniqueId().toString(),item);
		if (telePad.ownerID() == null) return;
		Block block;
		try {
			block = origin.getWorld().getBlockAt(origin.clone().add(0,1,0));
			if (block != null && !block.getType().isAir()) {
				event.setCancelled(true);
				return;
			}
		} catch (Exception e) {}
		try {
			block = origin.getWorld().getBlockAt(origin.clone().add(0,2,0));
			if (block != null && !block.getType().isAir()) {
				event.setCancelled(true);
				return;
			}
		} catch (Exception e) {}
		int amount = manager.getPrivate(event.getPlayer().getUniqueId().toString()).size() + 1;
		int limit = limit(event.getPlayer());
		if (amount > limit) {
			event.setCancelled(true);
			Utils.chatColors(event.getPlayer(),"&bTele&6Pad &climit reached!");
		} else {
			manager.add(event.getPlayer().getUniqueId(),location,telePad);
			if (manager.getPrivate(event.getPlayer().getUniqueId().toString()).size() == amount)
				Utils.chatColors(event.getPlayer(),"&bTele&6Pad &ecreated! &e" + amount + "&e/&o&c" + limit);
			else event.setCancelled(true);
		}
	}
	
	@EventHandler
	void onInteractEvent1(PlayerInteractEvent event) {
		if (event.hasItem() && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && !event.getPlayer().isSneaking()) {
			if (Utils.compareItems(event.getItem(),Recipes.recipePocketTelePad().getResult()) && !Utils.isInteractable(event.getClickedBlock()) &&
					!manager.TelePadsSingleUse.containsKey(event.getPlayer().getUniqueId().toString())) {
				event.setCancelled(true);
				manager.TelePadsSingleUse.put(event.getPlayer().getUniqueId().toString(), new TelePad(event.getPlayer().getUniqueId().toString(),1,0,0,false));
				new Menu(event.getPlayer(),(Location) null);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onInteractEvent2(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.END_PORTAL_FRAME) return;
		TelePad telePad = manager.get(event.getClickedBlock().getLocation());
		if (telePad == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking()) return;
		event.setCancelled(true);
		if (telePad.ownerID().equals(event.getPlayer().getUniqueId().toString()) || telePad.global())
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(TelePadtation.getMain(), new Runnable() {
				@Override
				public void run() {
					new Menu(event.getPlayer(),event.getClickedBlock().getLocation());
				}
			},3);
	}
	
	@EventHandler(ignoreCancelled = true)
	void onWorldSave(WorldSaveEvent event) {
		manager.write(event.getWorld());
	}
	
	@EventHandler(ignoreCancelled = true)
	void onBlockBreak(BlockBreakEvent event) {
		if (manager.get(event.getBlock().getLocation()) != null) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	void onBlockForm(BlockFormEvent event) {
		if (manager.get(event.getBlock().getLocation()) != null || checkTelePad(event.getBlock().getLocation())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	void onBlockFormTo(BlockFromToEvent event) {
		if (manager.get(event.getToBlock().getLocation()) != null || checkTelePad(event.getToBlock().getLocation())) event.setCancelled(true);
	}
	
	private static boolean checkTelePad(Location location) {
		return (manager.get(location.add(0,-1,0)) != null || manager.get(location.add(0,-2,0)) != null);
	}
	
	public static boolean checkTelePad(org.bukkit.Location location) {
		return checkTelePad(new Location(location));
	}
	
	private int limit(Player player) {
		int limit = TelePadtation.getConfigLoader().getBaseLimit();
		for (String perm : TelePadtation.getConfigLoader().getPerms()) 
			if (perm != null && !perm.isEmpty() && player.hasPermission(perm)) limit += TelePadtation.getConfigLoader().getPermLimit();
		return limit;
	}
}