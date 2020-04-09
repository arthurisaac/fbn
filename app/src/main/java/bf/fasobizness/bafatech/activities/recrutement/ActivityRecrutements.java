package bf.fasobizness.bafatech.activities.recrutement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import bf.fasobizness.bafatech.ActivityBoutique;
import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnonceCategory;
import bf.fasobizness.bafatech.activities.annonce.ActivityOffreOr;
import bf.fasobizness.bafatech.activities.entreprise.ActivityEntreprisesUne;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityDiscussions;
import bf.fasobizness.bafatech.adapters.RecrutementAdapter;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recrutement;
import bf.fasobizness.bafatech.utils.Constants;
import bf.fasobizness.bafatech.utils.MySharedManager;

public class ActivityRecrutements extends AppCompatActivity implements OnItemListener {

    private static final String TAG = "ActivityRecrutements";
    private ArrayList<Recrutement> mRecrutements;
    private RecrutementAdapter mRecrutementAdapter;
    private RequestQueue requestQueue;
    private ShimmerFrameLayout mShimmerViewContainer;
    // private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recrutements);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Recrutements");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.setVisibility(View.VISIBLE);

        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();

        mRecrutements = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
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

        jsonParse();

    }

    private void jsonParse() {
        String url = Constants.HOST_URL + "v1/recruits";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                mShimmerViewContainer.setVisibility(View.GONE);

                JSONArray jsonArray = response.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    JSONArray affiches = data.getJSONArray("affiches");

                    String nom_r = data.getString("nom_r");
                    String desc = data.getString("desc");
                    String domaine = data.getString("domaine");
                    String date_pub = data.getString("date_pub");
                    String description = data.getString("description");
                    String date_fin = data.getString("date_fin");
                    String heure_fin = data.getString("heure_fin");
                    String nom_ent = data.getString("nom_ent");
                    String vue = data.getString("vue");
                    String id_rec = data.getString("id_recr");
                    String lien = data.getString("lien");

                    Recrutement recrutement = new Recrutement();
                    recrutement.setNom_r(nom_r);
                    recrutement.setDesc(desc);
                    recrutement.setDomaine(domaine);
                    recrutement.setDate_pub(date_pub);
                    recrutement.setDescription(description);
                    recrutement.setAffiches(affiches.toString());
                    recrutement.setDate_fin(date_fin);
                    recrutement.setHeure_fin(heure_fin);
                    recrutement.setNom_ent(nom_ent);
                    recrutement.setVue(vue);
                    recrutement.setId_recr(id_rec);
                    recrutement.setLien(lien);

                    mRecrutements.add(recrutement);
                }
                mRecrutementAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                mShimmerViewContainer.setVisibility(View.GONE);
                e.printStackTrace();
                Toast.makeText(ActivityRecrutements.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            mShimmerViewContainer.setVisibility(View.GONE);
            Log.d(TAG, error.toString());
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 1)
        );
        requestQueue.add(request);
    }

    @Override
    public void onItemClicked(int position) {
        Recrutement recrutement = mRecrutements.get(position);
        Intent intent = new Intent(getApplicationContext(), ActivityDetailsRecrutement.class);
        intent.putExtra("recrutement", recrutement);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recherche, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_recherche) {
            startActivity(new Intent(this, ActivityRecrutementRecherche.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
