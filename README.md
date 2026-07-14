# Super Snowmen

[简体中文](README_zh.md) | English

Super Snowmen turns ordinary Minecraft snow golems into configurable combat companions. Open a dedicated upgrade inventory, improve their core attributes, and fill 20 plugin slots with vanilla items to replace snowballs with arrows, fireballs, potions, TNT, sonic booms, and more. Plugins can also grant passive defenses and combine into stronger synergies.

![Upgraded snow golems fighting monsters](publish/assets/demo/common/snow_golem_battle_01.png)

## Compatibility

| Mod version | Minecraft | Loader | Java | Version-specific content |
| --- | --- | --- | --- | --- |
| 1.0.0 | 1.20.1 | Forge 47.4.10 | 17 | All upgrades below except Wind Charge |
| 1.0.0 | 1.21.1 | NeoForge 21.1.235 | 21 | Includes the Wind Charge plugin |

- Side: client and server. Install the mod on both sides for multiplayer.
- Dependencies: no additional mods are required beyond the matching loader.
- Mod ID: `super_snowmen`
- License: [MIT](LICENSE)
- Issues: [GitHub issue tracker](https://github.com/LostPatrol/SuperSnowmen/issues)

Use the build made for your exact Minecraft version. A 1.20.1 JAR is not compatible with 1.21.1, and the two versions use different mod loaders.

## Installation and basic use

1. Install the matching Forge or NeoForge version shown above.
2. Put the matching `super_snowmen-<minecraft-version>-<mod-version>.jar` in the `mods` folder on the client and server.
3. Build a vanilla snow golem, then right-click it to open the upgrade screen.
4. Place valid items in the three base slots, 20 projectile-plugin slots, and three special slots. Hover the `?` button for the plugin guide and the `i` button for the current effects.
5. Shift-right-click the snow golem to withdraw every installed item. Items that do not fit in your inventory are dropped safely.
6. Right-click a damaged snow golem with a snowball to restore 10 health. One snowball is consumed unless the player is in Creative mode; nothing is consumed at full health.

![Empty English upgrade screen](publish/assets/demo/en/upgrade_gui_empty_en.png)

Upgrades are stored on the individual snow golem. They are preserved with the entity and are dropped with their counts and data when the golem dies. Snow-golem-owned attacks do not damage players or other snow golems, including explosions, harmful potion effects, and Channeling lightning.

## Upgrade screen and probability

The center grid contains 20 plugin slots, and every slot represents one of 20 equally likely outcomes. A normal plugin occupies one outcome, so one installed plugin gives a 5% replacement chance. Empty slots remain snowball outcomes. Duplicate plugins increase that projectile's share in 5% steps, except Bow, Crossbow, and Lightning Rod, which are limited to one each.

The composition bar shows the real outcome distribution after synergies are resolved. Plugin slots hold one item each. Whether a selected item is consumed is controlled by the server configuration described below.

![Fully upgraded screen and effects](publish/assets/demo/en/upgrade_gui_full_01_en.png)

![Fully upgraded screen and projectile guide](publish/assets/demo/en/upgrade_gui_full_02_en.png)

![Fully upgraded screen and composition](publish/assets/demo/en/upgrade_gui_full_03_en.png)

## Base upgrades

| Item | Slot limit | Effect |
| --- | ---: | --- |
| <img src="publish/assets/icon/pumpkin.png" width="24" alt="Pumpkin"> Pumpkin or Carved Pumpkin | 64 | Each pumpkin adds 4% attack speed by shortening the vanilla 20-tick attack interval. |
| <img src="publish/assets/icon/snow_block.png" width="24" alt="Snow Block"> Snow Block | 64 | Each block adds 2 maximum health to the snow golem's base 4 health. |
| <img src="publish/assets/icon/diamond.png" width="24" alt="Diamond"> Diamond | 4 | Each diamond adds 1 raw outgoing damage and one Protection-equivalent point of damage reduction. The damage bonus is applied once per projectile and target. |

## Special ice upgrades

Each of the three special slots activates only when it contains a full stack of 64. The three slots are independent and their armor, toughness, and immunities stack, even when the same ice type is used more than once.

| Item | Effect per full stack |
| --- | --- |
| <img src="publish/assets/icon/ice.png" width="24" alt="Ice"> Ice | Climate immunity and +5 armor. Climate immunity prevents environmental heat damage while the golem is not actually burning; it is not fire resistance. |
| <img src="publish/assets/icon/packed_ice.png" width="24" alt="Packed Ice"> Packed Ice | Climate immunity, drowning immunity, and +10 armor. |
| <img src="publish/assets/icon/blue_ice.png" width="24" alt="Blue Ice"> Blue Ice | Climate immunity, drowning immunity, +15 armor, and +5 armor toughness. |

Minecraft's normal effective-armor cap still applies.

## Projectile plugins

All entries use vanilla items. Tipped arrows and all three potion forms retain the exact potion stored in the item. Enchantments on Tridents, Bows, and Crossbows are read from the installed stack where described.

| Plugin | Projectile and passive effects |
| --- | --- |
| <img src="publish/assets/icon/arrow.png" width="24" alt="Arrow"> **Arrow** | Fires a vanilla arrow. |
| <img src="publish/assets/icon/spectral_arrow.png" width="24" alt="Spectral Arrow"> **Spectral Arrow** | Fires a spectral arrow and applies its normal Glowing effect. |
| <img src="publish/assets/icon/fire_charge.png" width="24" alt="Fire Charge"> **Fire Charge** | Fires a small fireball and continuously grants Fire Resistance I while installed. |
| <img src="publish/assets/icon/ghast_tear.png" width="24" alt="Ghast Tear"> **Ghast Tear** | Fires a block-safe large ghast fireball with explosion power 1. Also raises the golem's current Regeneration effect by one level; multiple tears do not stack this passive bonus. |
| <img src="publish/assets/icon/firework_rocket.png" width="24" alt="Firework Rocket"> **Firework Rocket** | Fires a damaging crossbow-style rocket. The source rocket is copied, guaranteed to have at least three explosion entries, and each entry receives a randomized vanilla shape, colors, fade, trail, and/or flicker. |
| <img src="publish/assets/icon/dragon_breath.png" width="24" alt="Dragon's Breath"> **Dragon's Breath** | Fires a dragon fireball that creates a lingering dragon-breath cloud. Grants immunity to dragon-breath damage. |
| <img src="publish/assets/icon/tnt.png" width="24" alt="TNT"> **TNT** | Launches primed TNT toward the target's feet. Its explosion cannot break blocks, and the plugin grants immunity to explosion and player-explosion damage. |
| <img src="publish/assets/icon/wither_skeleton_skull.png" width="24" alt="Wither Skeleton Skull"> **Wither Skeleton Skull** | Fires a wither skull, removes and rejects Wither, and uses vanilla difficulty-dependent Wither duration. Kills heal the golem by 5. At half health or below, the golem gains the powered visual and blocks direct arrow attacks. |
| <img src="publish/assets/icon/sculk_shrieker.png" width="24" alt="Sculk Shrieker"> **Sculk Shrieker** | Emits a 10-damage sonic boom with vanilla particles, sound, and knockback. It hits the primary target and other monsters intersecting the beam. |
| <img src="publish/assets/icon/totem_of_undying.png" width="24" alt="Totem of Undying"> **Totem of Undying** | Summons the vanilla long-range evoker attack: 16 sequential fangs along the ground toward the target. |
| <img src="publish/assets/icon/tipped_arrow.png" width="24" alt="Tipped Arrow"> **Any Tipped Arrow** | Fires the installed tipped arrow and preserves vanilla duration scaling. Instant effects are applied with correct ownership and can land independently from the arrow hit when cooldown bypass is enabled. |
| <img src="publish/assets/icon/potion.png" width="24" alt="Potion"> **Any Regular Potion** | The snow golem drinks the installed potion and receives its effects. |
| <img src="publish/assets/icon/splash_potion.png" width="24" alt="Splash Potion"> **Any Splash Potion** | Throws the installed splash potion with vanilla range and intensity behavior. |
| <img src="publish/assets/icon/lingering_potion.png" width="24" alt="Lingering Potion"> **Any Lingering Potion** | Throws the installed lingering potion and creates its vanilla area-effect cloud. Snow-golem-owned potion effects and clouds never affect players. |
| <img src="publish/assets/icon/egg.png" width="24" alt="Egg"> **Egg** | Throws eggs, continuously grants Slow Falling, and makes the golem lay an egg every 5-10 minutes like a chicken. |
| <img src="publish/assets/icon/trident.png" width="24" alt="Trident"> **Trident** | Throws a copy of the installed trident. Impaling and Channeling are preserved; Loyalty and Riptide are removed. Impaling adds its vanilla bonus against aquatic mobs. |
| <img src="publish/assets/icon/shulker_shell.png" width="24" alt="Shulker Shell"> **Shulker Shell** | Fires a target-seeking shulker bullet. Grants +20 armor once, regardless of shell count, and removes and rejects Levitation. If there is no target, the snowball is kept and the shell is not consumed. |
| <img src="publish/assets/icon/lightning_rod.png" width="24" alt="Lightning Rod"> **Lightning Rod** | Utility synergy; only one may be installed. Alone its slot remains a snowball outcome. With any Trident, it becomes an extra 5% Trident outcome and prefers a Channeling trident. Channeling can then summon lightning in clear or rainy weather as well as thunderstorms, but the impact still needs sky access. |
| <img src="publish/assets/icon/bow.png" width="24" alt="Bow"> **Bow** | Utility synergy; only one may be installed. Its slot becomes an extra 5% Arrow outcome. Every regular, spectral, or tipped arrow uses full-draw speed and inherits Power, Punch, and Flame from the installed bow. The bow never loses durability. |
| <img src="publish/assets/icon/crossbow.png" width="24" alt="Crossbow"> **Crossbow** | Utility synergy; only one may be installed. Alone its slot remains snowball. With a Firework Rocket installed, it becomes an extra 5% Firework Bolt outcome. Multishot launches three matching rockets at `0°/-10°/+10°` while consuming at most the one selected rocket; the crossbow never loses durability. |
| <img src="publish/assets/icon/wind_charge.png" width="24" alt="Wind Charge"> **Wind Charge** *(Minecraft 1.21.1 only)* | Fires a vanilla wind charge directly toward the target. This item and projectile do not exist in 1.20.1. |

### Set bonuses

- Filling all 20 plugin slots grants Regeneration I and Resistance I continuously.
- Filling all 20 plugin slots **and** activating all six attribute slots upgrades both effects to level II. The three base slots must be nonempty, and every special slot must contain a full valid stack.
- A Ghast Tear adds one Regeneration level on top of an existing set bonus or other Regeneration effect.

## Important synergies and combat rules

- **Bow + arrows:** the Bow enhances Arrow, Spectral Arrow, and Tipped Arrow outcomes in addition to adding its own 5% Arrow outcome.
- **Crossbow + fireworks:** the Crossbow adds a Firework Bolt outcome only while a Firework Rocket is present. Multishot turns every chosen firework outcome into a three-rocket volley.
- **Lightning Rod + Trident:** the rod becomes another Trident outcome and relaxes Channeling's weather requirement. It does not produce a standalone lightning projectile.
- **Defensive plugins remain active:** Fire Charge, Ghast Tear, Dragon's Breath, TNT, Wither Skeleton Skull, Egg, and Shulker Shell have passive effects in addition to their projectile outcomes.
- **Protected allies:** attacks owned by a snow golem cannot hurt players or other snow golems. Explosions also remove their knockback targets, and harmful snow-golem-owned effects are rejected from allied snow golems.
- **Damage cooldown:** when enabled, distinct snow-golem projectiles can damage a non-player target without being swallowed by vanilla invulnerability frames. Area-effect clouds and lightning retain normal cooldown behavior.

![Super snow golems in battle](publish/assets/demo/common/snow_golem_battle_02.png)

## Server configuration

The server config is stored per world in `serverconfig/super_snowmen-server.toml`. Commands update and save the same values immediately.

| Key | Default | Effect |
| --- | :---: | --- |
| `enableUpgrades` | `true` | Enables the upgrade GUI, withdrawal/healing interactions, and projectile replacement. Turning it off does not remove already installed upgrades; their passive attribute/effect maintenance continues until the items are removed. |
| `consumeProjectileItems` | `false` | Consumes the selected plugin item after a successful replacement. Utility outcomes such as Bow, Crossbow, and Lightning Rod are not consumed. When this is `true`, potion-like items are also consumed regardless of the potion-specific setting. |
| `consumePotionProjectiles` | `true` | Additionally consumes Tipped Arrows and regular, splash, and lingering Potions when selected. To make potion-like plugins reusable, both consumption options must be `false`. |
| `bypassDamageCooldown` | `true` | Lets snow-golem-owned attacks bypass a non-player target's vanilla damage cooldown, with per-projectile safeguards against double-counting one impact. Lightning and area-effect-cloud damage are excluded. |

## Commands

All commands require permission level 2 and accept `true` or `false`.

| Command | Purpose |
| --- | --- |
| `/supersnowmen enable <boolean>` | Sets `enableUpgrades`. |
| `/supersnowmen consumeProjectiles <boolean>` | Sets `consumeProjectileItems`. |
| `/supersnowmen consumePotionProjectiles <boolean>` | Sets `consumePotionProjectiles`. |
| `/supersnowmen bypassDamageCooldown <boolean>` | Sets `bypassDamageCooldown`. |

## Building from source

Use the Java version for the target branch, then run `gradlew.bat clean build`. The finished JAR is written to `build/libs` and includes the Minecraft version in its filename.

## License

Super Snowmen is available under the [MIT License](LICENSE).
