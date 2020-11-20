package bf.fasobizness.bafatech.activities.entreprise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.EntrepriseAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnEntrepriseListener;
import bf.fasobizness.bafatech.models.Entreprise;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearchEnterprise extends AppCompatActivity
        implements OnEntrepriseListener {
    private static final String TAG = "ActivityRecherche";
    private TextView resultats;
    private String query;
    private LinearLayout layout_ent_offline, layout_busy_system;
    private ShimmerFrameLayout shimmer_view_container;

    private ArrayList<Entreprise.Entreprises> mEntreprises;
    private EntrepriseAdapter mEntrepriseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_enterprise);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.entreprise_a_la_une));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_entreprise);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mEntreprises = new ArrayList<>();

        mEntrepriseAdapter = new EntrepriseAdapter(this, mEntreprises);
        mRecyclerView.setAdapter(mEntrepriseAdapter);
        mEntrepriseAdapter.setOnEntrepriseListener(this);

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_ent_offline.setVisibility(View.GONE);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> jsonParse());

        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_busy_system.setVisibility(View.GONE);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        shimmer_view_container.setVisibility(View.GONE);

        resultats = findViewById(R.id.resultats);
        EditText ed_rechercher = findViewById(R.id.ed_rechercher);
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
    }

    private void jsonParse() {
        mEntreprises.clear();
        mEntrepriseAdapter.notifyDataSetChanged();
        resultats.setText(R.string.recherche_en_cours);
        resultats.setVisibility(View.VISIBLE);

        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        shimmer_view_container.setVisibility(View.VISIBLE);

        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();
        API api = RetrofitClient.getClient().create(API.class);
        Call<Entreprise> call = api.searchEnterprises(user, query);
        call.enqueue(new Callback<Entreprise>() {
            @Override
            public void onResponse(@NonNull Call<Entreprise> call, @NonNull Response<Entreprise> response) {
                shimmer_view_container.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Entreprise entreprise = response.body();
                    List<Entreprise.Entreprises> entreprises = null;
                    if (entreprise != null) {
                        entreprises = entreprise.entreprises;
                    }
                    if (entreprises != null) {
                        mEntreprises.addAll(entreprises);
                    }
                    mEntrepriseAdapter.notifyDataSetChanged();
                    String reslt = getString(R.string.resultats, mEntreprises.size() + "");
                    if (mEntreprises.size() == 0) {
                        resultats.setText(R.string.aucun_resultat);
                    }
                    resultats.setText(reslt);
                } else {
                    // Toast.makeText(ActivitySearchEnterprise.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                    layout_ent_offline.setVisibility(View.VISIBLE);
                    resultats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Entreprise> call, @NonNull Throwable t) {
                shimmer_view_container.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
                Toast.makeText(ActivitySearchEnterprise.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEntrepriseClicked(int position) {
        Entreprise.Entreprises entreprise = mEntreprises.get(position);
        Intent intent = new Intent(this, ActivityDetailsEntreprise.class);
        intent.putExtra("id_ent", entreprise.getId());
        startActivity(intent);
    }
}