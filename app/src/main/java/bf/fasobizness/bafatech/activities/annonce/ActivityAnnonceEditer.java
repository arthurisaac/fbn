package bf.fasobizness.bafatech.activities.annonce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAnnonceEditer extends AppCompatActivity {
    private TextInputLayout til_titre_annonce, til_desc_annonce, til_tel_annonce, til_tel1_annonce, til_tel2_annonce, ed_prix;
    private Spinner sp_ville, sp_categorie;
    private Button btn_publish_offer;
    private String user;
    private CoordinatorLayout ll_base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonce_editer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.modification));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        MySharedManager mySharedManager = new MySharedManager(this);
        user = mySharedManager.getUser();

        til_titre_annonce = findViewById(R.id.til_titre_annonce);
        til_desc_annonce = findViewById(R.id.til_description_annonce);
        til_tel_annonce = findViewById(R.id.til_tel_annonce);
        til_tel1_annonce = findViewById(R.id.til_tel1_annonce);
        til_tel2_annonce = findViewById(R.id.til_tel2_annonce);
        ed_prix = findViewById(R.id.ed_prix_annonce);
        btn_publish_offer = findViewById(R.id.btn_publish);
        ll_base = findViewById(R.id.ll_base);

        sp_ville = findViewById(R.id.sp_ville_annonce);
        sp_categorie = findViewById(R.id.sp_categorie_annonce);

        sp_ville.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.villes)));
        sp_categorie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categories)));

        Intent extras = getIntent();
        Announce.Annonce annonce = (Announce.Annonce) extras.getSerializableExtra("annonce");
        if (annonce != null) {
            populateData(annonce);
        }
    }

    private void updatePost(String id_ann) {
        ProgressDialog progressDoalog = new ProgressDialog(this);
        progressDoalog.setTitle(R.string.chargement_en_cours);
        progressDoalog.show();

        API api = RetrofitClient.getClient().create(API.class);

        String titre = Objects.requireNonNull(til_titre_annonce.getEditText()).getText().toString();
        String texte = Objects.requireNonNull(til_desc_annonce.getEditText()).getText().toString();
        String tel1 = Objects.requireNonNull(til_tel1_annonce.getEditText()).getText().toString();
        String tel2 = Objects.requireNonNull(til_tel2_annonce.getEditText()).getText().toString();
        String tel = Objects.requireNonNull(til_tel_annonce.getEditText()).getText().toString();
        String prix = Objects.requireNonNull(ed_prix.getEditText()).getText().toString();
        String ville = sp_ville.getSelectedItem().toString();
        String categorie = sp_categorie.getSelectedItem().toString();

        Call<MyResponse> call = api.updateAnnounce(
                id_ann,
                user,
                texte,
                prix,
                ville,
                tel,
                tel1,
                tel2,
                titre,
                categorie
        );
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                btn_publish_offer.setEnabled(true);
                btn_publish_offer.setText(R.string.publier);
                progressDoalog.dismiss();
                Log.d("Activity", response.toString());

                if (response.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAnnonceEditer.this);
                    builder.setMessage(R.string.l_annonce_a_ete_modifiee_avec_succes);
                    builder.setPositiveButton(R.string.ok, (dialog, id) -> finish());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(ActivityAnnonceEditer.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Snackbar.make(ll_base, R.string.pas_d_acces_internet, Snackbar.LENGTH_SHORT).show();
                btn_publish_offer.setEnabled(true);
                btn_publish_offer.setText(R.string.publier);
                progressDoalog.dismiss();
            }
        });

        /*String url = Constants.HOST_URL + "annonce/update";
        StringRequest request1 = new StringRequest(Request.Method.POST, url, response -> {
            btn_publish_offer.setEnabled(true);
            btn_publish_offer.setText(R.string.publier);
            Log.d("activity", response );
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean status = jsonObject.getBoolean("status");
                if (status) {
                    Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(ll_base, getString(R.string.l_annonce_a_ete_modifiee_avec_succes), Snackbar.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();                }
        }, error -> {
            btn_publish_offer.setEnabled(true);
            btn_publish_offer.setText(R.string.publier);
            Toast.makeText(this, "Pas d'acces internet", Toast.LENGTH_SHORT).show();
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_per_fk", user);
                params.put("texte", texte);
                params.put("prix", prix);
                params.put("ville", ville);
                params.put("tel", tel);
                params.put("tel1", tel1);
                params.put("tel2", tel2);
                params.put("titre", titre);
                params.put("categorie", categorie);
                params.put("id_ann", id_ann);

                return params;
            }
        };
        request1.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2)
        );
        Volley.newRequestQueue(this).add(request1);*/
    }

    private void populateData(Announce.Annonce annonce) {
        Objects.requireNonNull(til_titre_annonce.getEditText()).setText(annonce.getTitre());
        Objects.requireNonNull(til_desc_annonce.getEditText()).setText(annonce.getTexte());
        Objects.requireNonNull(til_tel1_annonce.getEditText()).setText(annonce.getTel1());
        Objects.requireNonNull(til_tel2_annonce.getEditText()).setText(annonce.getTel2());
        Objects.requireNonNull(til_tel_annonce.getEditText()).setText(annonce.getTel());
        Objects.requireNonNull(ed_prix.getEditText()).setText(annonce.getPrix());

        for (int i = 0; i < sp_ville.getAdapter().getCount(); i++) {
            if (sp_ville.getAdapter().getItem(i).toString().contains(annonce.getLocation())) {
                sp_ville.setSelection(i);
            }
        }
        for (int i = 0; i < sp_categorie.getAdapter().getCount(); i++) {
            if (sp_categorie.getAdapter().getItem(i).toString().contains(annonce.getCategorie())) {
                sp_categorie.setSelection(i);
            }
        }

        btn_publish_offer.setOnClickListener(v -> {
            btn_publish_offer.setText(R.string.publication_en_cours);
            btn_publish_offer.setEnabled(false);
            updatePost(annonce.getId_ann());
        });
    }
}
