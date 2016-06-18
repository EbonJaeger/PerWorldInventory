package me.gnat008.perworldinventory.permission;

public enum PermissionSystem {

    B_PERMISSIONS("bPermissions", "bPermissions"),

    ESSENTIALS_GROUP_MANAGER("Essentials Group Manager", "GroupManager"),

    PERMISSIONS_BUKKIT("Permissions Bukkit", "PermissionsBukkit"),

    PERMISSIONS_EX("PermissionsEx", "PermissionsEx"),

    VAULT("Vault", "Vault"),

    Z_PERMISSIONS("zPermissions", "zPermissions");

    private String name;
    private String pluginName;

    PermissionSystem(String name, String pluginName) {
        this.name = pluginName;
        this.pluginName = pluginName;
    }

    /**
     * Get the display name of the permissions system.
     *
     * @return Display name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the server name.
     *
     * @return Plugin name.
     */
    public String getPluginName() {
        return this.pluginName;
    }

    /**
     * Cast the permissions system type to a string.
     *
     * @return The display name of the permissions system.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Check if a given server is a permissions system.
     *
     * @param name The name of the server to check.
     * @return If the server is a valid permissions system.
     */
    public static boolean isPermissionSystem(String name) {
        for (PermissionSystem permissionsSystemType : values()) {
            if (permissionsSystemType.pluginName.equals(name)) {
                return true;
            }
        }

        return false;
    }
}
