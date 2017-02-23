package me.gnat008.perworldinventory;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import javax.inject.Inject;

/**
 * Service for scheduling things with the Bukkit API.
 */
public class BukkitService {

    /** Number of ticks per second in the Bukkit main thread. */
    public static final int TICKS_PER_SECOND = 20;
    /** Number of ticks per minutes. */
    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;

    private final PerWorldInventory plugin;

    @Inject
    BukkitService(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs a task on the next server tick and returns the task.
     *
     * @param task The task to run.
     * @return A BukkitTask with the ID number.
     */
    public BukkitTask runTask(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Runs a task after a delay in ticks.
     *
     * @param task The task to run.
     * @param delay The number of ticks before the task is run for the first time.
     * @return A BukkitTask with the ID number.
     */
    public BukkitTask runTaskLater(Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    /**
     * Schedules the given task to repeatedly run until cancelled, starting after the
     * specified number of server ticks.
     *
     * @param task The task to run.
     * @param delay The number of ticks before the task is run for the first time.
     * @param period The number of ticks between each time the task is run.
     * @return A BukkitTask with the ID number.
     */
    public BukkitTask runRepeatingTask(Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    /**
     * Runs a task on the next server tick, either asynchronously or synchronously.
     *
     * @param task The task to run.
     * @param async If the task should be run asynchronously or not.
     * @return A BukkitTask with the ID number.
     */
    public BukkitTask runTaskOptionallyAsync(Runnable task, boolean async) {
        if (async) {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a task to be run asynchronously.
     *
     * @param task The task to run.
     * @return A BukkitTask with the ID number.
     */
    public BukkitTask runTaskAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
