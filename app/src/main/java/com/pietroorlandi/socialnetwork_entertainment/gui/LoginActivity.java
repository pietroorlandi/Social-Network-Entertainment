package com.pietroorlandi.socialnetwork_entertainment.gui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Questa Activity è la prima activity che si vedrà quando verrà lanciata l'applicazione.
 * Permette all'utente di accedere alla piattaforma, questo tramite due metodi di accesso: email e con account Google.
 *
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    final int CODE_SIGN_IN_RESULT=1;
    private final String TAG = "DEBUG_PROGRAMMA";
    private Context ctx;
    private FirebaseAuth auth;
    private Button btnSignUp;
    private Button btnLogin;
    private FirebaseUser user;
    private EditText txtEmail;
    private EditText txtPassword;
    private ProgressBar progressBar;
    private SignInButton btnLoginGoogle;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ctx = this;
        auth = FirebaseAuth.getInstance();
        btnSignUp = this.findViewById(R.id.btn_go_signup);
        btnSignUp.setOnClickListener(this);
        btnLogin = this.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        txtEmail = this.findViewById(R.id.txt_email_login);
        txtPassword = this.findViewById(R.id.txt_password_login);
        progressBar = this.findViewById(R.id.progress_bar_login);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        btnLoginGoogle = findViewById(R.id.btn_login_google);
        btnLoginGoogle.setOnClickListener(this);
        googleSignInClient.signOut(); /* Questo è stato fatto per riuscire a scegliere sempre con che account Google entrare */
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Risultato ritornato dal lancio dell'Intent che riguarda il SignIn con Google*/
        if (requestCode == CODE_SIGN_IN_RESULT) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    user = auth.getCurrentUser();
                                    ProfileDbHandler.setUsernameIfNewProfile(user, ctx);

                                } else {
                                    Toast.makeText(ctx, "Errore nel sign-in con Google", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,task.getException().toString());
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(!isNetworkConnected()){
            Toast.makeText(this, this.getString(R.string.error_internet_connection),Toast.LENGTH_SHORT).show();
            return;
        }
        if (v.getId()==R.id.btn_login_google){
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, CODE_SIGN_IN_RESULT);
        }
        else if (v.getId()==R.id.btn_login){
            this.checkLogin();
        }
        else if(v.getId()==R.id.btn_go_signup){
            Intent intentSignUp = new Intent(this, SignUpActivity.class);
            startActivity(intentSignUp);
        }
    }

    /**
     * Metodo che controlla il Login con Firebase Authentication attraverso l'email e la password
     */
    public void checkLogin(){
        this.progressBar.setVisibility(View.VISIBLE);
        String emailStr = txtEmail.getText().toString();
        String passwordStr = txtPassword.getText().toString();
        if (emailStr.equals("") || passwordStr.equals("")){
            Toast.makeText(ctx, getResources().getString(R.string.empty_value_message), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            auth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                // Se il login è avvenuto correttamente e l'email è verificata si può accedere alla piattaforma
                                if (user.isEmailVerified()) {
                                    ProfileDbHandler.checkProfileIsVerified(user, progressBar);
                                    Intent mainActivityIntent = new Intent(ctx, PlatformActivity.class);
                                    mainActivityIntent.putExtra("user", user);
                                    startActivity(mainActivityIntent);
                                } else {
                                    Toast.makeText(ctx, getResources().getString(R.string.verify_email_message), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ctx, getResources().getString(R.string.login_error_message), Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }


    /**
     * Controlla se il telefono è connesso ad internet
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }



}