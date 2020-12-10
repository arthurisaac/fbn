package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IUser;

public class User implements IUser {

    // private int id;
    private String name;
    private String avatar;
    private boolean online;

    @SerializedName("nom")
    private String nom;

    @SerializedName("id")
    private String id;

    @SerializedName("prenom")
    private String prenom;

    @SerializedName("photo")
    private String photo;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("username")
    private String username;

    @SerializedName("tel")
    private String tel;

    @SerializedName("authorization")
    private String authorization;

    @SerializedName("sect_activite")
    private String sect_activite;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private boolean status;

    public boolean getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSect_activite() {
        return sect_activite;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getTel() {
        return tel;
    }

    public String getPhoto() {
        return photo;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public boolean isOnline() {
        return online;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
