package com.example.bloodbank;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static com.example.bloodbank.R.layout.activity_login;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,View.OnKeyListener {


    RelativeLayout rel;
    ImageView imageView;
    private FirebaseAuth mAuth;

    EditText email;
    EditText password;

    FirebaseUser currentUser;

    TextView or;
    Button LoginButton;
   ProgressBar progress;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
       try {
           imm.hideSoftInputFromWindow(view.getWindowToken(),0);
       }catch (Exception e){
           e.printStackTrace();
       }
    }


    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            log(view);
        }
        return true;
    }

    public void getToNext() {
        try {
            currentUser = mAuth.getCurrentUser();
       } catch (Exception e) {
           e.printStackTrace();
       }
        if (currentUser != null) {
            progress.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.textView) {

            Intent intent = new Intent(getApplicationContext(), Signner.class);
            startActivity(intent);


        } else if (view.getId() == R.id.rel || view.getId() == R.id.imageView) {
            hideKeyboard(LoginActivity.this);

        }


    }


    public void log(View v){

            if (email.getText().toString().matches("") || password.getText().toString().matches("")) {
                Toast.makeText(this, "EMAIL AND PASSWORD ARE REQUIRED", Toast.LENGTH_SHORT).show();

            }else{
                progress.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("signInWithEmail", "success");

                                getToNext();
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.i( "signInfailure", task.getException().toString());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                    progress.setVisibility(View.INVISIBLE);
                            }

                            // ...
                        }
                    });}
        }

    public void showSettingsDialog(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Needs Permission");
        builder.setMessage("You can grant permission in Settings");
        builder.setPositiveButton("GOTO SETTINGS",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
                Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri=Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);
            }



        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();

    }


    public void requestPermission() {

        Dexter.withActivity(LoginActivity.this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    Log.i("login permission","all granted");
                }else
                if (report.isAnyPermissionPermanentlyDenied()) {
                    showSettingsDialog();

                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                token.continuePermissionRequest();

            }
        }).check();

    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_login);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        requestPermission();
        progress=findViewById(R.id.login_progress);




        LoginButton = (Button) findViewById(R.id.LoginButton);
        or =  (TextView)findViewById(R.id.textView);
       rel=(RelativeLayout)findViewById(R.id.rel);
       imageView=(ImageView)findViewById(R.id.imageView);
       imageView.setImageResource(R.drawable.logo);
        mAuth = FirebaseAuth.getInstance();
        try {
            currentUser = mAuth.getCurrentUser();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        getToNext();

        ProgressBarAnimation anim = new ProgressBarAnimation(progress, 0, 100);
        anim.setDuration(1000);
        progress.startAnimation(anim);



    }

}


