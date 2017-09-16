package me.gnat008.perworldinventory.events;

import me.gnat008.perworldinventory.data.serializers.DeserializeCause;
import me.gnat008.perworldinventory.groups.Group;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player's new inventory is about to be
 * loaded. If the event is cancelled, the inventory will not
 * be loaded.
 */
public class InventoryLoadEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private Player player;
    private DeserializeCause cause;
    private GameMode newGameMode;
    private Group group;

    public InventoryLoadEvent(Player player, DeserializeCause cause, GameMode newGameMode, Group group) {
        this.player = player;
        this.cause = cause;
        this.newGameMode = newGameMode;
        this.group = group;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the {@link Player}.
     *
     * @return The player
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

    /**
     * Get the {@link GameMode} the player will be in.
     *
     * @return The player's future gamemode.
     */
    public GameMode getNewGameMode() {
        return newGameMode;
    }

    /**
     * Get the {@link Group} that the player is going to.
     *
     * @return The player's new world group.
     */
    public Group getGroup() {
        return group;
    }
}
