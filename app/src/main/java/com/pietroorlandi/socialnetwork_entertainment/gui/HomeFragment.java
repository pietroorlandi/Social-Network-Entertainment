package com.pietroorlandi.socialnetwork_entertainment.gui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Entertainment;
import com.pietroorlandi.socialnetwork_entertainment.logic.EntertainmentDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pietroorlandi.socialnetwork_entertainment.logic.RecommenderSystem;

import java.util.List;

/**
 * In questo Fragment verrà mostrata la home della piattaforma.
 * La home è personalizzata per ogni utente, e cerca di consigliare all'utente dei titoli che gli potrebbero piacere.
 * All'interno della classe infatti vi è un riferimento a un oggetto RecommenderSystem, in cui si cerca appunto di avere un sistema di raccomandazione.
 * Il sistema verrà aggiornata se viene fatto lo swipe verso il basso del fragment stesso
 */
public class HomeFragment extends Fragment{

    private final static String TAG = "DEBUG_PROGRAMMA";
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EntertainmentAdapter adapter;
    private RecommenderSystem recommenderSystem;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        return view;
    }

    @SuppressLint("WrongThread")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view_entertainment_home);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar_home);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recommenderSystem = new RecommenderSystem(user.getUid(), this);
        recommenderSystem.recommend();

        /* Quando faccio swipe verso il basso si aggiornerà la lista dei consigliati */
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressBar.setVisibility(View.VISIBLE);
                recommenderSystem.recommend();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Questo metodo verrà chiamato dall'oggetto appartenente alla classe RecommenderSystem, quando avrà finito di elaborare i propri risultati, e aggiornerà i contenuti da mostrare all'utente.
     * @param listRecommendation
     */
    public void updateUIRecommendationSystem(List<Long> listRecommendation){
        FirestoreRecyclerOptions<Entertainment> options = EntertainmentDbHandler.loadEntertainmentByIds(listRecommendation);
        this.adapter = new EntertainmentAdapter(options, false);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        progressBar.setVisibility(View.INVISIBLE);
    }


}