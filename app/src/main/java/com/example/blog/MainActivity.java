package com.example.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blog.model.Blog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG =MainActivity.class.getName();
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
FirebaseRecyclerOptions<Blog> recyclerOptions;
    FirebaseRecyclerAdapter<Blog,BlogViewHolder> adapter;
 DatabaseReference blogsreference;
    DatabaseReference userreference;
    DatabaseReference likesreference;
    Boolean like;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"In onCreate()");
        mAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null)
                {
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);

                    // Closing all the Activities from stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    // Add new Flag to start new Activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        userreference=FirebaseDatabase.getInstance().getReference().child("blog_users");
        userreference.keepSynced(true);
blogsreference=FirebaseDatabase.getInstance().getReference().child("blogs");
blogsreference.keepSynced(true);
likesreference=FirebaseDatabase.getInstance().getReference().child("Likes");
likesreference.keepSynced(true);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("blogs");

         recyclerOptions =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(query, Blog.class)
                        .build();
    recyclerView=findViewById(R.id.recyclerview);
    recyclerView.setHasFixedSize(true);


         adapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull Blog model) {
                 final String postkey=getRef(position).getKey();
                holder.txt_title.setText(model.getTitle());
                holder.txt_desc.setText(model.getDescription());
                holder.txt_name.setText(model.getName());
                holder.txt_date.setText(model.getPosted());
                Glide.with(getApplicationContext()).load(model.getImage_url()).into(holder.img);
                holder.setuplikebutton(postkey);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent single=new Intent(MainActivity.this,SinglePostActivity.class);
                        single.putExtra("post_key", postkey);
                    startActivity(single);
                    }
                });
                holder.btn_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        processLike(postkey);
                    }
                });
                // holder.img.setImageResource(model.getImage_url());
            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent,false);
                return new BlogViewHolder(view);
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
void processLike(final String postkey)
{
    final String uid=mAuth.getCurrentUser().getUid();
    like=true;

        likesreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(like) {
                    if (snapshot.child(postkey).hasChild(uid)) {
                        like = false;
                        likesreference.child(postkey).child(uid).removeValue();
                    } else {
                        likesreference.child(postkey).child(uid).setValue("randomvalue");
                        like = false;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}
    @Override
    protected void onStart() {
        super.onStart();
        /*
        * Right after the listener has been registered
When a user signs in
When the current user signs out
When the current user changes*/
        mAuth.addAuthStateListener(authStateListener);
       // checkUserAcccount();
        Log.d(TAG,"In onStart()");
        adapter.startListening();
        //checkUserAcccount();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"In onStop()");
        adapter.stopListening();
    }

    public class BlogViewHolder extends RecyclerView.ViewHolder{
View view;
TextView txt_title,txt_desc,txt_name,txt_date;
ImageView img;

ImageButton btn_like;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            txt_title=(TextView)itemView. findViewById(R.id.txt_title);
            txt_desc=(TextView)itemView.findViewById(R.id.txt_desc);
            txt_name=itemView.findViewById(R.id.txt_name);
            btn_like=itemView.findViewById(R.id.btn_like);
            txt_date=itemView.findViewById(R.id.txt_date);
            img=(ImageView)itemView.findViewById(R.id.txt_image);
        }
        public void setuplikebutton(final String postkey)
        {
            likesreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postkey).hasChild(mAuth.getCurrentUser().getUid()))
                        btn_like.setImageResource(R.drawable.ic_thumb_blue);
                    else
                        btn_like.setImageResource(R.drawable.ic_thumb_up);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    void checkUserAcccount() {
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            final String uid = mAuth.getCurrentUser().getUid();
            userreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChild(uid)) {
                        Intent accintent = new Intent(MainActivity.this, AccountSetupActivity.class);
                        accintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(accintent);
                    } else {
                        getSupportActionBar().setTitle("Blog "+snapshot.child(uid).child("name").getValue());
                        Toast.makeText(MainActivity.this, "Account is setup succesfully!!", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_add:
                startActivity(new Intent(this,AddActivity.class));
                break;
            case R.id.action_logout:
                mAuth.signOut();
                Intent intent=new Intent(this,LoginActivity.class);
               // Closing all the Activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Add new Flag to start new Activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
               // finish();
        }
        return true;
    }

}
