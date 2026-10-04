
// 模拟器核心，包含：内存、寄存器、指令解码与执行
public class CPU {

    byte[] memory = new byte[4096]; // 内存 4kb
    int[] V = new int[16]; // 数据寄存器，8位
    int I; // 地址寄存器，12位
    int pc = 0x200; // 程序计数器。指向下一条指令。指令从内存的0x200开始，一个指令2个字节，8位
    int[] stack = new int[16]; // 栈 16层
    int sp; // 栈指针，指向栈顶
    int delayTimer; // 延时定时器，60hz递减直到0停止
    int soundTimer; // 声音定时器，60hz递减，大于0时发出声音
    boolean[] keyboard = new boolean[16]; // 十六进制键盘，0-F共16个键
    boolean[] display = new boolean[64 * 32]; //显示屏，64 * 32的单色像素窗口
    boolean drawFlag; // 标记是否重绘

    // CHIP-8内置字体集
    private static final int[] FONT_SET = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    // 加载ROM
    public void loadRom(byte[] rom) {
        // 将ROM程序拷贝到内存当中
        System.arraycopy(rom,0,memory,0x200,rom.length);
        // 将内置字体集加载到内存中
        for (int i = 0; i < FONT_SET.length; i++) {
            memory[0x50 + i] = (byte) FONT_SET[i];
        }
    }

    // 取指令、解码、执行的核心循环
    public void cycle() {
        // 1.取指令 一条指令两个字节，所以取memory[pc] + memory[pc + 1]，取完pc += 2指向下一个指令
        int opcode = ((memory[pc] & 0xFF) << 8) | memory[pc + 1] & 0xFF;
        pc += 2;
        // 2.提取地址、立即数、低4位以及寄存器编号Vx、Vy
        int nnn = opcode & 0x0FFF; // 低12位地址
        int nn = opcode & 0x00FF; // 低8位立即数
        int n = opcode & 0x000F; // 低4位
        // x和y是寄存器编号
        int x = (opcode >> 8) & 0x000F;
        int y = (opcode >> 4) & 0x000F;

        // 3.解码并执行
        switch (opcode & 0xF000) { // 指令按操作码高4位分类
            case 0x0000 -> {
                switch (nn & 0xFF) {
                    case 0xEE -> {// 从子程序返回

                    }
                    case 0xE0 -> { // 清空屏幕
                        for (int i = 0; i < 64 * 32; i++) {
                            display[i] = false;
                        }
                    }
                } // 进一步判断是哪个指令
            } // 系统指令
            case 0x1000 -> pc = nnn; // 跳转
            case 0x2000 -> { stack[sp++] = pc; pc = nnn; } // 调用子程序
            case 0x3000 -> { if (V[x] == nn) pc += 2; } // 相等跳过
            case 0x4000 -> { if (V[x] != nn) pc += 2; } // 不等跳过
            case 0x5000 -> { if (V[x] == V[y]) pc += 2; } // 相等跳过
            case 0x6000 -> V[x] = nn; // 赋值
            case 0x7000 -> V[x] = (V[x] + nn) & 0xFF; // 寄存器加立即数
            case 0x8000 -> execute8xxx(x, y, n); // 算术与逻辑运算
            case 0x9000 -> { if (V[x] != V[y]) pc += 2; } // 不等跳过
            case 0xA000 -> I = nnn; // 设置地址
            case 0xB000 -> pc = nnn + V[0]; // 偏移跳转
            case 0xC000 -> V[x] = (int)(Math.random() * 256) & nn; // 随机数
            case 0xD000 -> drawSprite(x, y, n); // 绘制精灵：从I开始读n字节，在(x,y)处画像素
            case 0xE000 -> executeExxx(x, nn); // 按键跳过
            case 0xF000 -> executeFxxx(x, nn); // 定时器、内存、十进制拆分、键盘
        }
    }

