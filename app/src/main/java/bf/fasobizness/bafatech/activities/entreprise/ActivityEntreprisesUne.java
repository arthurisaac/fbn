package bf.fasobizness.bafatech.activities.entreprise;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ActivityEntreprisesUne extends AppCompatActivity
        implements OnEntrepriseListener {
    private ArrayList<Entreprise.Entreprises> mEntreprises;
    private EntrepriseAdapter mEntrepriseAdapter;
    private LinearLayout layout_ent_offline;

    private ShimmerFrameLayout shimmer_view_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entreprises_une);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.entreprise_a_la_une));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view_entreprise);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mEntreprises = new ArrayList<>();

        mEntrepriseAdapter = new EntrepriseAdapter(this, mEntreprises);
        mRecyclerView.setAdapter(mEntrepriseAdapter);
        mEntrepriseAdapter.setOnEntrepriseListener(this);

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(view -> getData());

        shimmer_view_container = findViewById(R.id.shimmer_view_container);

        getData();
    }

    private void getData() {
        shimmer_view_container.setVisibility(View.VISIBLE);

        layout_ent_offline.setVisibility(View.GONE);
        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();
        API api = RetrofitClient.getClient().create(API.class);
        Call<Entreprise> call = api.getEnterprises(user);
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
                } else {
                    Toast.makeText(ActivityEntreprisesUne.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                    layout_ent_offline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Entreprise> call, @NonNull Throwable t) {
                shimmer_view_container.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
                Toast.makeText(ActivityEntreprisesUne.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_enterprise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_menu_search) {
            startActivity(new Intent(ActivityEntreprisesUne.this, ActivitySearchEnterprise.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
