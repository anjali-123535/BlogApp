package com.example.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;

public class SinglePostActivity extends AppCompatActivity {
TextView title,desc;
ImageView imageView;
FirebaseAuth auth;
Button remove;
DatabaseReference postreference,userreference,likereference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);
        final String postkey= getIntent().getStringExtra("post_key");
        title=findViewById(R.id.posttitle);
        desc=findViewById(R.id.postdesc);
        imageView=findViewById(R.id.postimage);
        remove=findViewById(R.id.btn_remove);
        auth=FirebaseAuth.getInstance();
           String userid=auth.getCurrentUser().getUid();
        postreference= FirebaseDatabase.getInstance().getReference().child("blogs");
        likereference=FirebaseDatabase.getInstance().getReference().child("Likes");
        postreference.child(postkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    title.setText((String) snapshot.child("title").getValue());
                    desc.setText((String) snapshot.child("description").getValue());
                    String uid = (String) snapshot.child("user_id").getValue();
                    String imageuri = (String) snapshot.child("image_url").getValue();
                    if (imageuri != null)
                        Glide.with(getApplicationContext()).load(imageuri).into(imageView);
                    else
                        Toast.makeText(SinglePostActivity.this, "failed to load the image", Toast.LENGTH_SHORT).show();
                    if (auth.getCurrentUser().getUid().equals(uid)) {
                        remove.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postreference.child(postkey).removeValue();
                likereference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.hasChild(postkey))
                   {
                       likereference.child(postkey).removeValue();
                   }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                startActivity(new Intent(SinglePostActivity.this,MainActivity.class));
            }
        });


    }
}
