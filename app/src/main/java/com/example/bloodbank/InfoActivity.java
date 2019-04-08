package com.example.bloodbank;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {

    TextView namer1;
    TextView namer2;
    TextView namer3;
    TextView emailer;
    TextView phoner;
    ImageView userpic;
    TextView genderer;
    TextView bloodgrouper;
    TextView locater;
    LatLng ulatLng;
    String imageurl;
    String name;
    int requestnum;
    Button coonecter;
    String userid;
    DatabaseReference mDatabase;
    DatabaseReference lDatabase= FirebaseDatabase.getInstance().getReference("users_location");
    String bloodgrp;
    boolean datafetched=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        namer1 = findViewById(R.id.namer1);
        namer2 = findViewById(R.id.namer2);
        namer3 = findViewById(R.id.namer3);
        emailer = findViewById(R.id.emailer1);
        phoner = findViewById(R.id.phoner);
        userpic = findViewById(R.id.userpic);
        genderer = findViewById(R.id.genderer);
        bloodgrouper = findViewById(R.id.bloodgrouper);
        locater=findViewById(R.id.locater);

        coonecter = findViewById(R.id.connecter);
        Intent intent = getIntent();
        requestnum = intent.getIntExtra("Requestnumber", 0);
        userid = intent.getStringExtra("userid");
        bloodgrp = intent.getStringExtra("bloodgroup");

       if (requestnum == 1) {
            coonecter.setVisibility(View.INVISIBLE);
        }

        mDatabase= FirebaseDatabase.getInstance().getReference("users").child(userid);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Userinfo user=dataSnapshot.getValue(Userinfo.class);
                namer1.setText(user.name1);
                name=user.name1;
                namer2.setText(user.name2);
                namer3.setText(user.name3);
                emailer.setText(user.email);
                genderer.setText(user.gender);
                phoner.setText(user.phone);
                imageurl=user.mImageUrl;
                Picasso.get().load(user.mImageUrl).into(userpic);
                bloodgrouper.setText(user.bldgrp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       lDatabase.child(bloodgrp).child(userid).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lat,lng;
                   lat = dataSnapshot.child("0").getValue().toString();
                    lng = dataSnapshot.child("1").getValue().toString();
                    double latitude = Double.parseDouble(lat);
                    double longitude = Double.parseDouble(lng);
                    ulatLng=new LatLng(latitude,longitude);
                    datafetched=true;

                    getLocation(ulatLng);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void getLocation(LatLng ulatLng){
        String address=" ";
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address>  listaddresses=geocoder.getFromLocation(ulatLng.latitude,ulatLng.longitude,1);
            if(listaddresses!=null&&listaddresses.size()!=0){
                if(listaddresses.get(0).getThoroughfare()!=null)
                    if(listaddresses.get(0).getSubThoroughfare()!=null)
                        address+=listaddresses.get(0).getSubThoroughfare();
                address+=listaddresses.get(0).getThoroughfare();

            }}
        catch(Exception e){
            e.printStackTrace();
        }
        if(address.equals(""))
            locater.setText("Latitude:"+ulatLng.latitude+"Longitude:"+ulatLng.longitude);
        else
            locater.setText(address);

    }

    }

