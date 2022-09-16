package com.pietroorlandi.socialnetwork_entertainment.logic;

/* La classe viene salvata in questo modo, senza la necessità di salvare tutti i dati relativi al Profilo e all'entertainment
Vengono salvati questi dati perchè verrano acceduti assieme nella View associata, quando si mostreranno tutte le recensioni di un contenuto
*/

import java.sql.Time;

public class Review {
    private String username;
    private String uid;
    private String title;
    private Long idEntertainment;
    private double vote;
    private Time time;
    private String description;

    // Costructors
    public Review(String username, String uid, String title, Long idEntertainment, double vote){
        this.username=username;
        this.uid=uid;
        this.title = title;
        this.idEntertainment = idEntertainment;
        this.vote = vote;
    }
    public Review(String username, String uid, String title, Long idEntertainment, double vote, String description){
        this(username, uid, title, idEntertainment, vote);
        this.description = description;
    }
    public Review(){}


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getIdEntertainment() {
        return idEntertainment;
    }

    public void setIdEntertainment(Long idEntertainment) {
        this.idEntertainment = idEntertainment;
    }

    public double getVote() {
        return vote;
    }

    public void setVote(double vote) {
        this.vote = vote;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
