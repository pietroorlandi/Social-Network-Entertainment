package com.pietroorlandi.socialnetwork_entertainment.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

/**
 * Questa classe offre dei metodi che permettono di comunicare con Firestore Database per quel che riguarda le recensioni fatte dagli utenti.
 *
 */
public class ReviewDbHandler {
    private final static String TAG = "DEBUG_PROGRAMMA";

    /**
     * Questo metodo crea una nuova recensione all'interno del Firestore Database da parte dell'utente.
     * Una volta inserita la recensione, andrà a modificare la media voti per quel determinato intrattenimento per cui è stata fatta la recensione.
     * @param username
     * @param uId
     * @param title
     * @param entertainmentId
     * @param vote
     * @param desc
     * @param activity
     */
    public static void createReviewByUser(String username, String uId, String title, Long entertainmentId, double vote, String desc, Activity activity){
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Review");
        Review review = null;
        if (desc.equals("")) {
            review = new Review(username, uId, title, entertainmentId, vote);
        }
        else
        {
            review = new Review(username, uId, title, entertainmentId, vote, desc);
        }
        ref.add(review).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG,"Review inserita");
                updateReviewAverage(entertainmentId, vote, activity); // Add field also in Entertainment document to optimize future view
                Toast.makeText(activity, activity.getString(R.string.review_done), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Questo metodo permette di fare un update dei campi sumReview, countReview e avgReview per un determinato titolo di intrattenimento nel database.
     * Si fa uso di Transaction, questo per evitare le race-conditions, essendo che questi dati potrebbero essere aggiornati contemporaneamente da più utenti.
     * Per calcolare l'avgReview si mantengono anche i campi sumReview e countReview
     * @param eId
     * @param review
     * @param activity
     */
    public static void updateReviewAverage(Long eId, double review, Activity activity){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("Entertainment").whereEqualTo("id",eId).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        DocumentReference ref = db.collection("Entertainment").document(key);
                        db.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(ref);
                                if (snapshot.get("avgReview")==null){   /* Non sono presenti recensioni per questo titolo di intrattenimento nel database*/
                                    Log.d(TAG,"Non vi erano dati su review, creato per la prima volta ");
                                    transaction.update(ref, "avgReview", review);
                                    transaction.update(ref, "sumReview", review);
                                    transaction.update(ref, "countReview", 1);
                                }
                                else{          /*  Sono già presenti recensioni per questo titolo nel database, devo aggiornare */
                                    double sumReview;
                                    long countReview;
                                    try {
                                        sumReview = (Double) snapshot.get("sumReview") ;
                                        countReview = (Long)snapshot.get("countReview");
                                    }
                                    catch (ClassCastException e){
                                        sumReview = (Long) snapshot.get("sumReview");
                                        countReview = (Long)snapshot.get("countReview");
                                    }
                                    long newCountReviewValue;
                                    Double newSumReviewValue=sumReview + review;;
                                    newCountReviewValue = countReview + 1;
                                    Double newAvgReviewValue = (Double) (newSumReviewValue/(double)newCountReviewValue);
                                    transaction.update(ref, "sumReview", newSumReviewValue);
                                    transaction.update(ref, "countReview", newCountReviewValue);
                                    transaction.update(ref, "avgReview", newAvgReviewValue);
                                }
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Transaction success!");
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Transaction failure.", e);
                                    }
                                });
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    /**
     * Con questo metodo controllo se è già stata fatta una recensione per un determinato titolo di intrattenimento da parte dell'utente e nel caso non sia stata fatta, creo l'activity per farla.
     * @param uid
     * @param eid
     * @param context
     * @param intent
     */
    public static void checkReviewExistByUser(String uid, Long eid, Context context, Intent intent){
        CollectionReference ref = FirebaseFirestore.getInstance().collection("Review");
        ref.whereEqualTo("uid",uid).whereEqualTo("idEntertainment",eid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                       if(snapshot.isEmpty()) {
                           Log.d(TAG, "Snapshot review vuoto");
                           context.startActivity(intent);
                       }
                       else{
                           Log.d(TAG, "Review già presente");
                           Toast.makeText(context, context.getResources().getString(R.string.review_already_done), Toast.LENGTH_SHORT).show();
                       }
                    }
                });
    }


}
