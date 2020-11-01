package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.models.Announce;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAnnounceFilter extends AppCompatActivity implements OnAnnonceListener {

    private LinearLayout layout_ent_offline, layout_busy_system, layout_no_annonce;

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;

    private String params = "";
    private String filter = "id";
    private int page = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShimmerFrameLayout shimmer_view_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonce_from_category);

        Intent extras = getIntent();
        if (extras.getStringExtra("params") != null) {
            params = extras.getStringExtra("params");
        }
        if (extras.getStringExtra("filter") != null) {
            filter = extras.getStringExtra("filter");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        String title = getString(R.string.vos_resultats);
        if (extras.getStringExtra("title") != null) {
            title = extras.getStringExtra("title");
        }
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        // Fetch annonces
        mAnnonces = new ArrayList<>();

        RecyclerView mRecyclerView = findViewById(R.id.annonces_card_view);
        LinearLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAnnonceAdapter = new AnnounceAdapter(this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                /*if (dy < 0) {
                    fab.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fab.setVisibility(View.GONE);
                }*/
            }
        });

        mAnnonceAdapter.setOnBottomReachedListener(() -> {

        });

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_ent_offline.setVisibility(View.GONE);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> refresh());

        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        layout_no_annonce.setVisibility(View.GONE);

        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_busy_system.setVisibility(View.GONE);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        shimmer_view_container.setVisibility(View.VISIBLE);

        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        jsonParse();
    }

    private void jsonParse() {

        API api = RetrofitClient.getClient().create(API.class);

        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);

        Call<Announce> call = api.filterAnnounce(filter, params);
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                // Log.d("ActivityAnnounce", response.toString());
                shimmer_view_container.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                Announce announce = response.body();
                List<Announce.Annonce> annonces = null;
                if (announce != null) {
                    annonces = announce.annonces;
                }
                if (annonces != null) {
                    mAnnonces.addAll(annonces);
                }
                if (mAnnonces.size() == 0) {
                    layout_no_annonce.setVisibility(View.VISIBLE);
                    layout_ent_offline.setVisibility(View.GONE);
                }
                page++;
                mAnnonceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {

                // Log.d("ActivityFilter", t.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                layout_ent_offline.setVisibility(View.VISIBLE);
                shimmer_view_container.setVisibility(View.GONE);

            }
        });

    }

    /*private void jsonParse() {
        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);

        String url = Constants.HOST_URL + "annonce/all";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
             Log.v(TAG, response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (!jsonObject.get("total").equals("null")) {
                    total = jsonObject.getInt("total");
                }
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
                if (mAnnonces.size() == 0) {
                    layout_no_annonce.setVisibility(View.VISIBLE);
                    layout_ent_offline.setVisibility(View.GONE);
                }
                page++;
                mAnnonceAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                if (page <= 1) {
                    layout_busy_system.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Fin", Toast.LENGTH_SHORT).show();
                }
            }

        }, error -> {
            Log.d(TAG, "" + error.toString());
            error.printStackTrace();
            mSwipeRefreshLayout.setRefreshing(false);
            layout_ent_offline.setVisibility(View.VISIBLE);
            shimmer_view_container.setVisibility(View.GONE);
            // page++;
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_pers_fk", user);
                params.put("page", page+"");
                params.put("filtre", filtre);
                params.put("params", params);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2)
        );
        requestQueue.add(request);
    }*/

    private void refresh() {
        page = 1;
        mAnnonceAdapter.clearAll();
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();
        shimmer_view_container.setVisibility(View.VISIBLE);

        jsonParse();
    }

    @Override
    public void onAnnonceClicked(int position) {
        Announce.Annonce annonce = mAnnonces.get(position);
        Intent intent = new Intent(this, ActivityDetailsAnnonce.class);
        intent.putExtra("id_ann", annonce.getId_ann());
        intent.putExtra("affiche", annonce.getAffiche());
        startActivity(intent);
    }
}
