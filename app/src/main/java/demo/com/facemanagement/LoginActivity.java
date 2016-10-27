package demo.com.facemanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.PersonGroup;

import demo.com.facemanagement.demo.com.IfNetWorkAvailable;
import demo.com.facemanagement.twodimension.com.CaptureActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView tv_subtitle;

    private long exitTime = 0;
    private EditText userEdit;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private ImageView iv_two_dimension;
    private FaceServiceClient fsc;
    private String userKey = "";

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x123:
                    try{
                        fsc = new FaceServiceRestClient(userKey);
                        PersonGroup[] personGroups;
                        personGroups = fsc.getPersonGroups();
                        editor.putString("USER_KEY",userKey);
                        editor.putString("mode","Success");
                        editor.commit();

                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this,MainviewActivity.class);
                        startActivity(intent);
                        userEdit.setText("");
                        finish();
                    }catch (Exception e){
                        Toast.makeText(LoginActivity.this,"Please enter the true key!",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tv_subtitle = (TextView) findViewById(R.id.tv_subtitle);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        iv_two_dimension = (ImageView) findViewById(R.id.iv_two_dimension);

        Typeface typeface1 = Typeface.createFromAsset(getAssets(),"fonts/axis.ttf");
        welcomeText.setTypeface(typeface1);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(),"fonts/axis.ttf");
        tv_subtitle.setTypeface(typeface2);

        userEdit = (EditText) findViewById(R.id.userKey);
        preferences = getSharedPreferences("para",MODE_APPEND);
        editor = preferences.edit();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        String mode = preferences.getString("mode",null);
        if ("Success".equals(mode)){
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this,MainviewActivity.class);
            startActivity(intent);
            finish();
        }
        userEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        userEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);

        iv_two_dimension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this,CaptureActivity.class), 0);
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "Press again to exit the program", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            userEdit.setText(data.getStringExtra(CaptureActivity.EXTRA_RESULT));
        }else {
            Toast.makeText(LoginActivity.this,"Unreasonable two-dimensional code", Toast.LENGTH_SHORT).show();
            userEdit.setText("");
        }
    }

    public void login(View v){

        if (!(IfNetWorkAvailable.isNetWorkAvailable(LoginActivity.this))) {
            Toast.makeText(LoginActivity.this,"It seems to be no net ah,please check your network", Toast.LENGTH_LONG).show();
            return;
        }
        userKey = userEdit.getText().toString().trim();
        if (!(TextUtils.isEmpty(userKey))) {
            try{
                Thread.sleep(500);
                Message msg = new Message();
                msg.what = 0x123;
                handler.sendMessage(msg);
            }catch (Exception e){
                return;
            }
        }else {
            Toast.makeText(LoginActivity.this,"Please do not enter a blank!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (ifHideInput(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean ifHideInput(View view, MotionEvent event) {
        if (view != null && (view instanceof EditText)) {
            int[] l = {0, 0};
            view.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + view.getHeight(),
                    right = left + view.getWidth();
            return !(event.getX() > left && event.getX() < right && event.getY() > top
                    && event.getY() < bottom);
        }
        return false;
    }

    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
