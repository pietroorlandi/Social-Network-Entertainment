package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Recommendation;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.sql.Timestamp;
import java.util.Date;

/**
 *  * Questa classe estende la classe FirestoreRecyclerAdapter e permette di fare un binding tra i dati relativi alle raccomandazioni fatte da un utente a un altro, che saranno mostrati nella recycler view in tempo reale.
 */
public class RecommendationAdapter extends FirestoreRecyclerAdapter<Recommendation, RecommendationAdapter.RecommendationViewHolder> {
    private final static String TAG = "DEBUG_PROGRAMMA";

    public RecommendationAdapter(@NonNull FirestoreRecyclerOptions<Recommendation> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecommendationAdapter.RecommendationViewHolder holder, int position, @NonNull Recommendation model) {
        holder.txtUsername.setText(model.getRecommendedBy());
        holder.txtTitle.setText(model.getTitleEntertainment());
        String strDateTime = new Date(new Timestamp(model.getTimestamp()).getTime()).toString();
        holder.txtTime.setText(strDateTime);
    }

    @NonNull
    @Override
    public RecommendationAdapter.RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                             int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommendation_item, parent, false);
        return new RecommendationAdapter.RecommendationViewHolder(view);
    }

    class RecommendationViewHolder extends RecyclerView.ViewHolder{
        private TextView txtUsername, txtTitle, txtTime;
        public RecommendationViewHolder(@NonNull View itemView)
        {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.recommendation_item_username);
            txtTitle = itemView.findViewById(R.id.recommendation_item_title);
            txtTime = itemView.findViewById(R.id.recommendation_item_time);
        }

    }
}


