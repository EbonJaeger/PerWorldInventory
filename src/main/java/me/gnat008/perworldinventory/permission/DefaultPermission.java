package me.gnat008.perworldinventory.permission;

import org.bukkit.command.CommandSender;

/**
 * Default permissions used when permission nodes are not available.
 */
public enum DefaultPermission {

    NOT_ALLOWED {
        @Override
        public boolean evaluate(CommandSender sender) {
            return false;
        }
    },

    OP_ONLY {
        @Override
        public boolean evaluate(CommandSender sender) {
            return sender.isOp();
        }
    },

    ALLOWED {
        @Override
        public boolean evaluate(CommandSender sender) {
            return true;
        }
    };

    /**
     * Evaluate whether the sender has permission.
     *
     * @param sender The sender to evaluate.
     * @return True if they have permission; false otherwise.
     */
    public abstract boolean evaluate(CommandSender sender);

}
