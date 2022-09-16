package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Questo Fragment permette all'utente di visualizzare e modificare le proprie informazioni come username e bio.
 * Inoltre può cliccare il bottone per visualizzare il proprio profilo e fare logout
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener {
    private Context ctx;
    private final static String TAG = "DEBUG_PROGRAMMA";
    private Button btnLogOut;
    private Button btnViewMyProfile;
    private Button btnEditConfirmed;
    private MaterialButton btnNetflix;
    private MaterialButton btnDisneyPlus;
    private MaterialButton btnAmazonPrime;
    private EditText txtEditUsername;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText txtEditBio;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_profile_fragment, container, false);
        this.ctx = getActivity();
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        progressBar = view.findViewById(R.id.progress_bar_profile_fragment);
        txtEditUsername = view.findViewById(R.id.txt_edit_username);
        txtEditUsername.setText(user.getDisplayName());
        txtEditBio = view.findViewById(R.id.txt_edit_bio);
        fillBioWithDatabase();
        btnLogOut = view.findViewById(R.id.btn_logout);
        btnLogOut.setOnClickListener(this);
        btnViewMyProfile = view.findViewById(R.id.btn_view_my_profile);
        btnViewMyProfile.setOnClickListener(this);
        btnEditConfirmed = view.findViewById(R.id.btn_edit_confirmed);
        btnEditConfirmed.setOnClickListener(this);
        btnNetflix = view.findViewById(R.id.btn_netflix);
        btnAmazonPrime = view.findViewById(R.id.btn_amazon_prime);
        btnDisneyPlus = view.findViewById(R.id.btn_disney_plus);
    }

    @Override
    public void onClick(View v) {
        Button btnPressed = ((Button)v);
        if(btnPressed.equals(btnLogOut)){
            Intent intentLogOut = new Intent(this.ctx, LoginActivity.class);
            startActivity(intentLogOut);
        }
        else if(btnPressed.equals(btnEditConfirmed)){
            String newUsername = txtEditUsername.getText().toString();
            String newBio = txtEditBio.getText().toString();
            if (newUsername.equals("")){
                Toast.makeText(ctx, getResources().getString(R.string.empty_string_error),Toast.LENGTH_SHORT).show();
            }
            else{
                /* Aggiorno il profilo */
                boolean hasNetflix = btnNetflix.isChecked();
                boolean hasPrime = btnAmazonPrime.isChecked();
                boolean hasDisneyPlus = btnDisneyPlus.isChecked();
                progressBar.setVisibility(View.VISIBLE);
                ProfileDbHandler.updateProfile(user, newUsername, newBio, hasNetflix, hasPrime, hasDisneyPlus, progressBar, ctx);
            }
        }
        else if(btnPressed.equals(btnViewMyProfile)){
            Intent intent = new Intent(ctx, ProfileActivity.class);
            intent.putExtra("username", user.getDisplayName());
            intent.putExtra("uid", user.getUid());
            ctx.startActivity(intent);
        }
    }

    /**
     * Questo metodo serve per aggiornare in base al suo contenuto nel database Firestore la bio
     */
    public void fillBioWithDatabase(){
        FirebaseFirestore.getInstance()
                .collection("Profile")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                String strBio = document.getString("bio");
                                txtEditBio.setText(strBio);
                            }
                        } else {
                            Log.d(TAG, "Errore nella lettura dati firestore ", task.getException());
                        }
                    }
                });
    }


}