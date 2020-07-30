package com.example.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getName();
    private static final int RC_SIGN_IN=1;
    TextView txt_name, txt_email, txt_pass;
    Button btn_login,btn_register;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    DatabaseReference mDatabaseUse;
    SignInButton btn_google;
    GoogleSignInClient mgoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpUIViews();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUse = FirebaseDatabase.getInstance().getReference().child("blog_users");
        mDatabaseUse.keepSynced(true);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfUser();
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerintent=new Intent(LoginActivity.this,RegisterActivity.class);
               // registerintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerintent);
            }
        });
        // Configure Google Sign In
        configureGoogleClient();
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }
    private void configureGoogleClient() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // for the requestIdToken, this is in the values.xml file that
                // is generated from your google-services.json
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mgoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent=mgoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            progressDialog.setMessage("Signing in with Google.....");
            progressDialog.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(LoginActivity.this, "Google Sign in Succeeded", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Google Sign in Failed " + e, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "signInWithCredential:success: currentUser: " + user.getEmail());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication Succeeded ", Toast.LENGTH_SHORT).show();
                           startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication failed:" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
    private void launchMainActivity(FirebaseUser user) {
        if (user != null) {
           // MainActivity.startActivity(this, user.getDisplayName());
            finish();
        }
    }
    void checkIfUser() {
        final String email = txt_email.getText().toString().trim();
        final String pass = txt_pass.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
            progressDialog.setMessage("Login User.....");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                        checkUserAccount();

                    else
                        Log.d(TAG,"Error login");
                    progressDialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.d(TAG,"sign in failed");
                    Toast.makeText(LoginActivity.this, "No such user exists", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    void checkUserAccount()
    {
        final String uid=mAuth.getCurrentUser().getUid();
        mDatabaseUse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(uid))
                {
                    Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
                    mainintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainintent);
                }
                else {
                    Toast.makeText(LoginActivity.this,"Accoutn Setup required",Toast.LENGTH_SHORT).show();
                    Intent accintent = new Intent(LoginActivity.this, AccountSetupActivity.class);
                   accintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(accintent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    void setUpUIViews()
    {
        txt_email=findViewById(R.id.edit_email);
        txt_pass=findViewById(R.id.edit_pass);
        btn_login=findViewById(R.id.btn_login);
        btn_register=findViewById(R.id.btn_signup);
        progressDialog=new ProgressDialog(this);
        btn_google=(SignInButton)findViewById(R.id.google_sign_in_button);
    }
}
