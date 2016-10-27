package demo.com.facemanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import demo.com.facemanagement.demo.com.IfNetWorkAvailable;
import demo.com.facemanagement.demo.com.ShowPopupMenuWindow;

/**
 * Created by v-caiwch on 2016/8/15.
 */
public class PersonPhotoActivity extends AppCompatActivity implements View.OnClickListener{

    private long exitTime = 0;
    Uri uri_pic;

    private static  String person_name;
    private static String userKey;
    private static String gId;
    private static UUID pId;

    private Button bt_submit;
    private ImageView sub_iv_menu;
    private ImageView sub_iv_search;
    private ImageView sub_iv_add;

    private ImageView iv_personImg;

    private TextView sub_tv_person_name;
    private TextView tv_big_personName;
    private TextView tv_sub_personId;
    private TextView photo_tv_title;
    private TextView person_tv_home;

    private TextView photo_groups;
    private TextView photo_person;

    private String groupId;
    private static String temp = "";
    private static int count = 0;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Intent intent = null;

    private void photoOperationSubMenu(View view) {
        final PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.meun_photo,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.select_photo :
                        Intent intent1 = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent1, 1);
                        break;
                    case R.id.take_photo :

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        temp = "/temp" +".jpg" + count++;
                        File file = new File(Environment.getExternalStorageDirectory(),temp);
                        if (file.exists()){
                            file.delete();
                        }
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                        startActivityForResult(intent, 0);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void init () {

        intent = new Intent();
        bt_submit = (Button) findViewById(R.id.bt_submit);

        sub_iv_menu = (ImageView) findViewById(R.id.sub_iv_menu);
        sub_iv_search = (ImageView) findViewById(R.id.sub_iv_search);
        iv_personImg = (ImageView) findViewById(R.id.iv_personImg);
        sub_iv_add = (ImageView) findViewById(R.id.sub_iv_add);

        tv_big_personName = (TextView) findViewById(R.id.tv_big_personName);
        sub_tv_person_name = (TextView) findViewById(R.id.sub_tv_person_name);
        tv_sub_personId = (TextView) findViewById(R.id.tv_sub_personId);

        photo_groups = (TextView) findViewById(R.id.photo_groups);
        photo_person = (TextView) findViewById(R.id.photo_person);

        photo_tv_title = (TextView) findViewById(R.id.photo_tv_title);
        person_tv_home = (TextView) findViewById(R.id.person_tv_home);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_person_photo);
        preferences = getSharedPreferences("para",MODE_APPEND);
        editor = preferences.edit();
        userKey = preferences.getString("USER_KEY",null);
        groupId = preferences.getString("PERSONGROUP_ID", null);
        Bundle bundle = getIntent().getExtras();

        gId = bundle.getString("PERSONGROUP_ID");
        pId = (UUID) bundle.get("PERSON_ID");

        person_name = bundle.getString("Person_Name");

        init();

        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/axis.ttf");
        tv_sub_personId.setTypeface(typeface);
        photo_tv_title.setTypeface(typeface);
        person_tv_home.setTypeface(typeface);

        photo_groups.setTypeface(typeface);
        photo_person.setTypeface(typeface);

        Typeface typefaces = Typeface.createFromAsset(getAssets(),"fonts/axis.ttf");
        tv_big_personName.setTypeface(typefaces);
        sub_tv_person_name.setTypeface(typefaces);

        sub_iv_menu.setOnClickListener(this);
        sub_iv_search.setOnClickListener(this);
        sub_iv_add.setOnClickListener(this);

        photo_groups.setOnClickListener(this);
        photo_person.setOnClickListener(this);

        bt_submit.setOnClickListener(this);

        tv_big_personName.setText(person_name);
        sub_tv_person_name.setText(person_name);
        tv_sub_personId.setText("ID:" + pId.toString());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sub_iv_menu : //subPersonPopupMainMeun(v);
                int [] iv_menu_position = new int[2];
                sub_iv_menu.getLocationOnScreen(iv_menu_position);

                int [] menu_size = new int[2];
                menu_size[0] = sub_iv_menu.getMeasuredHeight();
                menu_size[1] = sub_iv_menu.getMeasuredWidth();

                ShowPopupMenuWindow.showPopupMenuWin(PersonPhotoActivity.this,iv_menu_position,
                        userKey,menu_size,R.layout.new_person_photo);

                break;
            case R.id.sub_iv_search :
                break;
            case R.id.sub_iv_add :  photoOperationSubMenu(v);
                break;
            case R.id.photo_groups :
                intent.setClass(PersonPhotoActivity.this,MainviewActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.photo_person :
                intent.setClass(PersonPhotoActivity.this,PersonActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_submit :
                if (!(IfNetWorkAvailable.isNetWorkAvailable(PersonPhotoActivity.this))) {
                    Toast.makeText(PersonPhotoActivity.this,"It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
                    return;
                }
                submitPhoto();
                break;
            case R.id.tv_group_menu_home :

                editor.putString("mode","Failure");
                editor.remove("USER_KEY");
                editor.remove("PERSONGROUP_ID");
                editor.commit();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                Intent intent = new Intent();
                intent.setClass( PersonPhotoActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_group_menu_key :
                Toast.makeText(PersonPhotoActivity.this,userKey, Toast.LENGTH_LONG).show();
                ShowPopupMenuWindow.getPopupWinMenu().dismiss();
                break;
        }
    }
    public void  submitPhoto () {
        if (uri_pic == null) {
            Toast.makeText(getApplicationContext(),"Please Choose a Picture",Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri_pic);
        } catch (Exception e) {
            Toast.makeText(PersonPhotoActivity.this,"Wrong1!",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        FaceServiceClient fsc = new FaceServiceRestClient(userKey);
        AddPersistedFaceResult result;
        try {
            result = fsc.addPersonFace(gId,pId,inputStream,"User data",null);
            fsc.trainPersonGroup(groupId);
            Toast.makeText(PersonPhotoActivity.this,groupId, Toast.LENGTH_SHORT).show();
            Toast.makeText(PersonPhotoActivity.this,"Upload your photo successful",Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent();
            intent2.setClass(this, PersonActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PersonPhotoActivity.this,"Failure to upload!",Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                File file = new File(Environment.getExternalStorageDirectory(),temp);
                uri_pic = Uri.fromFile(file);
                iv_personImg.setImageURI(uri_pic);
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    uri_pic = data.getData();
                    iv_personImg.setImageURI(uri_pic);
                }
                break;
        }
    }
}
