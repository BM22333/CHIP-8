# CHIP-8 模拟器（Java + Swing）

## 运行

在项目根目录：

```
javac -encoding UTF-8 -d out/production/CHIP-8 src/*.java
java -cp out/production/CHIP-8 Main
```

换一个 ROM（不给参数时按内置的相对路径列表依次查找）：

```
java -cp out/production/CHIP-8 Main "src/ch8/IBM Logo.ch8"
java -cp out/production/CHIP-8 Main "src/ch8/Animal Race [Brian Astle].ch8"
```

## 结构

| 文件 | 职责 |
| --- | --- |
| `CPU.java` | 模拟核心：内存、寄存器、指令解码执行、定时器、双缓冲画面 |
| `Display.java` | 画面缩放渲染（最近邻、等比整数放大、居中） |
| `Keyboard.java` | 键盘映射 |
| `Sound.java` | 声音（尚未实现） |
| `Main.java` | 窗口、60Hz 节奏、每帧指令预算 |

## 画面闪烁是怎么解决的

CHIP-8 的 `DXYN` 是用异或画的。很多 ROM 靠"在旧位置再画一次"来擦除精灵，
所以一帧的绘制其实是**擦掉 → 过几条指令 → 在新位置画上**。
`Main` 每个 tick 只跑 10 条指令（≈600 条/秒），一条擦除到重画之间的间隔能跨 1~2 个 tick，
如果这时把画面显示出去，精灵就没了。

解决办法是把显示分成两块缓冲：

- `CPU.display` 实时缓冲：异或翻转、碰撞检测都在这里，随时可能只画了一半；
- `CPU.frameBuffer` 呈现缓冲：**只有一整帧确实画完了才整块拷过去**，`Display` 永远只读它。

关键在"什么时候算画完"。这里用的判据是 **ROM 跳回循环开头**：
绝大多数 ROM 的主循环都以"画完这一帧所有精灵"收尾，往回跳的瞬间画面恰好是完整的。
判据在 `CPU.onLoopBack()`，提交发生在**指令边界**上（不是 tick 末尾，否则一拖就是 10 条指令）。

对少数迟迟不回跳的特殊 ROM，`CPU.maxHoldTicks` 是兜底：连续多 tick 没回跳，
就强行提交，避免画面卡住。

## 兼容性开关（quirk）

CHIP-8 从来没有正式标准，同一段 ROM 在不同年代的机器上行为并不一致。这里有两个开关：

| 开关 | 默认 | 说明 |
| --- | --- | --- |
| `CPU.incrementIOnMemoryOps` | `true` | `FX55` / `FX65` 传输结束后，`I` 是否前进 `x+1` |
| `CPU.maxHoldTicks` | `8` | 见上面「画面闪烁是怎么解决的」一节 |

前者的来历：原版 COSMAC VIP 会自增 `I`。