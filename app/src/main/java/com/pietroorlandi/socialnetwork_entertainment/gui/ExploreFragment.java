package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Entertainment;
import com.pietroorlandi.socialnetwork_entertainment.logic.EntertainmentDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

/**
 * In questo Fragment l'utente potrà cercare un titolo di intrattenimento, sia attraverso una barra di ricerca, sia attraverso un bottone che permette di filtrare per tipo, categoria e anno di uscita.
 */
public class ExploreFragment extends Fragment implements View.OnClickListener {

    private Context ctx;
    private FirebaseUser user;
    private final static String TAG="DEBUG_PROGRAMMA";
    private RecyclerView recyclerView;
    private EntertainmentAdapter adapter;
    private Button btnSearchEntertainment;
    private Button btnFilterEnterainment;
    private EditText txtSearchEntertainment;
    private ProgressBar progressBar;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explore_fragment, container, false);
        ctx = getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_entertainment);
        btnSearchEntertainment = view.findViewById(R.id.btn_search_entertainment);
        btnSearchEntertainment.setOnClickListener(this);
        btnFilterEnterainment = view.findViewById(R.id.btn_filter_entertainment_search);
        btnFilterEnterainment.setOnClickListener(this);
        txtSearchEntertainment = view.findViewById(R.id.txt_entertainment_search);
        progressBar = view.findViewById(R.id.progress_bar_explore);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        user = FirebaseAuth.getInstance().getCurrentUser();

        FirestoreRecyclerOptions<Entertainment> options = EntertainmentDbHandler.loadDataWithoutQuery();
        adapter = new EntertainmentAdapter(options, false);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.adapter.startListening();
    }


    @Override
    public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        Button btnPressed = ((Button)v);
        if(btnPressed.equals(btnSearchEntertainment)){
            String entertainmentName = txtSearchEntertainment.getText().toString();
            if (entertainmentName.equals("")){
                Toast.makeText(ctx, getResources().getString(R.string.empty_string_error),Toast.LENGTH_SHORT).show();
            }
            else {
                FirestoreRecyclerOptions<Entertainment> options = EntertainmentDbHandler.loadDataWithQueryByName(entertainmentName, progressBar);
                adapter.updateOptions(options);
                recyclerView.setAdapter(adapter);
                this.adapter.startListening();
            }
        }
        else if (btnPressed.equals(btnFilterEnterainment)){
            createNewFilterDialog();
        }

    }

    /**
     * Questo metodo crea e mostra il Dialog che permette all'utente di filtrare i titoli di intrattenimento per tipo, anno di uscita e categoria
     */
    public void createNewFilterDialog(){
        dialogBuilder = new AlertDialog.Builder(ctx);
        final View filterPopupView = getLayoutInflater().inflate(R.layout.filter_entertainment_popup, null);

        MaterialButton btnFilmFilter = filterPopupView.findViewById(R.id.btn_film_filter);
        MaterialButton btnTVShowFilter = filterPopupView.findViewById(R.id.btn_tvshow_filter);
        MaterialButton btnBookFilter = filterPopupView.findViewById(R.id.btn_book_filter);
        btnFilmFilter.setChecked(true);
        ToggleButton btnCategoryFantasyFilter = filterPopupView.findViewById(R.id.btn_filter_category_fantasy);
        ToggleButton btnCategoryActionFilter = filterPopupView.findViewById(R.id.btn_filter_category_action);
        ToggleButton btnCategoryAdventureFilter = filterPopupView.findViewById(R.id.btn_filter_category_adventure);
        ToggleButton btnCategoryFamilyFilter = filterPopupView.findViewById(R.id.btn_filter_category_family);
        ToggleButton btnCategoryComedyFilter = filterPopupView.findViewById(R.id.btn_filter_category_comedy);
        ToggleButton btnCategoryDramaFilter = filterPopupView.findViewById(R.id.btn_filter_category_drama);
        ToggleButton btnCategorySportsFilter = filterPopupView.findViewById(R.id.btn_filter_category_sports);
        ToggleButton btnCategoryDocumentariesFilter = filterPopupView.findViewById(R.id.btn_filter_category_documentaries);
        MaterialButton btnDone = filterPopupView.findViewById(R.id.btn_filter_done);
        RangeSlider rangeSliderReleaseYear = filterPopupView.findViewById(R.id.discrete_rangeslider_release_year);
        dialogBuilder.setView(filterPopupView);
        dialog = dialogBuilder.create();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!btnFilmFilter.isChecked()) && (!btnTVShowFilter.isChecked()) && (!btnBookFilter.isChecked())){
                    Toast.makeText(ctx, getResources().getString(R.string.filter_empty_entertainment_error), Toast.LENGTH_SHORT).show();
                }
                else {
                    List<Float> valuesRelaseYear = rangeSliderReleaseYear.getValues();
                    List<String> listCategoriesFilter = new ArrayList<>();
                    if(btnCategoryFantasyFilter.isChecked()){
                        listCategoriesFilter.add("Fantasy");
                    }
                    if(btnCategoryActionFilter.isChecked()){
                        listCategoriesFilter.add("Action");
                    }
                    if(btnCategoryAdventureFilter.isChecked()){
                        listCategoriesFilter.add("Adventure");
                    }
                    if(btnCategoryFamilyFilter.isChecked()){
                        listCategoriesFilter.add("Family");
                    }
                    if(btnCategoryComedyFilter.isChecked()){
                        listCategoriesFilter.add("Comedy");
                    }
                    if(btnCategoryDramaFilter.isChecked()){
                        listCategoriesFilter.add("Drama");
                    }
                    if(btnCategorySportsFilter.isChecked()){
                        listCategoriesFilter.add("Sports");
                    }
                    if(btnCategoryDocumentariesFilter.isChecked()){
                        listCategoriesFilter.add("Documentaries");
                    }

                    FirestoreRecyclerOptions<Entertainment> options = EntertainmentDbHandler.loadDataWithQueryFilter(
                            btnFilmFilter.isChecked(), btnTVShowFilter.isChecked(), btnBookFilter.isChecked(),
                            valuesRelaseYear.get(0), valuesRelaseYear.get(1),
                            listCategoriesFilter,
                            progressBar);
                    adapter.updateOptions(options);
                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }


}