package com.budi.go_learn.Activity.Public;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Activity.Admin.AdminActivity;
import com.budi.go_learn.Activity.User.UserActivity;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;
import com.budi.go_learn.Variable.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputPhone;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputAddress;
    private RadioGroup RadioGroup;
    private RadioButton radioButton;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputAddress = (EditText) findViewById(R.id.address);
        RadioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        RadioButton l = findViewById(R.id.Laki);
        l.setChecked(true);

        if (session.isLoggedIn()) {
            HashMap<String, String> user = db.getUserDetails();

            String status = user.get("status");
            if (status.equalsIgnoreCase("admin")){
                Intent intent = new Intent(RegisterActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(RegisterActivity.this, UserActivity.class);
                startActivity(intent);
                finish();
            }
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String phone = inputPhone.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String address = inputAddress.getText().toString().trim();
                int selectedRadioButtonID = RadioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedRadioButtonID);

                String gender = radioButton.getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !phone.isEmpty()
                        && !address.isEmpty() &&  !password.isEmpty()) {
                    registerUser(name, email, phone, gender, address, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Masukkan semua data!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */

    private void registerUser(final String name, final String email, final String phone,
                              final String gender, final String address, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                Konfigurasi.URL_REGISTER_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    Toast.makeText(getApplicationContext(), "Registrasi user berhasil. Coba login sekarang!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(
                            RegisterActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    finish();

                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString(User.KEY_NAME);
                        String email = user.getString(User.KEY_EMAIL);
                        String phone = user.getString(User.KEY_PHONE);
                        String gender = user.getString(User.KEY_GENDER);
                        String address = user.getString(User.KEY_ADDRESS);
                        String created_at = user.getString(User.KEY_CREATED);
                        String status = user.getString(User.KEY_STATUS);

                        db.addUser(name, email, phone, gender, address, uid, created_at, status);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put(User.KEY_NAME, name);
                params.put(User.KEY_EMAIL, email);
                params.put(User.KEY_PHONE, phone);
                params.put(User.KEY_GENDER, gender);
                params.put(User.KEY_ADDRESS, address);
                params.put(User.KEY_PASSWORD, password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
