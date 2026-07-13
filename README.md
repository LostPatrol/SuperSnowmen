# Super Snowmen

Super Snowmen is a Minecraft Java Edition Forge mod that lets players upgrade snow golems through a dedicated GUI.

## Supported version

- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17

## Features

- Right-click a snow golem to open its upgrade inventory.
- Shift-right-click a snow golem to remove all upgrade items back to the player.
- Base upgrade slots: pumpkin/carved pumpkin, and snow blocks. Each snow block adds 2 max health.
- Plugin slots: 20 one-item slots. Each installed plugin contributes 5% chance to replace a snowball projectile.
- The GUI shows a GitHub-language-style composition bar for installed projectile plugins.
- Special upgrade slots accept ice, packed ice, and blue ice. A full stack of 64 enables the corresponding tier.
- Server config and admin commands control the feature switch, projectile item consumption, and damage-cooldown bypass.

## Projectile plugins

The 1.20.1 implementation supports arrows, spectral arrows, fire charges, fireworks, dragon breath fireballs, TNT, wither skulls, sculk shrieker sonic booms, evoker fangs, tipped arrows, potions, eggs, tridents, and shulker bullets. Wind charges are not available in Minecraft 1.20.1 and are reserved for the later 1.21.1 port.

## Commands

All commands require permission level 2.

- `/supersnowmen enable <true|false>`
- `/supersnowmen consumeProjectiles <true|false>`
- `/supersnowmen consumePotionProjectiles <true|false>`
- `/supersnowmen bypassDamageCooldown <true|false>`

## License

This project is licensed under the MIT License. The license text follows the SPDX MIT license text: https://spdx.org/licenses/MIT

---

# Super Snowmen 中文说明

Super Snowmen 是一个 Minecraft Java Edition Forge 模组，用于通过 GUI 升级雪傀儡。

## 支持版本

- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17

## 功能

- 对雪傀儡右键打开升级界面。
- 对雪傀儡 Shift + 右键取回所有升级物品。
- 基础升级槽：南瓜/雕刻南瓜，以及雪块。每个雪块增加 2 点最大生命值。
- 插件槽：20 个单物品槽。每个插件提供 5% 概率将雪球替换为对应发射物。
- GUI 会展示类似 GitHub 仓库语言组成条的发射物组成条。
- 特殊升级槽可放入冰、浮冰、蓝冰。数量达到 64 时启用对应等级。
- 服务端配置和管理员命令可控制功能开关、发射物插件消耗及是否无视受伤冷却。

## 发射物插件

1.20.1 实现支持箭、光灵箭、火焰弹、烟花火箭、龙息火球、TNT、凋灵之首、幽匿尖啸体音波、不死图腾尖牙、药水箭、药水、鸡蛋、三叉戟和潜影贝飞弹。风弹不属于 Minecraft 1.20.1 内容，预留到后续 1.21.1 移植。

## 命令

所有命令需要 2 级权限。

- `/supersnowmen enable <true|false>`
- `/supersnowmen consumeProjectiles <true|false>`
- `/supersnowmen consumePotionProjectiles <true|false>`
- `/supersnowmen bypassDamageCooldown <true|false>`

## 许可证

本项目使用 MIT 许可证。许可证文本来源参照 SPDX MIT: https://spdx.org/licenses/MIT
