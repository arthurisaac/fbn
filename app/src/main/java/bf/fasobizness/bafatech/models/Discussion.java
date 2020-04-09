package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Discussion {

    @SerializedName("data")
    public final List<Discussions> discussions = new ArrayList<>();

    public class Discussions {
        @SerializedName("discussion_id")
        private String discussion_id;

        @SerializedName("titre")
        private String titre;

        @SerializedName("message")
        private String message;

        @SerializedName("count")
        private int count;

        @SerializedName("timestamp")
        private String timestamp;

        @SerializedName("id_receiver")
        private String receiver_id;

        @SerializedName("username")
        private String nom;

        @SerializedName("id_ann")
        private String id_ann;

        @SerializedName("id_user")
        private String id_user;

        public String getId_user() {
            return id_user;
        }

        public String getId_ann() {
            return id_ann;
        }

        public String getReceiver_id() {
            return receiver_id;
        }

        public String getDiscussion_id() {
            return discussion_id;
        }

        public String getTitre() {
            return titre;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getTimestamp() {
            return timestamp;
        }

    }


}
