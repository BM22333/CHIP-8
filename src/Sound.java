import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {
    private static final float SAMPLE_RATE = 44100f;
    private static final double FREQUENCY = 440.0;   // 音高
    private static final int AMPLITUDE = 3000;       // 音量，别太大

    private static Clip clip;
    private static boolean playing = false;

    static {
        try {
            // 生成一个周期的方波
            int samplesPerPeriod = (int) (SAMPLE_RATE / FREQUENCY);
            byte[] buffer = new byte[samplesPerPeriod * 2]; // 16 位，单声道

            for (int i = 0; i < samplesPerPeriod; i++) {
                short value = (i < samplesPerPeriod / 2)
                        ? (short) AMPLITUDE
                        : (short) -AMPLITUDE;
                buffer[i * 2]     = (byte) (value & 0xFF);
                buffer[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
            }

            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            clip = AudioSystem.getClip();
            clip.open(format, buffer, 0, buffer.length);
        } catch (Exception e) {
            System.err.println("声音初始化失败: " + e.getMessage());
            clip = null;
        }
    }

    public static void playSound() {
        if (playing | clip == null) {
            return;
        }
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        playing = true;
    }

    public static void stopSound() {
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.flush();
        playing = false;
    }
}

