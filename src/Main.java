import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CPU cpu = new CPU();
            byte[] rom;
            try {
                rom = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/ch8/Pong (1 player).ch8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cpu.loadRom(rom);

            Display display = new Display(cpu);

            JFrame frame = new JFrame("CHIP-8");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(display);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            frame.addKeyListener(new Keyboard(cpu.getKeyboard()));
            frame.setFocusable(true);
            frame.requestFocus();

            Timer timer = new Timer(1000 / 60, e -> {
                for (int i = 0; i < 10; i++) cpu.cycle();
                cpu.updateTimers();
                display.repaint();
            });
            timer.start();
        });
}}

