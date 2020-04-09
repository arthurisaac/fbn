package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Advertising {

    @SerializedName("data")
    public final List<Ads> adsList = new ArrayList<>();

    public class Ads {

        @SerializedName("id")
        private String id;

        @SerializedName("description")
        private String description;

        @SerializedName("nom")
        private String lien;

        @SerializedName("vue")
        private String vue;

        @SerializedName("shared")
        private String shared;

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
    }
}
