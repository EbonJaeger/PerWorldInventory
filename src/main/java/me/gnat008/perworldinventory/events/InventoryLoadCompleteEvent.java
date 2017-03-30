package me.gnat008.perworldinventory.events;

import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event to be called when a player's inventory and stats are fully
 * loaded and set to the player.
 */
public class InventoryLoadCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private Player player;
    private DeserializeCause cause;

    public InventoryLoadCompleteEvent(Player player, DeserializeCause cause) {
        this.player = player;
        this.cause = cause;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the player in this event.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link DeserializeCause} of this event.
     *
     * @return The cause of the event.
     */
    public DeserializeCause getCause() {
        return cause;
    }
}
