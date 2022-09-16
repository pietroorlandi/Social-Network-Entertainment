package com.pietroorlandi.socialnetwork_entertainment.gui;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Questa Activity viene mostrata una volta che l'utente ha acceduto con successo alla piattaforma.
 * L'utente potrà navigare tra i 5 fragment, dove con ognuno potrà accedere a funzionalità diverse
 */
public class PlatformActivity extends AppCompatActivity {
    private final String TAG = "DEBUG_PROGRAMMA";
    private BottomNavigationView bottomNavigationView;
    private Fragment fragmentSelected;
    private int idFragmentSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.platform_activity);
        if(savedInstanceState==null){
            // Fragment di Default è l'Home Fragment
            fragmentSelected = new HomeFragment();
            idFragmentSelected = R.id.home;
        }
        else{
            // Nel caso si ruoti lo schermo, si riprende dallo stesso fragment
            idFragmentSelected = savedInstanceState.getInt("idFragmentSelected");
        }
        this.selectFragmentById();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);
        bottomNavigationView.setSelectedItemId(idFragmentSelected);
        // Il fragment di default è l'HomeFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragmentSelected).commit();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener onNav = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //Fragment fragmentSelected = null;
            idFragmentSelected = item.getItemId();
            selectFragmentById();
            // Change the fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragmentSelected).commit();
            return true;
        }
    };


    private void selectFragmentById(){
        switch(idFragmentSelected){
            case R.id.search:
                fragmentSelected = new SearchUserFragment();
                break;
            case R.id.explore:
                fragmentSelected = new ExploreFragment();
                break;
            case R.id.home:
                fragmentSelected = new HomeFragment();
                break;
            case R.id.recommendation:
                fragmentSelected = new ListRecommendationFragment();
                break;
            case R.id.profile:
                fragmentSelected = new MyProfileFragment();
                break;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("idFragmentSelected", idFragmentSelected);
    }

}
