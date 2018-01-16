package com.budi.go_learn.Activity.Pengajar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.budi.go_learn.Controller.RequestHandler;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ProfilPengajar extends AppCompatActivity implements View.OnClickListener{
    private Button btnEditProfile, buttonChoose, buttonCamera;
    private EditText inputFullName, inputPhone, inputEmail, inputAddress, inputPelajaran, inputDescription;
    private String name, phone, email, address, pelajaran, gender, description;
    private android.widget.RadioGroup RadioGroup;
    private RadioButton radioButton;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private SharedPreferences permissionStatus;

    private int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Bitmap bitmap;

    private Uri filePath;
    private String filePat = null;

    private static final String TAG = ProfilPengajar.class.getSimpleName();

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
        setContentView(R.layout.activity_profil_pengajar);

        setTitle("Edit Profil");

        inputFullName = (EditText) findViewById(R.id.name);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputEmail = (EditText) findViewById(R.id.email);
        inputAddress = (EditText) findViewById(R.id.address);
        inputPelajaran = (EditText) findViewById(R.id.pelajaran);
        inputDescription = (EditText) findViewById(R.id.description);
        RadioGroup = (android.widget.RadioGroup) findViewById(R.id.RadioGroup);
        btnEditProfile = (Button) findViewById(R.id.btnEditProfil);
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonCamera = (Button) findViewById(R.id.buttonCamera);
        imageView = (ImageView) findViewById(R.id.editimageView);

        inputDescription.setSingleLine(false);
        inputDescription.setHorizontalScrollBarEnabled(false);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        name = user.get(Pengajar.KEY_NAME);
        phone = user.get(Pengajar.KEY_PHONE);
        email = user.get(Pengajar.KEY_EMAIL);
        address = user.get(Pengajar.KEY_ADDRESS);
        pelajaran = user.get(Pengajar.KEY_PELAJARAN);
        gender = user.get(Pengajar.KEY_GENDER);
        description = user.get(Pengajar.KEY_KET);

        if (gender.equalsIgnoreCase("Laki-laki")==true){
            RadioButton l = (RadioButton) findViewById(R.id.Laki);
            l.setChecked(true);
        }
        else if(gender.equalsIgnoreCase("Perempuan")==true){
            RadioButton p = (RadioButton) findViewById(R.id.Perempuan);
            p.setChecked(true);
        }

        inputFullName.setText(name);
        inputPhone.setText(phone);
        inputEmail.setText(email);
        inputAddress.setText(address);
        inputPelajaran.setText(pelajaran);
        inputDescription.setText(description);
        inputEmail.setEnabled(false);
        inputPelajaran.setEnabled(false);

        btnEditProfile.setOnClickListener(this);
        buttonChoose.setOnClickListener(this);
        buttonCamera.setOnClickListener(this);
        getImage();
    }

    @Override
    public void onClick(View view) {
        if (view == btnEditProfile) {
            name        = inputFullName.getText().toString().trim();
            phone       = inputPhone.getText().toString().trim();
            email       = inputEmail.getText().toString().trim();
            address     = inputAddress.getText().toString().trim();
            int selectedRadioButtonID = RadioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) findViewById(selectedRadioButtonID);
            gender      = radioButton.getText().toString();
            description = inputDescription.getText().toString().trim();

            if(bitmap == null){
                editProfileWP(name, phone, email, address, gender, description);
            }else{
                editProfile(name, phone, email, address, gender, description);
            }
            getImage();
        }

        if (view == buttonChoose) {
            showFileChooser();
        }
        if (view == buttonCamera){
            askPermission();
            captureImage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getImage() {
        class GetImage extends AsyncTask<String,Void,Bitmap> {
            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                imageView.setImageBitmap(b);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                email = params[0];
                String add = Konfigurasi.URL_FOTOPROFIL_PENGAJAR+"?email="+email;
                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(add);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }
        }

        GetImage gi = new GetImage();
        gi.execute(email);
    }

    private void editProfile(final String name, final String phone, final String email,
                             final String address, final String gender,final String description){
        class editProfile extends AsyncTask<Bitmap,Void,String> {

            ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ProfilPengajar.this,"Editing...","Please Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.profil_pengajar),
                        s, Snackbar.LENGTH_LONG);
                mySnackbar.show();
//                Toast.makeText(ProfilPengajar.this,s,Toast.LENGTH_LONG).show();
                db.updatePengajar(name, phone, address, gender, email, description);
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

                HashMap<String,String> data = new HashMap<>();
                data.put(Pengajar.KEY_NAME, name);
                data.put(Pengajar.KEY_PHONE, phone);
                data.put(Pengajar.KEY_EMAIL, email);
                data.put(Pengajar.KEY_ADDRESS, address);
                data.put(Pengajar.KEY_GENDER, gender);
                data.put(Pengajar.KEY_FOTO, uploadImage);
                data.put(Pengajar.KEY_KET, description);

                String result = rh.sendPostRequest(Konfigurasi.URL_EDITPROFIL_PENGAJAR , data);
                return result;
            }
        }

        editProfile ae = new editProfile();
        ae.execute(bitmap);
    }

    private void editProfileWP(final String name, final String phone, final String email,
                             final String address, final String gender,final String description){
        class editProfile extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ProfilPengajar.this,"Editing...","Please Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.profil_pengajar),
                        s, Snackbar.LENGTH_LONG);
                mySnackbar.show();
//                Toast.makeText(ProfilPengajar.this,s,Toast.LENGTH_LONG).show();
                db.updatePengajar(name, phone, address, gender, email, description);
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String,String> data = new HashMap<>();
                data.put(Pengajar.KEY_NAME, name);
                data.put(Pengajar.KEY_PHONE, phone);
                data.put(Pengajar.KEY_EMAIL, email);
                data.put(Pengajar.KEY_ADDRESS, address);
                data.put(Pengajar.KEY_GENDER, gender);
                data.put(Pengajar.KEY_KET, description);

                String result = rh.sendPostRequest(Konfigurasi.URL_EDITPROFIL_PENGAJARWP , data);
                return result;
            }
        }

        editProfile ae = new editProfile();
        ae.execute();
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
                        "Batal", Toast.LENGTH_SHORT)
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

        if (ActivityCompat.checkSelfPermission(ProfilPengajar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfilPengajar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfilPengajar.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ProfilPengajar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfilPengajar.this);
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
                ActivityCompat.requestPermissions(ProfilPengajar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            editor.commit();


        } else {
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission() {
//        Toast.makeText(getBaseContext(), "", Toast.LENGTH_LONG).show();
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