    private void execute8xxx(int x, int y, int n) {
        switch (n) {
            case 0x0 -> V[x] = V[y]; // 赋值
            case 0x1 -> {
                V[x] = V[x] | V[y];
                V[0xF] = 0;
            } // 或运算
            case 0x2 -> {
                V[x] = V[x] & V[y];
                V[0xF] = 0;
            } // 与运算
            case 0x3 -> {
                V[x] = V[x] ^ V[y];
                V[0xF] = 0;
            } // 异或运算
            case 0x4 -> {
                int sum = V[x] + V[y];
                V[0xF] = (sum > 0xFF) ? 1 : 0;
                V[x] = sum & 0xFF;
            } // 加法
            case 0x5 -> {
                V[0xF] = (V[x] > V[y]) ? 1 : 0;
                V[x] = (V[x] - V[y]) & 0xFF;
            } // 减法 vx = vx - vy
            case 0x6 -> {
                V[0xF] = V[x] & 0x1;
                V[x] = V[x] >> 1;
            } // 右移
            case 0x7 -> {
                V[0xF] = (V[y] > V[x]) ? 1 : 0;
                V[x] = (V[y] - V[x]) & 0xFF;
            } // 减法 vx = vy - vx
            case 0xE -> {
                V[0xF] = (V[x] >> 7) & 0x1;
                V[x] = (V[x] << 1) & 0xFF;
            } // 左移
        }
    }

    private void executeFxxx(int x, int nn) {
        switch (nn) {
            case 0x07 -> V[x] = delayTimer;
            case 0x0A -> waitForKey(x); // 等待按键，将键值存入Vx
            case 0x15 -> delayTimer = V[x];
            case 0x18 -> soundTimer = V[x];
            case 0x1E -> I = (I + V[x]) & 0xFFF;
            case 0x29 -> I = 0x50 + (V[x] & 0x0F) * 5; // I指向Vx低4位
            case 0x33 -> {
                memory[I] = (byte) (V[x] / 100);
                memory[I + 1] = (byte) ((V[x] / 10) % 10);
                memory[I + 2] = (byte) (V[x] % 10);
            } // 把Vx的百、十、个位写入内存I、I+1、I+2
            case 0x55 -> {
                for (int i = 0; i <= x; i++)
                    memory[I + i] = (byte) V[i];
            } // 把V0..Vx写入内存I..I+x
            case 0x65 -> {
                for (int i = 0; i <= x; i++)
                    V[i] = memory[I + i];
            } // 从内存I开始读入V0..Vx
        }
    }

    private void executeExxx(int x, int nn) {
        switch (nn) {
            case 0x9E -> {if (keyboard[V[x]]) pc += 2;} // 键被按下则跳过
            case 0xA1 -> {if (!keyboard[V[x]]) pc += 2;} // 键没被按下则跳过
        }
    }

    private void drawSprite(int x, int y, int n) {
        // 1.VF置为0
        V[0xF] = 0;

        // 2.获取起始坐标
        int xPos = V[x] % 64;
        int yPos = V[y] % 32;

        // 3.绘制n行8列的数据： n代表要绘制的精灵字节(n行)，而一个精灵字节8位(8列)，每一位代表一个像素
        for (int row = 0; row < n; row++) {
            // 先取一个精灵字节
            int spriteByte = memory[I + row];

            // 逐列检查精灵字节当中的8位，绘制数据
            for (int col = 0; col < 8; col++) {
                // 精灵字节当中的某一位不为0,才绘制
                if ((spriteByte & (0x80 >> col)) != 0) {
                    // 找到绘制位置
                    int px = (xPos + col) % 64;
                    int py = (yPos + row) % 32;
                    int index = py * 64 + px;

                    // 碰撞检测：1变为0时碰撞
                    if (display[index]) {
                        V[0xF] = 1;
                    }

                    // 异或翻转像素
                    display[index] ^= true;
                }
            }

        }
    }

    private void waitForKey(int x) {
        System.out.println("暂未实现等待键盘输入");
    }

}
