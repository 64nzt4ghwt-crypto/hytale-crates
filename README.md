# CratesPlugin for Hytale

Loot crates with key-based opening, weighted random rewards, and server-wide broadcasts for legendary wins.

## Commands

| Command | Description |
|---------|-------------|
| `/crate open <type>` | Open a crate using a key |
| `/crate keys` | View your key inventory |
| `/crate list` | List all available crate types |
| `/crateadmin give <player> <type> [amount]` | Give keys to a player (staff) |

## Default Crate Types

| Crate | Rewards | Best Reward |
|-------|---------|-------------|
| `vote` | Coins, kit access | 10,000 coins |
| `donor` | Coins, VIP ranks | 25,000 coins + 30-Day VIP |
| `legendary` | MVP ranks, epic kits | 100,000 coins + 90-Day MVP+ |

## Features
- **Weighted reward rolls** — configurable probability per reward
- **4 rarity tiers** — Common, Rare, Epic, Legendary
- **Server-wide broadcast** — Epic/Legendary wins shown to entire server
- **Login notification** — players reminded if they have pending keys
- **Persistent storage** — keys saved across restarts
- **Tebex integration** — sell keys via donation store with `/crateadmin give`
- **Economy integration** — reward commands work with EconomyPlugin

## Integration with Tebex

In your Tebex store, set the fulfillment command to:
```
/crateadmin give {username} donor 1
```
