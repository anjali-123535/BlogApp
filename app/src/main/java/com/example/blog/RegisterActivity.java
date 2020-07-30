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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getName();
    private static final int GALLERY_REQUEST = 890;
    TextView txt_name,txt_email,txt_pass;
Button btn_register;
FirebaseAuth mAuth;
Uri mImageuri;
ImageView img_user;
ProgressDialog progressDialog;
StorageReference photoreference;
DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpUIViews();
        reference=FirebaseDatabase.getInstance().getReference().child("blog_users");
        photoreference= FirebaseStorage.getInstance().getReference().child("profile_images");
        mAuth=FirebaseAuth.getInstance();
        img_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(RegisterActivity.this);
              //  Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("image/*");
                //startActivityForResult(intent, GALLERY_REQUEST);
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             RegisterUser();
            }
        });
    }
    void RegisterUser()
    {
        final String email=txt_email.getText().toString().trim();
        final String name=txt_name.getText().toString().trim();
        final String pass=txt_pass.getText().toString().trim();
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass))
        {
            progressDialog.setMessage("Registering User......");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,"User Registered",Toast.LENGTH_SHORT).show();
                        String uid =mAuth.getCurrentUser().getUid();
                        final DatabaseReference uidreference = reference.child(uid);
                        uidreference.child("name").setValue(name);
                        final StorageReference filepath = photoreference.child(mImageuri.getLastPathSegment());
                        UploadTask uploadTask = filepath.putFile(mImageuri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Uri downloaduri=taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                Task<Uri> downloadUrl = filepath.getDownloadUrl();
                                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        if (mImageuri != null)
                                            uidreference.child("image").setValue(mImageuri.toString());
                                        else
                                            uidreference.child("image").setValue(String.valueOf(R.drawable.ic_person));
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "account setup succesfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "faileed to upload the image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
Log.d(TAG,"Failed to create register user");
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      /*  if(requestCode == GALLERY_REQUEST && resultCode==RESULT_OK && data!=null)
        {
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }*/
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
               mImageuri = result.getUri();
                img_user.setImageURI(mImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    void setUpUIViews()
    {
        txt_name=findViewById(R.id.edit_name);
        txt_pass=findViewById(R.id.edit_pass);
        txt_email=findViewById(R.id.edit_email);
        btn_register=findViewById(R.id.btn_register);
        img_user=findViewById(R.id.photo_id_register);
        progressDialog=new ProgressDialog(this);
    }
}
