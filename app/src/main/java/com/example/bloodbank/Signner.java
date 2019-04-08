package com.example.bloodbank;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Signner extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    EditText namer1;
    EditText namer2;
    EditText namer3;
    EditText emailer1;
    EditText passworder1;
    ImageView imageView2;
    String gender = "";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    EditText phone;
    private DatabaseReference mDatabase;
    Spinner s;
    private StorageReference mstorageRef;
    RadioGroup radiosex;
    boolean imageSel = false;
    Uri selectedImage;
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationListener locationListener;
    LocationManager locationManager;
    Marker marker;
    LatLng finalLocation;
    Boolean authenticating =false;
    ProgressBar progress;


    public void getToNext() {
        try {
            currentUser = mAuth.getCurrentUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    public void signUpper(View view) {
            if(authenticating){
                Toast.makeText(this, "Authenticating,Please wait", Toast.LENGTH_SHORT).show();
            }
            else
        if (emailer1.getText().toString().matches("") || passworder1.getText().toString().matches("") || phone.getText().toString().matches("") || namer1.getText().toString().matches("") || (!imageSel)||marker==null) {
            Toast.makeText(this, "ALL FIELDS WITH ASTERISK,LOCATION AND PROFILE PIC ARE NECESSARY", Toast.LENGTH_SHORT).show();


        } else {
            progress.setVisibility(View.VISIBLE);
            authenticating=true;
            mAuth.createUserWithEmailAndPassword(emailer1.getText().toString(), passworder1.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("createUserWithEmail", "success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                String userId = user.getUid();
                                writeNewUser(userId);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("failure", task.getException().toString());
                                Toast.makeText(Signner.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                authenticating=false;
                                progress.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
        }


    }

    public void centerMapOnLocation(Location location) {

        LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());
       // mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation, 12));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signner);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //    .findFragmentById(R.id.map);
        progress=findViewById(R.id.login_progress);
        ProgressBarAnimation anim = new ProgressBarAnimation(progress, 0, 100);
        anim.setDuration(1000);
        progress.startAnimation(anim);
        if (mMap == null) {
            mapFragment = (Workaround) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getUiSettings().setZoomControlsEnabled(true);

                    final NestedScrollView mScrollView = findViewById(R.id.scroll); //parent scrollview in xml, give your scrollview id value
                    ((Workaround) getSupportFragmentManager().findFragmentById(R.id.map))
                            .setListener(new Workaround.OnTouchListener() {
                                @Override
                                public void onTouch() {
                                    mScrollView.requestDisallowInterceptTouchEvent(true);
                                }
                            });
                }
            });
        }


        mapFragment.getMapAsync(this);
        namer1 = (EditText) findViewById(R.id.namer1);
        namer2 = (EditText) findViewById(R.id.namer2);
        namer3 = (EditText) findViewById(R.id.namer3);
        phone = (EditText) findViewById(R.id.phone);
        emailer1 = (EditText) findViewById(R.id.emailer1);
        passworder1 = (EditText) findViewById(R.id.passworder1);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        radiosex = (RadioGroup) findViewById(R.id.radioSex);
        mstorageRef = FirebaseStorage.getInstance().getReference();

        try {
            currentUser = mAuth.getCurrentUser();
        } catch (Exception e) {
            e.printStackTrace();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }});

        ArrayList<String> bloodgrp = new ArrayList<String>();
        bloodgrp.add("O+");
        bloodgrp.add("O-");
        bloodgrp.add("A+");
        bloodgrp.add("A-");
        bloodgrp.add("B+");
        bloodgrp.add("B-");
        bloodgrp.add("AB+");
        bloodgrp.add("AB-");
        ArrayAdapter<String> bloodg = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bloodgrp);
        s = (Spinner) findViewById(R.id.bloodgrouper);
        s.setAdapter(bloodg);

        RadioButton genderBtn = (RadioButton) findViewById(radiosex.getCheckedRadioButtonId());
        gender = genderBtn.getText().toString();
        radiosex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.male:
                        gender = "Male";
                        break;
                    case R.id.female:
                        gender = "Female";
                        break;
                } }}); }

    private void writeNewUser(final String userId) {


        final StorageReference fileReference = mstorageRef.child(userId + "." + getFileExtension(selectedImage));

        fileReference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Userinfo user = new Userinfo(userId,namer1.getText().toString(), namer2.getText().toString(), namer3.getText().toString(), emailer1.getText().toString(), phone.getText().toString(),
                                s.getSelectedItem().toString(), gender, uri.toString());
                        GeoHash geoHash = new GeoHash(new GeoLocation(finalLocation.latitude, finalLocation.longitude));
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("users/" +  userId, user);
                        updates.put("users_location/" + s.getSelectedItem().toString() +"/" + userId + "/g", geoHash.getGeoHashString());
                        updates.put("users_location/" + s.getSelectedItem().toString() +"/" +userId + "/l", Arrays.asList(finalLocation.latitude, finalLocation.longitude));
                        mDatabase.updateChildren(updates);
                        authenticating=false;
                        progress.setVisibility(View.INVISIBLE);
                        getToNext();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signner.this, "Error :uploading image" + e.getMessage(), Toast.LENGTH_SHORT).show();
                currentUser.delete();
                authenticating=false;
                progress.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1)) {
            try {
                selectedImage = data.getData();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if (selectedImage != null) {
                imageSel = true;
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
            Picasso.get().load(selectedImage).into(imageView2);

        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);




        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                centerMapOnLocation(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {


            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }else{

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mMap.setMyLocationEnabled(true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
                try {  Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                  centerMapOnLocation(lastKnownLocation);
              }catch (Exception e){
                  e.printStackTrace();
              }
            }else
                showSettingsDialog();
        }


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        String address=" ";
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address>  listaddresses=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listaddresses!=null&&listaddresses.size()!=0){
                if(listaddresses.get(0).getThoroughfare()!=null)
                    if(listaddresses.get(0).getSubThoroughfare()!=null)
                     address+=listaddresses.get(0).getSubThoroughfare();
                    address+=listaddresses.get(0).getThoroughfare();

            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        if(marker!=null){
            marker.remove();
        }
        finalLocation=latLng;
       marker= mMap.addMarker(new MarkerOptions().position(latLng).title(address));

    }
    public void requestPermission() {

        Dexter.withActivity(Signner.this)
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
                    Log.i("permission","all granted");
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
                Intent intent=new Intent(Signner.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        builder.show();

    }

}

