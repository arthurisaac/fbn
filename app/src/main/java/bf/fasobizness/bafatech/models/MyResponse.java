package bf.fasobizness.bafatech.models;

import com.google.gson.annotations.SerializedName;

public class MyResponse {
    @SerializedName("status")
    private Boolean status;

    @SerializedName("id")
    private int id;

    @SerializedName("admin")
    private int admin;

    @SerializedName("count")
    private int count;

    public int getAdmin() {
        return admin;
    }

    public int getCount() {
        return count;
    }

    public Boolean getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

}
