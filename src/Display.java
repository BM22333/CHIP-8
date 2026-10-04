import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

@SuppressWarnings("serial") // Swing 组件不需要序列化，省掉 javac 的 serial 警告
class Display extends JPanel {
    private final CPU cpu;
    private final BufferedImage image =
            new BufferedImage(64, 32, BufferedImage.TYPE_INT_RGB);
    private final int[] pixels =
            ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

    private static final int ON  = 0xFFFFFF;
    private static final int OFF = 0x000000;

    Display(CPU cpu) {
        this.cpu = cpu;
        setPreferredSize(new Dimension(640, 320)); // 10 倍放大
        setBackground(Color.BLACK);
        setOpaque(true);
        setFocusable(true); // 让面板自己接收键盘事件（见 Main）
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 用背景色铺满整个面板，四周自然留黑边

        // 1. 只读取 CPU 提交好的「完整帧」快照，不碰随时可能被改写的实时缓冲
        boolean[] frame = cpu.getFrameBuffer();
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = frame[i] ? ON : OFF;
        }

        // 2. 等比整数缩放 + 居中。窗口小于 64*32 时也要保证 scale >= 1，否则会什么都画不出来
        int scale = Math.min(getWidth() / 64, getHeight() / 32);
        if (scale < 1) scale = 1;
        int drawW = 64 * scale;
        int drawH = 32 * scale;
        int offsetX = (getWidth() - drawW) / 2;
        int offsetY = (getHeight() - drawH) / 2;

        // 3. 关闭插值，保持硬边像素风
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        // 4. 一次放大绘制整幅画面
        g2.drawImage(image, offsetX, offsetY, drawW, drawH, null);
    }
}
