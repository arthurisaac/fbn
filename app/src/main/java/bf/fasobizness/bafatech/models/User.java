package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("nom")
    private String nom;

    @SerializedName("id")
    private int id;

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
    private String error;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private int status;

    public int getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
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

}
