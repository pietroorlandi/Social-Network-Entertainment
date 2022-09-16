package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Entertainment;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.pietroorlandi.socialnetwork_entertainment.logic.ReviewDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Questa classe estende la classe FirestoreRecyclerAdapter e permette di fare un binding tra i dati relativi ai titoli di intrattenimento, che saranno mostrati nella recycler view in tempo reale.
 */
public class EntertainmentAdapter extends FirestoreRecyclerAdapter<Entertainment, EntertainmentAdapter.EntertainmentViewHolder> {
    private final static String TAG = "DEBUG_PROGRAMMA";
    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean isProfileOfUserLogged;

    public EntertainmentAdapter(@NonNull FirestoreRecyclerOptions<Entertainment> options, boolean isProfileOfUserLogged)
    {
        super(options);
        this.isProfileOfUserLogged = isProfileOfUserLogged;
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }


    @Override
    protected void onBindViewHolder(@NonNull EntertainmentViewHolder holder,
                     int position, @NonNull Entertainment model)
    {
        holder.txtType.setText(model.getType());
        holder.txtName.setText(model.getTitle());
        holder.txtReleaseYear.setText(model.getYear().toString());
        holder.txtCategory.setText(model.returnCategoriesStr());
        String strAvgReview = model.getStringAverageReview();
        holder.txtAvgReview.setText(strAvgReview);
        if (isProfileOfUserLogged){
            holder.btnAddListConsumed.setVisibility(View.INVISIBLE);
        }

        if (strAvgReview.equals("N/A")){
            holder.rating.setRating(Float.parseFloat("0.0"));
        }
        else
        {
            holder.rating.setRating(Float.parseFloat(strAvgReview));
        }

        // Rendo il singolo elemento cliccabile
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, InfoEntertainmentActivity.class);
                intent.putExtra("entertainment", model);
                context.startActivity(intent);
            }
        });
        // Click sul bottone per scrivere una recensione
        holder.btnWriteReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intentReview = new Intent(context, WriteReviewActivity.class);
                intentReview.putExtra("title_entertainment", model.getTitle());
                intentReview.putExtra("type_entertainment", model.getType());
                intentReview.putExtra("year_entertainment", model.getYear());
                intentReview.putExtra("category_entertainment", model.returnCategoriesStr());
                intentReview.putExtra("id_entertainment", model.getId());
                ReviewDbHandler.checkReviewExistByUser(user.getUid(), model.getId(), v.getContext(), intentReview);
            }
        });
        // Click sul bottone per aggiungere il titolo di intrattenimento alla lista dei consumati
        holder.btnAddListConsumed.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                int lastPosition = holder.getAbsoluteAdapterPosition();
                String documentId = getSnapshots().getSnapshot(lastPosition).getId();
                ProfileDbHandler.addEntertaimentListConsumed(user, model.getTitle(), model.getId(), documentId, context);
            }
        });
        // Click sul bottone per consigliare il titolo a un utente
        holder.btnRecommend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RecommendDialog recommendDialog=new RecommendDialog(v.getContext(), model.getId(), model.getTitle());
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(recommendDialog.getWindow().getAttributes());
                lp.width = (int)(v.getResources().getDisplayMetrics().widthPixels*0.95);
                lp.height = (int)(v.getResources().getDisplayMetrics().heightPixels*0.95);
                recommendDialog.show();
                recommendDialog.getWindow().setAttributes(lp);
            }
        });

    }



    @NonNull
    @Override
    public EntertainmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entertainment_item, parent, false);
        return new EntertainmentViewHolder(view);
    }

    class EntertainmentViewHolder extends RecyclerView.ViewHolder{
        private TextView txtName, txtCategory, txtType, txtAvgReview, txtReleaseYear;
        private Button btnWriteReview;
        private Button btnAddListConsumed;
        private Button btnRecommend;
        private RatingBar rating;
        public EntertainmentViewHolder(@NonNull View itemView)
        {
            super(itemView);
            txtType = itemView.findViewById(R.id.txt_type_entertainment_RV);
            txtName = itemView.findViewById(R.id.txt_name_film_RV);
            txtCategory = itemView.findViewById(R.id.txt_category_film_RV);
            txtReleaseYear = itemView.findViewById(R.id.txt_release_year_entertainment_RV);
            btnWriteReview = itemView.findViewById(R.id.btn_make_review);
            btnAddListConsumed = itemView.findViewById(R.id.btn_add_list_consumed);
            btnRecommend = itemView.findViewById(R.id.btn_recommend_entertainment);
            txtAvgReview = itemView.findViewById(R.id.txt_avg_review);
            rating = itemView.findViewById(R.id.rating_bar_entertainment);
        }
    }
}

