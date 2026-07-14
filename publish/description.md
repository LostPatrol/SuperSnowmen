# Super Snowmen

Super Snowmen turns ordinary Minecraft snow golems into configurable combat companions. Open a dedicated upgrade inventory, improve their core attributes, and fill 20 plugin slots with vanilla items to replace snowballs with arrows, fireballs, potions, TNT, sonic booms, and more. Plugins can also grant passive defenses and combine into stronger synergies.

![Upgraded snow golems fighting monsters](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/common/snow_golem_battle_01.png)

## Compatibility

- **Minecraft 1.20.1:** Forge 47.4.10, Java 17. Includes every upgrade below except Wind Charge.
- **Minecraft 1.21.1:** NeoForge 21.1.235, Java 21. Includes the Wind Charge plugin.
- **Current mod version:** 1.0.0
- **Sides:** client and server. Install the mod on both sides for multiplayer.
- **Dependencies:** no additional mods are required beyond the matching loader.

Use the file made for your exact Minecraft version. The 1.20.1 and 1.21.1 files are not interchangeable and use different loaders.

## Installation and basic use

1. Install the matching Forge or NeoForge version.
2. Put the matching Super Snowmen JAR in the `mods` folder on the client and server.
3. Build a vanilla snow golem, then right-click it to open the upgrade screen.
4. Place valid items in the three base slots, 20 projectile-plugin slots, and three special slots. The `?` button opens the plugin guide and the `i` button shows current effects.
5. Shift-right-click the snow golem to withdraw all installed items. Items that do not fit in your inventory are dropped safely.
6. Right-click a damaged snow golem with a snowball to restore 10 health. This consumes one snowball outside Creative mode and consumes nothing at full health.

![Empty upgrade screen](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/en/upgrade_gui_empty_en.png)

Upgrades are stored on the individual snow golem, survive saving and loading, and drop with their counts and item data when the golem dies. Snow-golem-owned attacks do not damage players or other snow golems, including explosions, harmful potion effects, and Channeling lightning.

## Upgrade slots and probability

Every one of the 20 plugin slots represents one equally likely outcome. A normal plugin therefore gives a 5% chance to replace a snowball. Empty slots remain snowball outcomes. Duplicate plugins increase that projectile's chance in 5% steps, except Bow, Crossbow, and Lightning Rod, which are limited to one each. Each plugin slot holds one item, and the server configuration controls consumption.

The in-game composition bar shows the actual distribution after all synergies are resolved.

![Fully upgraded screen and effects](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/en/upgrade_gui_full_01_en.png)

![Projectile guide](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/en/upgrade_gui_full_02_en.png)

![Projectile composition](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/en/upgrade_gui_full_03_en.png)

## Base and special upgrades

- **Pumpkin or Carved Pumpkin (up to 64):** each adds 4% attack speed by shortening the vanilla 20-tick attack interval.
- **Snow Block (up to 64):** each adds 2 maximum health to the snow golem's base 4 health.
- **Diamond (up to 4):** each adds 1 raw outgoing damage and one Protection-equivalent point of damage reduction. The damage bonus is applied once per projectile and target.

There are three independent special slots. A special upgrade activates only at a full stack of 64, and effects from all three slots stack:

- **Ice:** climate immunity and +5 armor. Climate immunity prevents environmental heat damage while the golem is not actually burning; it is not fire resistance.
- **Packed Ice:** climate immunity, drowning immunity, and +10 armor.
- **Blue Ice:** climate immunity, drowning immunity, +15 armor, and +5 armor toughness.

Minecraft's normal effective-armor cap still applies.

## Every projectile plugin

