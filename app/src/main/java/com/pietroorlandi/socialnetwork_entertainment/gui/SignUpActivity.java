package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.FutureUsernameExistsThread;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Questa Activity permette all'utente di registrarsi alla piattaforma per la prima volta usando come metodo d'accesso la sua mail
 *
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "DEBUG_PROGRAMMA";
    private Context ctx;
    private FirebaseAuth auth;
    private Button btnGoLogin;
    private Button btnSignUp;
    private TextView txtEmail;
    private TextView txtUsername;
    private TextView txtPassword;
    private TextView txtConfirmPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        ctx = this;
        auth = FirebaseAuth.getInstance();
        btnGoLogin = this.findViewById(R.id.btn_go_login);
        btnGoLogin.setOnClickListener(this);
        btnSignUp = this.findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(this);
        txtEmail = this.findViewById(R.id.txt_email_signup);
        txtUsername = this.findViewById(R.id.txt_username_signup);
        txtPassword = this.findViewById(R.id.txt_password_signup);
        txtConfirmPassword = this.findViewById(R.id.txt_confirm_password_signup);
        progressBar = this.findViewById(R.id.progress_bar_signup);

    }

    @Override
    public void onClick(View v) {
        Button btnPressed = ((Button)v);
        if(!isNetworkConnected()){
            Toast.makeText(this, this.getString(R.string.error_internet_connection),Toast.LENGTH_SHORT).show();
            return;
        }
        if (btnPressed.equals(btnSignUp)){
            String email = txtEmail.getText().toString();
            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();
            String confirmPassword = txtConfirmPassword.getText().toString();
            if (email.equals("") || username.equals("") || password.equals("")){
                Toast.makeText(ctx, getResources().getString(R.string.empty_field_error),Toast.LENGTH_SHORT).show();
            }
            else if (! password.equals(confirmPassword)){
                Toast.makeText(ctx, getResources().getString(R.string.password_confirm_error),Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);
                /* Controllo che lo username non sia già presente nel database */
                FutureTask<Boolean> futureTaskUsernameExists = new FutureTask<Boolean>(
                        new FutureUsernameExistsThread(username));
                Executor exec = Executors.newSingleThreadExecutor();
                exec.execute(futureTaskUsernameExists);
                try {
                    Boolean isUsernameAlreadyTaken = futureTaskUsernameExists.get();
                    if (isUsernameAlreadyTaken){ /* Username già presente */
                        Toast.makeText(ctx, ctx.getString(R.string.username_duplicate), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    else {
                        /* Username non presente, creo l'utente sia su Firebase Authentication che nel database */
                        ProfileDbHandler.createUser(auth, email, password, username, this, progressBar);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
        else if (btnPressed.equals(btnGoLogin)){
            Intent intentLogIn = new Intent(this, LoginActivity.class);
            startActivity(intentLogIn);
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
