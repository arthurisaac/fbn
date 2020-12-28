package bf.fasobizness.bafatech.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.net.URLEncoder;
import java.util.Objects;

import bf.fasobizness.bafatech.R;

public class ActivityInfos extends AppCompatActivity {

    // private static final String TAG = "ActivityInfos";
    // private String user;
    // private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.infos_pratiques));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        RelativeLayout btn_share = findViewById(R.id.btn_share);
        RelativeLayout rl_rate = findViewById(R.id.rl_rate);
        rl_rate.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=" + getApplication().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplication().getPackageName())));
            }
        });
        Button btn_voir_plus = findViewById(R.id.btn_voir_plus);
        btn_voir_plus.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fasobizness.com/uploads/cgu.pdf"));
            startActivity(intent);
        });

        btn_share.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBodyText = getString(R.string.telecharger_et_partager_l_application_recommender);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "  ");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)));
        });

        // Contact us
        CardView btn_sms = findViewById(R.id.btn_sms);
        CardView btn_appel = findViewById(R.id.btn_appel);
        CardView btn_facebook = findViewById(R.id.btn_facebook);
        CardView btn_whatsapp = findViewById(R.id.btn_whatsapp);

        RelativeLayout btn_facebook_like = findViewById(R.id.btn_facebook_like);

        btn_sms.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse("smsto:+22666691915");
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btn_appel.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "+22666691915", null));
            startActivity(intent);
        });

        btn_facebook.setOnClickListener(v -> {
            String url = "https://www.facebook.com/Fasobizness/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        btn_facebook_like.setOnClickListener(v -> {
            String url = "https://www.facebook.com/Fasobizness/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        btn_whatsapp.setOnClickListener(v -> {
            String phone = "+22666691915";
            String message = getString(R.string.bonjour_je_souhaite_recourir_a_vos_services);
            try {
                PackageManager packageManager = getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                try {
                    String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(message, "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*private void createDiscussion() {

        if (user.isEmpty()) {
            FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
            notConnected.show(getSupportFragmentManager(), "");
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.chargement_en_cours);
            progressDialog.setMessage(getString(R.string.assistance));
            progressDialog.show();

            API api = RetrofitClient.getClient().create(API.class);
            Call<MyResponse> call = api.createDiscussion(String.valueOf(0), user, String.valueOf(0));
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    progressDialog.dismiss();

                    if (response.isSuccessful()) {
                        int discussion_id, admin;
                        if (response.body() != null) {
                            discussion_id = response.body().getId();
                            admin = response.body().getAdmin();
                            Log.d(TAG, "admin: " +admin);
                            Log.d(TAG, "id: " +discussion_id);

                            Intent intent = new Intent(getApplicationContext(), ActivityMessage.class);
                            intent.putExtra("discussion_id", String.valueOf(discussion_id));
                            intent.putExtra("receiver_id", String.valueOf(admin));
                            intent.putExtra("id_ann", "0");
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(ActivityInfos.this, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ActivityInfos.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }*/
}
