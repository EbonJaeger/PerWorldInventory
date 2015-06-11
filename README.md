PerWorldInventory
=================
Made by Gnat008, for Spigot 1.8.
Version: 1.5.1

Introduction
=================
I wrote this plugin not long before name changing went live, when I realized that MultiVerse-Inventories had not been updated to use UUID's. We've been using this on our server for a few months now, and I decided to share it with all of you as well. This is also why the first version on here is 1.1.0.

Description
=================
The way this plugin stores data is fundamentally different from MultiVerse-Inventories. Like MV-I, this plugin saves data in a .json format, but that is where the similarities end. Not only does this save your inventory, it saves your stats as well! This includes things like active potion effects, exp, level, and more. These all can be configured on a per-item basis, should you not want all of these saved. See the Configuration section below for these. All commands can be seen ingame with the '/pwi help' command.

Installation
=================
Like all plugins, drop the .jar in your plugins folder, and restart the server to generate the necessary files.

Configuration
=================
Pretty straightforward, turning the options to false will cause that item to not be saved. The option 'manage-gamemodes' is set to false by default to not interfere with other plugins that you may have to manage GameModes.
NOTE: If you change 'first-start' to 'true', all of your configurations will be reset to default! This setting is not meant to be changed, and exists for certain things during the first start.


This is where you configure your world groups. Follow the default or above example to get the format. If you have 'manage-gamemodes' set to false in your main config, the 'default-gamemode' option here does nothing, and can be safely removed. Worlds in the same group will all share the same inventory and stats.
Once you have made your desired changes, simply type /pwi reload.

A default inventory loadout is provided for each configured group, as well as for a server fallback, if for some reason a world isn't in any group. These can be set ingame by using the '/pwi setworlddefault [group]' command, where group is a configured group name, or 'default'. If no group is specified, it will set the loadout for the group that you are currently standing in. This does not require a reload.

Conversion
=================
If you are converting from MV-I, simply run the "/pwi convert multiverse" command and the plugin will take care of everything. You must have MV-I running for this. Keep in mind, converting only works with MV-I version 2.5, it will not work on 2.4. When MV-I is updated from 2.4 to 2.5, it switches from yml to json files, but does not convert the data until it is used. Be sure to keep your MV-I data around, just in case something does go wrong.

 A converter from MultiInv is still in the works. There is a beginning framework as of version 1.4.0; do not use it! It is not fully operational yet, and will result in errors.
