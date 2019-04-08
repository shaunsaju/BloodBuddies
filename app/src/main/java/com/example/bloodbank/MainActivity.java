package com.example.bloodbank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DatabaseReference mDatabase;
    FirebaseUser user;
    String userid;
    String imageurl;
    String uname;
    String dname;
    String email;
    TextView navUsername;
    TextView navEmail;
    ImageView profilepic;
    String bldgr;
    String bloodgrp;
    boolean datafetched = false;
    List<ModelUser> listofusers;
    static List<ModelUser> duplist;
    ListView userslist;
    CustomAdapterUser useradapter;
    ProgressBar progress;
    TextView emptylist;
    FrameLayout fragmenter;
    NavigationView navigationView;


    DatabaseReference cDatabase = FirebaseDatabase.getInstance().getReference("connections");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        navUsername = (TextView) headerView.findViewById(R.id.namer);
        navEmail = (TextView) headerView.findViewById(R.id.emailer);
        profilepic = (ImageView) headerView.findViewById(R.id.profilepic);
        fragmenter = findViewById(R.id.fragment_container);

        Picasso.get().setLoggingEnabled(true);
        listofusers = new ArrayList<>();
        emptylist = findViewById(R.id.emptylist);
        userslist = findViewById(R.id.userslist);
        useradapter = new CustomAdapterUser(MainActivity.this, R.layout.user_item, listofusers);
        userslist.setAdapter(useradapter);
        progress = findViewById(R.id.login_progress);
        ProgressBarAnimation anim = new ProgressBarAnimation(progress, 0, 100);
        anim.setDuration(1000);
        progress.startAnimation(anim);
        progress.setVisibility(View.VISIBLE);

        if (listofusers.size() > 0) {
            emptylist.setVisibility(View.INVISIBLE);
        }
        userslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("donorid", listofusers.get(i).userid);
                intent.putExtra("donorname", listofusers.get(i).username);
                intent.putExtra("donorimage", listofusers.get(i).imageUrl);
                intent.putExtra("bloodgrp", listofusers.get(i).bldgrp);
                intent.putExtra("username", uname);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {

            navigationView.setCheckedItem(R.id.chats);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();
        try {
            userid = user.getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }


        mDatabase.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // showInformation(dataSnapshot);

                Userinfo user = dataSnapshot.getValue(Userinfo.class);
                uname = user.name1 + " " + user.name2 + " " + user.name3;
                email = user.email;
                imageurl = user.getmImageUrl();
                bloodgrp = user.getBldgrp();

                navUsername.setText(uname);
                navEmail.setText(email);
                Picasso.get().load(imageurl).resize(80, 80).centerCrop().into(profilepic);
                datafetched = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (datafetched) {
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                    intent.putExtra("Requestnumber", 1);
                    intent.putExtra("userid", userid);
                    intent.putExtra("bloodgroup", bloodgrp);
                    startActivity(intent);

                }
            }
        });

        cDatabase.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postDataSnapshot : dataSnapshot.getChildren()) {
                    mDatabase.child(postDataSnapshot.getValue().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Userinfo user = dataSnapshot.getValue(Userinfo.class);
                            dname = user.name1 + " " + user.name2 + " " + user.name3;
                            imageurl = user.getmImageUrl();
                            bldgr = user.getBldgrp();

                            listofusers.add(new ModelUser(imageurl, dname, bldgr, user.userid));
                            useradapter.notifyDataSetChanged();
                            if (listofusers.size() > 0) {
                                emptylist.setVisibility(View.INVISIBLE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                progress.setVisibility(View.INVISIBLE);


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("bldgrp", bldgr);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
            if(fragmenter.getVisibility() == View.VISIBLE) {
                fragmenter.setVisibility(View.INVISIBLE);
                navigationView.setCheckedItem(R.id.chats);
            }
            else {
                super.onBackPressed();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.findusers:
                if (datafetched) {

                    duplist = listofusers;
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("bldgrp", bldgr);
                    startActivity(intent);
                } else
                    Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chats:
                fragmenter.setVisibility(View.INVISIBLE);


                break;

            case R.id.info:
                fragmenter.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InfoFragment()).commit();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}