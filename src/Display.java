import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class Display extends JPanel {
    private final CPU cpu;
    private final BufferedImage image =
            new BufferedImage(64, 32, BufferedImage.TYPE_INT_RGB);
    private final int[] pixels =
            ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();

    private static final int ON  = 0xFFFFFF;
    private static final int OFF = 0x000000;

    Display(CPU cpu) {
        this.cpu = cpu;
        setPreferredSize(new Dimension(640, 320));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        // 1. 更新中间图像
        for (int y = 0; y < 32; y++) {
            for (int i = 0; i < 64 * 32; i++) {
                pixels[i] = cpu.getDisplay()[i] ? ON : OFF;
            }
        }

        // 2. 计算保持比例的缩放
        int panelW = getWidth();
        int panelH = getHeight();
        int scale = Math.min(panelW / 64, panelH / 32);
        int drawW = 64 * scale;
        int drawH = 32 * scale;
        int offsetX = (panelW - drawW) / 2;
        int offsetY = (panelH - drawH) / 2;

        // 3. 关闭插值，保持硬边
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        // 4. 一次放大绘制
        g2.drawImage(image, offsetX, offsetY, drawW, drawH, null);
    }
}
