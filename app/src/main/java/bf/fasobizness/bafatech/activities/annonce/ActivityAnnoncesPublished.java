package bf.fasobizness.bafatech.activities.annonce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.interfaces.OnLongItemListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAnnoncesPublished extends AppCompatActivity implements OnAnnonceListener, OnLongItemListener {
    private LinearLayout layout_ent_offline, layout_busy_system, layout_no_annonce;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShimmerFrameLayout shimmer_view_container;
    private MenuItem deleteMenu;
    private final API api = RetrofitClient.getClient().create(API.class);

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonces_publiees);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.mes_annonces);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        Button btn_add_post = findViewById(R.id.btn_add_post);
        // btn_add_post.setOnClickListener(v -> startActivity(new Intent(this, ActivityNouvelleAnnonce.class)));
        btn_add_post.setOnClickListener(v -> startActivity(new Intent(this, ActivityNewAnnounce.class)));

        mAnnonces = new ArrayList<>();

        FloatingActionButton fab_up = findViewById(R.id.fab_up);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_annonce);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAnnonceAdapter = new AnnounceAdapter(this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);
        mAnnonceAdapter.setOnLongItemListener(this);

        MySharedManager mySharedManager = new MySharedManager(this);
        user = mySharedManager.getUser();

        //Loading more data
        mAnnonceAdapter.setOnBottomReachedListener(() -> {
            //
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    fab_up.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fab_up.setVisibility(View.GONE);
                }
            }
        });
        fab_up.setOnClickListener( v -> mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0));

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
        shimmer_view_container.setVisibility(View.VISIBLE);
        fetchAnnounces();
    }

    private void refresh() {
        mAnnonceAdapter.clearAll();
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();
        mAnnonceAdapter.clearSelectedStated();
        mAnnonceAdapter.setIsInChoiceMode(false);
        shimmer_view_container.setVisibility(View.VISIBLE);
        shimmer_view_container.startShimmer();

        fetchAnnounces();
    }

    private void fetchAnnounces() {
        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);

        Call<Announce> call = api.getUsersAnnounces(user);
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                shimmer_view_container.setVisibility(View.GONE);
                shimmer_view_container.stopShimmer();
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
                shimmer_view_container.stopShimmer();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recherche, menu);

        MenuItem item = menu.findItem(R.id.nav_recherche);
        deleteMenu = menu.findItem(R.id.nav_supprimer);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Rechercher dans mes annonces");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAnnonceAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAnnonceAdapter.getFilter().filter(newText);

                // TODO
                /*if (mAnnonceAdapter.getItemCount() == 0) {
                    layout_no_annonce.setVisibility(View.VISIBLE);
                } else {
                    layout_no_annonce.setVisibility(View.GONE);
                }*/
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_supprimer) {
            this.deleteAnnoncePublished();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAnnonceClicked(int position) {
        if (mAnnonceAdapter.getIsInChoiceMode()) {
            mAnnonceAdapter.switchSelectedState(position);
            deleteMenu.setVisible(mAnnonceAdapter.getSelectedItemCount() > 0);
            if(mAnnonceAdapter.getSelectedItemCount() <=0 ) {
                mAnnonceAdapter.setIsInChoiceMode(false);
            }
        } else {
            Announce.Annonce annonce = mAnnonces.get(position);
            Intent intent = new Intent(this, ActivityDetailAnnonceUser.class);
            intent.putExtra("id_ann", annonce.getId_ann());
            intent.putExtra("affiche", annonce.getAffiche());
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongItemClicked(int position) {
        mAnnonceAdapter.beginChoiceMode(position);
        deleteMenu.setVisible(mAnnonceAdapter.getSelectedItemCount() > 0);
        return true;
    }

    private void deleteAnnoncePublished(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Etes vous sûr de vouloir supprimer ces annonces? Cette action est irréversible.");
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            List<Integer> positions = mAnnonceAdapter.getSelectedItems();
            ArrayList<Announce.Annonce> annonceArrayList = new ArrayList<>();

            for (Integer position: positions) {
                Announce.Annonce annonce = mAnnonces.get(position);
                annonceArrayList.add(annonce);
                Log.d("annonce", "" + position);
            }
            for (Announce.Annonce annonce: annonceArrayList) {
                mAnnonces.remove(annonce);
                deleteAnnounce(annonce.getId_ann());
            }
            mAnnonceAdapter.notifyDataSetChanged();
            mAnnonceAdapter.clearSelectedStated();
            mAnnonceAdapter.setIsInChoiceMode(false);

            AlertDialog.Builder _builder = new AlertDialog.Builder(ActivityAnnoncesPublished.this);
            _builder.setMessage(R.string.annonces_supprimees_avec_succes);
            AlertDialog _dialog = _builder.create();
            _dialog.show();
        });
        builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    // TODO Bulk delete
    private void deleteAnnounce(String id_ann) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.suppresion_en_cours));
        Call<MyResponse> call = api.deleteAnnounce(id_ann);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                /*if (response.isSuccessful()) {

                } else {
                    Toast.makeText(ActivityAnnoncesPublished.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }*/
            }
            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d("activity", t.getMessage());
            }
        });
    }
}
