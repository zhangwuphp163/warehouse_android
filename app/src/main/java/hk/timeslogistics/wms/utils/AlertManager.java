package hk.timeslogistics.wms.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;

import net.mabboud.android_tone_player.OneTimeBuzzer;

public class AlertManager {
    public static boolean shouldPlay(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    public static void okay(Context context) {
        if (!shouldPlay(context)) {
            return;
        }
        OneTimeBuzzer buzzer = new OneTimeBuzzer();
        buzzer.setDuration(0.2);
        buzzer.setToneFreqInHz(1000);
        buzzer.play();
    }

    public static void noop(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);

        if (!shouldPlay(context)) {
            return;
        }

        OneTimeBuzzer buzzer = new OneTimeBuzzer();
        buzzer.setDuration(0.2);
        buzzer.setToneFreqInHz(500);
        buzzer.play();
    }

    public static void success(Context context) {
        if (!shouldPlay(context)) {
            return;
        }

        OneTimeBuzzer buzzer = new OneTimeBuzzer();
        buzzer.setDuration(0.5);
        buzzer.setToneFreqInHz(660);
        buzzer.play();
    }

    public static void error(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        assert vibrator != null;
        vibrator.vibrate(1000);

        if (!shouldPlay(context)) {
            return;
        }

        OneTimeBuzzer buzzer = new OneTimeBuzzer();
        buzzer.setDuration(1);
        buzzer.setToneFreqInHz(2000);
        buzzer.play();
    }
}
