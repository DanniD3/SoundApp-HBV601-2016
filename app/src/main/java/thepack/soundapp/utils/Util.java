package thepack.soundapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class Util {
    public static boolean isNetworkAvailableAndConnected(Context c, String CONNECTIVITY_SERVICE) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}
