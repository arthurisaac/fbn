package bf.fasobizness.bafatech.activities.recrutement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.ActivityBoutique;
import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnonceCategory;
import bf.fasobizness.bafatech.activities.annonce.ActivityOffreOr;
import bf.fasobizness.bafatech.activities.entreprise.ActivityEntreprisesUne;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityDiscussions;
import bf.fasobizness.bafatech.adapters.RecrutementAdapter;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recruit;
import bf.fasobizness.bafatech.utils.DatabaseManager;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRecrutements extends AppCompatActivity implements OnItemListener, OnSearchViewListener {

    private static final String TAG = "ActivityRecrutements";
    private ArrayList<Recruit.Recrutement> mRecrutements;
    private RecrutementAdapter mRecrutementAdapter;
    // private RequestQueue requestQueue;
    private ShimmerFrameLayout mShimmerViewContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // private LinearLayout offline_layout;
    private DatabaseManager databaseManager;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recrutements);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Espace emplois");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();

        // offline_layout = findViewById(R.id.layout_ent_offline);
        searchView = findViewById(R.id.search_view);
        Button refresh = findViewById(R.id.btn_refresh);
        refresh.setOnClickListener(v -> getRecruits());

        mRecrutements = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_recrutements);
        mRecrutementAdapter = new RecrutementAdapter(this, mRecrutements);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecrutementAdapter);
        mRecrutementAdapter.setOnItemListener(this);

        TextView tab_chat = findViewById(R.id.tab_chat);
        TextView tab_boutiques = findViewById(R.id.tab_boutiques);
        TextView tab_offre_en_or = findViewById(R.id.tab_offre_en_or);
        TextView tab_entreprise = findViewById(R.id.tab_entrepise);
        TextView tab_categorie = findViewById(R.id.tab_categorie);

        tab_boutiques.setOnClickListener(v -> startActivity(new Intent(this, ActivityBoutique.class)));
        tab_offre_en_or.setOnClickListener(v -> startActivity(new Intent(this, ActivityOffreOr.class)));
        tab_entreprise.setOnClickListener(v -> startActivity(new Intent(this, ActivityEntreprisesUne.class)));
        tab_categorie.setOnClickListener(v -> startActivity(new Intent(this, ActivityAnnonceCategory.class)));
        tab_chat.setOnClickListener(v -> {
            if (!user.isEmpty()) {
                startActivity(new Intent(this, ActivityDiscussions.class));
            } else {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
            }
        });

        databaseManager = new DatabaseManager(this);
        getRecruits();
        searchView.setOnSearchViewListener(this);
    }

    private void refresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.stopShimmer();
        mRecrutementAdapter.clearAll();
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();

        getRecruits();
    }
    /*private void refresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mRecrutementAdapter.clearAll();
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();

        jsonParse();
    }*/

    /*private void jsonParse() {
        offline_layout.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        API api = RetrofitClient.getClient().create(API.class);
        Call<Recruit> call = api.getRecruits();
        call.enqueue(new Callback<Recruit>() {
            @Override
            public void onResponse(@NonNull Call<Recruit> call, @NonNull Response<Recruit> response) {
                mShimmerViewContainer.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, response.toString());
                Recruit recruit = response.body();
                List<Recruit.Recrutement> recrutements = null;
                if (recruit != null) {
                    recrutements = recruit.recrutements;
                }
                if (recrutements != null) {
                    mRecrutements.addAll(recrutements);

                    for (Recruit.Recrutement recrutement: recrutements) {
                        databaseManager.insertRecrutement(
                                recrutement.getNom_ent(),
                                recrutement.getDomaine(),
                                recrutement.getDescription(),
                                recrutement.getDesc(),
                                recrutement.getDate_pub(),
                                recrutement.getDate_fin(),
                                recrutement.getHeure_fin(),
                                recrutement.getNom_r(),
                                recrutement.getVue(),
                                recrutement.getLien(),
                                recrutement.getShare()
                        );

                        for (Recruit.Recrutement.Affiche affiche: recrutement.affiches)
                        databaseManager.insertRecruitAttachment(
                                affiche.getNom(),
                                affiche.getThumbnail()
                        );
                    }
                    databaseManager.close();
                }
                mRecrutementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<Recruit> call, @NonNull Throwable t) {
                mShimmerViewContainer.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                offline_layout.setVisibility(View.VISIBLE);
                Log.d(TAG, t.toString());
            }
        });

    }*/

    private void getRecruits(){
        List<Recruit.Recrutement> recrutementList = databaseManager.getRecruits();
        mRecrutements.addAll(recrutementList);
        mRecrutementAdapter.notifyDataSetChanged();
        if (mRecrutements.size() == 0) {
            mShimmerViewContainer.setVisibility(View.VISIBLE);
        }

        API api = RetrofitClient.getClient().create(API.class);
        Call<Recruit> call = api.getRecruits();
        call.enqueue(new Callback<Recruit>() {
            @Override
            public void onResponse(@NonNull Call<Recruit> call, @NonNull Response<Recruit> response) {
                mShimmerViewContainer.setVisibility(View.GONE);
                mShimmerViewContainer.stopShimmer();
                mSwipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, response.toString());
                Recruit recruit = response.body();

                List<Recruit.Recrutement> recrutements = null;
                if (recruit != null) {
                    recrutements = recruit.recrutements;
                }
                if (recrutements != null) {
                    mRecrutementAdapter.clearAll();
                    mRecrutements.addAll(recrutements);
                    databaseManager.truncateRecruits();
                    for (Recruit.Recrutement recrutement: recrutements) {
                        databaseManager.insertRecrutement(
                                recrutement.getId_recr(),
                                recrutement.getNom_ent(),
                                recrutement.getDomaine(),
                                recrutement.getDescription(),
                                recrutement.getDesc(),
                                recrutement.getDate_pub(),
                                recrutement.getDate_fin(),
                                recrutement.getHeure_fin(),
                                recrutement.getNom_r(),
                                recrutement.getVue(),
                                recrutement.getLien(),
                                recrutement.getShare()
                        );

                        for (Recruit.Recrutement.Affiche affiche: recrutement.affiches)
                            databaseManager.insertRecruitAttachment(
                                    affiche.getNom(),
                                    affiche.getThumbnail(),
                                    recrutement.getId_recr()
                            );
                    }
                    databaseManager.close();
                }
                mRecrutementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<Recruit> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
                mShimmerViewContainer.setVisibility(View.GONE);
                mShimmerViewContainer.stopShimmer();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        MySharedManager sharedManager = new MySharedManager(ActivityRecrutements.this);
            if (!sharedManager.getUser().isEmpty()) {
                Recruit.Recrutement recrutement = mRecrutements.get(position);
                Intent intent = new Intent(getApplicationContext(), ActivityDetailsRecrutement.class);
                intent.putExtra("recrutement", recrutement);
                startActivity(intent);
            } else {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recherche, menu);

        MenuItem item = menu.findItem(R.id.nav_recherche);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_recherche) {
            // startActivity(new Intent(this, ActivityRecrutementRecherche.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mRecrutementAdapter.getFilter().filter(s);
        return false;
    }

    @Override
    public void onQueryTextChange(String s) {
        mRecrutementAdapter.getFilter().filter(s);
    }
}
