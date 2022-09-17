package com.pietroorlandi.socialnetwork_entertainment.logic

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.pietroorlandi.socialnetwork_entertainment.gui.HomeFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Questa classe rappresenta un sistema di raccomandazione, in cui si consigliano titoli di intrattenimento.
 * Ha un attributo in cui si tiene traccia della lista di titoli che sono consigliati per l'utente.
 * Il sistema consiglierà titoli in base a:
 *   - titoli che sono stati consumati da profili seguiti dall'utente
 *   - titoli con categoria comune ai titoli piaciuti dall'utente stesso
 *   - titoli più recensiti nel database
 */
class RecommenderSystem() {
    private val TAG : String = "DEBUG_PROGRAMMA"
    private val NUM_CATEGORIES_TO_CONSIDER = 2
    private val NUM_ENTERTAINMENT_RECOMMENDED_FOR_CATEGORY = 7
    private var uid: String? = null
    private var listRecommendedForUser: MutableList<Long?>
    private lateinit var fragment: HomeFragment
    private var activity: Activity? = null

    init{
        listRecommendedForUser = ArrayList()
    }

    constructor(uid: String, fragment: HomeFragment) : this() {
        this.uid = uid
        this.fragment = fragment
        this.activity = fragment.activity
    }

    /**
     * Questo metodo aggiorna la lista dei titoli consigliati dell'utente.
     * Utilizza un co-routine, avendo un carico di lavoro non trascurabile, che permette all'UI dell'applicazione di non freezarsi
     * Vengono consigliati all'utente titoli che sono stati consumati da profili seguiti da lui stesso, titoli con categorie che piacciono all'utente e i titoli più recensiti all'interno del database
     */
    public fun recommend(){

        GlobalScope.launch {
            val listUidFollowing: MutableList<String> = ArrayList()
            val db = FirebaseFirestore.getInstance()
            val refEntertainment = db.collection("Entertainment")
            val refProfile = db.collection("Profile")
            val taskEntertainmentConsumedByLoggedUser: Task<QuerySnapshot> = refProfile.document(uid!!).collection("listEntertainmentConsumed").get()
            val listEntertainmentConsumedByLoggedUser: MutableList<Long> = ArrayList()

            /* Genero la lista degli intrattenimenti consumati dall'utente loggato */
            try {
                /* Il comando Tasks.await permette di comportarsi in maniera sincrona.
                 * È stato utilizzato in questa maniera perché questa volta, l'elaborazione dei risultati è più complessa e Firestore Database non riesce a trattare query complesse
                 */
                val snapEntertainmentConsumedByLoggedUser = Tasks.await(taskEntertainmentConsumedByLoggedUser) as QuerySnapshot
                for (docEntertainment in snapEntertainmentConsumedByLoggedUser.documents) {
                    listEntertainmentConsumedByLoggedUser.add(docEntertainment.id.toLong())
                }
            } catch (e: ExecutionException ) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            /* Popolo la lista dei profili (uid) seguiti dall'utente loggato */
            val taskFollowingUser: Task<QuerySnapshot> = refProfile.document(uid!!).collection("following").get()
            try {
                val snapFollowingUser = Tasks.await(taskFollowingUser) as QuerySnapshot
                for (docUser in snapFollowingUser.documents) {
                    listUidFollowing.add(docUser.id)
                }
                /* Lista di intrattenimenti consumati dai profili seguiti dall'utente loggato */
                for (uidFollowing in listUidFollowing) {
                    val taskEntertainmentConsumedByUser =
                        refProfile.document(uidFollowing).collection("listEntertainmentConsumed")
                            .get()
                    val snapEntertainmentConsumedByFollowing = Tasks.await(taskEntertainmentConsumedByUser) as QuerySnapshot
                    for (docEntertainment in snapEntertainmentConsumedByFollowing.documents) {
                        listRecommendedForUser.add(docEntertainment.id.toLong())
                    }
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            /* Intrattenimenti che appartengono alle categorie più consumate dall'utente loggato */
            if(listEntertainmentConsumedByLoggedUser.size>0) {   /* Solo se l'utente ha consumato almeno un titolo */
                val taskEntertainmentConsumed: Task<QuerySnapshot> =
                    refEntertainment.whereIn("id", listEntertainmentConsumedByLoggedUser).get()
                try {
                    val snapEntertainmentConsumed = Tasks.await(taskEntertainmentConsumed) as QuerySnapshot
                    val counterCategories = HashMap<String, Int>()
                    for (docEntertainment in snapEntertainmentConsumed.documents) {
                        /* Conto quanti intrattenimenti ha visto l'utente loggato per ogni categoria */
                        for (el in (docEntertainment["category"] as List<String>?)!!) {
                            if (counterCategories[el] == null) {
                                counterCategories[el] = 1
                            } else {
                                counterCategories[el] = counterCategories[el]!! + 1
                            }
                        }
                    }
                    /* Si prendono solo le categorie più consumate dall'utente loggato */
                    val listBestCategoriesConsumedByUser: List<String> = sortMapByValue(counterCategories, NUM_CATEGORIES_TO_CONSIDER)
                    Log.d(TAG, "Ecco categorie più consumate: " + listBestCategoriesConsumedByUser)

                    val taskEntertainmentWithCategoriesInCommon: Task<QuerySnapshot> =
                        refEntertainment
                            .whereArrayContainsAny("category", listBestCategoriesConsumedByUser)
                            .orderBy("year", Query.Direction.DESCENDING)
                            .limit(NUM_ENTERTAINMENT_RECOMMENDED_FOR_CATEGORY.toLong())
                            .get()
                    val snapEntertainmentWithCommonCategory = Tasks.await<QuerySnapshot>(taskEntertainmentWithCategoriesInCommon) as QuerySnapshot
                    for (docEntertainment in snapEntertainmentWithCommonCategory.documents) {
                        listRecommendedForUser.add(docEntertainment.getLong("id"))
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            /* Raccomandati in base a quelli con recensioni più alte (in base al campo sumReview, così tengo conto sia del numero che del valore) */
            var numberEntertainmentHighReviewToReccomend = 5L
            if(listRecommendedForUser.size<3){  /* Se la lista dei titoli raccomandati è corta, si prendono i 10 titoli più recensiti */
                numberEntertainmentHighReviewToReccomend = 10L
            }
            val taskEntertainmentMostReviewd: Task<QuerySnapshot> = refEntertainment.orderBy("sumReview", Query.Direction.DESCENDING).limit(numberEntertainmentHighReviewToReccomend).get()
            try {
                val snapEntertainmentMostReviewed = Tasks.await(taskEntertainmentMostReviewd) as QuerySnapshot
                for (docEntertainment in snapEntertainmentMostReviewed.documents) {
                    listRecommendedForUser.add(docEntertainment.getLong("id"))
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            /* Tolgo dalla lista dei consigliati, quelli già visti dall'utente loggato e i duplicati */
            listRecommendedForUser = (listRecommendedForUser.toSet().subtract(listEntertainmentConsumedByLoggedUser.toSet())).toMutableList()

            /* Faccio uno shuffling della lista e prendo i primi 10 elementi (in tal modo ad ogni aggiornamento dò una lista parzialmente diversa) */
            listRecommendedForUser = listRecommendedForUser.shuffled().take(10).toMutableList()

            /* Aggiorno la GUI con la nuova lista dei consigli per l'utente */
            activity?.runOnUiThread(Runnable {
                fragment.updateUIRecommendationSystem(listRecommendedForUser)
            })
        }
    }

    /**
     * Funzione che prendendo in ingresso un Map con valori interi, ritorna le categorie con valori maggiori.
     * Serve quindi per ritornare le categorie più consumate dall'utente
     */
    fun sortMapByValue(hm: HashMap<String, Int>, numCategoriesToConsider: Int): List<String> {
        val list = hm.toList().sortedBy { (_, value) -> value}.takeLast(numCategoriesToConsider)
        return list.toMap().keys.toList()
    }


}

