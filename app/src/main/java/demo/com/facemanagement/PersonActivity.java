package demo.com.facemanagement;

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
import android.util.Log;
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
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import demo.com.facemanagement.demo.com.IfNetWorkAvailable;
import demo.com.facemanagement.demo.com.MyPersonAlertView;
import demo.com.facemanagement.demo.com.ShowPopupMenuWindow;

public class PersonActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "PersonActivity";
    private TextView tv_person_home;
    private TextView tv_person_title;
    private TextView tv_group_home;
    private TextView tv_person;

    private long exitTime = 0;

    private Person[] persons;
    private String groupId;
    FaceServiceClient fsc;

    private String userKey;
    LayoutInflater layoutInflater;

    boolean visflag = false;
    private List<Boolean> boolList;

    private List<String> group_person_name;
    private List<UUID> group_person_id;

    private ImageView p_iv_back;
    private ImageView p_iv_menu;
    private ImageView p_iv_refresh;
    private ImageView p_iv_add;
    private ImageView p_iv_delete;
    private ImageView p_iv_confirm;

    private MyPersonAdapter adapter;
    private ListView lv_group;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Typeface typeface;

    private static boolean Flag_If_Delete = false;
    Intent intent = null;

    private PopupWindow popupWindow;
    private static int count_checked = 0;

    //ADD Person
    private void showPersonPopupAddMenu(View view) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add_person, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_person:
                        item.setChecked(true);
                        handler.sendEmptyMessage(0x125);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x123:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(PersonActivity.this))) {
                        Toast.makeText(PersonActivity.this, "It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Flag_If_Delete = true;
                    if (visflag) {
                        visflag = false;
                        for (int i = 0; i < boolList.size(); i++) {
                            boolList.set(i, false);
                        }
                    } else {
                        visflag = true;
                    }
                    PersonActivity.this.adapter.notifyDataSetChanged();
                    p_iv_delete.setVisibility(View.GONE);
                    p_iv_delete.setClickable(false);

                    p_iv_confirm.setVisibility(View.VISIBLE);
                    p_iv_confirm.setClickable(false);
                    p_iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.nochecked));

                    p_iv_add.setVisibility(View.GONE);
                    p_iv_back.setVisibility(View.VISIBLE);
                    p_iv_back.setClickable(true);

                    break;
                case 0x124:
                    if (boolList.size() > 0) {
                        if (visflag) {
                            for (int i = 0; i < boolList.size(); ) {
                                if (boolList.get(i)) {
                                    try {
                                        fsc.deletePerson(groupId, group_person_id.get(i));
                                    } catch (Exception e) {
                                        Toast.makeText(PersonActivity.this, "Delete Failure?", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                    boolList.remove(i);
                                    group_person_name.remove(i);
                                    group_person_id.remove(i);
                                    continue;
                                }
                                i++;
                            }
                        }
                    }
                    visflag = false;
                    PersonActivity.this.adapter.notifyDataSetChanged();
                    try {
                        fsc.trainPersonGroup(groupId);
                    } catch (ClientException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    p_iv_confirm.setVisibility(View.GONE);
                    p_iv_confirm.setClickable(false);
                    p_iv_delete.setVisibility(View.VISIBLE);
                    p_iv_delete.setClickable(true);

                    p_iv_back.setVisibility(View.GONE);
                    p_iv_add.setVisibility(View.VISIBLE);
                    p_iv_add.setClickable(true);
                    break;
                case 0x125:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(PersonActivity.this))) {
                        Toast.makeText(PersonActivity.this, "It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final MyPersonAlertView myPersonAlertView = new MyPersonAlertView(PersonActivity.this, "ADD PERSON",
                            "CONFIRM", "CANCEL");
                    myPersonAlertView.show();
                    myPersonAlertView.setCanceledOnTouchOutside(true);

                    myPersonAlertView.setClickListener(new MyPersonAlertView.ClickListenerInterface() {
                        @Override
                        public void pressConfirm(String person_name) {
                            UUID personId = null;
                            if (!(TextUtils.isEmpty(person_name))) {
                                try {
                                    personId = fsc.createPerson(groupId, person_name, "").personId;
                                    group_person_id.add(personId);
                                    group_person_name.add(person_name);
                                    boolList.add(false);
                                    persons = fsc.getPersons(groupId);
                                    PersonActivity.this.adapter.notifyDataSetChanged();
                                    Toast.makeText(PersonActivity.this, "ADD NEW PERSON SUCCESS",
                                            Toast.LENGTH_SHORT).show();
                                    myPersonAlertView.dismiss();
                                } catch (Exception e) {
                                    Toast.makeText(PersonActivity.this, "Create Failure?", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(PersonActivity.this, "Failure,do not enter a blank,carry on!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void pressCancel() {
                            Toast.makeText(PersonActivity.this, "Cancel Add",
                                    Toast.LENGTH_SHORT).show();
                            myPersonAlertView.dismiss();
                        }
                    });

                    break;
                case 0x126:
                    if (!(IfNetWorkAvailable.isNetWorkAvailable(PersonActivity.this))) {
                        Toast.makeText(PersonActivity.this, "It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                        return;
                    }
                    intent.setClass(PersonActivity.this, PersonPhotoActivity.class);
                    intent.putExtra("PERSON_ID", group_person_id.get((Integer) msg.obj));
                    intent.putExtra("PERSONGROUP_ID", groupId);
                    intent.putExtra("Person_Name", group_person_name.get((Integer) msg.obj));
                    startActivity(intent);
                    break;
                case 0x127:
//                    if (group_person_name.size() < 1) {
                    group_person_name.clear();
                    group_person_id.clear();
                    PersonActivity.this.adapter.notifyDataSetChanged();

                    try {
                        persons = fsc.getPersons(groupId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PersonActivity.this, "Failed to access data,or there are no any datas?", Toast.LENGTH_SHORT).show();
                    }
                    if (persons.length > 0) {
                        for (Person person : persons) {
                            group_person_name.add(person.name);
                            group_person_id.add(person.personId);
                            boolList.add(false);
                        }
                        lv_group.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
//                    }
                    break;
                case 0x128:
                    Toast.makeText(PersonActivity.this, "Cancel operation", Toast.LENGTH_SHORT).show();
                    do_cancel_operation();
                    break;
            }
        }
    };

    private void init() {

        intent = new Intent();
        boolList = new ArrayList<>();

        group_person_name = new ArrayList<>();
        group_person_id = new ArrayList<>();

        tv_group_home = (TextView) findViewById(R.id.tv_group_home);
        tv_person_title = (TextView) findViewById(R.id.tv_person_title);
        tv_group_home = (TextView) findViewById(R.id.tv_group_home);
        tv_person = (TextView) findViewById(R.id.tv_person);
        tv_person_home = (TextView) findViewById(R.id.tv_person_home);

        p_iv_menu = (ImageView) findViewById(R.id.p_iv_menu);
        p_iv_refresh = (ImageView) findViewById(R.id.p_iv_refresh);
        p_iv_add = (ImageView) findViewById(R.id.p_iv_add);
        p_iv_delete = (ImageView) findViewById(R.id.p_iv_delete);
        p_iv_confirm = (ImageView) findViewById(R.id.p_iv_confirm);
        p_iv_back = (ImageView) findViewById(R.id.p_iv_back);

        try {
            persons = fsc.getPersons(groupId);
            for (Person person : persons) {
                group_person_name.add(person.name);
                group_person_id.add(person.personId);
                boolList.add(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PersonActivity.this, "Failed to access data?", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_all_person);
        preferences = getSharedPreferences("para", MODE_APPEND);
        editor = preferences.edit();
        layoutInflater = LayoutInflater.from(PersonActivity.this);

        groupId = preferences.getString("PERSONGROUP_ID", null);
        userKey = preferences.getString("USER_KEY", null);

        lv_group = (ListView) findViewById(R.id.p_lv_group);

        fsc = new FaceServiceRestClient(userKey);

        init();
        typeface = Typeface.createFromAsset(getAssets(), "fonts/axis.ttf");

        tv_person_title.setTypeface(typeface);
        tv_group_home.setTypeface(typeface);
        tv_person.setTypeface(typeface);
        tv_person_home.setTypeface(typeface);

        p_iv_menu.setOnClickListener(this);
        p_iv_refresh.setOnClickListener(this);
        p_iv_add.setOnClickListener(this);
        p_iv_delete.setOnClickListener(this);
        p_iv_confirm.setOnClickListener(this);
        tv_group_home.setOnClickListener(this);
        p_iv_back.setOnClickListener(this);

        adapter = new MyPersonAdapter(this);
        lv_group.setAdapter(adapter);

        lv_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Toast.makeText(PersonActivity.this, position + "", Toast.LENGTH_SHORT).show();
                    FaceServiceClient fsc = new FaceServiceRestClient(userKey);
//                    fsc.updatePerson(groupId, group_person_id.get(position), persons[position].name, persons[position].name);
                    Person person = fsc.getPerson(groupId, group_person_id.get(position));
//                            fsc.addPersonFace(groupId, group_person_id.get(position), "User data",
//                                    null, null);
                    Message msg = new Message();
                    msg.what = 0x126;
                    msg.obj = position;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    Toast.makeText(PersonActivity.this, "Data has been deleted by another device!!", Toast.LENGTH_SHORT).show();
                    group_person_name.remove(position);
                    group_person_id.remove(position);

                    PersonActivity.this.adapter.notifyDataSetChanged();
                    e.printStackTrace();
                }
            }

        });
    }

    private void do_cancel_operation() {
//        count_checked = 0;
        visflag = false;
        for (int j = 0; j < boolList.size(); j++) {
            if (boolList.get(j)) {
                boolList.set(j, false);
            }
        }
        PersonActivity.this.adapter.notifyDataSetChanged();
        p_iv_confirm.setVisibility(View.GONE);
        p_iv_confirm.setClickable(false);
        p_iv_delete.setVisibility(View.VISIBLE);
        p_iv_delete.setClickable(true);
        p_iv_back.setVisibility(View.GONE);
        p_iv_add.setVisibility(View.VISIBLE);
        p_iv_add.setClickable(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (Flag_If_Delete) {
                do_cancel_operation();
                Flag_If_Delete = false;
            } else {
                finish();
                group_person_name.clear();
                group_person_id.clear();
                PersonActivity.this.adapter.notifyDataSetChanged();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.p_iv_menu:  //showPersonPopupMenuMenu(view);
                int[] iv_menu_position = new int[2];
                p_iv_menu.getLocationOnScreen(iv_menu_position);

                int[] menu_size = new int[2];
                menu_size[0] = p_iv_menu.getMeasuredHeight();
                menu_size[1] = p_iv_menu.getMeasuredWidth();

                ShowPopupMenuWindow.showPopupMenuWin(PersonActivity.this, iv_menu_position,
                        userKey, menu_size, R.layout.new_all_person);

                break;
            case R.id.p_iv_refresh:
                handler.sendEmptyMessage(0x127);
                break;
            case R.id.p_iv_add:
                showPersonPopupAddMenu(view);
                break;
            case R.id.p_iv_delete:
                handler.sendEmptyMessage(0x123);
                break;
            case R.id.p_iv_confirm:
                showPopupWindow();
                break;
            case R.id.tv_group_home:
                intent = new Intent();
                intent.setClass(PersonActivity.this, MainviewActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.p_iv_back:
                handler.sendEmptyMessage(0x128);
                break;
            case R.id.tv_person_confirm:
                handler.sendEmptyMessage(0x124);
                popupWindow.dismiss();
                break;
            case R.id.tv_person_back:
                do_cancel_operation();
                popupWindow.dismiss();
                break;
            case R.id.tv_group_menu_home:
                editor.putString("mode", "Failure");
                editor.remove("USER_KEY");
                editor.remove("PERSONGROUP_ID");
                editor.commit();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                Intent intent = new Intent();
                intent.setClass(PersonActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_group_menu_key:
                Toast.makeText(PersonActivity.this, userKey, Toast.LENGTH_LONG).show();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                break;
        }
    }

    class ViewHolder {
        TextView tv_person_name;
        CheckBox cb_check;
    }

    class MyPersonAdapter extends BaseAdapter {

        private Context context;

        MyPersonAdapter(Context context) {
            this.context = context;

        }

        @Override
        public int getCount() {
            return group_person_name == null ? 0 : group_person_name.size();
        }

        @Override
        public Object getItem(int i) {
            return group_person_name.get(i);
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
                convertView = LayoutInflater.from(PersonActivity.this).inflate(R.layout.new_person_item_style, null);

                holder.tv_person_name = (TextView) convertView.findViewById(R.id.tv_person_name);
                holder.cb_check = (CheckBox) convertView.findViewById(R.id.p_cb_check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_person_name.setText(group_person_name.get(i));
            holder.tv_person_name.setTypeface(typeface);
            holder.cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        boolList.set(i, true);
                        Log.d(TAG, "onCheckedChanged,++count_checked: " + count_checked);
                        ++count_checked;
                        Toast.makeText(PersonActivity.this, "onCheckedChanged,++count_checked" + count_checked, Toast.LENGTH_SHORT).show();
                        p_iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.confirm));
                        p_iv_confirm.setClickable(true);

                    } else {
                        boolList.set(i, false);
                        count_checked--;
                        Toast.makeText(PersonActivity.this, "onCheckedChanged,--count_checked" + count_checked, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onCheckedChanged,--count_checked: " + count_checked);
                        if (count_checked == 0) {
                            p_iv_confirm.setImageDrawable(getResources().getDrawable(R.mipmap.nochecked));
                            p_iv_confirm.setClickable(false);
                        }
                    }
                }
            });

            if (visflag) {
                holder.cb_check.setVisibility(View.VISIBLE);
                holder.cb_check.setClickable(true);
                holder.cb_check.setChecked(boolList.get(i));
            } else {
                holder.cb_check.setVisibility(View.INVISIBLE);
                holder.cb_check.setClickable(false);
                holder.cb_check.setChecked(boolList.get(i));
            }
            return convertView;
        }
    }

    private void showPopupWindow() {

        View view = LayoutInflater.from(PersonActivity.this).
                inflate(R.layout.new_popup_person_window, null);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(view);

        TextView tv_person_confirm = (TextView) view.findViewById(R.id.tv_person_confirm);
        TextView tv_person_back = (TextView) view.findViewById(R.id.tv_person_back);
        TextView tv_person_title = (TextView) view.findViewById(R.id.tv_person_title);

        tv_person_confirm.setOnClickListener(this);
        tv_person_back.setOnClickListener(this);
        tv_person_title.setOnClickListener(this);

        View rootView = LayoutInflater.from(PersonActivity.this).inflate(R.layout.new_all_person,
                null);

        if (count_checked > 0) {
            tv_person_title.setText("Are you sure to delete " + count_checked + " datas ?");
        }

        final WindowManager.LayoutParams wl = getWindow().getAttributes();
        wl.alpha = (float) 0.3;
        getWindow().setAttributes(wl);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                wl.alpha = (float) 1;
                getWindow().setAttributes(wl);
            }
        });

        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xff000000));
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

    }
}
