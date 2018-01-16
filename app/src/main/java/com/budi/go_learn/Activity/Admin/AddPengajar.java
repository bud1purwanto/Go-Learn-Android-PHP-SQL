package com.budi.go_learn.Activity.Admin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPengajar extends AppCompatActivity implements View.OnClickListener{
    private Button btnRegister;
    private Spinner pelajaranSpinner;
    private EditText inputFullName, inputPhone, inputEmail, inputPassword, inputAddress;
    private android.widget.RadioGroup RadioGroup;
    private RadioButton radioButton;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private SharedPreferences permissionStatus;
    ArrayAdapter<String> adapterPelajaran;

    private int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Bitmap bitmap;
    private Uri filePath;
    private String filePat = null;

    private static final String TAG = AddPengajar.class.getSimpleName();

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri;

    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pengajar);
        setTitle("Tambah mPengajar");

        inputFullName = (EditText) findViewById(R.id.name);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputAddress = (EditText) findViewById(R.id.address);
        RadioGroup = (android.widget.RadioGroup) findViewById(R.id.RadioGroup);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        pelajaranSpinner = (Spinner) findViewById(R.id.pelajaranSpinner);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        RadioButton l = (RadioButton) findViewById(R.id.Laki);
        l.setChecked(true);
        spinnerStatus();

        btnRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == btnRegister){
            String name = inputFullName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String address = inputAddress.getText().toString().trim();
            int selectedRadioButtonID = RadioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) findViewById(selectedRadioButtonID);
            String pelajaran = pelajaranSpinner.getSelectedItem().toString();
            String gender = radioButton.getText().toString();

            if (!name.isEmpty() && !email.isEmpty() && !phone.isEmpty()
                    && !address.isEmpty() &&  !password.isEmpty()) {
                registerUser(name, email, phone, gender, address, password, pelajaran);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Masukkan semua data!", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void spinnerStatus(){
        List<String> list1;
        pelajaranSpinner.setPrompt("Pilih Mata Pelajaran");

        list1 = new ArrayList<String>();
        list1.add("Bahasa Indonesia");
        list1.add("Matematika");
        list1.add("IPA");
        list1.add("Bahasa Inggris");

        adapterPelajaran = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list1);
        adapterPelajaran.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pelajaranSpinner.setAdapter(adapterPelajaran);
    }

    private void registerUser(final String name, final String email, final String phone,
                              final String gender, final String address, final String password,
                              final String pelajaran) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Konfigurasi.URL_REGISTER_PENGAJAR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.add_pengajar),
                            "Registrasi Pengajar Berhasil", Snackbar.LENGTH_LONG);
                        mySnackbar.show();
//                        Toast.makeText(getApplicationContext(), "Registrasi pengajar berhasil!", Toast.LENGTH_LONG).show();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Pengajar.KEY_NAME, name);
                params.put(Pengajar.KEY_EMAIL, email);
                params.put(Pengajar.KEY_PHONE, phone);
                params.put(Pengajar.KEY_GENDER, gender);
                params.put(Pengajar.KEY_ADDRESS, address);
                params.put(Pengajar.KEY_PASSWORD, password);
                params.put(Pengajar.KEY_PELAJARAN, pelajaran);
                return params;
            }

        };

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

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                filePat = fileUri.getPath();
                imageView.setVisibility(View.VISIBLE);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                bitmap = BitmapFactory.decodeFile(filePat, options);
                imageView.setImageBitmap(bitmap);

            } else if (resultCode == RESULT_CANCELED) {
                imageView.setImageBitmap(bitmap);
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void askPermission(){
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(AddPengajar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddPengajar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPengajar.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(AddPengajar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPengajar.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(AddPengajar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            editor.commit();


        } else {
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission() {
        Toast.makeText(getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "upload");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + "upload directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
