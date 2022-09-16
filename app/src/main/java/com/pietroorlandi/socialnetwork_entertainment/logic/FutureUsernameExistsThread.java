package com.pietroorlandi.socialnetwork_entertainment.logic;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Questa classe permette di controllare se è già presente nel database lo stesso username.
 * Ritorna true se l'username è presente nel database, false altrimenti.
 */
public class FutureUsernameExistsThread implements Callable<Boolean> {
    public String newUsername;
    public FutureUsernameExistsThread(String newUsername){
        this.newUsername = newUsername;
    }

    @Override
    public Boolean call() throws Exception {
        Query query = FirebaseFirestore.getInstance()
                .collection("Profile")
                .whereEqualTo("username", newUsername);
        Task taskUsernameExists = query.get();
        try {
            QuerySnapshot snapUsernameExists = (QuerySnapshot) Tasks.await(taskUsernameExists);
            return !snapUsernameExists.isEmpty();   /* ritorna true se è presente, false altrimenti */
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;  /* Se c'è qualche errore dico che l'username è già presente in modo da fermare il processo di modifica username */
    }
}