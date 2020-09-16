package com.example.sociallogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private AppCompatButton logoutBtn;
    ImageView userImage;
    TextView userEmail, userName;

    //auth
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        logoutBtn = findViewById(R.id.logoutBtn);
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.usernameTV);
        userEmail = findViewById(R.id.emailTV);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,signInOptions);
        mGoogleAccount = GoogleSignIn.getLastSignedInAccount(this);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
        updateUI();
    }

    /**
     * Update UI for logged in user
     */
    private void updateUI()
    {
        if(mGoogleAccount !=null)
        {
            userName.setText(mGoogleAccount.getDisplayName());
            userEmail.setText(mGoogleAccount.getEmail());
            Picasso.get().load(mGoogleAccount.getPhotoUrl()).into(userImage);
        }
    }

    /**
     * Logout the user
     */
    private void logoutUser()
    {
        //first sign out from firebase
        mAuth.signOut();
        //then sign out from the google sign in
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(HomeActivity.this, "Cant logout", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}