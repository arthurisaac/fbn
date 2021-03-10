package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entreprise {

    @SerializedName("data")
    public final List<Entreprises> entreprises = new ArrayList<>();

    public class Entreprises implements Serializable {
        @SerializedName("pictures")
        public final List<Pictures> pictures = new ArrayList<>();
        @SerializedName("commentaires")
        public final List<Comment> commentaires = new ArrayList<>();
        @SerializedName("id")
        private String id;
        @SerializedName("nom")
        private String nom;
        @SerializedName("presentation")
        private String presentation;
        @SerializedName("sujet")
        private String desc;
        @SerializedName("tel")
        private String tel;
        @SerializedName("email")
        private String email;
        @SerializedName("adresse")
        private String adresse;
        @SerializedName("date_pub")
        private String date_pub;
        @SerializedName("heure_pub")
        private String heure_pub;
        @SerializedName("domaine")
        private String domaine;
        @SerializedName("archive")
        private String archive;
        @SerializedName("logo")
        private String logo;
        @SerializedName("site")
        private String site;
        @SerializedName("affiche")
        private String affiche;
        @SerializedName("location")
        private String location;
        @SerializedName("aimer")
        private int aimer;
        @SerializedName("id_aime")
        private String id_aime;
        @SerializedName("vue")
        private String nbVue;
        @SerializedName("likes")
        private String nbLike;
        @SerializedName("comments")
        private String nbComment;

        public String getNbComment() {
            return nbComment;
        }

        public String getDesc() {
            return desc;
        }

        public String getNbLike() {
            return nbLike;
        }

        public String getId() {
            return id;
        }

        public String getNom() {
            return nom;
        }

        public String getPresentation() {
            return presentation;
        }

        public String getTel() {
            return tel;
        }

        public String getEmail() {
            return email;
        }

        public String getAdresse() {
            return adresse;
        }

        public String getDate_pub() {
            return date_pub;
        }

        public String getDomaine() {
            return domaine;
        }

        public String getLogo() {
            return logo;
        }

        public String getSite() {
            return site;
        }

        public String getAffiche() {
            return affiche;
        }

        public String getLocation() {
            return location;
        }

        public String getNbVue() {
            return nbVue;
        }

        public int getAimer() {
            return aimer;
        }

        public void setAimer(int aimer) {
            this.aimer = aimer;
        }

        public class Pictures implements Serializable {
            @SerializedName("nom")
            private String nom;

            @SerializedName("id_affiche")
            private String id_affiche;

            public String getNom() {
                return nom;
            }

        }

        public class Comment implements Serializable {
            @SerializedName("id_comment")
            private String id_comment;
            @SerializedName("commentaire")
            private String commentaire;
            @SerializedName("date")
            private String date;
            @SerializedName("heure")
            private String heure;
            @SerializedName("id_personne")
            private String id_personne;
            @SerializedName("id_article_fk")
            private String id_article_fk;
            @SerializedName("email")
            private String email;
            @SerializedName("username")
            private String username;
            @SerializedName("photo")
            private String photo;
            @SerializedName("tel")
            private String tel;
            @SerializedName("type")
            private String type;

            public String getId_comment() {
                return id_comment;
            }

            public String getCommentaire() {
                return commentaire;
            }

            public String getDate() {
                return date;
            }

            public String getHeure() {
                return heure;
            }

            public String getUsername() {
                return username;
            }

            public String getPhoto() {
                return photo;
            }

            public String getId_personne() {
                return id_personne;
            }

        }


    }


}