- **Arrow:** fires a vanilla arrow.
- **Spectral Arrow:** fires a spectral arrow and applies Glowing.
- **Fire Charge:** fires a small fireball and continuously grants Fire Resistance I.
- **Ghast Tear:** fires a block-safe large ghast fireball with explosion power 1 and raises the current Regeneration effect by one level. Multiple tears do not stack the passive bonus.
- **Firework Rocket:** fires a damaging crossbow-style rocket. The source rocket is copied, guaranteed at least three explosion entries, and randomized with vanilla shapes, colors, fades, trails, and flickers.
- **Dragon's Breath:** fires a dragon fireball that creates a lingering dragon-breath cloud and grants immunity to dragon-breath damage.
- **TNT:** launches block-safe primed TNT toward the target's feet and grants immunity to explosion and player-explosion damage.
- **Wither Skeleton Skull:** fires a wither skull, removes and rejects Wither, and keeps the vanilla difficulty-dependent Wither duration. Kills heal 5 health. At half health or below, the golem gains the powered visual and blocks direct arrow attacks.
- **Sculk Shrieker:** emits a 10-damage sonic boom with vanilla particles, sound, and knockback. It hits the primary target and other monsters intersecting the beam.
- **Totem of Undying:** summons the vanilla long-range evoker attack: 16 sequential fangs along the ground toward the target.
- **Any Tipped Arrow:** fires the exact installed arrow and preserves vanilla duration scaling. Instant effects can land independently from the arrow hit when cooldown bypass is enabled.
- **Any Regular Potion:** the snow golem drinks the installed potion and receives its effects.
- **Any Splash Potion:** throws the installed splash potion with vanilla range and intensity behavior.
- **Any Lingering Potion:** throws the installed lingering potion and creates its vanilla area-effect cloud. Snow-golem-owned potion effects and clouds never affect players.
- **Egg:** throws eggs, continuously grants Slow Falling, and makes the golem lay an egg every 5-10 minutes like a chicken.
- **Trident:** throws a copy of the installed trident. Impaling and Channeling are preserved; Loyalty and Riptide are removed. Impaling adds its vanilla aquatic-mob bonus.
- **Shulker Shell:** fires a target-seeking shulker bullet, grants +20 armor once regardless of shell count, and removes and rejects Levitation. With no target, the snowball is kept and the shell is not consumed.
- **Lightning Rod:** one may be installed. Alone its slot remains snowball. With a Trident, it becomes an extra 5% Trident outcome and prefers a Channeling trident. Channeling can work in clear or rainy weather as well as thunderstorms, but still needs sky access.
- **Bow:** one may be installed. Its slot becomes an extra 5% Arrow outcome. Every regular, spectral, or tipped arrow uses full-draw speed and inherits Power, Punch, and Flame. The bow never loses durability.
- **Crossbow:** one may be installed. Alone its slot remains snowball. With a Firework Rocket, it becomes an extra 5% Firework Bolt outcome. Multishot fires three matching rockets at `0°/-10°/+10°`, consumes at most one selected rocket, and never damages the crossbow.
- **Wind Charge (Minecraft 1.21.1 only):** fires a vanilla wind charge directly toward the target. This item does not exist in 1.20.1.

Tipped arrows and all potion forms retain the exact potion stored in the item. Tridents, Bows, and Crossbows read the installed stack's relevant enchantments.

## Set bonuses and important synergies

- Filling all 20 plugin slots grants Regeneration I and Resistance I continuously.
- Filling all 20 plugin slots and activating all six attribute slots upgrades both effects to level II. The base slots must be nonempty, and all three special slots must contain full valid stacks.
- A Ghast Tear adds one Regeneration level on top of another Regeneration effect.
- Bow enhances Arrow, Spectral Arrow, and Tipped Arrow outcomes as well as adding its own Arrow outcome.
- Crossbow adds a Firework Bolt only while a Firework Rocket is installed; Multishot turns selected firework attacks into three-rocket volleys.
- Lightning Rod becomes another Trident outcome and relaxes Channeling's weather requirement; it never creates a standalone lightning shot.
- Fire Charge, Ghast Tear, Dragon's Breath, TNT, Wither Skeleton Skull, Egg, and Shulker Shell provide passive effects in addition to projectile outcomes.
- When damage-cooldown bypass is enabled, distinct snow-golem projectiles can hit non-player targets without vanilla invulnerability frames swallowing the damage. Area-effect clouds and lightning keep normal cooldown behavior.

![Super snow golems in battle](https://raw.githubusercontent.com/LostPatrol/SuperSnowmen/1.20.1/publish/assets/demo/common/snow_golem_battle_02.png)

## Server configuration

The per-world file is `serverconfig/super_snowmen-server.toml`. Commands update and save these values immediately.

- **`enableUpgrades` (default `true`):** enables the GUI, withdrawal/healing interactions, and projectile replacement. Disabling it does not remove installed items; their existing passive attribute/effect maintenance continues until removal.
- **`consumeProjectileItems` (default `false`):** consumes a selected plugin after a successful replacement. Utility outcomes such as Bow, Crossbow, and Lightning Rod are not consumed. When enabled, potion-like items are consumed regardless of the potion-specific setting.
- **`consumePotionProjectiles` (default `true`):** additionally consumes Tipped Arrows and all three Potion forms. Both consumption settings must be `false` to make potion-like plugins reusable.
- **`bypassDamageCooldown` (default `true`):** lets snow-golem attacks bypass a non-player target's vanilla damage cooldown, with safeguards against double-counting one impact. Lightning and area-effect-cloud damage are excluded.

## Commands

All commands require permission level 2 and accept `true` or `false`.

- `/supersnowmen enable <true|false>` sets `enableUpgrades`.
- `/supersnowmen consumeProjectiles <true|false>` sets `consumeProjectileItems`.
- `/supersnowmen consumePotionProjectiles <true|false>` sets `consumePotionProjectiles`.
- `/supersnowmen bypassDamageCooldown <true|false>` sets `bypassDamageCooldown`.

## Links and license

- Source and full documentation: [GitHub](https://github.com/LostPatrol/SuperSnowmen)
- Bug reports: [GitHub Issues](https://github.com/LostPatrol/SuperSnowmen/issues)
- License: [MIT](https://github.com/LostPatrol/SuperSnowmen/blob/1.20.1/LICENSE)
