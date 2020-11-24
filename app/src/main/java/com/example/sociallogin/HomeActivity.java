package com.example.sociallogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {
    private ShareDialog shareDialog;
    private AppCompatButton logoutBtn;
    ImageView userImage;
    TextView userEmail, userName;
    private static final String TAG = "HomeActivity";
    //auth
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_home);
        logoutBtn = findViewById(R.id.logoutBtn);
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.usernameTV);
        userEmail = findViewById(R.id.emailTV);
        shareDialog = new ShareDialog(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareLinkContent content = new ShareLinkContent.Builder().build();
                shareDialog.show(content);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        mGoogleAccount = GoogleSignIn.getLastSignedInAccount(this);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();

            }
        });
        updateUI();
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String surname = intent.getStringExtra("surname");
        String imageUrl = intent.getStringExtra("imageUrl");
        userName.setText(name + " " + surname);
            new HomeActivity.DownloadImage(userImage).execute(imageUrl);
        }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /**
     * Update UI for logged in user
     */
    private void updateUI() {
        SharedPref sharedPrefs = SharedPref.getInstance();
        HashMap<String, String> userData = sharedPrefs.getUserData(this);
        if (userData != null) {
            userName.setText(userData.get("displayName"));
            userEmail.setText(userData.get("email"));
            Picasso.get().load(userData.get("userImage")).into(userImage);
        }


    }

    /**
     * Logout the user
     */
    private void logoutUser() {
        SharedPref sharedPrefs = SharedPref.getInstance();
        sharedPrefs.clearUserData(this);

        //first sign out from firebase
        mAuth.signOut();
        //then sign out from the google sign in
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "Cant logout", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LoginManager.getInstance().logOut();
        Intent login = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(login);
        finish();

    }
}


