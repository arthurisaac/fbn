package bf.fasobizness.bafatech.activities.recrutement;

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
import bf.fasobizness.bafatech.utils.DatabaseManager;
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
    private DatabaseManager databaseManager;

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

        databaseManager = new DatabaseManager(this);
        List<Recruit.Recrutement> recrutementList = databaseManager.getRecruits();
        mRecrutements.addAll(recrutementList);
        mRecrutementAdapter.notifyDataSetChanged();

    }

    private void jsonParse() {
        mRecrutementAdapter.getFilter().filter(query);
        Log.d("Filtering", "Recherche " + query);
    }

    /*private void jsonParse() {
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
    }*/

    @Override
    public void onItemClicked(int position) {
        Recruit.Recrutement recrutement = mRecrutements.get(position);
        Intent intent = new Intent(getApplicationContext(), ActivityDetailsRecrutement.class);
        intent.putExtra("recrutement", recrutement);
        startActivity(intent);
    }
}
