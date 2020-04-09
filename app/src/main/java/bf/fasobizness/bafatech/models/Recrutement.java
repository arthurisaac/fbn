package bf.fasobizness.bafatech.models;

import java.io.Serializable;

public class Recrutement implements Serializable {

    private String id_recr;
    private String nom_ent;
    private String domaine;
    private String description;
    private String date_pub;
    private String heure_pub;
    private String date_depot;
    private String date_fin;
    private String heure_fin;
    private String nom_r;
    private String affiches;
    private String desc;
    private String vue;
    private String lien;

    public Recrutement() {
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public String getVue() {
        return vue;
    }

    public void setVue(String vue) {
        this.vue = vue;
    }

    public String getId_recr() {
        return id_recr;
    }

    public void setId_recr(String id_recr) {
        this.id_recr = id_recr;
    }

    public String getNom_ent() {
        return nom_ent;
    }

    public void setNom_ent(String nom_ent) {
        this.nom_ent = nom_ent;
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate_pub() {
        return date_pub;
    }

    public void setDate_pub(String date_pub) {
        this.date_pub = date_pub;
    }

    public String getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(String date_fin) {
        this.date_fin = date_fin;
    }

    public String getHeure_fin() {
        return heure_fin;
    }

    public void setHeure_fin(String heure_fin) {
        this.heure_fin = heure_fin;
    }

    public String getNom_r() {
        return nom_r;
    }

    public void setNom_r(String nom_r) {
        this.nom_r = nom_r;
    }

    public String getAffiches() {
        return affiches;
    }

    public void setAffiches(String affiches) {
        this.affiches = affiches;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
