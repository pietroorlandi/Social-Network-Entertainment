package com.pietroorlandi.socialnetwork_entertainment.logic;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.gui.EntertainmentAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;


/**
 * Questa classe offre dei metodi che permettono di comunicare col Database (Firestore Database) per quel che concerne gli intrattenimenti.
 *
 */
public class EntertainmentDbHandler {
    private final static String TAG = "DEBUG_PROGRAMMA";

    /**
     * Metodo che serve per la creazione della recycler view in cui sarà mostrata la lista degli intrattenimenti presenti nella piattaforma.
     * Ritornerò un oggetto di tipo FirestoreRecyclerOptions, che servirà per popolare la recycler view, in cui ci saranno i primi 30 titoli di intrattenimento
     * @return
     */
    public static FirestoreRecyclerOptions<Entertainment> loadDataWithoutQuery() {
        Query query = FirebaseFirestore.getInstance()
                .collection("Entertainment")
                .limit(30);

        FirestoreRecyclerOptions<Entertainment> options
                = new FirestoreRecyclerOptions.Builder<Entertainment>()
                .setQuery(query, Entertainment.class)
                .build();
        return options;
    }

    /**
     * Metodo che ritorna un oggetto di tipo FirestoreRecyclerOptions contenente tutti gli Entertainment in cui il parametro nameEntertainment è prefisso del titolo.
     * Permette quindi all'utente di cercare un determinato intrattenimento in base al titolo
     * Fare attenzione perchè la ricerca è case-sensitive.
     * @param nameEntertainment
     * @param progressBar
     * @return
     */
    public static FirestoreRecyclerOptions<Entertainment> loadDataWithQueryByName(String nameEntertainment, ProgressBar progressBar) {
        Query query = FirebaseFirestore.getInstance()
                .collection("Entertainment")
                .orderBy("title")
                .startAt(nameEntertainment)
                .endAt(nameEntertainment + '\uf8ff') // è tipo la clausola LIKE di SQL
                .limit(30);
        FirestoreRecyclerOptions<Entertainment> options
                = new FirestoreRecyclerOptions.Builder<Entertainment>()
                .setQuery(query, Entertainment.class)
                .build();
        progressBar.setVisibility(View.INVISIBLE);
        return options;
    }

    /**
     * Questo metodo serve per popolare la recycler view in base al filtro voluto dall'utente.
     * Si filtreranno i titoli di intrattenimento in base al tipo, all'anno di rilascio e alla categoria di esso.
     * @param filmIsChecked
     * @param tvShowIsChecked
     * @param bookIsChecked
     * @param yearReleaseMin
     * @param yearReleaseMax
     * @param listCategories
     * @param progressBar
     * @return
     */
    public static FirestoreRecyclerOptions<Entertainment> loadDataWithQueryFilter(boolean filmIsChecked, boolean tvShowIsChecked, boolean bookIsChecked,
                                                                            float yearReleaseMin, float yearReleaseMax,
                                                                            List<String> listCategories,
                                                                            ProgressBar progressBar) {
        List<String> listEntertainmentFilter = new ArrayList();
        String strTypeFilter = null;
        if (filmIsChecked) {
            strTypeFilter = "Movie";
        }
        if (tvShowIsChecked) {
            strTypeFilter = "TV Show";
        }
        if (bookIsChecked) {
            strTypeFilter = "Book";
        }

        Query query = FirebaseFirestore.getInstance()
                .collection("Entertainment")
                .whereEqualTo("type", strTypeFilter)
                .whereGreaterThan("year", yearReleaseMin)
                .whereLessThan("year", yearReleaseMax);

        if (!listCategories.isEmpty()) {
            query = query
                    .whereArrayContainsAny("category", listCategories)
                    .limit(40);
        } else {
            query = query.limit(40);
        }

        FirestoreRecyclerOptions<Entertainment> options
                = new FirestoreRecyclerOptions.Builder<Entertainment>()
                .setQuery(query, Entertainment.class)
                .build();
        progressBar.setVisibility(View.INVISIBLE);
        return options;


    }


    /**
     * Questo metodo si occupa di caricare tutti i titoli di intrattenimento consumati dall'utente
     * @param username
     * @param uid
     * @param isProfileOfUserLogged
     * @param recyclerView
     * @param progressBar
     */
    public static void loadEntertainmentByUser(String username, String uid, boolean isProfileOfUserLogged, RecyclerView recyclerView, ProgressBar progressBar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refEntertainment = db.collection("Entertainment");
        CollectionReference refProfile = db.collection("Profile");
        refProfile.document(uid).collection("listEntertainmentConsumed").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Long> listIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                listIds.add(Long.parseLong(document.getId()));
                                Query query = refEntertainment.whereIn("id", listIds);
                                FirestoreRecyclerOptions<Entertainment> options
                                        = new FirestoreRecyclerOptions.Builder<Entertainment>()
                                        .setQuery(query, Entertainment.class)
                                        .build();
                                EntertainmentAdapter adapter = new EntertainmentAdapter(options, isProfileOfUserLogged);
                                recyclerView.setAdapter(adapter);
                                adapter.startListening();
                            }
                        } else {
                            Log.d(TAG, "Errore");
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }


    /**
     * Questo metodo ritorna un oggetto di tipo FirestoreRecyclerOptions contenente tutti gli intrattenimenti che gli vengono passati come parametro nella lista di id.
     * Verrà utilizzato per popolare la recycler view con i titoli di intrattenimento presenti nella lista passata come parametro.
     *
     * @param listId  lista degli idEntertainment
     * @return
     */
    public static FirestoreRecyclerOptions<Entertainment> loadEntertainmentByIds(List<Long> listId) {
        Log.d(TAG, "Lista id: "+listId);
        Query query = FirebaseFirestore.getInstance()
                .collection("Entertainment")
                .whereIn("id", listId)
                .limit(20);
        FirestoreRecyclerOptions<Entertainment> options
                = new FirestoreRecyclerOptions.Builder<Entertainment>()
                .setQuery(query, Entertainment.class)
                .build();
        return options;
    }

}
