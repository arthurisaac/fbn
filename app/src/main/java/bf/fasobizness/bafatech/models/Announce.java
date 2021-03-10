package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Announce {

    @SerializedName("data")
    public final List<Annonce> annonces = new ArrayList<>();

    @SerializedName("user")
    public final User user = new User();

    public static class Annonce implements Serializable {

        @SerializedName("illustrations")
        public final List<Illustration> illustrations = new ArrayList<>();
        @SerializedName("id_ann")
        private String id_ann;
        @SerializedName("texte")
        private String texte;
        @SerializedName("date_pub")
        private String date_pub;
        @SerializedName("heure_pub")
        private String heure_pub;
        @SerializedName("type")
        private String type;
        @SerializedName("favoris")
        private String favoris;
        @SerializedName("id_per_fk")
        private String id_per_fk;
        @SerializedName("valid")
        private String valid;
        @SerializedName("prix")
        private String prix;
        @SerializedName("titre")
        private String titre;
        @SerializedName("email")
        private String email;
        @SerializedName("tel")
        private String tel;
        @SerializedName("location")
        private String location;
        @SerializedName("tel1")
        private String tel1;
        @SerializedName("tel2")
        private String tel2;
        @SerializedName("affiche")
        private String affiche;
        @SerializedName("vue")
        private String vue;
        @SerializedName("categorie")
        private String categorie;
        @SerializedName("vip")
        private String vip;
        @SerializedName("desc")
        private String desc;
        @SerializedName("share")
        private String share;
        @SerializedName("updatedAt")
        private String updatedAt;
        @SerializedName("username")
        private String username;
        @SerializedName("photo")
        private String photo;
        @SerializedName("status")
        private String status;
        @SerializedName("id_personne")
        private String id_personne;
        @SerializedName("favori")
        private String favori;
        @SerializedName("id_fav")
        private String id_fav;
        @SerializedName("whatsapp")
        private String whatsapp;
        @SerializedName("audio")
        private String audio;

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getFavori() {
            return favori;
        }

        public String getShare() {
            return share;
        }

        public String getVip() {
            return vip;
        }

        public String getCategorie() {
            return categorie;
        }

        public String getId_ann() {
            return id_ann;
        }

        public String getTexte() {
            return texte;
        }

        public String getDate_pub() {
            return date_pub;
        }

        public String getId_per_fk() {
            return id_per_fk;
        }

        public String getPrix() {
            return prix;
        }

        public String getTitre() {
            return titre;
        }

        public String getEmail() {
            return email;
        }

        public String getTel() {
            return tel;
        }

        public String getLocation() {
            return location;
        }

        public String getTel1() {
            return tel1;
        }

        public String getTel2() {
            return tel2;
        }

        public String getUsername() {
            return username;
        }

        public String getPhoto() {
            return photo;
        }

        public String getAffiche() {
            return affiche;
        }

        public String getVue() {
            return vue;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public void setFavoris(String favoris) {
            this.favoris = favoris;
        }

        public String getAudio() {
            return audio;
        }

        public class Illustration implements Serializable {

            @SerializedName("nom")
            private String nom;

            @SerializedName("id_illustration")
            private String id_illustration;

            public String getId_illustration() {
                return id_illustration;
            }

            public String getNom() {
                return nom;
            }

        }

    }

}
