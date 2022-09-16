package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.ReviewDbHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Questa Activity permette all'utente di poter scrivere una review su un determinato titolo di intrattenimento, valutandola con un voto da 1 a 5 e inserendo (se vuole) una recensione testuale
 */
public class WriteReviewActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtName;
    private TextView txtType;
    private FirebaseAuth auth;
    private TextView txtCategory;
    private TextView txtYear;
    private Button bntSubmitReview;
    private Button btnBack;
    private Long idEntertainment;
    private RatingBar ratingReview;
    private EditText txtDescriptionReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review_activity);
        this.txtName = findViewById(R.id.txt_name_entertaiment_review);
        this.txtType = findViewById(R.id.txt_type_entertaiment_review);
        this.txtCategory = findViewById(R.id.txt_category_entertaiment_review);
        this.txtYear = findViewById(R.id.txt_year_entertaiment_review);
        this.bntSubmitReview = findViewById(R.id.btn_submit_review);
        this.ratingReview = findViewById(R.id.rating_review);
        this.txtDescriptionReview = findViewById(R.id.txt_description_review);
        this.bntSubmitReview.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title_entertainment");
        String type = extras.getString("type_entertainment");
        String categories = extras.getString("category_entertainment");
        Long year = extras.getLong("year_entertainment");
        idEntertainment = extras.getLong("id_entertainment");
        this.txtName.setText(title);
        this.txtType.setText(type);
        this.txtCategory.setText(categories);
        this.txtYear.setText(year.toString());
        this.auth = FirebaseAuth.getInstance();
        btnBack = findViewById(R.id.btn_back_write_review);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Button btnPressed = ((Button)v);
        if (btnPressed.equals(bntSubmitReview)){
            FirebaseUser user = auth.getCurrentUser();
            String username = user.getDisplayName();
            String uid = user.getUid();
            String title = txtName.getText().toString();
            double vote = ratingReview.getRating();
            String desc = txtDescriptionReview.getText().toString();
            ReviewDbHandler.createReviewByUser(username, uid, title, this.idEntertainment, vote, desc, this);
            finish();
        }
    }

}
