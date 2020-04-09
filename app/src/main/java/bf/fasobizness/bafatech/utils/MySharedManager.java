package bf.fasobizness.bafatech.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySharedManager {
    private final SharedPreferences mPreferences;
    private final SharedPreferences.Editor mEditor;


    @SuppressLint("CommitPrefEdits")
    public MySharedManager(Context mContext) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.mEditor = mPreferences.edit();
    }

    public String getUser() {
        return mPreferences.getString("id", "");
    }

    public void setUser(String user) {
        mEditor.putString("id", user);
        mEditor.apply();
    }

    public String getUsername() {
        return mPreferences.getString("username", "");
    }

    public void setUsername(String username) {
        mEditor.putString("username", username);
        mEditor.apply();
    }

    public String getEmail() {
        return mPreferences.getString("email", "");
    }

    public void setEmail(String email) {
        mEditor.putString("email", email);
        mEditor.apply();
    }

    public String getPhoto() {
        return mPreferences.getString("photo", "");
    }

    public void setPhoto(String photo) {
        mEditor.putString("photo", photo);
        mEditor.apply();
    }

    public String getType() {
        return mPreferences.getString("type", "");
    }

    public void setType(String type) {
        mEditor.putString("type", type);
        mEditor.apply();
    }

    public float geRating() {
        return mPreferences.getFloat("rating", 0);
    }

    public String getToken() {
        return mPreferences.getString("token", "");
    }

    public void setToken(String token) {
        mEditor.putString("token", token);
        mEditor.apply();
    }

    public void setRating(float rating) {
        mEditor.putFloat("rating", rating);
        mEditor.apply();
    }

    /*
    Announces
     */

    public String getTel() {
        return mPreferences.getString("tel", "");
    }

    public void setTel(String tel) {
        mEditor.putString("tel", tel);
        mEditor.apply();
    }

    public String getTel1() {
        return mPreferences.getString("tel1", "");
    }

    public void setTel1(String tel1) {
        mEditor.putString("tel1", tel1);
        mEditor.apply();
    }

    public String getTel2() {
        return mPreferences.getString("tel2", "");
    }

    public void setTel2(String tel2) {
        mEditor.putString("tel2", tel2);
        mEditor.apply();
    }

    public String getCategorie() {
        return mPreferences.getString("categorie", "");
    }

    public void setCategorie(String categorie) {
        mEditor.putString("categorie", categorie);
        mEditor.apply();
    }

    public String getVille() {
        return mPreferences.getString("ville", "");
    }

    public void setVille(String ville) {
        mEditor.putString("ville", ville);
        mEditor.apply();
    }
}
