package com.example.sociallogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    TextView email_txt,name_txt;
    ImageView img;
    //google sign in vars
    private GoogleSignInClient mGoogleSignInClient;
    private AppCompatButton signInButton;
    private CallbackManager callbackManager;
    //firebase auth
    private FirebaseAuth mAuth;
    //req code
    private static final int REQ_GOOGLE_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = findViewById(R.id.googleSignInBtn);
        name_txt = findViewById(R.id._name);
        email_txt = findViewById(R.id.email);
        img = findViewById(R.id.image_view);
        // face book
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton =  findViewById(R.id.fbSignInBtn);
        loginButton.setPermissions("email","public_profile");
        checkLoginStatus();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        //configure google sign in
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //build a google sign client
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mAuth = FirebaseAuth.getInstance();


    }
    // ============================ VIEW CONTROL ===========================

    @Override
    protected void onStart() {
        super.onStart();
        //check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Change the login to home
     */
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }

    // ============================ LOGIN RESULT ===========================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        //check if the intent is launched
        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //google sign successful
                // get data and login to firebase auth
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.e(TAG, "onActivityResult: " + account.getId());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //google sign in failed
                Log.e(TAG, "onActivityResult: SignInFailed", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
    // access token tracker for fb
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken==null){
                name_txt.setText("");
                email_txt.setText("");
                img.setImageResource(0);
                Toast.makeText(MainActivity.this, "User Loged Out", Toast.LENGTH_SHORT).show();
            }else {
                loadUserProfile(currentAccessToken);
            }
        }
    };

    // load user profile

    private void loadUserProfile(AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    String image_url = "https://graph.facebook.com/"+id+"/picture?type=normal";
                    email_txt.setText(email);
                    name_txt.setText(first_name+""+last_name);
                    Picasso.get().load(image_url).into(img);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    // ============================ GOOGLE SIGN IN ===========================

    /**
     * Helps to start the sign in process
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
    }

    /**
     * After getting the google account data we use it to create auth in firebase
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        //got the account credential
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        //use auth for sigin
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //sign in successful, update your UI to go to next activity
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            Log.e(TAG, "onComplete: user details " + mUser.getDisplayName());
                            Log.e(TAG, "onComplete: user details " + mUser.getEmail());
                            Log.e(TAG, "onComplete: user details " + mUser.getUid());
                            saveUserData(mUser);
                            updateUI(mUser);
                        } else {
                            //sign in fails display a message
                            Toast.makeText(MainActivity.this, "Sorry couldnot authenticate you!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // ============================ SAVE DATa ===========================

    /**
     * Save user data to shared preferences
     */
    private void saveUserData(FirebaseUser user) {
        SharedPref sharedPrefs = SharedPref.getInstance();
        sharedPrefs.saveUserData(this,
                user.getDisplayName(),
                user.getEmail(),
                user.getPhotoUrl().toString());
    }

    private void checkLoginStatus(){
        if (AccessToken.getCurrentAccessToken()!=null){
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }
}