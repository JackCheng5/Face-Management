package demo.com.facemanagement.demo.com;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import demo.com.facemanagement.R;

/**
 * Created by v-caiwch on 2016/8/20.
 */
public class MyPersonAlertView extends Dialog {

    private Context context;
    private String confirmButtonText;
    private String cacelButtonText;
    private String title;


    private EditText et_get_person_name;

    private TextView bt_add_person_cancel;
    private TextView bt_add_person_confirm;

    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {
        public void pressConfirm(String person_name);
        public void pressCancel();
    }

    public MyPersonAlertView(Context context, String title, String confirmButton, String cancelButton) {

        super(context, R.style.MyAlertView);
        this.context = context;
        this.confirmButtonText = confirmButton;
        this.cacelButtonText = cancelButton;
        this.title = title;
    }

    public void setClickListener (ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_add_person_alertview);
        setTitle(title);
        et_get_person_name = (EditText)findViewById(R.id.et_get_person_name);

        et_get_person_name.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        et_get_person_name.setImeOptions(EditorInfo.IME_ACTION_DONE);

        bt_add_person_cancel = (TextView) findViewById(R.id.bt_add_person_cancel);
        bt_add_person_confirm = (TextView)findViewById(R.id.bt_add_person_confirm);

        bt_add_person_cancel.setOnClickListener(new clickListener());
        bt_add_person_confirm.setOnClickListener(new clickListener());

    }
    private class clickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_add_person_confirm :
                    clickListenerInterface.pressConfirm(et_get_person_name.getText().toString().trim());
                    break;
                case R.id.bt_add_person_cancel :
                    clickListenerInterface.pressCancel();
                    break;
            }
        }
    }
}
