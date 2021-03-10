package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Discussion {

    @SerializedName("data")
    public final List<Discussions> discussions = new ArrayList<>();

    public static class Discussions {
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

        @SerializedName("affiche")
        private String affiche;

        @SerializedName("id_ann")
        private String id_ann;

        @SerializedName("messages")
        public List<Message.Messages> messages;

        @SerializedName("user")
        public User user;

        @SerializedName("annonce")
        private boolean annonce;

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

        public void setDiscussion_id(String discussion_id) {
            this.discussion_id = discussion_id;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getAffiche() {
            return affiche;
        }

        public void setAffiche(String affiche) {
            this.affiche = affiche;
        }

        public boolean isAnnonce() {
            return annonce;
        }

        public void setAnnonce(boolean annonce) {
            this.annonce = annonce;
        }

        public List<Message.Messages> getMessages() {
            return messages;
        }

        public void setMessages(List<Message.Messages> messages) {
            this.messages = messages;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getId_ann() {
            return id_ann;
        }

        public void setId_ann(String id_ann) {
            this.id_ann = id_ann;
        }
    }


}
