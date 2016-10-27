package demo.com.facemanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.PersonGroup;

import java.util.ArrayList;
import java.util.List;


import demo.com.facemanagement.demo.com.IfNetWorkAvailable;
import demo.com.facemanagement.demo.com.MyAlertView;

import demo.com.facemanagement.demo.com.ShowPopupMenuWindow;

public class MainviewActivity extends AppCompatActivity implements View.OnClickListener{

    FaceServiceClient faceServiceClient;
    private TextView tv_title;
    private TextView tv_home;
    private TextView tv_groups;

    private ImageView iv_back;
    private ImageView iv_refresh;
    private ImageView iv_menu;

    private ImageView iv_add;
    private ImageView iv_delete;
    private ImageView iv_confirm;
    LayoutInflater layoutInflater;
    private long exitTime = 0;
    private static String userKey;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean visflag = false;
    static List<Boolean> boolList;
    static List<String> group_names;
    static List<String> group_ids;
    private MyAdapter adapter;
    private ListView lv_group;

    Typeface typeface2;

    private static boolean Flag_If_Delete = false;
    private PopupWindow popupWindow;
    private static int count_checked = 0;

    private void showPopupAddMenu(View view) {
        final PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.meun_add_group,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.add_group:
                        item.setChecked(true);
                        handler.sendEmptyMessage(0x125);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x123:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(MainviewActivity.this))) {
                        Toast.makeText(MainviewActivity.this,"It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Flag_If_Delete = true;
                    if (visflag) {
                        visflag = false;
                        for (int i = 0; i < boolList.size(); i++) {
                            boolList.set(i, false);
                        }
                    }else {
                        visflag = true;
                    }
                    MainviewActivity.this.adapter.notifyDataSetChanged();
                    iv_delete.setVisibility(View.GONE);
                    iv_delete.setClickable(false);

                    iv_confirm.setVisibility(View.VISIBLE);
                    iv_confirm.setClickable(false);
                    iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.nochecked));

                    iv_add.setVisibility(View.GONE);

                    iv_back.setVisibility(View.VISIBLE);
                    iv_back.setClickable(true);

                    break;
                case 0x124:
                    if (boolList.size() > 0) {
                        if (visflag) {
                            for(int i = 0; i < boolList.size();){
                                if (boolList.get(i)) {
                                    try {
                                        faceServiceClient.deletePersonGroup(group_ids.get(i));
                                    } catch (Exception e) {
                                        Toast.makeText(MainviewActivity.this,"Delete failed~",Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                    boolList.remove(i);
                                    group_names.remove(i);
                                    group_ids.remove(i);
                                    continue;
                                }
                                i++;
                            }
                        }
                    }
//                    count_checked = 0;
                    visflag = false;
                    MainviewActivity.this.adapter.notifyDataSetChanged();
                    iv_confirm.setVisibility(View.GONE);
                    iv_confirm.setClickable(false);
                    iv_delete.setVisibility(View.VISIBLE);
                    iv_delete.setClickable(true);

                    iv_back.setVisibility(View.GONE);
                    iv_add.setVisibility(View.VISIBLE);

                    break;
                case 0x125:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(MainviewActivity.this))) {
                        Toast.makeText(MainviewActivity.this,"It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final MyAlertView myAlertView = new MyAlertView(MainviewActivity.this,"Add a Group","CONFIRM","CANCEL");
                    myAlertView.show();
                    myAlertView.setCanceledOnTouchOutside(true);
                    myAlertView.setClickListener(new MyAlertView.ClickListenerInterface() {
                        @Override
                        public void pressConfirm(String group_name, String group_id) {

                            if (!(TextUtils.isEmpty(group_name)) &&!(TextUtils.isEmpty(group_id))) {
                                group_names.add(group_name);
                                group_ids.add(group_id);
                                boolList.add(false);
                                try {
                                    faceServiceClient.createPersonGroup(group_id,group_name,"");
                                    Toast.makeText(MainviewActivity.this,"SUCCESS", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainviewActivity.this,"Add Failure?",Toast.LENGTH_SHORT).show();
                                }
                                MainviewActivity.this.adapter.notifyDataSetChanged();
                                myAlertView.dismiss();
                            }else {
                                Toast.makeText(MainviewActivity.this,"Failure,do not enter a blank,carry on!", Toast.LENGTH_SHORT).show();
                            }

                        }
                        @Override
                        public void pressCancel() {
                            Toast.makeText(MainviewActivity.this,"Cancel Add",
                                    Toast.LENGTH_SHORT).show();
                            myAlertView.dismiss();
                        }
                    });
                    break;
                case 0x126:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(MainviewActivity.this))) {
                        Toast.makeText(MainviewActivity.this,"It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    editor.putString("PERSONGROUP_ID",group_ids.get((Integer) msg.obj));
                    editor.commit();
                    Intent intent = new Intent();
                    intent.setClass(MainviewActivity.this,PersonActivity.class);
                    startActivity(intent);
                    break;
                case 0x127 :
                    group_names.clear();
                    group_ids.clear();
                    MainviewActivity.this.adapter.notifyDataSetChanged();

                    FaceServiceClient fsc = new FaceServiceRestClient(userKey);
                    PersonGroup[] personGroups;
                    try {
                        personGroups = fsc.getPersonGroups();
                    } catch (Exception e){
                        e.printStackTrace();
                        return;
                    }
                    for (PersonGroup p : personGroups) {
                        group_names.add(p.name);
                        group_ids.add(p.personGroupId);
                        boolList.add(false);
                    }
                    lv_group.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private void init () {

        tv_home = (TextView) findViewById(R.id.tv_home);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_groups = (TextView) findViewById(R.id.tv_groups);
        iv_refresh = (ImageView) findViewById(R.id.iv_refresh);


        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_add = (ImageView) findViewById(R.id.iv_add);
        iv_delete = (ImageView) findViewById(R.id.iv_delete);
        iv_confirm = (ImageView) findViewById(R.id.iv_confirm);

        boolList = new ArrayList<>();
        group_names = new ArrayList<>();
        group_ids = new ArrayList<>();

        FaceServiceClient fsc = new FaceServiceRestClient(userKey);
        PersonGroup[] personGroups;
        try {
            personGroups = fsc.getPersonGroups();
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        for (PersonGroup p : personGroups) {
            group_names.add(p.name);
            group_ids.add(p.personGroupId);
            boolList.add(false);
        }
    }
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_activity);

        lv_group = (ListView) findViewById(R.id.lv_group);
        layoutInflater = LayoutInflater.from(MainviewActivity.this);

        preferences = getSharedPreferences("para",MODE_APPEND);
        editor = preferences.edit();
        userKey = preferences.getString("USER_KEY","none");

        faceServiceClient = new FaceServiceRestClient(userKey);

        init();

        typeface2 = Typeface.createFromAsset(getAssets(),"fonts/axis.ttf");

        tv_title.setTypeface(typeface2);
        tv_home.setTypeface(typeface2);
        tv_groups.setTypeface(typeface2);

        iv_back.setOnClickListener(this);
        iv_refresh.setOnClickListener(this);
        iv_menu.setOnClickListener(this);

        iv_add.setOnClickListener(this);
        iv_delete.setOnClickListener(this);
        iv_confirm.setOnClickListener(this);
        tv_home.setOnClickListener(this);

        adapter = new MyAdapter(this);
        lv_group.setAdapter(adapter);

        lv_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FaceServiceClient fsc = new FaceServiceRestClient(userKey);
                try {
                    fsc.getPersons(group_ids.get(position));
                    Message msg = new Message();
                    msg.what = 0x126;
                    msg.obj = position;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Toast.makeText(MainviewActivity.this,"DData has been deleted by another device!!",Toast.LENGTH_SHORT).show();
                    group_names.remove(position);
                    group_ids.remove(position);
                    MainviewActivity.this.adapter.notifyDataSetChanged();

                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (Flag_If_Delete) {
                do_cancel();
                Flag_If_Delete = false;
            }else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "Press again to exit the program", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_menu :  //showPopupMenuMenu(view);
                int [] iv_menu_position = new int[2];
                iv_menu.getLocationOnScreen(iv_menu_position);

                int [] menu_size = new int[2];
                menu_size[0] = iv_menu.getMeasuredHeight();
                menu_size[1] = iv_menu.getMeasuredWidth();

                ShowPopupMenuWindow.showPopupMenuWin(MainviewActivity.this,iv_menu_position,
                        userKey,menu_size,R.layout.new_group_activity);
                break;
            case R.id.iv_refresh : handler.sendEmptyMessage(0x127);
                break;
            case R.id.iv_add :   showPopupAddMenu(view);
                break;
            case R.id.iv_delete :
                handler.sendEmptyMessage(0x123);
                break;
            case R.id.iv_confirm :
                showPopupWindow();
                break;
            case R.id.iv_back :
                do_cancel();
                break;
            case R.id.tv_popup_confirm :
                handler.sendEmptyMessage(0x124);
                popupWindow.dismiss();
                break;
            case R.id.tv_popup_back :

                visflag = false;
                do_cancel();
                popupWindow.dismiss();
                break;
            case R.id.tv_group_menu_home :

                editor.putString("mode","Failure");
                editor.remove("USER_KEY");
                editor.remove("PERSONGROUP_ID");
                editor.commit();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                Intent intent = new Intent();
                intent.setClass( MainviewActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_group_menu_key :
                Toast.makeText(MainviewActivity.this,userKey, Toast.LENGTH_LONG).show();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                break;
        }
    }

    private void do_cancel (){

//        count_checked = 0;
        visflag = false;
        for (int j = 0; j < boolList.size();j++) {
            if (boolList.get(j)){
                boolList.set(j,false);
            }
        }
        MainviewActivity.this.adapter.notifyDataSetChanged();
        iv_confirm.setVisibility(View.GONE);
        iv_confirm.setClickable(false);
        iv_delete.setVisibility(View.VISIBLE);
        iv_delete.setClickable(true);

        iv_back.setVisibility(View.GONE);

        iv_add.setVisibility(View.VISIBLE);
        iv_add.setClickable(true);
    }

    class ViewHolder {
        public TextView tv_group_name;
        public TextView tv_group_id;
        public TextView tv_id_title;
        public CheckBox cb_check;
    }

    class MyAdapter extends BaseAdapter {

        private Context context;
        public MyAdapter(Context context) {
            this.context = context;

        }
        @Override
        public int getCount() {
            return group_names == null ? 0:group_names.size();
        }

        @Override
        public Object getItem(int i) {
            return group_names.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (convertView == null) {

                holder = new ViewHolder();
                convertView = LayoutInflater.from(MainviewActivity.this).inflate(R.layout.new_group_item, null);
                holder.tv_group_name = (TextView) convertView.findViewById(R.id.tv_group_name);
                holder.tv_id_title = (TextView) convertView.findViewById(R.id.tv_IDTitle);
                holder.tv_group_id = (TextView) convertView.findViewById(R.id.tv_id);
                holder.cb_check = (CheckBox) convertView.findViewById(R.id.cb_ckeck);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_group_name.setText(group_names.get(i));
            holder.tv_id_title.setText("ID:");
            holder.tv_group_id.setText(group_ids.get(i));
            holder.tv_group_name.setTypeface(typeface2);
            holder.tv_id_title.setTypeface(typeface2);
            holder.tv_group_id.setTypeface(typeface2);
            holder.cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (b) {
                        boolList.set(i,true);
                        count_checked++;
                        iv_confirm.setClickable(true);
                        iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.confirm));

                    }else {
                        boolList.set(i,false);
                        count_checked--;
                        if (count_checked == 0 ) {
                            iv_confirm.setClickable(false);
                            iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.nochecked));
                        }
                    }
                }
            });
            if (holder.cb_check.isChecked()) {
                holder.cb_check.setVisibility(View.VISIBLE);
            }
            if(visflag){
                holder.cb_check.setVisibility(View.VISIBLE);
                holder.cb_check.setClickable(true);
                holder.cb_check.setChecked(boolList.get(i));
            } else{
                holder.cb_check.setVisibility(View.GONE);
                holder.cb_check.setClickable(false);
                holder.cb_check.setChecked(boolList.get(i));
            }
            return convertView;
        }
    }
    private void showPopupWindow() {

        View view = LayoutInflater.from(MainviewActivity.this).inflate(R.layout.new_popup_window,
                null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(view);

        TextView tv_popup_confirm = (TextView) view.findViewById(R.id.tv_popup_confirm);
        TextView tv_popup_back = (TextView) view.findViewById(R.id.tv_popup_back);
        TextView tv_popup_title = (TextView) view.findViewById(R.id.tv_popup_title);

        tv_popup_back.setOnClickListener(this);
        tv_popup_confirm.setOnClickListener(this);
        tv_popup_title.setOnClickListener(this);

        if (count_checked > 0) {
            tv_popup_title.setText("Are you sure to delete " + count_checked +  " datas ?");
        }
        View rootView = LayoutInflater.from(MainviewActivity.this).inflate(R.layout.new_group_activity,
                null);

        final WindowManager.LayoutParams wl = getWindow().getAttributes();
        wl.alpha = (float) 0.3;
        getWindow().setAttributes(wl);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                wl.alpha = (float)1;
                getWindow().setAttributes(wl);
            }
        });

        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
    }
}
