package com.howlstudio.crates;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * CratesPlugin — Loot crates with key-based opening and reward pools.
 *
 * Features:
 *   - 3 default crate types: Vote, Donor, Legendary
 *   - Weighted random reward rolls per crate
 *   - Reward types: execute command, give currency, send message
 *   - Server-wide broadcast for Epic/Legendary wins
 *   - Login notification when player has pending keys
 *   - Persistent key storage across restarts
 *   - Admin key distribution with /crateadmin give
 *
 * Commands:
 *   /crate open <type>              — open a crate with a key
 *   /crate keys                     — view your key inventory
 *   /crate list                     — list all crate types
 *   /crateadmin give <player> <type> [amount]  — give keys (staff)
 *
 * Integration:
 *   - Works with EconomyPlugin (eco give command rewards)
 *   - Works with PermissionsPlugin (rank set command rewards)
 *   - Works with TebexConnect (sell keys via donation store)
 */
public final class CratesPlugin extends JavaPlugin {

    private CratesManager manager;

    public CratesPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        System.out.println("[Crates] Loading...");

        manager = new CratesManager(getDataDirectory());

        CommandManager cmd = CommandManager.get();
        cmd.register(new CrateOpenCommand(manager));
        cmd.register(new CrateAdminCommand(manager));

        new CratesListener(manager).register();

        System.out.println("[Crates] Ready! " + manager.getCrateTypes().size() + " crate types loaded.");
    }

    @Override
    protected void shutdown() {
        if (manager != null) {
            manager.save();
            System.out.println("[Crates] Saved and stopped.");
        }
    }
}
