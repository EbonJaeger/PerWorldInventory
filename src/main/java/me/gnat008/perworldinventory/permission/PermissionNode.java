package me.gnat008.perworldinventory.permission;

public interface PermissionNode {

    /**
     * Return the node of the permission.
     * For example, 'perworldinventory.reload'.
     *
     * @return The name of the permission node.
     */
    String getNode();

    /**
     * Return the default permission for this node if no permission server is
     * available.
     *
     * @return The default level of permission.
     */
    DefaultPermission getDefaultPermission();
}
