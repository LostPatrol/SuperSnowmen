# Super Snowmen

简体中文 | [English](README.md)

Super Snowmen（超级雪傀儡）可以把普通雪傀儡变成可自由配置的战斗伙伴。右键打开专用升级栏，强化基础属性，再用原版物品填充 20 个插件槽，让雪球变成箭、火球、药水、TNT、音波等攻击。部分插件还会提供常驻防御能力，并能与其他插件组成联动。

![升级后的雪傀儡与怪物战斗](publish/assets/demo/common/snow_golem_battle_01.png)

## 兼容信息

| 模组版本 | Minecraft | 加载器 | Java | 版本特有内容 |
| --- | --- | --- | --- | --- |
| 1.0.0 | 1.20.1 | Forge 47.4.10 | 17 | 支持下文除风弹外的全部升级 |
| 1.0.0 | 1.21.1 | NeoForge 21.1.235 | 21 | 额外支持风弹插件 |

- 运行端：客户端与服务端；多人游戏时两端都要安装。
- 依赖：除对应版本的加载器外，不需要其他模组。
- 模组 ID：`super_snowmen`
- 许可证：[MIT](LICENSE)
- 问题反馈：[GitHub Issues](https://github.com/LostPatrol/SuperSnowmen/issues)

请下载与 Minecraft 版本完全对应的构建。1.20.1 与 1.21.1 的 JAR 不能混用，而且两者使用不同的模组加载器。

## 安装与基本用法

1. 安装上表中对应版本的 Forge 或 NeoForge。
2. 将对应的 `super_snowmen-<Minecraft版本>-<模组版本>.jar` 放入客户端和服务端的 `mods` 文件夹。
3. 按原版方式建造雪傀儡，然后右键雪傀儡打开升级界面。
4. 在 3 个基础槽、20 个发射物插件槽和 3 个特殊槽内放入有效物品。悬停 `?` 按钮可查看插件指南，悬停 `i` 按钮可查看当前效果。
5. Shift + 右键雪傀儡可一次取回全部升级。背包放不下的物品会安全掉落。
6. 手持雪球右键受伤的雪傀儡可恢复 10 点生命。非创造模式消耗 1 个雪球；满血时不会消耗。

![未安装升级的中文界面](publish/assets/demo/zh/upgrade_gui_empty_zh.png)

升级数据保存在每一只雪傀儡身上，会随实体存档；雪傀儡死亡时，所有升级都会保留数量和物品数据并正常掉落。雪傀儡拥有的攻击不会伤害玩家或其他雪傀儡，爆炸、有害药水效果和引雷产生的闪电也在保护范围内。

## 升级界面与概率

中央区域有 20 个插件槽，每个槽都对应 20 次等概率结果中的 1 次。普通插件占据一个结果，因此每件插件提供 5% 的替换概率；空槽仍然发射雪球。除弓、弩和避雷针最多只能安装一个外，重复插件会让对应发射物的占比以 5% 为单位累加。

界面中的发射物组成条会在处理联动后显示真实概率。每个插件槽最多放 1 件物品；被选中的物品是否消耗由下文的服务端配置决定。

![装满升级后的效果列表](publish/assets/demo/zh/upgrade_gui_full_01_zh.png)

![装满升级后的插件指南](publish/assets/demo/zh/upgrade_gui_full_02_zh.png)

![装满升级后的发射物组成](publish/assets/demo/zh/upgrade_gui_full_03_zh.png)

## 基础升级

| 物品 | 槽位上限 | 效果 |
| --- | ---: | --- |
| <img src="publish/assets/icon/pumpkin.png" width="24" alt="南瓜"> 南瓜或雕刻南瓜 | 64 | 每个南瓜增加 4% 攻击速度，通过缩短原版 20 tick 的攻击间隔实现。 |
| <img src="publish/assets/icon/snow_block.png" width="24" alt="雪块"> 雪块 | 64 | 每个雪块在雪傀儡原有 4 点生命上增加 2 点最大生命。 |
| <img src="publish/assets/icon/diamond.png" width="24" alt="钻石"> 钻石 | 4 | 每颗钻石增加 1 点原始攻击伤害，并提供相当于 1 级保护的伤害减免。同一个发射物对同一个目标只结算一次钻石增伤。 |

## 特殊冰类升级

3 个特殊槽都只有在放满 64 个物品时才会激活。每个槽独立计算，即使放入相同冰块，护甲、韧性和免疫效果也会叠加。

| 物品 | 每个满组提供的效果 |
| --- | --- |
| <img src="publish/assets/icon/ice.png" width="24" alt="冰"> 冰 | 气候免疫、+5 护甲。气候免疫只会在雪傀儡没有真的着火时阻止环境热伤害，不等于抗火。 |
| <img src="publish/assets/icon/packed_ice.png" width="24" alt="浮冰"> 浮冰 | 气候免疫、溺水免疫、+10 护甲。 |
| <img src="publish/assets/icon/blue_ice.png" width="24" alt="蓝冰"> 蓝冰 | 气候免疫、溺水免疫、+15 护甲、+5 护甲韧性。 |

原版的有效护甲上限仍然生效。

## 发射物插件

所有插件都是原版物品。药箭和三种药水会保留物品中实际储存的药水；三叉戟、弓和弩则会按下文说明读取已安装物品的附魔。

| 插件 | 发射物与常驻效果 |
| --- | --- |
| <img src="publish/assets/icon/arrow.png" width="24" alt="箭"> **箭** | 发射原版箭。 |
| <img src="publish/assets/icon/spectral_arrow.png" width="24" alt="光灵箭"> **光灵箭** | 发射光灵箭，并施加原版发光效果。 |
| <img src="publish/assets/icon/fire_charge.png" width="24" alt="火焰弹"> **火焰弹** | 发射小火球；安装期间持续获得抗火 I。 |
| <img src="publish/assets/icon/ghast_tear.png" width="24" alt="恶魂之泪"> **恶魂之泪** | 发射爆炸威力 1、不会破坏方块的大火球；同时让雪傀儡当前的生命恢复效果提高 1 级，多颗恶魂之泪不会重复叠加这一被动加成。 |
| <img src="publish/assets/icon/firework_rocket.png" width="24" alt="烟花火箭"> **烟花火箭** | 发射能造成伤害的弩式烟花。它会复制已安装的烟花，保证至少有 3 个爆炸条目，并为每个条目随机选择原版形状、颜色、淡化色、轨迹和/或闪烁。 |
| <img src="publish/assets/icon/dragon_breath.png" width="24" alt="龙息"> **龙息** | 发射会生成滞留龙息云的末影龙火球；同时免疫龙息伤害。 |
| <img src="publish/assets/icon/tnt.png" width="24" alt="TNT"> **TNT** | 把点燃的 TNT 投向目标脚边。爆炸不会破坏方块，同时免疫普通爆炸和玩家爆炸伤害。 |
| <img src="publish/assets/icon/wither_skeleton_skull.png" width="24" alt="凋灵骷髅头颅"> **凋灵骷髅头颅** | 发射凋灵之首，移除并免疫凋零效果；凋零持续时间仍按原版难度决定。击杀目标时恢复 5 点生命。生命低于一半时显示充能外观，并免疫直接命中的箭类攻击。 |
| <img src="publish/assets/icon/sculk_shrieker.png" width="24" alt="幽匿尖啸体"> **幽匿尖啸体** | 发出固定 10 点伤害、带原版粒子/音效/击退的音波；主目标和射线上相交的其他怪物都会受伤。 |
| <img src="publish/assets/icon/totem_of_undying.png" width="24" alt="不死图腾"> **不死图腾** | 施放原版唤魔者远程攻击，沿地面向目标依次召唤 16 个尖牙。 |
| <img src="publish/assets/icon/tipped_arrow.png" width="24" alt="药箭"> **任意药箭** | 发射已安装的药箭，并保留原版持续时间缩放。启用受伤冷却绕过时，瞬间效果可与箭的直接伤害分别正确结算。 |
| <img src="publish/assets/icon/potion.png" width="24" alt="药水"> **任意普通药水** | 雪傀儡喝下已安装的药水并获得其效果。 |
| <img src="publish/assets/icon/splash_potion.png" width="24" alt="喷溅药水"> **任意喷溅药水** | 以原版范围和强度规则投掷已安装的喷溅药水。 |
| <img src="publish/assets/icon/lingering_potion.png" width="24" alt="滞留药水"> **任意滞留药水** | 投掷已安装的滞留药水并生成原版效果云。雪傀儡拥有的药水效果和效果云不会影响玩家。 |
| <img src="publish/assets/icon/egg.png" width="24" alt="鸡蛋"> **鸡蛋** | 投掷鸡蛋、持续获得缓降，并像鸡一样每 5-10 分钟下一个蛋。 |
| <img src="publish/assets/icon/trident.png" width="24" alt="三叉戟"> **三叉戟** | 投掷已安装三叉戟的副本。保留穿刺与引雷，移除忠诚与激流；穿刺会对水生生物造成原版附加伤害。 |
| <img src="publish/assets/icon/shulker_shell.png" width="24" alt="潜影壳"> **潜影壳** | 发射会追踪目标的潜影贝导弹；无论安装多少个都只提供一次 +20 护甲，并移除和免疫漂浮。没有目标时保留原雪球，也不会消耗潜影壳。 |
| <img src="publish/assets/icon/lightning_rod.png" width="24" alt="避雷针"> **避雷针** | 联动插件，最多安装一个。单独安装时该槽仍是雪球；存在任意三叉戟时变成额外 5% 三叉戟，并优先使用带引雷的三叉戟。此时晴天或雨天也能引雷，但命中位置仍必须能看见天空。 |
| <img src="publish/assets/icon/bow.png" width="24" alt="弓"> **弓** | 联动插件，最多安装一个。所在槽变成额外 5% 箭；所有普通箭、光灵箭和药箭都会使用满弓速度，并继承力量、冲击和火矢。弓不会损失耐久。 |
| <img src="publish/assets/icon/crossbow.png" width="24" alt="弩"> **弩** | 联动插件，最多安装一个。单独安装时该槽仍是雪球；存在烟花火箭时变成额外 5% 烟花弩箭。多重射击会以 `0°/-10°/+10°` 一次发射 3 枚相同烟花，最多只消耗 1 枚被选中的烟花；弩不会损失耐久。 |
| <img src="publish/assets/icon/wind_charge.png" width="24" alt="风弹"> **风弹** *（仅 Minecraft 1.21.1）* | 直接朝目标发射原版风弹。Minecraft 1.20.1 中不存在该物品和发射物。 |

### 套装效果

- 填满全部 20 个插件槽会持续获得生命恢复 I 和抗性提升 I。
- 在填满 20 个插件槽的基础上，再激活全部 6 个属性槽，两种效果都会升级为 II 级。3 个基础槽必须非空，3 个特殊槽都必须放满有效物品。
- 恶魂之泪可以在套装效果或其他现有生命恢复效果上再增加 1 级。

## 重要联动与战斗规则

- **弓 + 箭：**弓除了增加自己的 5% 箭结果，还会强化箭、光灵箭和药箭结果。
- **弩 + 烟花：**只有存在烟花火箭时，弩才会增加烟花弩箭结果；多重射击会让每次选中的烟花攻击变成三连发。
- **避雷针 + 三叉戟：**避雷针会变成额外的三叉戟结果，并放宽引雷的天气条件；它不会单独发射闪电。
- **插件被动持续生效：**火焰弹、恶魂之泪、龙息、TNT、凋灵骷髅头颅、鸡蛋和潜影壳除了替换发射物，还会提供常驻效果。
- **友军保护：**雪傀儡拥有的攻击不能伤害玩家或其他雪傀儡；爆炸也不会对他们造成击退，有害效果不会施加给友方雪傀儡。
- **受伤冷却：**启用配置后，不同雪傀儡发射物可以连续命中非玩家目标，不会被原版受伤无敌帧吞掉。效果云与闪电仍遵循原版冷却。

![超级雪傀儡战斗场景](publish/assets/demo/common/snow_golem_battle_02.png)

## 服务端配置

服务端配置按世界保存在 `serverconfig/super_snowmen-server.toml`。下文的命令会立即修改并保存同一组配置。

| 配置键 | 默认值 | 作用 |
| --- | :---: | --- |
| `enableUpgrades` | `true` | 启用升级 GUI、取回/雪球治疗交互和发射物替换。关闭后不会自动拆除已安装升级；物品移除前，已有升级的被动属性和效果仍会继续维护。 |
| `consumeProjectileItems` | `false` | 成功替换发射物后消耗被选中的插件。弓、弩和避雷针等联动结果不会被消耗。此项为 `true` 时，药水类也会被消耗，不受药水专用开关关闭的影响。 |
| `consumePotionProjectiles` | `true` | 额外控制药箭、普通药水、喷溅药水和滞留药水的消耗。若要让药水类插件可重复使用，两个消耗配置都必须是 `false`。 |
| `bypassDamageCooldown` | `true` | 让雪傀儡拥有的攻击绕过非玩家目标的原版受伤冷却，并防止同一发射物的一次命中重复结算。闪电和区域效果云伤害除外。 |

## 指令

全部指令都需要 2 级权限，参数接受 `true` 或 `false`。

| 指令 | 作用 |
| --- | --- |
| `/supersnowmen enable <布尔值>` | 设置 `enableUpgrades`。 |
| `/supersnowmen consumeProjectiles <布尔值>` | 设置 `consumeProjectileItems`。 |
| `/supersnowmen consumePotionProjectiles <布尔值>` | 设置 `consumePotionProjectiles`。 |
| `/supersnowmen bypassDamageCooldown <布尔值>` | 设置 `bypassDamageCooldown`。 |

## 从源码构建

根据目标分支使用上表中的 Java 版本，然后运行 `gradlew.bat clean build`。生成的 JAR 位于 `build/libs`，文件名中包含 Minecraft 版本。

## 许可证

Super Snowmen 使用 [MIT 许可证](LICENSE)。
