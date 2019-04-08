package com.example.bloodbank;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FloatingActionButton fab;
    String donorid;
    String threadid;
    String userid;
    String username;
    DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference("threads");
    Date date;
    ListView messagelist;
    CustomAdapterChat chatadapter;
    List <ModelChat> listofmessages;
    String dname;
    String dimage;
    ImageView donorimage;
    TextView donorname;
    String dbloodgrp;
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.showinfo) {
            Intent intent = new Intent(ChatActivity.this, InfoActivity.class);
            intent.putExtra("Requestnumber", 1);
            intent.putExtra("userid", donorid);
            intent.putExtra("bloodgroup", dbloodgrp);
            startActivity(intent);
        }
        if(id==R.id.removeconnection){
            FirebaseDatabase.getInstance().getReference("connections").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot postDataSnapshot:dataSnapshot.getChildren()){
                        if(postDataSnapshot.getValue().toString().equals(donorid)){
                            postDataSnapshot.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mDatabase.child(threadid).removeValue();
            Intent intent=new Intent(ChatActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    private void scrollMyListViewToBottom() {
        messagelist.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                messagelist.setSelection(chatadapter.getCount() - 1);
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent=getIntent();
        donorid=intent.getStringExtra("donorid");
        username=intent.getStringExtra("username");
        dbloodgrp=intent.getStringExtra("bloodgrp");
        dname=intent.getStringExtra("donorname");
        dimage=intent.getStringExtra("donorimage");
        donorimage=findViewById(R.id.donorimage);
        donorname=findViewById(R.id.donorname);
        Picasso.get().load(dimage).into(donorimage);
        donorname.setText(dname);
        messagelist=findViewById(R.id.messagelist);
        listofmessages=new ArrayList<>();
        chatadapter=new CustomAdapterChat(ChatActivity.this,R.layout.list_item,listofmessages);
        messagelist.setAdapter(chatadapter);
        scrollMyListViewToBottom();
        userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userid.compareTo(donorid)<0){
            threadid=userid+donorid;
        }else
            threadid=donorid+userid;


        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input=findViewById(R.id.input);
                ModelChat userchat=new ModelChat(input.getText().toString(),username);
                mDatabase.child(threadid).child("messages").push().setValue(userchat);
                input.setText("");

            }
        });

        mDatabase.child(threadid).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        ModelChat chat = dataSnapshot.getValue(ModelChat.class);
                        listofmessages.add(new ModelChat(chat.getMessagetext(), chat.getMessageuser()));
                        chatadapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
