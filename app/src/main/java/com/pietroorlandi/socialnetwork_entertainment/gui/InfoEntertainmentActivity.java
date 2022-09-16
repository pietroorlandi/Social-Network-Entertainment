package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.logic.Entertainment;

/**
 * Questa Activity mi mostra tutte le informazioni disponibili per un determinato intrattenimento.
 */
public class InfoEntertainmentActivity extends AppCompatActivity {
    private Entertainment entertainment;
    private TextView txtTitleEntertainment;
    private TextView txtCategory;
    private TextView txtTypeEntertainment;
    private TextView txtReleaseYear;
    private Button btnBack;
    private TextView txtPlatforms;
    private TextView txtAverageReview;
    private TextView txtDuration;
    private RatingBar ratingBar;


    private final String TAG = "DEBUG_PROGRAMMA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_entertainment);
        entertainment = (Entertainment) getIntent().getSerializableExtra("entertainment");
        txtTitleEntertainment = findViewById(R.id.txt_title_entertaiment);
        txtCategory = findViewById(R.id.txt_category_entertaiment);
        txtTypeEntertainment = findViewById(R.id.txt_type_entertaiment);
        txtReleaseYear = findViewById(R.id.txt_year_entertaiment);
        txtPlatforms = findViewById(R.id.txt_platforms_entertainment);
        txtDuration = findViewById(R.id.txt_duration_entertainment);
        ratingBar = findViewById(R.id.rating_review_entertainment_info);
        txtAverageReview = findViewById(R.id.txt_avg_review_info);
        txtTitleEntertainment.setText(entertainment.getTitle());
        txtCategory.setText(entertainment.returnCategoriesStr());
        txtTypeEntertainment.setText(entertainment.getType());
        txtReleaseYear.setText(entertainment.getYear().toString());
        txtPlatforms.setText(entertainment.getPlatform());
        txtDuration.setText(entertainment.getDuration());
        String strAvgReview = entertainment.getStringAverageReview();
        txtAverageReview.setText(strAvgReview);
        if (strAvgReview.equals("N/A")){
            ratingBar.setRating(Float.parseFloat("0.0"));
        }
        else
        {
            ratingBar.setRating(Float.parseFloat(strAvgReview));
        }
        btnBack = findViewById(R.id.btn_back_info_entertainment);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}
