package bf.fasobizness.bafatech.utils;

import android.annotation.SuppressLint;

import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;

class FormatUtils {
    private FormatUtils() {
        throw new AssertionError();
    }

    public static String getDurationString(int seconds) {
        Date date = new Date(seconds * 1000);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(seconds >= 3600 ? "HH:mm:ss" : "mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }
}
