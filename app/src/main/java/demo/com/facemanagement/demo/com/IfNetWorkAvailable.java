package demo.com.facemanagement.demo.com;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by v-caiwch on 2016/8/19.
 */
public class IfNetWorkAvailable {

    public static boolean isNetWorkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return  false;
        }else  {
            NetworkInfo [] networkInfos = connectivityManager.getAllNetworkInfo();
            if (networkInfos != null && networkInfos.length > 0) {
                for (int i = 0; i < networkInfos.length; i++) {
                    if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
                        return  true;
                    }
                }
            }
        }
        return false;
    }
}
