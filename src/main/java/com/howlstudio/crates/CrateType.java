package com.howlstudio.crates;

import java.io.Serializable;
import java.util.*;

/** A named crate type with a reward pool. */
public class CrateType implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;        // e.g. "Donor", "Legendary", "Vote"
    private final String displayName; // colored, e.g. "§6Legendary Crate"
    private final List<CrateReward> rewards;
    private int totalWeight;

    public CrateType(String name, String displayName, List<CrateReward> rewards) {
        this.name = name.toLowerCase();
        this.displayName = displayName;
        this.rewards = new ArrayList<>(rewards);
        recalcWeight();
    }

    private void recalcWeight() {
        totalWeight = rewards.stream().mapToInt(CrateReward::getWeight).sum();
    }

    /** Weighted random roll. */
    public CrateReward roll() {
        if (rewards.isEmpty()) return null;
        int rand = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (CrateReward r : rewards) {
            cumulative += r.getWeight();
            if (rand < cumulative) return r;
        }
        return rewards.get(rewards.size() - 1);
    }

    public void addReward(CrateReward r) { rewards.add(r); recalcWeight(); }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public List<CrateReward> getRewards() { return Collections.unmodifiableList(rewards); }
    public int getTotalWeight() { return totalWeight; }

    private static final java.util.concurrent.ThreadLocalRandom ThreadLocalRandom =
        java.util.concurrent.ThreadLocalRandom.current();
}
