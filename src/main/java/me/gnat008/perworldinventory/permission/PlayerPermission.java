package me.gnat008.perworldinventory.permission;

public enum PlayerPermission implements PermissionNode {

    BYPASS_WORLDS("perworldinventory.bypass.world", DefaultPermission.NOT_ALLOWED),

    BYPASS_GAMEMODE("perworldinventory.bypass.gamemode", DefaultPermission.NOT_ALLOWED),
    
    BYPASS_ENFORCEGAMEMODE("perworldinventory.bypass.enforcegamemode", DefaultPermission.NOT_ALLOWED);

    private String node;
    private DefaultPermission defaultPermission;

    PlayerPermission(String node, DefaultPermission defaultPermission) {
        this.node = node;
        this.defaultPermission = defaultPermission;
    }

    @Override
    public String getNode() {
        return this.node;
    }

    @Override
    public DefaultPermission getDefaultPermission() {
        return this.defaultPermission;
    }
}
