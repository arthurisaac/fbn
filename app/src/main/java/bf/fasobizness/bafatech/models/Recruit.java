package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recruit {

    @SerializedName("data")
    public final List<Recrutement> recrutements = new ArrayList<>();

    public class Recrutement implements Serializable {

        @SerializedName("affiches")
        public final List<Affiche> affiches = new ArrayList<>();
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

        public class Affiche implements Serializable {

            @SerializedName("nom")
            private String nom;

            @SerializedName("thumbnail")
            private String thumbnail;

            public String getNom() {
                return nom;
            }

            public String getThumbnail() {
                return thumbnail;
            }
        }
    }
}


