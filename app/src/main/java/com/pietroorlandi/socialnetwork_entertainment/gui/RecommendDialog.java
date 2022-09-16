package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Profile;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Questo Dialog serve per permettere all'utente di consigliare un titolo di intrattenimento a un qualsiasi altro utente.
 * All'interno di questa finestra di dialogo sarà possibile cercare un utente
 */
public class RecommendDialog extends Dialog implements android.view.View.OnClickListener {

    private Context ctx;
    private long idEntertainment;
    private String titleEntertainment;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private EditText txtUser;
    private ProfileAdapter adapter;
    private Button btnSearchUser;
    private Button btnClose;
    private final static String TAG ="DEBUG_PROGRAMMA";


    public RecommendDialog(Context ctx, long idEntertainment, String titleEntertainment) {
        super(ctx);
        this.ctx = ctx;
        this.idEntertainment = idEntertainment;
        this.titleEntertainment = titleEntertainment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_entertainment_dialog);
        btnSearchUser = findViewById(R.id.btn_search_user_recommend);
        btnClose = findViewById(R.id.btn_close_recommend_dialog);
        btnClose.setOnClickListener(this);
        txtUser = findViewById(R.id.txt_user_search_recommend);
        recyclerView = findViewById(R.id.recycler_view_profile_recommend);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirestoreRecyclerOptions<Profile> options = ProfileDbHandler.loadProfileWithoutQuery(user.getDisplayName());
        adapter = new ProfileAdapter(options, true , idEntertainment, this, titleEntertainment);
        recyclerView.setAdapter(adapter);
        btnSearchUser.setOnClickListener(this);

    }

    @Override
    public void onStart(){
        super.onStart();
        this.adapter.startListening();
    }


    @Override
    public void onClick(View v) {
        Button btnClicked = (Button) v;
        if (btnClicked.equals(btnSearchUser)){
            String usernameSearch = txtUser.getText().toString();
            if (usernameSearch.equals("")){
                Toast.makeText(ctx, v.getResources().getString(R.string.empty_string_error),Toast.LENGTH_SHORT).show();
            }
            else {
                FirestoreRecyclerOptions<Profile> options = ProfileDbHandler.loadDataWithQueryByName(usernameSearch);
                adapter.updateOptions(options);
                recyclerView.setAdapter(adapter);
                adapter.startListening();
            }
        }
        else if(btnClicked.equals(btnClose)){
            this.dismiss();
        }
    }


}
