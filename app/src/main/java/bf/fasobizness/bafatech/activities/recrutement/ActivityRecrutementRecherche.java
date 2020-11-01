package bf.fasobizness.bafatech.activities.recrutement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.RecrutementAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recruit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRecrutementRecherche extends AppCompatActivity
        implements OnItemListener {
    // private static final String TAG = "ActivityRecherche";
    private LinearLayout layout_ent_offline, layout_busy_system, layout_no_recruit;
    private ShimmerFrameLayout mShimmerViewContainer;
    private RecrutementAdapter mRecrutementAdapter;
    private ArrayList<Recruit.Recrutement> mRecrutements;
    // private RequestQueue requestQueue;
    private TextView resultats;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recrutement_recherche);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        EditText ed_rechercher = findViewById(R.id.ed_rechercher);
        resultats = findViewById(R.id.resultats);

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

        // requestQueue = Volley.newRequestQueue(this);
        mRecrutements = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_recrutements);
        mRecrutementAdapter = new RecrutementAdapter(getApplicationContext(), mRecrutements);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecrutementAdapter);
        mRecrutementAdapter.setOnItemListener(this);
        if (ed_rechercher.getText().toString().isEmpty()) {
            resultats.setText(R.string.que_recherchez_vous);
        }

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_no_recruit = findViewById(R.id.layout_no_recruit);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> jsonParse());

    }

    private void jsonParse() {
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();
        layout_busy_system.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);
        layout_no_recruit.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();

        API api = RetrofitClient.getClient().create(API.class);
        Call<Recruit> call = api.searchRecruits(query);
        call.enqueue(new Callback<Recruit>() {
            @Override
            public void onResponse(@NonNull Call<Recruit> call, @NonNull Response<Recruit> response) {
                mShimmerViewContainer.setVisibility(View.GONE);
                Recruit recruit = response.body();
                List<Recruit.Recrutement> recrutements = null;
                if (recruit != null) {
                    recrutements = recruit.recrutements;
                }
                if (recrutements != null) {
                    mRecrutements.addAll(recrutements);
                }
                mRecrutementAdapter.notifyDataSetChanged();
                if (mRecrutements.size() == 0) {
                    layout_no_recruit.setVisibility(View.VISIBLE);
                } else {
                    resultats.setVisibility(View.VISIBLE);
                    String reslt = getString(R.string.resultats, mRecrutements.size() + "");
                    resultats.setText(reslt);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Recruit> call, @NonNull Throwable t) {
                mShimmerViewContainer.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
            }
        });
    }

    /*private void jsonParse() {
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();
        layout_busy_system.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();

        String url = Constants.HOST_URL + "v1/recruits/search";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, response -> {
            mShimmerViewContainer.setVisibility(View.GONE);
            // Log.d(TAG, response.toString());
            try {
                JSONArray jsonArray = response.getJSONArray("data");
                if (mRecrutements.size() == 0) {
                    resultats.setText(R.string.aucun_resultat);
                }
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
                resultats.setVisibility(View.VISIBLE);
                String reslt = getString(R.string.resultats, mRecrutements.size() + "");
                resultats.setText(reslt);
                if (mRecrutements.size() == 0) {
                    resultats.setText(R.string.aucun_resultat);
                }

            } catch (Exception e) {
                layout_busy_system.setVisibility(View.VISIBLE);
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.d(TAG, error.toString());
            layout_ent_offline.setVisibility(View.VISIBLE);
            resultats.setVisibility(View.GONE);
            mShimmerViewContainer.setVisibility(View.GONE);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("query", query);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 1)
        );
        requestQueue.add(request);
    }*/

    @Override
    public void onItemClicked(int position) {
        Recruit.Recrutement recrutement = mRecrutements.get(position);
        Intent intent = new Intent(getApplicationContext(), ActivityDetailsRecrutement.class);
        intent.putExtra("recrutement", recrutement);
        startActivity(intent);
    }
}
