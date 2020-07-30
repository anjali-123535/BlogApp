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
import android.widget.ImageView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AccountSetupActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST =101 ;
    ImageView photo;
    private ProgressDialog progressDialog;
EditText edt_name;
Button btn_setup;
DatabaseReference userreference;
StorageReference reference;
FirebaseAuth mAuth;
    private Uri mImageuri;
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        reference= FirebaseStorage.getInstance().getReference().child("profile_images");
        userreference= FirebaseDatabase.getInstance().getReference().child("blog_users");
        mAuth=FirebaseAuth.getInstance();
        photo=findViewById(R.id.photo_id);
        edt_name=findViewById(R.id.edt_name);
        progressDialog=new ProgressDialog(this);
        btn_setup=findViewById(R.id.btn_setup);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             Intent intent=new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST);

                    }
                });
        btn_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupaccount();
            }
        });
    }
    void setupaccount() {
        final String name = edt_name.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && mImageuri!=null) {
            if (mAuth.getCurrentUser() != null) {
                progressDialog.setMessage("Account setup....");
                progressDialog.show();
                final StorageReference filepath = reference.child(mImageuri.getLastPathSegment());
                UploadTask uploadTask = filepath.putFile(mImageuri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Uri downloaduri=taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        Task<Uri> downloadUrl = filepath.getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String uid = mAuth.getCurrentUser().getUid();
                                DatabaseReference uidreference = userreference.child(uid);
                                uidreference.child("name").setValue(name);
                                if (uri != null)
                                    uidreference.child("image").setValue(uri.toString());
                                else
                                    uidreference.child("image").setValue(null);
                                progressDialog.dismiss();
                                Toast.makeText(AccountSetupActivity.this, "account setup succesfull", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AccountSetupActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccountSetupActivity.this, "account setup not succesfull", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            } else
                Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK && data!=null) {
            Uri imageuri=data.getData();
            CropImage.activity(imageuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(AccountSetupActivity.this);

        }if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageuri = result.getUri();
                photo.setImageURI(mImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
