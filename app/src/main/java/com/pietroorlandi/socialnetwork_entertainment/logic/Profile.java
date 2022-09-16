package com.pietroorlandi.socialnetwork_entertainment.logic;


/**
 * Questa classe rappresenta il profilo del singolo utente.
 * I getter e i setter vengono usati per popolare gli elementi della recycler view per mostrare la lista dei profili
 */
public class Profile {

    private String uid;
    private String username;
    private String email;
    private String bio;
    private Boolean isVerified;

    public Profile(){}

    public Profile(String uid, String username, String email) {
        this.setUid(uid);
        this.setUsername(username);
        this.setEmail(email);
        this.setVerified(false);
    }
    public Profile(String uid, String username, String email, Boolean isVerified) {
        this.setUid(uid);
        this.setUsername(username);
        this.setEmail(email);
        this.setVerified(isVerified);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public String getBio() {
        if(bio==null){
            return "";
        }
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


}
