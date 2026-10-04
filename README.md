# CHIP-8 模拟器（Java + Swing）

## 介绍

- 晚上无聊打发时间之作
- 画面好像有点问题
- 或许还有其他没发现的bug

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
| `Sound.java` | 声音 |
| `Main.java` | 窗口、60Hz 节奏、每帧指令预算 |

## 兼容性开关（quirk）

同一段 ROM 在不同年代的机器上行为并不一致。这里有两个开关：

| 开关 | 默认 | 说明 |
| --- | --- | --- |
| `CPU.incrementIOnMemoryOps` | `true` | `FX55` / `FX65` 传输结束后，`I` 是否前进 `x+1` |
| `CPU.maxHoldTicks` | `8` | 见上面「画面闪烁是怎么解决的」一节 |
