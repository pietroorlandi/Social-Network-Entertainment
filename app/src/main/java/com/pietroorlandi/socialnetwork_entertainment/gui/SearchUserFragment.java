package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Profile;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Questo Fragment permette all'utente loggato di potere cercare i vari utenti che sono iscritti alla piattaforma.
 * Verrà mostrata tramite una recycler view collegata a Firestore Database una lista degli utenti.
 * Cliccando sul singolo utente si aprirà un'activity in cui saranno presenti i titoli consumati da quell'utente.
 */
public class SearchUserFragment extends Fragment implements View.OnClickListener {
    private Context ctx;
    private RecyclerView recyclerViewProfile;
    private ProfileAdapter adapter;
    private FirebaseUser user;
    private Button btnSearchUser;
    private EditText txtUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_user_fragment, container, false);
        ctx = getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewProfile = view.findViewById(R.id.recycler_view_profile);
        txtUser = view.findViewById(R.id.txt_user_search);
        btnSearchUser = view.findViewById(R.id.btn_search_user);
        btnSearchUser.setOnClickListener(this);
        recyclerViewProfile.setHasFixedSize(true);
        recyclerViewProfile.setLayoutManager(new LinearLayoutManager(view.getContext()));
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirestoreRecyclerOptions<Profile> options = ProfileDbHandler.loadProfileWithoutQuery(user.getDisplayName());
        adapter = new ProfileAdapter(options, false, null, null, null);
        recyclerViewProfile.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        Button btnPressed = ((Button)v);
        if (btnPressed.equals(btnSearchUser)){
            String usernameSearch = txtUser.getText().toString();
            if (usernameSearch.equals("")){
                Toast.makeText(ctx, v.getResources().getString(R.string.empty_string_error),Toast.LENGTH_SHORT).show();
            }
            else {
                FirestoreRecyclerOptions<Profile> options = ProfileDbHandler.loadDataWithQueryByName(usernameSearch);
                adapter.updateOptions(options);
                recyclerViewProfile.setAdapter(adapter);
                adapter.startListening();
            }
        }
    }

}