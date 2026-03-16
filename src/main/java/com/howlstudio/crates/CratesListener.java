package com.howlstudio.crates;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class CratesListener {

    private final CratesManager manager;

    public CratesListener(CratesManager manager) {
        this.manager = manager;
    }

    public void register() {
        HytaleServer.get().getEventBus().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
    }

    private void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        PlayerRef ref = player.getPlayerRef();
        if (ref == null) return;
        UUID uuid = ref.getUuid();
        if (uuid == null) return;
        String name = ref.getUsername() != null ? ref.getUsername() : uuid.toString().substring(0, 8);
        manager.registerPlayer(uuid, name);

        // Notify player if they have pending keys
        var keys = manager.getAllKeys(uuid);
        int total = keys.values().stream().mapToInt(Integer::intValue).sum();
        if (total > 0) {
            ref.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "§6[Crates] §fYou have §e" + total + " §fcrate key(s)! Use §e/crate keys §fto view them."));
        }
    }
}
