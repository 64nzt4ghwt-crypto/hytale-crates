package com.howlstudio.crates;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;

/** /crate open <type>  — open a crate if you have a key */
public class CrateOpenCommand extends AbstractPlayerCommand {
    private final CratesManager manager;

    public CrateOpenCommand(CratesManager manager) {
        super("crate", "Open a crate or manage crates. Usage: /crate <open|keys|list> [type]");
        this.manager = manager;
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                           PlayerRef playerRef, World world) {
        UUID uuid = playerRef.getUuid();
        if (uuid == null) return;

        String input = ctx.getInputString().trim();
        String[] parts = input.split("\\s+");
        String sub = parts.length > 1 ? parts[1].toLowerCase() : "keys";

        switch (sub) {
            case "open" -> {
                if (parts.length < 3) {
                    playerRef.sendMessage(Message.raw("§cUsage: /crate open <type>  (e.g., /crate open vote)"));
                    showAvailableTypes(playerRef);
                    return;
                }
                String typeName = parts[2].toLowerCase();
                CrateType crate = manager.getCrateType(typeName);
                if (crate == null) {
                    playerRef.sendMessage(Message.raw("§c[Crates] Unknown crate type: §f" + typeName));
                    showAvailableTypes(playerRef);
                    return;
                }
                int keys = manager.getKeys(uuid, typeName);
                if (keys <= 0) {
                    playerRef.sendMessage(Message.raw("§c[Crates] §fYou don't have a §e" + crate.getDisplayName() + " §fkey!"));
                    playerRef.sendMessage(Message.raw("§7Earn keys by voting, donating, or asking staff."));
                    return;
                }

                CrateReward reward = manager.openCrate(uuid, typeName, playerRef);
                if (reward == null) {
                    playerRef.sendMessage(Message.raw("§c[Crates] Failed to open crate. Try again."));
                    return;
                }

                // Broadcast epic+ rewards server-wide
                String rarityColor = reward.getRarityColor();
                playerRef.sendMessage(Message.raw("§6✦ §lCRATE OPENED: §f" + crate.getDisplayName()));
                playerRef.sendMessage(Message.raw("§7You won: " + rarityColor + reward.getDisplay()));
                playerRef.sendMessage(Message.raw("§7You have §e" + (keys - 1) + " §7" + crate.getDisplayName() + " §7key(s) left."));

                if (reward.getRarity().equalsIgnoreCase("legendary") || reward.getRarity().equalsIgnoreCase("epic")) {
                    String playerName = playerRef.getUsername() != null ? playerRef.getUsername() : "Someone";
                    Universe.get().getPlayers().forEach(r ->
                        r.sendMessage(Message.raw("§6✦ §l" + playerName + " §6won §l"
                            + rarityColor + reward.getDisplay() + " §6from a §l" + crate.getDisplayName() + "§6!"))
                    );
                }
            }
            case "keys" -> {
                Map<String, Integer> keys = manager.getAllKeys(uuid);
                playerRef.sendMessage(Message.raw("§6§l--- Your Crate Keys ---"));
                if (keys.isEmpty()) {
                    playerRef.sendMessage(Message.raw("§7You have no crate keys. Vote with §e/vote §7to earn some!"));
                } else {
                    keys.forEach((type, count) -> {
                        CrateType ct = manager.getCrateType(type);
                        String name = ct != null ? ct.getDisplayName() : "§f" + type;
                        playerRef.sendMessage(Message.raw("  §e" + count + "x §f" + name));
                    });
                }
            }
            case "list" -> showAvailableTypes(playerRef);
            default -> {
                playerRef.sendMessage(Message.raw("§cUsage: /crate <open|keys|list>"));
            }
        }
    }

    private void showAvailableTypes(PlayerRef playerRef) {
        playerRef.sendMessage(Message.raw("§6Available crates:"));
        manager.getCrateTypes().forEach((name, ct) ->
            playerRef.sendMessage(Message.raw("  §e" + name + " §7— " + ct.getDisplayName()))
        );
    }
}
