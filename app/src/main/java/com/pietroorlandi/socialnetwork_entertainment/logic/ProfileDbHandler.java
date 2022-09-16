package com.pietroorlandi.socialnetwork_entertainment.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pietroorlandi.socialnetwork_entertainment.R;
import com.pietroorlandi.socialnetwork_entertainment.gui.PlatformActivity;
import com.pietroorlandi.socialnetwork_entertainment.gui.ProfileAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Questa classe offre dei metodi che permettono di comunicare col Database per quel che riguarda i Profili utente.
 *
 */
public class ProfileDbHandler {
    private final static String TAG = "DEBUG_PROGRAMMA";

    /**
     * Questo metodo viene usata per creare l'utente la prima volta.
     * Verrà creato l'utente con Firebase Authentication sia nel database di Firestore Database
     * @param auth
     * @param email
     * @param password
     * @param username
     * @param ctx
     * @param progressBar
     */
    public static void createUser(FirebaseAuth auth, String email, String password, String username, Context ctx, ProgressBar progressBar){
        // Creazione user con Firebase auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            // Invio email per verificare l'account
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                /* Creo il profilo dell'utente anche nel database */
                                                Toast.makeText(ctx, "Utente registrato - Verifica Email a "+email,Toast.LENGTH_SHORT).show();
                                                ProfileDbHandler.createProfileUser(user, username);
                                                ((Activity)ctx).finish();
                                            }
                                        }
                                    });
                            /* Settaggio dell'username su Firebase Authentication */
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG,"Aggiornato correttamente username");
                                            } else
                                                Log.d(TAG, task.getException().toString());
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });

                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.error_registration), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }


    /**
     * Creo il profilo dell'utente su Firestore Database, viene fatto quando l'utente accede alla piattaforma per la prima volta
     * @param user
     * @param username
     */
    public static void createProfileUser(FirebaseUser user, String username){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference ref = database.collection("Profile");
        String uid = user.getUid();
        String email = user.getEmail();
        Profile profile = new Profile(uid, username, email);
        ref.document(uid)
                .set(profile)
                .addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete( @NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Profilo creato nel DB correttamente" );
                }   else {
                    Log.d(TAG, task.getException().getMessage() );
                }
            }
        });
    }

    /**
     * Questo metodo serve per generare un nuovo username in base al nome dell'account Google.
     * L'username è scelto a caso è generato combinando le generalità dell'utente e aggiungendo un numero random
     * @param displayName
     * @return
     */
    private static String generateUsernameOfGoogleAccount(String displayName){
        displayName = (displayName.replaceAll(" ", "_")).toLowerCase();
        int random = new Random().nextInt(10000) + 1;
        displayName += random;
        return displayName;
    }

    /**
     * Questo metodo controlla se l'utente che sta cercando di accedere alla piattaforma è nuovo.
     * In tal caso a esso verrà assegnato un username consono (combinazione di nome e cognome piu numero random) e verrà creata l'entry nel database
     * @param user
     * @param ctx
     */
    public static void setUsernameIfNewProfile(FirebaseUser user, Context ctx){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference profileRef = database.collection("Profile");
        profileRef.document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()){
                        String username = generateUsernameOfGoogleAccount(user.getDisplayName());
                        Profile profile = new Profile(user.getUid(), username, user.getEmail(), true);
                        profileRef.document(user.getUid())
                                .set(profile);
                        updateUsername(user, username);
                    }
                    Intent mainActivityIntent = new Intent(ctx, PlatformActivity.class);
                    mainActivityIntent.putExtra("user", user);
                    ctx.startActivity(mainActivityIntent);
                }
            }
        });
    }

    /**
     * Metodo che aggiorna l'username (o display-name) di Google FireAuth
     * @param user
     * @param newUsername
     */
    public static void updateUsername(FirebaseUser user, String newUsername){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"Aggiornato correttamente username in "+newUsername);
                        } else
                            Log.d(TAG,task.getException().toString());
                    }
                });
    }

    /**
     * Metodo che controlla nel database il profilo dato come parametro ha verificato l'email
     * @param user
     * @param progressBar
     */
    public static void checkProfileIsVerified(FirebaseUser user, ProgressBar progressBar){
        String uid = user.getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference ref = database.collection("Profile").document(uid);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Boolean isVerified = documentSnapshot.getBoolean("verified");
                if (!isVerified){
                    ref.update("verified", true);
                    Log.d(TAG, "Update campo 'verified' di Profile");
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    /**
     * Metodo che aggiunge un determinato titolo di Intrattenimento alla lista dei titoli consumati di un determinato user.
     * @param user
     * @param title
     * @param idEntertainment
     * @param docIdEntertainment
     * @param context
     */
    public static void addEntertaimentListConsumed(FirebaseUser user, String title, Long idEntertainment, String docIdEntertainment, Context context){
        String uid = user.getUid();
        String username = user.getDisplayName();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refEntertainment = db.collection("Entertainment");
        CollectionReference refProfile = db.collection("Profile");
        refProfile.document(uid)
                .collection("listEntertainmentConsumed")
                .document(idEntertainment.toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Log.d(TAG, "Context: "+context);
                        Toast.makeText(context, context.getString(R.string.duplicate_entertainment_in_list_consumed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Map<String, Object> dataProfile = new HashMap<>();
                        dataProfile.put("title", title);
                        refProfile.document(uid).collection("listEntertainmentConsumed")
                                .document(idEntertainment.toString()).set(dataProfile);
                        Map<String, Object> dataEntertainment = new HashMap<>();
                        dataEntertainment.put("username", username);
                        refEntertainment.document(docIdEntertainment).collection("consumedBy")
                                .document(uid).set(dataEntertainment);
                        Toast.makeText(context, context.getString(R.string.added_to_list_consumed), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Inseriti nel db dati riguardanti alle liste consumati");
                    }
                }

            }
        });
    }

    /**
     * Metodo che serve per la creazione della recycler view in cui sarà mostrata la lista degli utenti presenti nella piattaforma.
     * Ritorna un oggetto di tipo FirestoreRecyclerOptions in cui vi sono i primi 20 profili presenti nel database (escluso il profilo dell'utente stesso).
     * @param username
     * @return
     */
    public static FirestoreRecyclerOptions<Profile> loadProfileWithoutQuery(String username){
        Query query = FirebaseFirestore.getInstance()
                .collection("Profile")
                .whereNotEqualTo("username", username)
                .limit(20);

        FirestoreRecyclerOptions<Profile> options
                = new FirestoreRecyclerOptions.Builder<Profile>()
                .setQuery(query, Profile.class)
                .build();
        return options;
    }

    /**
     * Metodo che serve per l'aggiornamento della recycler view, permettendo all'utente di poter cercare un determinato User,
     * Fare attenzione perchè la ricerca è case-sensitive.
     * @param usernameQuery
     * @return
     */
    public static FirestoreRecyclerOptions<Profile> loadDataWithQueryByName(String usernameQuery){
        Query query = FirebaseFirestore.getInstance()
                .collection("Profile")
                .orderBy("username")
                .startAt(usernameQuery)
                .endAt(usernameQuery+'\uf8ff') // è tipo la clausola LIKE di SQL
                .limit(10);
        FirestoreRecyclerOptions<Profile> options
                = new FirestoreRecyclerOptions.Builder<Profile>()
                .setQuery(query, Profile.class)
                .build();
        return options;
    }

    /**
     * Metodo che aggiunge nel database il fatto che un utente ne segue un altro.
     * Andrà quindi a lavorare sui field "follower" e "following" dei due utenti
     * @param uidLoggedIn
     * @param usernameLoggedIn
     * @param uidProfile
     * @param usernameProfile
     */
    public static void followUser(String uidLoggedIn, String usernameLoggedIn, String uidProfile, String usernameProfile){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refProfile = db.collection("Profile");
        // Set following
        Map<String, Object> dataUserLoggedIn = new HashMap<>();
        dataUserLoggedIn.put("username", usernameProfile);
        refProfile.document(uidLoggedIn)
                .collection("following")
                .document(uidProfile)
                .set(dataUserLoggedIn);
        // Set follower
        Map<String, Object> dataUserProfile = new HashMap<>();
        dataUserProfile.put("username", usernameLoggedIn);
        refProfile.document(uidProfile)
                .collection("follower")
                .document(uidLoggedIn)
                .set(dataUserProfile);
        Log.d(TAG, "Inseriti nel db dati follower e following");
    }

    /**
     *  Metodo che andrà a cancellare nel database il fatto che un utente ne segue un altro.
     *  Riguarederà quindi i field "follower" e "following" dei due utenti
     * @param uidLoggedIn
     * @param usernameLoggedIn
     * @param uidProfile
     * @param usernameProfile
     */
    public static void unfollowUser(String uidLoggedIn, String usernameLoggedIn, String uidProfile, String usernameProfile){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refProfile = db.collection("Profile");
        // Delete following
        refProfile.document(uidLoggedIn)
                .collection("following")
                .document(uidProfile)
                .delete();
        // Delete follower
        refProfile.document(uidProfile)
                .collection("follower")
                .document(uidLoggedIn)
                .delete();
        Log.d(TAG, "Eliminati nel db dati follower e following");
    }

    /**
     * Aggiunge nel database la raccomandazione di un intrattenimento fatta da un utente a un altro,
     * @param userSource
     * @param usernameDest
     * @param uidDest
     * @param idEntertainment
     * @param titleEntertainment
     */
    public static void sendRecommendationToUser(FirebaseUser userSource, String usernameDest, String uidDest, Long idEntertainment, String titleEntertainment){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refRecommendation = db.collection("Recommendation");

        Map<String, Object> dataRecommendation = new HashMap<>();
        dataRecommendation.put("recommendedBy", userSource.getDisplayName());
        dataRecommendation.put("recommendedTo", usernameDest);
        dataRecommendation.put("idEntertainment", idEntertainment);
        dataRecommendation.put("titleEntertainment", titleEntertainment);
        dataRecommendation.put("timestamp", System.currentTimeMillis());

        refRecommendation.document()
                .set(dataRecommendation);
    }



    public static void updateProfile(FirebaseUser user, String newUsername, String bio, boolean hasNetflix, boolean hasPrime, boolean hasDisneyPlus, ProgressBar pBar, Context ctx){
        if(! (user.getDisplayName().equals(newUsername))){ /* Controllo se sto provando a modificare username */
            FutureTask<Boolean> futureTaskUsernameExists = new FutureTask<Boolean>(
                    new FutureUsernameExistsThread(newUsername));
            Executor exec = Executors.newSingleThreadExecutor();
            exec.execute(futureTaskUsernameExists);
            try {
                Boolean isUsernameAlreadyTaken = futureTaskUsernameExists.get();
                if (isUsernameAlreadyTaken){
                    Toast.makeText(ctx, ctx.getString(R.string.username_duplicate), Toast.LENGTH_SHORT).show();
                    pBar.setVisibility(View.INVISIBLE);
                    return; /* Username già presente, esco dalla funzione */
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();
        Task taskUpdateUsernameAuthentication = user.updateProfile(profileUpdates);
        String oldUsername = user.getDisplayName();
        Map<String, Object> profileData = new HashMap<>();
        List<String> platformOwnByUser = new ArrayList<>();
        DocumentReference ref = FirebaseFirestore.getInstance().collection("Profile")
                .document(user.getUid());
        profileData.put("username", newUsername);
        profileData.put("bio", bio);
        if (hasNetflix){
            platformOwnByUser.add("Netflix");
        }
        if (hasPrime){
            platformOwnByUser.add("Amazon Prime");
        }
        if (hasDisneyPlus){
            platformOwnByUser.add("Disney Plus");
        }
        profileData.put("platform", platformOwnByUser);
        Task taskUpdateProfileDatabase = ref.update(profileData);
        Tasks.whenAllSuccess(taskUpdateUsernameAuthentication, taskUpdateProfileDatabase).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
            @Override
            public void onComplete(@NonNull Task<List<Object>> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "Update del profilo avvenuto correttamente");
                    pBar.setVisibility(View.INVISIBLE);
                    if (oldUsername!=newUsername) {
                        updateDataAssociatedToUsername(user.getUid(), oldUsername, newUsername, ctx);
                    }
                    else{
                        Toast.makeText(ctx, ctx.getString(R.string.update_profile_message), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.d(TAG, "Errore nell'update del profilo");
                }
            }
        });
    }

    /**
     * Questo metodo è chiamato quando l'utente ha deciso di modificare il proprio username.
     * Si andranno quindi a modificare tutti i documenti nel database che riguardano l'username stesso (quindi i vari follower, following, raccomandazioni, recensioni ...)
     * @param uid
     * @param oldUsername
     * @param newUsername
     * @param ctx
     */
    public static void updateDataAssociatedToUsername(String uid, String oldUsername, String newUsername, Context ctx){
        CollectionReference refProfile = FirebaseFirestore.getInstance()
                .collection("Profile");

        refProfile.document(uid).collection("follower").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot docProfile : task.getResult()) {
                        Log.d(TAG, "Follower: "+docProfile.getId());
                        refProfile.document(docProfile.getId())
                                .collection("following")
                                .document(uid)
                                .update("username", newUsername);
                    }
                    }
            }
        });

        refProfile.document(uid).collection("following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot docProfile : task.getResult()) {
                        refProfile.document(docProfile.getId())
                                .collection("follower")
                                .document(uid)
                                .update("username", newUsername);
                    }
                }
            }
        });

        CollectionReference recommendationRef = FirebaseFirestore.getInstance()
                .collection("Recommendation");
        Task<QuerySnapshot> queryRecommendationTo = recommendationRef
                .whereEqualTo("recommendedTo",oldUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot recommendationDoc : task.getResult()){
                                recommendationRef.document(recommendationDoc.getId()).update("recommendedTo",newUsername);
                            }
                        }
                    }
                });

        Task<QuerySnapshot> queryRecommendationBy = recommendationRef
                .whereEqualTo("recommendedBy",oldUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot recommendationDoc : task.getResult()){
                                recommendationRef.document(recommendationDoc.getId()).update("recommendedBy",newUsername);
                            }
                        }
                    }
                });

        CollectionReference reviewRef = FirebaseFirestore.getInstance()
                .collection("Review");
        Task<QuerySnapshot> queryReview = reviewRef
                .whereEqualTo("username",oldUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot reviewDoc : task.getResult()){
                                reviewRef.document(reviewDoc.getId()).update("username",newUsername);
                            }
                            Toast.makeText(ctx, ctx.getString(R.string.update_profile_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Questo metodo è chiamato quando viene cliccato il numero di follower o numero di utenti seguiti da un determinato profilo.
     * Quello che farà è caricare la lista dei profili desiderata
     * @param uidProfile
     * @param recyclerView
     * @param type
     */
    public static void loadListOfUser(String uidProfile, RecyclerView recyclerView, String type){
        CollectionReference refProfile = FirebaseFirestore.getInstance()
                .collection("Profile");
        refProfile
                .document(uidProfile)
                .collection(type)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            List<String> listUsername = new ArrayList<>();
                            for (QueryDocumentSnapshot docProfile : task.getResult()){
                                listUsername.add(docProfile.get("username").toString());
                                Log.d(TAG, docProfile.get("username").toString());
                            }

                            Query query = refProfile.whereIn("username", listUsername);
                            FirestoreRecyclerOptions<Profile> options
                                    = new FirestoreRecyclerOptions.Builder<Profile>()
                                    .setQuery(query, Profile.class)
                                    .build();
                            ProfileAdapter adapter = new ProfileAdapter(options, false, null, null, null);
                            recyclerView.setAdapter(adapter);
                            adapter.startListening();
                        }
                    }
                });
    }


}

