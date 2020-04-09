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
        private String user_id;
        private String receiver_id;
        private String message;
        private String created_at;
        private String discussion_id;
        private String isdeleted_receiver_id;
        private String isdeleted_user_id;
        private String type;


        public String getType() {
            return type;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getMessage() {
            return message;
        }

        public String getCreated_at() {
            return created_at;
        }

    }

}
