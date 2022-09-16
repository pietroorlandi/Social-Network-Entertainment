package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Profile;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Questa classe estende la classe FirestoreRecyclerAdapter e permette di fare un binding tra i dati relativi ai profili e che saranno mostrati nella recycler view in tempo reale.
 */
public class ProfileAdapter extends FirestoreRecyclerAdapter<Profile, ProfileAdapter.ProfileViewHolder> {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean recommendToUser;
    private Long idEntertainment;
    private String titleEntertainment;
    private Dialog dialog;
    private final static String TAG = "DEBUG_PROGRAMMA";

    public ProfileAdapter(@NonNull FirestoreRecyclerOptions<Profile> options, boolean recommendToUser, Long idEntertainment, RecommendDialog dialog, String titleEntertainment)
    {
        super(options);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        this.idEntertainment = idEntertainment;
        this.titleEntertainment = titleEntertainment;
        this.recommendToUser = recommendToUser;
        this.dialog = dialog;
    }


    @Override
    protected void onBindViewHolder(@NonNull ProfileViewHolder holder,
                                    int position, @NonNull Profile model)
    {
        holder.txtUsernameProfile.setText(model.getUsername());
        holder.txtBioProfile.setText(model.getBio());
        if(this.recommendToUser){
            holder.btnSendRecommendation.setVisibility(View.VISIBLE);
            holder.btnSendRecommendation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (idEntertainment!=null && titleEntertainment!=null) {
                        ProfileDbHandler.sendRecommendationToUser(user, model.getUsername(), model.getUid(), idEntertainment, titleEntertainment);
                        dialog.dismiss();
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.recommendation_done), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("username", model.getUsername());
                intent.putExtra("uid", model.getUid());
                context.startActivity(intent);
                Log.d(TAG, "Username: "+model.getUsername()+" uid: "+model.getUid());
            }
        });

    }


    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ProfileViewHolder(view);
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView txtUsernameProfile;
        private TextView txtBioProfile;
        private Button btnSendRecommendation;

        public ProfileViewHolder(@NonNull View itemView)
        {
            super(itemView);
            txtUsernameProfile = itemView.findViewById(R.id.txt_username_profile_RV);
            txtBioProfile = itemView.findViewById(R.id.txt_bio_profile_RV);
            btnSendRecommendation = itemView.findViewById(R.id.btn_send_recommendation);

        }
    }
}

