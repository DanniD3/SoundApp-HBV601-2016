package thepack.soundapp.utils;

import android.content.Context;
import android.widget.Toast;

public class Util {
    public void displayText(Context activityContext, String text, int duraction) {
        Toast.makeText(activityContext, text, duraction).show();
    }
}
