package com.howlstudio.crates;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/** /crateadmin give <player> <type> [amount]  */
public class CrateAdminCommand extends AbstractPlayerCommand {
    private final CratesManager manager;

    public CrateAdminCommand(CratesManager manager) {
        super("crateadmin", "[Staff] Give crate keys. Usage: /crateadmin give <player> <type> [amount]");
        this.manager = manager;
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                           PlayerRef playerRef, World world) {
        String input = ctx.getInputString().trim();
        String[] parts = input.split("\\s+");

        if (parts.length < 4) {
            playerRef.sendMessage(Message.raw("§cUsage: /crateadmin give <player> <type> [amount]"));
            playerRef.sendMessage(Message.raw("§7Crate types: §e" + String.join("§7, §e", manager.getCrateTypes().keySet())));
            return;
        }

        String sub = parts[1].toLowerCase();
        if (!sub.equals("give")) {
            playerRef.sendMessage(Message.raw("§cUnknown subcommand. Use: give"));
            return;
        }

        String targetName = parts[2];
        String crateType = parts[3].toLowerCase();
        int amount = parts.length > 4 ? parseIntSafe(parts[4], 1) : 1;

        if (manager.getCrateType(crateType) == null) {
            playerRef.sendMessage(Message.raw("§c[Crates] Unknown crate type: §f" + crateType));
            playerRef.sendMessage(Message.raw("§7Types: §e" + String.join("§7, §e", manager.getCrateTypes().keySet())));
            return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT);
        UUID targetUuid = targetRef != null ? targetRef.getUuid() : UUID.nameUUIDFromBytes(targetName.getBytes());
        if (targetUuid == null) targetUuid = UUID.nameUUIDFromBytes(targetName.getBytes());

        manager.giveKey(targetUuid, crateType, amount);

        CrateType ct = manager.getCrateType(crateType);
        String displayName = ct != null ? ct.getDisplayName() : crateType;

        playerRef.sendMessage(Message.raw("§a[Crates] §fGave §e" + amount + "x " + displayName + " §fkey(s) to §e" + targetName + "§f."));

        if (targetRef != null) {
            targetRef.sendMessage(Message.raw("§6[Crates] §fYou received §e" + amount + "x " + displayName + " §fkey(s)!"));
            targetRef.sendMessage(Message.raw("§7Use §e/crate open " + crateType + " §7to open " + (amount > 1 ? "them" : "it") + "!"));
        }
    }

    private int parseIntSafe(String s, int def) {
        try { return Math.max(1, Integer.parseInt(s)); } catch (NumberFormatException e) { return def; }
    }
}
