package me.gnat008.perworldinventory.permission;

import org.bukkit.command.CommandSender;

/**
 * Default permissions used when permission nodes are not available.
 */
public enum DefaultPermission {

    NOT_ALLOWED("No permission") {
        @Override
        public boolean evaluate(CommandSender sender) {
            return false;
        }
    },

    OP_ONLY("Operators only") {
        @Override
        public boolean evaluate(CommandSender sender) {
            return sender.isOp();
        }
    },

    ALLOWED("Everyone allowed") {
        @Override
        public boolean evaluate(CommandSender sender) {
            return true;
        }
    };

    private final String title;

    DefaultPermission(String title) {
        this.title = title;
    }

    /**
     * Evaluate whether the sender has permission.
     *
     * @param sender The sender to evaluate.
     * @return True if they have permission; false otherwise.
     */
    public abstract boolean evaluate(CommandSender sender);

    /**
     * Return the textual representation.
     *
     * @return The textual representation of the default permission.
     */
    public String getTitle() {
        return title;
    }
}
