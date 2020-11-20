package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Message {

    @SerializedName("data")
    public final List<Messages> messages = new ArrayList<>();

    @SerializedName("user")
    public final User user = new User();

    @SerializedName("titre")
    private String titre;

    @SerializedName("id_ann")
    private String id_ann;

    @SerializedName("affiche")
    private String affiche;

    public String getTitre() {
        return titre;
    }

    public String getId_ann() {
        return id_ann;
    }

    public String getAffiche() {
        return affiche;
    }

    public class Messages {
        private String message_id;
        private String message;
        private String created_at;
        private String discussion_id;
        private String bellow;
        private String sender;
        private String type;
        private String is_deleted;
        private String isread_receiver;

        public String getMessage_id() {
            return message_id;
        }

        public String getMessage() {
            return message;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getDiscussion_id() {
            return discussion_id;
        }

        public String getBellow() {
            return bellow;
        }

        public String getSender() {
            return sender;
        }

        public String getType() {
            return type;
        }

        public String getIs_deleted() {
            return is_deleted;
        }

        public String getIsread_receiver() {
            return isread_receiver;
        }
    }

}
