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
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityFavorite extends AppCompatActivity implements OnAnnonceListener {
    private static final String TAG = "ActivityFavorite";
    private LinearLayout layout_ent_offline, layout_busy_system, layout_no_annonce;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShimmerFrameLayout shimmer_view_container;

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;
    // private RequestQueue requestQueue;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoris);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.mes_favoris));
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

        MySharedManager mySharedManager = new MySharedManager(this);
        token = mySharedManager.getToken();

        //Loading more data
        mAnnonceAdapter.setOnBottomReachedListener(() -> {
            //
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
        shimmer_view_container.setVisibility(View.VISIBLE);
    }

    private void refresh() {
        mAnnonceAdapter.clearAll();
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();
        layout_ent_offline.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);
        shimmer_view_container.setVisibility(View.VISIBLE);

        jsonParse();
    }

    private void jsonParse() {

        API api = RetrofitClient.getClient().create(API.class);
        Call<Announce> call = api.getFavoriteAnnounces("Bearer " + token);
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                shimmer_view_container.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, response.toString());

                if (response.isSuccessful()) {
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
                } else {
                    layout_busy_system.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {

                layout_ent_offline.setVisibility(View.VISIBLE);
                shimmer_view_container.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });
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

