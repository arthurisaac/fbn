package bf.fasobizness.bafatech.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import bf.fasobizness.bafatech.interfaces.IMessage;
import bf.fasobizness.bafatech.interfaces.MessageContentType;


public class Message {

    @SerializedName("data")
    public final List<Messages> messages = new ArrayList<>();

    @SerializedName("user")
    public User user;

    @SerializedName("titre")
    private String titre;

    @SerializedName("id_ann")
    private String id_ann;

    @SerializedName("affiche")
    private String affiche;

    @SerializedName("annonce")
    private boolean annonce;

    public String getTitre() {
        return titre;
    }

    public String getId_ann() {
        return id_ann;
    }

    public String getAffiche() {
        return affiche;
    }

    public boolean isAnnonce() {
        return annonce;
    }

    public void setAnnonce(boolean annonce) {
        this.annonce = annonce;
    }

    public User getUser() {
        return user;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setId_ann(String id_ann) {
        this.id_ann = id_ann;
    }

    public void setAffiche(String affiche) {
        this.affiche = affiche;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class Messages
            implements IMessage, MessageContentType.Image {
        private String message_id;
        private String message;
        private String created_at;
        private String etat;
        private String discussion_id;
        private String isread;
        private String bellow;
        private String sender;
        private String type;
        private String is_deleted;
        private Image image;

        public String getBellow() {
            return bellow;
        }

        public String getIs_deleted() {
            return is_deleted;
        }

        public String getEtat() {
            return etat;
        }

        public String getDiscussion_id() {
            return discussion_id;
        }

        public String getIsread() {
            return isread;
        }

        public void setIsread(String isread) {
            this.isread = isread;
        }

        @Override
        public String getMessage_id() {
            return message_id;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getCreated_at() {
            return created_at;
        }

        @Override
        public String getSender() {
            return sender;
        }

        @Override
        public String getType() {
            return type;
        }

        public void setMessage_id(String message_id) {
            this.message_id = message_id;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public void setDiscussion_id(String discussion_id) {
            this.discussion_id = discussion_id;
        }

        public void setBellow(String bellow) {
            this.bellow = bellow;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setEtat(String etat) {
            this.etat = etat;
        }

        public void setIs_deleted(String is_deleted) {
            this.is_deleted = is_deleted;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }

        @Nullable
        @Override
        public String getImageUrl() {
            return image == null ? null : image.url;
        }

        public static class Image {

            private final String url;

            public Image(String url) {
                this.url = url;
            }
        }

        public static class Voice {

            private final String url;
            private final int duration;

            public Voice(String url, int duration) {
                this.url = url;
                this.duration = duration;
            }

            public String getUrl() {
                return url;
            }

            public int getDuration() {
                return duration;
            }
        }

    }

}
