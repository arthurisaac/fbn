package bf.fasobizness.bafatech.activities.annonce;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ActivitySearchAnnonce extends AppCompatActivity implements OnAnnonceListener {
    private static final String TAG = "ActivityRecherche";
    private TextView resultats;
    private String query;
    private LinearLayout layout_ent_offline, layout_busy_system;
    private ShimmerFrameLayout shimmer_view_container;

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche_annonce);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        EditText ed_rechercher = findViewById(R.id.ed_rechercher);
        resultats = findViewById(R.id.resultats);

        // Init annonce recyclerview
        mAnnonces = new ArrayList<>();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_annonce);
        LinearLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAnnonceAdapter = new AnnounceAdapter(this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);
        mAnnonceAdapter.setOnBottomReachedListener(() -> {
            /*int totalPage = total / 10;
            if (page <= totalPage) {
                loading_indicator.setVisibility(View.VISIBLE);
                jsonParse();
            }*/
        });

        ed_rechercher.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                query = v.getText().toString();
                resultats.setText(R.string.recherche_en_cours);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (in != null) {
                    in.hideSoftInputFromWindow(ed_rechercher.getWindowToken(), 0);
                }
                handled = true;
                jsonParse();
            }
            return handled;
        });

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_ent_offline.setVisibility(View.GONE);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> jsonParse());

        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_busy_system.setVisibility(View.GONE);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        shimmer_view_container.setVisibility(View.GONE);
    }

    private void jsonParse() {
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();
        resultats.setText(R.string.recherche_en_cours);
        resultats.setVisibility(View.VISIBLE);

        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        shimmer_view_container.setVisibility(View.VISIBLE);

        API api = RetrofitClient.getClient().create(API.class);
        Call<Announce> call = api.searchAnnounces(query);
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                shimmer_view_container.setVisibility(View.GONE);
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
                        layout_ent_offline.setVisibility(View.GONE);
                    }
                    mAnnonceAdapter.notifyDataSetChanged();
                    String reslt = getString(R.string.resultats, mAnnonces.size() + "");
                    if (mAnnonces.size() == 0) {
                        resultats.setText(R.string.aucun_resultat);
                    }

                    resultats.setText(reslt);
                } else {
                    layout_busy_system.setVisibility(View.VISIBLE);
                    resultats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {
                shimmer_view_container.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
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
