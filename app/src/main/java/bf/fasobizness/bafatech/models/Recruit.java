package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recruit {

    @SerializedName("data")
    public final List<Recrutement> recrutements = new ArrayList<>();

    public static class Recrutement implements Serializable {

        @SerializedName("affiches")
        public List<Affiche> affiches;
        @SerializedName("id_recr")
        private String id_recr;
        @SerializedName("nom_ent")
        private String nom_ent;
        @SerializedName("domaine")
        private String domaine;
        @SerializedName("description")
        private String description;
        @SerializedName("date_pub")
        private String date_pub;
        @SerializedName("date_fin")
        private String date_fin;
        @SerializedName("heure_fin")
        private String heure_fin;
        @SerializedName("nom_r")
        private String nom_r;
        @SerializedName("desc")
        private String desc;
        @SerializedName("vue")
        private String vue;
        @SerializedName("lien")
        private String lien;
        @SerializedName("share")
        private String share;

        public String getId_recr() {
            return id_recr;
        }

        public String getNom_ent() {
            return nom_ent;
        }

        public String getDomaine() {
            return domaine;
        }

        public String getDescription() {
            return description;
        }

        public String getDate_pub() {
            return date_pub;
        }

        public String getDate_fin() {
            return date_fin;
        }

        public String getHeure_fin() {
            return heure_fin;
        }

        public String getNom_r() {
            return nom_r;
        }

        public String getDesc() {
            return desc;
        }

        public String getVue() {
            return vue;
        }

        public String getLien() {
            return lien;
        }

        public String getShare() {
            return share;
        }

        public void setId_recr(String id_recr) {
            this.id_recr = id_recr;
        }

        public void setNom_ent(String nom_ent) {
            this.nom_ent = nom_ent;
        }

        public void setDomaine(String domaine) {
            this.domaine = domaine;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setDate_pub(String date_pub) {
            this.date_pub = date_pub;
        }

        public void setDate_fin(String date_fin) {
            this.date_fin = date_fin;
        }

        public void setHeure_fin(String heure_fin) {
            this.heure_fin = heure_fin;
        }

        public void setNom_r(String nom_r) {
            this.nom_r = nom_r;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setVue(String vue) {
            this.vue = vue;
        }

        public void setLien(String lien) {
            this.lien = lien;
        }

        public void setShare(String share) {
            this.share = share;
        }

        public void affiches(List<Affiche> affiches) {
            this.affiches = affiches;
        }


        public static class Affiche implements Serializable {

            @SerializedName("nom")
            private String nom;

            private String id_recr;

            @SerializedName("thumbnail")
            private String thumbnail;

            public String getNom() {
                return nom;
            }

            public String getThumbnail() {
                return thumbnail;
            }

            public String getId_recr() {
                return id_recr;
            }

            public void setNom(String nom) {
                this.nom = nom;
            }

            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }

            public void setId_recr(String id_recr) {
                this.id_recr = id_recr;
            }
        }
    }
}


