package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityPromouvoirAnnonces;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityOffreOr extends AppCompatActivity implements OnAnnonceListener {
    private static final String TAG = "ActivityOffreOr";
    private LinearLayout layout_ent_offline, layout_busy_system, layout_no_annonce;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShimmerFrameLayout shimmer_view_container;

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offre_or);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.offres_en_or));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        mAnnonces = new ArrayList<>();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_annonce);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAnnonceAdapter = new AnnounceAdapter(this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);


        //Loading more data
        mAnnonceAdapter.setOnBottomReachedListener(() -> {

        });

        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            MySharedManager sharedManager = new MySharedManager(this);
            if (sharedManager.getUser().isEmpty()) {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
            } else {
                startActivity(new Intent(this, ActivityPromouvoirAnnonces.class));
            }

        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    fab.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fab.setVisibility(View.GONE);
                }
            }
        });


        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_ent_offline.setVisibility(View.GONE);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> refresh());

        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        layout_no_annonce.setVisibility(View.GONE);

        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_busy_system.setVisibility(View.GONE);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);

        jsonParse();
    }

    private void refresh() {
        mAnnonceAdapter.clearAll();
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();
        shimmer_view_container.setVisibility(View.VISIBLE);

        jsonParse();
    }

    private void jsonParse() {

        API api = RetrofitClient.getClient().create(API.class);

        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);
        shimmer_view_container.setVisibility(View.VISIBLE);

        Call<Announce> call = api.filterAnnounce("vip", "1");
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
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
                mAnnonceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {

                mSwipeRefreshLayout.setRefreshing(false);
                layout_ent_offline.setVisibility(View.VISIBLE);
                shimmer_view_container.setVisibility(View.GONE);
                Log.d(TAG, t.getMessage());

            }
        });

    }

    /*private void jsonParse() {
//        layout_ent_offline.setVisibility(View.GONE);
//        layout_busy_system.setVisibility(View.GONE);
//        layout_no_annonce.setVisibility(View.GONE);
//        shimmer_view_container.setVisibility(View.VISIBLE);

        String url = Constants.HOST_URL + "annonce/premium/all";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
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
                    layout_no_annonce.setVisibility(View.GONE);
                }
                mAnnonceAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                layout_busy_system.setVisibility(View.VISIBLE);
            }

        }, error -> {
            Log.d(TAG, "" + error.toString());
            error.printStackTrace();
            layout_ent_offline.setVisibility(View.VISIBLE);
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_pers_fk", user);
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

    @Override
    public void onAnnonceClicked(int position) {
        Announce.Annonce annonce = mAnnonces.get(position);
        Intent intent = new Intent(this, ActivityDetailsAnnonce.class);
        intent.putExtra("id_ann", annonce.getId_ann());
        intent.putExtra("affiche", annonce.getAffiche());
        startActivity(intent);
    }
}
