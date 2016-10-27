package demo.com.facemanagement.demo.com;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import demo.com.facemanagement.R;

/**
 * Created by v-caiwch on 2016/8/24.
 */
public class ShowPopupMenuWindow {

    private static String show_userKey;
    private static PopupWindow popupWinMenu;

    public static void showPopupMenuWin(Context context, int[] position,
                                 String userkey,int [] menu_size,int R_id){

        show_userKey = userkey;
        show_userKey = show_userKey.substring(show_userKey.length() - 5, show_userKey.length());
        show_userKey = "KEY: **** " + show_userKey;

        View view = LayoutInflater.from(context).inflate(
                R.layout.new_group_menu_,null
        );
        popupWinMenu = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWinMenu.setContentView(view);

        TextView tv_group_menu_home = (TextView) view.findViewById(R.id.tv_group_menu_home);
        TextView tv_group_menu_key = (TextView) view.findViewById(R.id.tv_group_menu_key);

        tv_group_menu_home.setOnClickListener((View.OnClickListener)context);
        tv_group_menu_key.setOnClickListener((View.OnClickListener) context);
        View rootView = LayoutInflater.from(context).inflate(R_id,
                null);
        tv_group_menu_key.setText(show_userKey);

        popupWinMenu.setTouchable(true);
        popupWinMenu.setBackgroundDrawable(new ColorDrawable());
        popupWinMenu.setOutsideTouchable(true);

        popupWinMenu.showAtLocation(rootView, Gravity.TOP | Gravity.START,
                position[0] + menu_size[0] , position[1] + menu_size[1]);
    }
    public static PopupWindow getPopupWinMenu(){
        return  popupWinMenu;
    }
}
