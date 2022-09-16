package com.pietroorlandi.socialnetwork_entertainment.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Questa classe rappresenta il singolo Entertainment, ogni oggetto Entertainment è composto da vari attributi quali tipo, titolo, categoria etc..
 */

public class Entertainment implements Serializable {
    private Long id;
    private String title;
    private String type;
    private ArrayList<String> category;
    private String country;
    private Long year;
    private String duration;
    private String platform;
    private HashMap<String, Object> reviewData;
    private Double avgReview;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<String> category) {
        this.category = category;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public String getDuration() {
        if(duration==null){
            return "";
        }
        else{
            return duration;
        }
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public double getAvgReview(){
        return this.avgReview;
    }

    /**
     * Ritorna in stringa la media delle recensioni del titolo di intrattenimento
     * Se non c'è ancora la media (no recensioni fatte) ritorna N/A
     * @return
     */
    public String getStringAverageReview(){
        if (this.avgReview == null){
            return "N/A";
        }
        else {
            return this.avgReview.toString();
        }
    }

    public HashMap <String, Object> getReviewData(){
        return this.reviewData;
    }

    /**
     * Ritorna una stringa che rappresenta le categorie del mio titolo di intrattenimento
     * @return
     */
    public String returnCategoriesStr(){
        String str = "";
        for(int i=0; i<category.size(); i++){
            str+= category.get(i) + " - ";
        }
        return str.substring(0, str.length() - 3);
    }




}
