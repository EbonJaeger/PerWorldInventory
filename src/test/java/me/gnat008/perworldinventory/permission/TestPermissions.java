package me.gnat008.perworldinventory.permission;

public enum TestPermissions implements PermissionNode {

    HELP("perworldinventory.help", DefaultPermission.ALLOWED),

    CONVERT("perworldinventory.convert", DefaultPermission.OP_ONLY),

    SYSTEM_LORD("goa'uld.systemlord", DefaultPermission.NOT_ALLOWED);

    private String node;
    private DefaultPermission defaultPermission;

    TestPermissions(String node, DefaultPermission defaultPermission) {
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
