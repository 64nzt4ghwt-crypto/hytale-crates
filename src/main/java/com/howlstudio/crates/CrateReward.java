package com.howlstudio.crates;

import java.io.Serializable;

/** A single reward entry in a crate's reward pool. */
public class CrateReward implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { COMMAND, CURRENCY, MESSAGE }

    private final String display;      // shown to player on win
    private final Type type;
    private final String value;        // command template (%player% = player name), amount for CURRENCY, text for MESSAGE
    private final int weight;          // relative probability weight (higher = more common)
    private final String rarity;       // "Common", "Rare", "Epic", "Legendary"

    public CrateReward(String display, Type type, String value, int weight, String rarity) {
        this.display = display;
        this.type = type;
        this.value = value;
        this.weight = weight;
        this.rarity = rarity;
    }

    public String getDisplay() { return display; }
    public Type getType() { return type; }
    public String getValue() { return value; }
    public int getWeight() { return weight; }
    public String getRarity() { return rarity; }

    public String getRarityColor() {
        return switch (rarity.toLowerCase()) {
            case "legendary" -> "§6§l";
            case "epic"      -> "§5§l";
            case "rare"      -> "§b";
            default          -> "§f";
        };
    }
}
