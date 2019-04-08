package com.example.bloodbank;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    int radius=1;
    LocationManager locationManager;
    LocationListener locationListener;
    String name;
    String email;
    String imageurl;
    boolean datafetched=false;
    ImageView profilepicer;
    Spinner s;
    Location lastLocation;
    String bldgr;
    TextView donorname;
    TextView donoremail;
    LinearLayout donorinfo;
    DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference("users_location");
    DatabaseReference Database=FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference cDatabase=FirebaseDatabase.getInstance().getReference();
    String donorid;
    String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
    boolean userfound;
    private Circle lastUserCircle;
    private long pulseDuration = 5000;
    private ValueAnimator lastPulseAnimator;
    Marker marker;
    private BottomSheetBehavior mBottomSheetBehaviour;
     LatLng dlatlng;
    LatLng ulatlng;
    Button mark;
    ProgressBar progress;
    Button chatter;






    private void addPulsatingEffect(final LatLng userLatlng){
        if(lastPulseAnimator != null){
            lastPulseAnimator.cancel();
            Log.d("onLocationUpdated: ","cancelled" );
        }
        if(lastUserCircle != null)
            lastUserCircle.setCenter(userLatlng);
        lastPulseAnimator = valueAnimate(radius*90*10f, pulseDuration, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(lastUserCircle != null)
                    lastUserCircle.setRadius((Float) animation.getAnimatedValue());
                else {
                    lastUserCircle = mMap.addCircle(new CircleOptions()
                            .center(userLatlng)
                            .radius(radius*90*10f)
                            .strokeColor(0xF0DC143C)
                            .fillColor(0X80DC143C));
                }
            }
        });

    }
    protected ValueAnimator valueAnimate(float accuracy,long duration, ValueAnimator.AnimatorUpdateListener updateListener){
        Log.d( "valueAnimate: ", "called");
        ValueAnimator va = ValueAnimator.ofFloat(0,accuracy);
        va.setDuration(duration);
        va.addUpdateListener(updateListener);
        va.setRepeatCount(1);
        va.setRepeatMode(ValueAnimator.REVERSE);

        va.start();
        return va;
    }




    public void searchDonor(View view){
        progress.setVisibility(View.VISIBLE);
        datafetched=false;
        imageurl=null;
        radius=1;
        userfound=false;
        donorname.setText("");
        donoremail.setText("");
        profilepicer.setImageDrawable(null);


        mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);


        if(lastLocation!=null)
            getClosestDonor(lastLocation, s.getSelectedItem().toString());
        else
        {
            Toast.makeText(this, "Unable to get your location", Toast.LENGTH_SHORT).show();
        } }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        donoremail=findViewById(R.id.donoremail);
        donorname=findViewById(R.id.donorname);
        donorinfo=findViewById(R.id.donorinfo);
        profilepicer=findViewById(R.id.profilepicer);
        userfound=false;
        mark=findViewById(R.id.mark);
        View bottomsheet =findViewById(R.id.bottom_sheet);
        mBottomSheetBehaviour =BottomSheetBehavior.from(bottomsheet);
        progress=findViewById(R.id.login_progress);
        ProgressBarAnimation anim = new ProgressBarAnimation(progress, 0, 100);
        chatter=findViewById(R.id.chatter);
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
        s = (Spinner) findViewById(R.id.bloodgro);
        s.setAdapter(bloodg);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
    public void centerMapOnLocation(Location location) {

        LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation, 12));

    }



    public void getClosestDonor(final Location lastLocation, final String bldgrp){
        final Location lastl=lastLocation;
       GeoFire geoFire=new GeoFire(mDatabase.child(bldgrp));
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(lastl.getLatitude(),lastl.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!userfound&&(!key.equals(userid))) {
                    progress.setVisibility(View.INVISIBLE);
                    final LatLng ulocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    addPulsatingEffect(ulocation);
                    userfound = true;
                    donorid=key;
                    mDatabase.child(bldgrp).child(donorid).child("l").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String lat, lng;


                            lat = dataSnapshot.child("0").getValue().toString();
                            lng = dataSnapshot.child("1").getValue().toString();

                            double latitude = Double.parseDouble(lat);
                            double longitude = Double.parseDouble(lng);
                            dlatlng = new LatLng(latitude, longitude);
                            ulatlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(ulatlng);
                            builder.include(dlatlng);
                            LatLngBounds bounds = builder.build();

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
                            mMap.animateCamera(cu, new GoogleMap.CancelableCallback(){

                                public void onCancel(){}

                                public void onFinish(){
                                    CameraUpdate zout = CameraUpdateFactory.zoomBy(-1);
                                    mMap.animateCamera(zout);
                                }
                            }); }
                            @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    Database.child(donorid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Userinfo user = dataSnapshot.getValue(Userinfo.class);
                            name= user.name1+" "+user.name2+" "+user.name3;
                            email=user.email;
                            imageurl=user.getmImageUrl();
                            bldgr=user.getBldgrp();

                            Picasso.get().load(imageurl).resize(150,150).centerCrop().into(profilepicer, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });
                            datafetched=true;
                            donoremail.setText(email);
                            donorname.setText(name);
                            donorinfo.setVisibility(View.VISIBLE);
                            chatter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    boolean flag=false;
                                    int size=MainActivity.duplist.size();

                                    for(int i=0;i<size;i++){
                                        if(MainActivity.duplist.get(i).username.equals(name)){
                                            flag=true;
                                        }

                                    }
                                    if(!flag) {
                                        cDatabase.child("connections").child(userid).push().setValue(donorid);
                                        cDatabase.child("connections").child(donorid).push().setValue(userid);

                                        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }else{
                                        Toast.makeText(MapsActivity.this, "Already Connected", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!userfound){
                    radius++;
                    getClosestDonor(lastl,bldgrp);
                } }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        }); }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation=location;
                if(!datafetched) {
                    centerMapOnLocation(location);
                }
            }@Override
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
               try{ Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation);
                lastLocation=lastKnownLocation;}
                catch (Exception e){
                   e.printStackTrace();
                }

            }else
                showSettingsDialog();
        }


    }
    public void requestPermission() {

        Dexter.withActivity(MapsActivity.this)
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
        builder.setTitle("Needs Permission or Gps connection");
        builder.setMessage("You can grant permission in Settings or Drop down menu");
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
                Intent intent=new Intent(MapsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.show();

    }
    public void showLocation(View v){



        Target mTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {


                String address=" ";
                Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address>  listaddresses=geocoder.getFromLocation(dlatlng.latitude,dlatlng.longitude,1);
                    if(listaddresses!=null&&listaddresses.size()!=0){
                        if(listaddresses.get(0).getThoroughfare()!=null)
                            if(listaddresses.get(0).getSubThoroughfare()!=null)
                                address+=listaddresses.get(0).getSubThoroughfare();
                        address+=listaddresses.get(0).getThoroughfare();

                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
                marker=   mMap.addMarker(new MarkerOptions()
                        .position(dlatlng)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(name+"'s Location")
                        .snippet(address)
                );
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
           Log.i("pci",e.toString());
            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };


        Picasso.get()
                .load(imageurl)
                .resize(250, 250)
                .centerCrop()
                .transform(new BubbleTransformation(20))
                .into(mTarget);


    }


}
