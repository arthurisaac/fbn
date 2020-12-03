package bf.fasobizness.bafatech.activities.annonce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.adapters.ImagesAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnImageListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailAnnonceUser extends AppCompatActivity implements OnImageListener {

    private static final String TAG = "ActivityDetailsAnnonce";
    private ArrayList<String> imagesList;
    private ImagesAdapter imagesAdapter;

    private String user;
    private TextView txt_titre_annonce;
    private TextView txt_text;
    private TextView txt_prix;
    private TextView txt_email;
    private TextView txt_tel1;
    private TextView txt_tel2;
    private TextView txt_tel;
    private TextView txt_location;
    private TextView txt_categorie;
    private TextView txt_updatedAt;
    private TextView txt_date_pub;
    private LinearLayout ann, layout_no_annonce, loading_indicator_ann, layout_ent_offline, layout_busy_system;
    private ViewPager pager;
    private String id_ann;

    private Button btn_edit, btn_delete, btn_edit_photo, btn_share;

    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_annonce_utilisateur);

        api = RetrofitClient.getClient().create(API.class);
        MySharedManager mySharedManager = new MySharedManager(this);
        user = mySharedManager.getUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        txt_titre_annonce = findViewById(R.id.txt_titre_annonce);
        txt_text = findViewById(R.id.txt_texte_ann);
        txt_prix = findViewById(R.id.txt_prix_annonce);
        txt_email = findViewById(R.id.txt_email_util);
        txt_tel1 = findViewById(R.id.txt_tel1_annonce);
        txt_tel2 = findViewById(R.id.txt_tel2_annonce);
        txt_tel = findViewById(R.id.txt_tel_annonce);
        txt_location = findViewById(R.id.txt_location_annonce);
        txt_categorie = findViewById(R.id.txt_categorie_annonce);
        btn_share = findViewById(R.id.btn_share);
        txt_updatedAt = findViewById(R.id.txt_date_modification);
        txt_date_pub = findViewById(R.id.txt_date_pub);
        pager = findViewById(R.id.flipper_affiche_annonce);

        loading_indicator_ann = findViewById(R.id.loading_indicator_ann);
        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        ann = findViewById(R.id.ann);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        Button btn_refresh = findViewById(R.id.btn_refresh);

        layout_ent_offline.setVisibility(View.GONE);

        ann.setVisibility(View.GONE);
        loading_indicator_ann.setVisibility(View.VISIBLE);
        layout_no_annonce.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

        imagesList = new ArrayList<>();
        imagesAdapter = new ImagesAdapter(this, imagesList);
        pager.setAdapter(imagesAdapter);
        imagesAdapter.setOnImageListener(this);

        Intent extras = getIntent();
        if (extras.getStringExtra("id_ann") != null) {
            id_ann = extras.getStringExtra("id_ann");
            btn_refresh.setOnClickListener(v -> getAnnounce(extras.getStringExtra("id_ann")));
        }

        btn_edit = findViewById(R.id.btn_editer);
        btn_delete = findViewById(R.id.btn_delete);
        btn_edit_photo = findViewById(R.id.btn_editer_photo);
        btn_share = findViewById(R.id.btn_share);

    }

    private void getAnnounce(String id_ann) {

        layout_ent_offline.setVisibility(View.GONE);
        loading_indicator_ann.setVisibility(View.VISIBLE);

        Call<Announce.Annonce> call = api.getAnnounce(id_ann, user);
        call.enqueue(new Callback<Announce.Annonce>() {
            @Override
            public void onResponse(@NonNull Call<Announce.Annonce> call, @NonNull Response<Announce.Annonce> response) {
                loading_indicator_ann.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Log.d(TAG, response.toString());
                    loading_indicator_ann.setVisibility(View.GONE);
                    ann.setVisibility(View.VISIBLE);
                    Announce.Annonce announce = response.body();
                    if (announce == null) {
                        layout_ent_offline.setVisibility(View.GONE);
                        ann.setVisibility(View.GONE);
                        layout_no_annonce.setVisibility(View.VISIBLE);
                    } else {
                        populateData(announce);
                    }
                } else {
                    layout_busy_system.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Announce.Annonce> call, @NonNull Throwable t) {

                layout_ent_offline.setVisibility(View.VISIBLE);
                ann.setVisibility(View.GONE);
                layout_no_annonce.setVisibility(View.GONE);
                loading_indicator_ann.setVisibility(View.GONE);

                Log.d(TAG, t.toString());
                Toast.makeText(ActivityDetailAnnonceUser.this, getString(R.string.pas_d_acces_internet), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void populateData(final Announce.Annonce annonce) {
        txt_titre_annonce.setText(annonce.getTitre());
        txt_text.setText(annonce.getTexte());
        txt_prix.setText(annonce.getPrix());
        txt_email.setText(annonce.getEmail());
        txt_tel.setText(annonce.getTel());
        txt_tel1.setText(annonce.getTel1());
        txt_tel2.setText(annonce.getTel2());
        txt_location.setText(annonce.getLocation());
        txt_categorie.setText(annonce.getCategorie());
        String str_share = this.getString(R.string.partager_annonce, annonce.getShare());
        btn_share.setText(str_share);

        String updatedAt = annonce.getUpdatedAt();
        String str_updatedAt = this.getString(R.string.derni_re_mise_jour_le_1_s, updatedAt);
        if (updatedAt != null) {
            if (!updatedAt.isEmpty()) {
                txt_updatedAt.setText(str_updatedAt);
                txt_updatedAt.setVisibility(View.VISIBLE);
            } else {
                txt_updatedAt.setVisibility(View.GONE);
            }
        }

        String datePub = getString(R.string.publi_le, annonce.getDate_pub());
        txt_date_pub.setText(datePub);

        if (annonce.getCategorie().equals("null") || annonce.getCategorie().isEmpty()) {
            txt_categorie.setText(R.string.aucune_categorie_renseignee);
        }

        if (annonce.getTel().isEmpty()) {
            txt_tel.setVisibility(View.GONE);
        }

        if (annonce.getTel1().isEmpty()) {
            txt_tel1.setVisibility(View.GONE);
        }

        if (annonce.getTel2().isEmpty()) {
            txt_tel2.setVisibility(View.GONE);
        }
        if (annonce.getEmail().length() == 0) {
            txt_email.setVisibility(View.GONE);
        }
        if (annonce.getPrix().length() == 0) {
            txt_prix.setText(R.string.prix_sur_demande);
        }
        if (annonce.getLocation().length() == 0) {
            txt_location.setVisibility(View.GONE);
        }
        if (annonce.getLocation().isEmpty()) {
            txt_location.setVisibility(View.GONE);
        }

        try {
            // JSONArray jsonArray = new JSONArray(annonce.getIllustrations());
            List<Announce.Annonce.Illustration> arrayList = annonce.illustrations;
            for (Announce.Annonce.Illustration data : arrayList) {
                imagesList.add(data.getNom());
                // Log.d(TAG, data.toString());
            }
            if (arrayList.size() == 0) {
                pager.setVisibility(View.GONE);
            }
            imagesAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_edit.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ActivityAnnonceEditer.class);
                intent.putExtra("annonce", annonce);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btn_edit_photo.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ActivityAnnounceEditImages.class);
                intent.putExtra("annonce", annonce);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btn_delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirmation_suppression_annonce);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> deleteAnnonce(annonce.getId_ann()));
            builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        btn_share.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBodyText = annonce.getTexte() + "\n" + getString(R.string.telecharger_et_partager_l_application, "http://fasobizness.com/annonces/" + id_ann);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, annonce.getTitre());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)));
        });


    }

    private void deleteAnnonce(String id_ann) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.suppresion_en_cours));
        Call<MyResponse> call = api.deleteAnnounce(id_ann);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                if (response.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetailAnnonceUser.this);
                    builder.setMessage(R.string.annonce_supprimee_avec_succes);
                    builder.setPositiveButton(R.string.ok, (dialog, id) -> finish());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(ActivityDetailAnnonceUser.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Toast.makeText(ActivityDetailAnnonceUser.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onImageClicked(int position) {
        Intent intent = new Intent(this, ActivityFullScreen.class);
        intent.putStringArrayListExtra("images", imagesList);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ann.setVisibility(View.GONE);
        loading_indicator_ann.setVisibility(View.VISIBLE);
        getAnnounce(id_ann);
    }
}
