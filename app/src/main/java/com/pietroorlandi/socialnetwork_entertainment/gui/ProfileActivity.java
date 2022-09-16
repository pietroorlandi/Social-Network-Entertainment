package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.EntertainmentDbHandler;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Questa Activity mostra la pagina di un determinato utente della piattaforma, con numero di follower e numero di utenti seguiti e lista di tutti gli intrattenimenti consumati dall'utente stesso
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "DEBUG_PROGRAMMA";
    private RecyclerView recyclerView;
    private TextView txtUsername;
    private ProgressBar progressBarUser;
    private Button btnNumberFollower;
    private Button btnNumberFollowing;
    private Button btnFollowUser;
    private String uidProfile;
    private String usernameProfile;
    private Button btnBack;
    private FirebaseUser userLoggedIn;
    private boolean isProfileOfUserLogged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        this.isProfileOfUserLogged = false;
        this.userLoggedIn = FirebaseAuth.getInstance().getCurrentUser();
        btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtUsername = findViewById(R.id.txt_username_profile);
        btnNumberFollower = findViewById(R.id.txt_number_follower);
        btnNumberFollower.setOnClickListener(this);
        btnNumberFollowing = findViewById(R.id.txt_number_following);
        btnNumberFollowing.setOnClickListener(this);
        progressBarUser = findViewById(R.id.progress_bar_profile);
        this.usernameProfile = getIntent().getExtras().getString("username","");
        this.uidProfile = getIntent().getExtras().getString("uid","");
        txtUsername.setText(usernameProfile);
        recyclerView = findViewById(R.id.recycler_view_entertainment_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnFollowUser = findViewById(R.id.btn_follow_user);
        if (!userLoggedIn.getUid().equals(uidProfile)){
            btnFollowUser.setOnClickListener(this);
            this.setFollowButton();
        }
        else {
            btnFollowUser.setVisibility(View.INVISIBLE);
            isProfileOfUserLogged = true;
        }
        /* Leggo dal database il numero di follower e utenti seguiti dal profilo */
        this.setNumberFollowerAndFollowing(uidProfile, btnNumberFollower, btnNumberFollowing);

        /* Carico tutti gli intrattenimenti consumati dal profilo */
        EntertainmentDbHandler.loadEntertainmentByUser(usernameProfile, uidProfile, isProfileOfUserLogged, recyclerView, progressBarUser);
    }


    /**
     * Questo metodo si interfaccia al Database e modifica l'interfaccia grafica in modo da avere il numero di follower e utenti seguiti aggiornati.
     * Questi due dati si aggiornano anche quando l'utente fa click su bottone per seguire/non seguire l'utente
     * @param uidProfile
     * @param txtNumberFollower
     * @param txtNumberFollowing
     */
    public void setNumberFollowerAndFollowing(String uidProfile, TextView txtNumberFollower, TextView txtNumberFollowing){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refProfile = db.collection("Profile");
        refProfile.document(uidProfile)
                .collection("following")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()){
                    int size = task.getResult().size();
                    txtNumberFollowing.setText(""+size);
                }
                else{
                    txtNumberFollowing.setText("0");
                }
            }
        });
        refProfile.document(uidProfile)
                .collection("follower")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()){
                    int size = task.getResult().size();
                    txtNumberFollower.setText(""+size);
                }
                else{
                    txtNumberFollower.setText("0");
                }
            }
        });
    }

    /**
     * Viene modificato la grafica del bottone se il profilo è seguito o meno dall'utente loggato
     */
    public void setFollowButton(){
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Profile");
        ref.document(userLoggedIn.getUid())
                .collection("following")
                .document(uidProfile)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()){
                    updateFollowButton(true);
                }
            }
        });
    }

    /**
     * Viene modificata la grafica del bottone in base al fatto che l'utente loggato segua o meno il profilo
     * @param followingUser
     */
    public void updateFollowButton(Boolean followingUser){
        if (followingUser){
            btnFollowUser.setBackgroundColor(getResources().getColor(R.color.grey));
            btnFollowUser.setText("Unfollow");
        }
        else{
            btnFollowUser.setBackgroundColor(getResources().getColor(R.color.blue));
            btnFollowUser.setText("Follow");
        }
    }


    @Override
    public void onClick(View v) {
        Button btnClicked = (Button) v;
        if (!(userLoggedIn.getUid().equals(uidProfile)) && btnClicked.equals(btnFollowUser)){
            String uidLoggedIn = userLoggedIn.getUid();
            String usernameLoggedIn = userLoggedIn.getDisplayName();
            switch (btnFollowUser.getText().toString()){
                case "Follow":
                    ProfileDbHandler.followUser(uidLoggedIn, usernameLoggedIn, uidProfile, usernameProfile);
                    updateFollowButton(true);
                    break;
                case "Unfollow":
                    updateFollowButton(false);
                    ProfileDbHandler.unfollowUser(uidLoggedIn, usernameLoggedIn, uidProfile, usernameProfile);
                    break;
            }
            this.setNumberFollowerAndFollowing(uidProfile, btnNumberFollower, btnNumberFollowing);
        }
        else if (btnClicked.equals(btnNumberFollower)){
            if(btnNumberFollower.getText()!="0") {
                ListUserDialog dialog = new ListUserDialog(this, this.uidProfile, this.usernameProfile, "follower");
                dialog.show();
            }
        }
        else if (btnClicked.equals(btnNumberFollowing)){
            if(btnNumberFollowing.getText()!="0") {
                ListUserDialog dialog = new ListUserDialog(this, this.uidProfile, this.usernameProfile, "following");
                dialog.show();
            }
        }
    }


}
