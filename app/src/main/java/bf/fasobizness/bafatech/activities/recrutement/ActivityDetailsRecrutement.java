package bf.fasobizness.bafatech.activities.recrutement;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.adapters.IllustrationRecAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recruit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailsRecrutement extends AppCompatActivity implements OnItemListener {

    private static final String TAG = "ActivityDetailsRecrut";
    private ArrayList<String> mIllustrations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_recrutement);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.details);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        Intent extras = getIntent();
        final Recruit.Recrutement recrutement = (Recruit.Recrutement) extras.getSerializableExtra("recrutement");

        TextView tv_nom_r = findViewById(R.id.tv_recrutement_nom_r);
        TextView tv_domaine = findViewById(R.id.tv_recrutement_domaine2);
        TextView tv_description = findViewById(R.id.tv_recrutement_description);
        TextView tv_date_pub = findViewById(R.id.tv_recrutement_date_pub_2);
        TextView tv_date_fin = findViewById(R.id.tv_recrutement_date_depot);
        TextView tv_heure_fin = findViewById(R.id.tv_recrutement_heure_depot);
        TextView tv_nom_ent = findViewById(R.id.tv_recrutement_nom_ent);
        Button btn_share = findViewById(R.id.btn_share);
        Button btn_postuler = findViewById(R.id.btn_postuler);

        try {
            if (recrutement != null) {
                if (recrutement.getLien() == null || recrutement.getLien().isEmpty() || recrutement.getLien().equals("null")) {
                    btn_postuler.setVisibility(View.GONE);
                } else {
                    btn_postuler.setVisibility(View.VISIBLE);
                }

                String date_pub = getString(R.string.publi_le, recrutement.getDate_pub());
                tv_nom_r.setText(recrutement.getNom_r());
                tv_domaine.setText(recrutement.getDomaine());
                tv_description.setText(recrutement.getDescription());
                tv_date_pub.setText(date_pub);
                tv_date_fin.setText(recrutement.getDate_fin());
                tv_heure_fin.setText(recrutement.getHeure_fin());
                tv_nom_ent.setText(recrutement.getNom_ent());

                btn_share.setOnClickListener(v -> {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBodyText = "Je partage cette offre d’emploi pour " + recrutement.getNom_r() + ". Tous les détails sont sur l’application Faso Biz Nèss, à télécharger sur Playstore, http://bit.ly/AndroidFBN.";
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Offre d'emploi " + recrutement.getNom_r());
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(sharingIntent, "Partager avec"));
                });

                btn_postuler.setOnClickListener(v -> {
                    String url = recrutement.getLien();
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                });

                seen(recrutement.getId_recr());
                jsonParse(recrutement);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
        }

    }

    private void seen(final String id_recr) {
        /*String url = Constants.HOST_URL + "recrutement/vue/"+id_recr;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> Log.v(TAG, response), error -> {
                    Log.v(TAG, error.toString());
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(request);*/
        API api = RetrofitClient.getClient().create(API.class);
        Call<Integer> call = api.markAsReadRecruit(id_recr);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                Log.d(TAG, response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    private void jsonParse(Recruit.Recrutement recrutement) {
        mIllustrations = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerview_piece_jointe);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            List<Recruit.Recrutement.Affiche> affiches = recrutement.affiches;
            for (int i = 0; i < affiches.size(); i++) {
                mIllustrations.add(affiches.get(i).getNom());
            }

            IllustrationRecAdapter mIllustrationAdapter = new IllustrationRecAdapter(this, mIllustrations);
            recyclerView.setAdapter(mIllustrationAdapter);
            mIllustrationAdapter.setOnItemListener(this);
            mIllustrationAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getExtension(String filePath) {
        int strLenght = filePath.lastIndexOf(".");
        if (strLenght > 0) {
            return filePath.substring(strLenght + 1).toLowerCase();
        }
        return null;
    }

    @Override
    public void onItemClicked(int position) {
        String nom = mIllustrations.get(position);
        if (Objects.requireNonNull(getExtension(nom)).equals("pdf")) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(nom));
                startActivity(browserIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Uri uri = Uri.parse(nom);
                Toast.makeText(this, R.string.telecharement_en_cours, Toast.LENGTH_SHORT).show();
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Faso Biz Ness");
                request.setDescription("Téléchargement");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "faso_biz_ness_" + System.currentTimeMillis() + ".jpg");
                request.setMimeType("*/*");
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }
            }

        } else {
            Intent intent = new Intent(this, ActivityFullScreen.class);
            intent.putExtra("position", position);
            intent.putExtra("images", mIllustrations);
            startActivity(intent);
        }
    }
}
