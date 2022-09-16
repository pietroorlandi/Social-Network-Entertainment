package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.ProfileDbHandler;

/**
 * Questo Dialog mi mostra una lista di profili.
 * Viene utilizzato per mostrata la lista di utenti seguiti oppure la lista di follower di un determinato profilo.
 */
public class ListUserDialog extends Dialog {
    private Context ctx;
    private RecyclerView recyclerView;
    private String type;
    private String uidProfile;
    private String usernameProfile;
    private TextView txtTitle;
    private Dialog dialog;
    private Button btnClose;


    public ListUserDialog(Context ctx, String uidProfile, String usernameProfile, String type) {
        super(ctx);
        this.uidProfile = uidProfile;
        this.usernameProfile = usernameProfile;
        this.type = type;
        this.dialog = this;
        this.ctx = ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_user_dialog);
        txtTitle = findViewById(R.id.txt_title_list_user_dialog);
        if (type.equals("follower")){
            txtTitle.setText(ctx.getString(R.string.list_follower)+usernameProfile);
        }
        else if (type.equals("following")){
            txtTitle.setText(ctx.getString(R.string.list_following)+usernameProfile);
        }
        btnClose = findViewById(R.id.btn_close_list_user_dialog);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        recyclerView = findViewById(R.id.recycler_view_profile_list_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        ProfileDbHandler.loadListOfUser(uidProfile, recyclerView, type);
    }
}
