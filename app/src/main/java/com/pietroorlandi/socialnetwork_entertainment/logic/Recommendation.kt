package com.pietroorlandi.socialnetwork_entertainment.logic

/**
 * Questa classe rappresenta la raccomandazione che invia un utente a un altro.
 * Viene utilizzata per popolare gli elementi della recycler view
 */
data class Recommendation(var recommendedBy : String? = "",
                           var recommendedTo : String? = "",
                           var titleEntertainment: String? = "",
                           var idEntertainment: Long? = -1,
                           var timestamp: Long? = 0,
                           )