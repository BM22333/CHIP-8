import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {
    private final boolean[] keyboard;
    private static final int[] KEYMAP = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
            KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R,
            KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F,
            KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V
    };

    public Keyboard(boolean[] keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        for (int i = 0; i < KEYMAP.length; i++) {
            if (e.getKeyCode() == KEYMAP[i]) {
                keyboard[i] = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        for (int i = 0; i < KEYMAP.length; i++) {
            if (e.getKeyCode() == KEYMAP[i]) {
                keyboard[i] = false;
            }
        }
    }


}
