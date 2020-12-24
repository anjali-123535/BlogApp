package com.example.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = AddActivity.class.getName();
    private ImageButton btn_img;
private EditText edt_title,edt_desc;
private Uri mImageuri;
private Button btn_submit;
private StorageReference mStorage;
private DatabaseReference databaseReference,currentuserreference;
FirebaseUser mCurrentUser;
FirebaseAuth auth;
private static final int GALLERY_REQUEST=1;
private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        mStorage= FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("blogs");
        mCurrentUser=auth.getCurrentUser();
     currentuserreference  =FirebaseDatabase.getInstance().getReference().child("blog_users").child(mCurrentUser.getUid());
        btn_img=findViewById(R.id.imageButton);
        edt_desc=findViewById(R.id.edit_desc);
        edt_title=findViewById(R.id.edit_title);
        btn_submit=findViewById(R.id.btn_submit);
        progressDialog=new ProgressDialog(this );
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        final String title=edt_title.getText().toString().trim();
        final String desc=edt_desc.getText().toString().trim();
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && mImageuri!=null)
        {
            progressDialog.setMessage("Posting to Blog....");
            progressDialog.show();
            final StorageReference filepath=mStorage.child("blog_images").child(mImageuri.getLastPathSegment());
            UploadTask uploadTask=filepath.putFile(mImageuri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri downloaduri=taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    Task<Uri> downloadUrl=filepath.getDownloadUrl();
                    progressDialog.dismiss();
                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            final DatabaseReference reference=databaseReference.push();
                            currentuserreference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    Date date = new Date();
                                    String sdate = dateFormat.format(date);
                                    reference.child("title").setValue(title);
                                    reference.child("description").setValue(desc);
                                    reference.child("image_url").setValue(uri.toString());
                                    reference.child("user_id").setValue(mCurrentUser.getUid());
                                    reference.child("name").setValue(snapshot.child("name").getValue());
                                    reference.child("posted").setValue(sdate.substring(0,11));
                                    Log.d(TAG, "succcesfully download the imageurl "+uri.toString());
                                    onBackPressed();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Failed to download the imageurl");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.d(TAG, "contewnt not uploaded");
                }
            });
        }
        else
            Toast.makeText(AddActivity.this,"Fill the details completely",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            mImageuri=data.getData();
            btn_img.setImageURI(mImageuri);
        }
    }
}
