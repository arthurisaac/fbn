package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.User;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityUserProfile extends AppCompatActivity implements OnAnnonceListener {
    private static final String TAG = "ActivityUserProfile";
    private CircleImageView iv_user_photo;
    private ImageView iv_big;
    // private RequestQueue requestQueue;
    private TextView tv_user_username, tv_user_account_type, tv_publish_count;

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        api = RetrofitClient.getClient().create(API.class);

        // requestQueue = Volley.newRequestQueue(this);
        tv_user_username = findViewById(R.id.tv_user_username);
        tv_user_account_type = findViewById(R.id.tv_user_account_type);
        tv_publish_count = findViewById(R.id.tv_publish_count);
        iv_user_photo = findViewById(R.id.iv_user_photo);
        iv_big = findViewById(R.id.iv_big);

        String count = getString(R.string.publications_x, "0");
        tv_publish_count.setText(count);

        // init recyclerview
        mAnnonces = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mAnnonceAdapter = new AnnounceAdapter(this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);
        mAnnonceAdapter.setOnBottomReachedListener(() -> {
            //
        });
        getData(id);

    }

    private void getData(String id) {

        Call<Announce> call = api.getUsersAnnounces(id);
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                Log.d(TAG, response.toString());
                try {
                    Announce announce = response.body();
                    List<Announce.Annonce> annonces = null;
                    User user;

                    if (announce != null) {
                        annonces = announce.annonces;
                        user = announce.user;

                        Glide.with(ActivityUserProfile.this)
                                .setDefaultRequestOptions(
                                        new RequestOptions()
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user))
                                .load(user.getPhoto())
                                .into(iv_user_photo);

                        iv_user_photo.setOnClickListener(v -> {
                            ArrayList<String> imagesList = new ArrayList<>();
                            Intent intent = new Intent(ActivityUserProfile.this, ActivityFullScreen.class);
                            intent.putStringArrayListExtra("images", imagesList);
                            intent.putExtra("position", 0);
                            startActivity(intent);
                        });

                        if (!user.getPhoto().contains("user.png")) {
                            Glide.with(ActivityUserProfile.this)
                                    .setDefaultRequestOptions(
                                            new RequestOptions()
                                                    .placeholder(R.color.colorPrimaryDark)
                                                    .error(R.color.colorPrimaryDark))
                                    .load(user.getPhoto())
                                    .into(iv_big);
                        }

                        tv_user_username.setText(user.getUsername());
                        if (user.getType().equals("entreprise")) {
                            tv_user_account_type.setText("Entreprise");
                        } else {
                            tv_user_account_type.setText("Particulier");
                        }
                        String count = getString(R.string.publications_x, annonces.size() + "");
                        tv_publish_count.setText(count);
                    }
                    if (annonces != null) {
                        mAnnonces.addAll(annonces);
                    }

                    mAnnonceAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(ActivityUserProfile.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });

        /*String url = Constants.HOST_URL + "/annonce/user/" + id;
        Log.d(TAG, url);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject user = jsonObject.getJSONObject("user");
                JSONArray annonce = jsonObject.getJSONArray("annonce");

                String photo = user.getString("photo");
                Glide.with(this)
                        .setDefaultRequestOptions(
                                new RequestOptions()
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user))
                        .load(photo)
                        .into(iv_user_photo);

                iv_user_photo.setOnClickListener( v -> {
                    ArrayList<String> imagesList = new ArrayList<>();
                    imagesList.add(photo);
                    Intent intent = new Intent(this, ActivityFullScreen.class);
                    intent.putStringArrayListExtra("images", imagesList);
                    intent.putExtra("position", 0);
                    startActivity(intent);
                });

                if (!photo.contains("user.png")) {
                    Glide.with(this)
                            .setDefaultRequestOptions(
                                    new RequestOptions()
                                            .placeholder(R.color.colorPrimaryDark)
                                            .error(R.color.colorPrimaryDark))
                            .load(photo)
                            .into(iv_big);
                }

                // populate(annonce);
                tv_user_username.setText(user.getString("username"));
                tv_user_account_type.setText(user.getString("type"));
                String count = getString(R.string.publications_x, annonce.length()+"");
                tv_publish_count.setText(count);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);*/
    }

    /*private void populate(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject data = jsonArray.getJSONObject(i);

                String titre = data.getString("titre");
                String vue = data.getString("vue");
                String id_ann = data.getString("id_ann");
                String categorie = data.getString("categorie");
                String vip = data.getString("vip");

                String affiche = data.getString("illNom");

                Annonce annonce = new Annonce();
                annonce.setAffiche(affiche);
                annonce.setVue(vue);
                annonce.setId_ann(id_ann);
                annonce.setCategorie(categorie);
                annonce.setTitre(titre);
                annonce.setVip(vip);

                mAnnonces.add(annonce);
            }
            mAnnonceAdapter.notifyDataSetChanged();
        } catch (JSONException j) {
            j.printStackTrace();
        }

    }*/

    @Override
    public void onAnnonceClicked(int position) {
        Announce.Annonce annonce = mAnnonces.get(position);
        Intent intent = new Intent(this, ActivityDetailsAnnonce.class);
        intent.putExtra("id_ann", annonce.getId_ann());
        intent.putExtra("affiche", annonce.getAffiche());
        startActivity(intent);
    }
}
