import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int TICKS_PER_SECOND = 60;   // CHIP-8 的延时/声音定时器就是 60Hz
    private static final int CYCLES_PER_TICK = 10;    // ≈600 条指令/秒，CHIP-8 的常见速度
    private static final long TICK_NANOS = 1_000_000_000L / TICKS_PER_SECOND;
    private static final int MAX_CATCH_UP_TICKS = 5;  // 一次最多补跑几个 tick，避免卡顿后疯狂追帧
    private static final String DEFAULT_ROM = "src/ch8/Pong (1 player).ch8";

    public static void main(String[] args) {
        Path romPath;
        byte[] rom;
        try {
            romPath = resolveRom(args);
            rom = Files.readAllBytes(romPath);
        } catch (IOException e) {
            System.err.println("读取 ROM 失败: " + e.getMessage());
            return;
        }
        Path finalRomPath = romPath;
        SwingUtilities.invokeLater(() -> start(finalRomPath, rom));
    }

    private static void start(Path romPath, byte[] rom) {
        CPU cpu = new CPU();
        cpu.loadRom(rom);

        Display display = new Display(cpu);

        JFrame frame = new JFrame("CHIP-8 - " + romPath.getFileName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 键盘事件挂到面板上，并主动把焦点交给它。
        // 挂在 JFrame 上时，焦点一旦跑到别处（比如点一下标题栏）按键就会失灵。
        display.addKeyListener(new Keyboard(cpu.getKeyboard()));
        display.requestFocusInWindow();

        long[] lastTick = { System.nanoTime() };
        Timer timer = new Timer(1000 / TICKS_PER_SECOND, e -> {
            // 按真实经过的时间决定这次要跑几个 1/60 秒，而不是假定定时器每次都准点触发。
            // Swing 的 Timer 在 EDT 繁忙时会被合并/延迟，不补偿的话定时器会越走越慢。
            long elapsedTicks = (System.nanoTime() - lastTick[0]) / TICK_NANOS;
            if (elapsedTicks <= 0) return;               // 还没到下一个 1/60 秒

            if (elapsedTicks > MAX_CATCH_UP_TICKS) {     // 拖窗口/断点之后落后太多，直接对齐，不补跑
                lastTick[0] = System.nanoTime();
                elapsedTicks = 1;
            } else {
                lastTick[0] += elapsedTicks * TICK_NANOS;
            }

            for (int t = 0; t < elapsedTicks; t++) {
                for (int i = 0; i < CYCLES_PER_TICK; i++) cpu.cycle();
                cpu.updateTimers();
                // 只有真正画完一整帧时才重绘：既不会显示画到一半的画面，也省掉无谓的重绘
                if (cpu.publishFrame()) display.repaint();
            }
        });
        timer.start();
    }

    /** 依次尝试命令行参数和几个常见的相对路径，返回第一个真实存在的 ROM。 */
    private static Path resolveRom(String[] args) throws IOException {
        List<Path> candidates = new ArrayList<>();
        if (args.length > 0) candidates.add(Paths.get(args[0]));
        candidates.add(Paths.get(DEFAULT_ROM));
        candidates.add(Paths.get("ch8/Pong (1 player).ch8"));

        for (Path p : candidates) {
            if (Files.isRegularFile(p)) return p;
        }
        throw new IOException("找不到 ROM，已尝试: " + candidates + "（也可以把 .ch8 路径作为命令行参数传入）");
    }
}
