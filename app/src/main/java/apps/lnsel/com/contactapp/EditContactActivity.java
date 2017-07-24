package apps.lnsel.com.contactapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import apps.lnsel.com.contactapp.VolleyLibrary.AppController;

/**
 * Created by apps2 on 7/24/2017.
 */
public class EditContactActivity extends AppCompatActivity {

    EditText et_contact_person_name, et_contact_no, et_contact_address, et_contact_email;
    TextInputLayout til_contact_person_name, til_contact_no, til_contact_address, til_contact_other_email;
    Button btn_cancel, btn_submit;

    String cntName, cntNumber, cntEmail, cntAddress, cntUsrId;

    private static final String TAG = "REQ_ADD_CONTACT";

    SharedManagerUtil session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);

        session = new SharedManagerUtil(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Contact");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(EditContactActivity.this, MainActivity.class);
                startActivity(signup);
            }
        });

        et_contact_person_name = (EditText) findViewById(R.id.activity_contact_add_et_person_name);
        et_contact_no = (EditText) findViewById(R.id.activity_contact_add_et_contact_no);
        et_contact_address = (EditText) findViewById(R.id.activity_contact_add_et_address);
        et_contact_email = (EditText) findViewById(R.id.activity_contact_add_et_email);

        btn_cancel = (Button) findViewById(R.id.activity_contact_add_btn_cancel);
        btn_submit = (Button) findViewById(R.id.activity_contact_add_btn_submit);

        til_contact_person_name = (TextInputLayout) findViewById(R.id.activity_contact_add_til_person_name);
        til_contact_no = (TextInputLayout) findViewById(R.id.activity_contact_add_til_contact_no);
        til_contact_address = (TextInputLayout) findViewById(R.id.activity_contact_add_til_address);
        til_contact_other_email = (TextInputLayout) findViewById(R.id.activity_contact_add_til_email);

        et_contact_person_name.addTextChangedListener(new MyTextWatcher(et_contact_person_name));
        et_contact_no.addTextChangedListener(new MyTextWatcher(et_contact_no));

        et_contact_person_name.setText(ContactsData.current_cntPersonName);
        et_contact_no.setText(ContactsData.current_cntNumber);
        et_contact_address.setText(ContactsData.current_cntAddress);
        et_contact_email.setText(ContactsData.current_cntEmail);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditContactActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validatePersonName()) {
                    return;
                }
                if (!validateContactNo()) {
                    return;
                }

                cntName = et_contact_person_name.getText().toString();
                cntNumber = et_contact_no.getText().toString();
                cntEmail = et_contact_email.getText().toString();
                cntAddress = et_contact_address.getText().toString();
                cntUsrId = session.getUserID();

                updateContactWebService();

            }
        });
    }

    public void updateContactWebService(){

        String url = WebServiceUrls.UPDATE_CONTACT_URL;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        String str_response = response;

                        try {
                            JSONObject jsonObj = new JSONObject(str_response);
                            String status = jsonObj.getString("status");
                            String message = jsonObj.getString("message");
                            if(status.equals("failed")){
                                Toast.makeText(EditContactActivity.this, message, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(EditContactActivity.this, message, Toast.LENGTH_LONG).show();
                                Intent signup = new Intent(EditContactActivity.this, MainActivity.class);
                                startActivity(signup);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(EditContactActivity.this, "Server not Responding, Please Check your Internet Connection", Toast.LENGTH_LONG).show();

                    }
                }
        ){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("cntId",ContactsData.current_cntId);
                params.put("cntName",cntName);
                params.put("cntNumber",cntNumber);
                params.put("cntEmail",cntEmail);
                params.put("cntAddress",cntAddress);
                params.put("usrId",cntUsrId);
                return params;
            }
        };

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(postRequest);

    }

    //********** Text Watcher for Validation *******************//
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.activity_contact_add_et_person_name:
                    validatePersonName();
                    break;
                case R.id.activity_contact_add_et_contact_no:
                    validateContactNo();
                    break;
            }
        }
    }


    private boolean validatePersonName() {
        if (et_contact_person_name.getText().toString().trim().isEmpty()) {
            til_contact_person_name.setError("person name can not be blank");
            requestFocus(et_contact_person_name);
            return false;
        } else {
            til_contact_person_name.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateContactNo() {
        if (et_contact_no.getText().toString().trim().isEmpty()) {
            til_contact_no.setError("contact no can not be blank");
            requestFocus(et_contact_no);
            return false;
        } else {
            til_contact_no.setErrorEnabled(false);
        }

        return true;
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
