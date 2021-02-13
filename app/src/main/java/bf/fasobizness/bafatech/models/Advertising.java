package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Advertising {

    @SerializedName("data")
    public final List<Ads> adsList = new ArrayList<>();

    public class Ads implements Serializable {

        @SerializedName("id")
        private String id;

        @SerializedName("description")
        private String description;

        @SerializedName("nom")
        private String lien;

        @SerializedName("vue")
        private String vue;

        @SerializedName("appel")
        private String appel;

        @SerializedName("whatsapp")
        private String whatsapp;

        @SerializedName("facebook")
        private String facebook;

        @SerializedName("shared")
        private String shared;

        @SerializedName("affiches")
        public final List<Ads.Affiche> affiches = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLien() {
            return lien;
        }

        public String getVue() {
            return vue;
        }

        public void setVue(String vue) {
            this.vue = vue;
        }

        public String getShared() {
            return shared;
        }

        public String getAppel() {
            return appel;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public String getFacebook() {
            return facebook;
        }

        public List<Affiche> getAffiches() {
            return affiches;
        }

        public class Affiche implements Serializable {

            @SerializedName("nom")
            private String nom;

            public String getNom() {
                return nom;
            }

        }
    }
}
