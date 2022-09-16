package com.pietroorlandi.socialnetwork_entertainment.gui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Recommendation;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Questo Fragment permette all'utente di vedere la lista di tutte le raccomandazione che gli sono state inviate da altri utenti, mettendole in ordine cronologico.
 *
 */
public class ListRecommendationFragment extends Fragment  {

    private FirebaseUser user;
    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;
    private final static String TAG = "DEBUG_PROGRAMMA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_recommendation_fragment, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view_list_recommendation);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        Query query = FirebaseFirestore.getInstance()
                .collection("Recommendation")
                .whereEqualTo("recommendedTo",user.getDisplayName())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30);
        FirestoreRecyclerOptions<Recommendation> options
                = new FirestoreRecyclerOptions.Builder<Recommendation>()
                .setQuery(query, Recommendation.class)
                .build();

        adapter = new RecommendationAdapter(options);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.adapter.startListening();
    }

}