package bf.fasobizness.bafatech.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.net.URLEncoder;
import java.util.Objects;

import bf.fasobizness.bafatech.R;

public class ActivityPromouvoirAnnonces extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promouvoir_annonces);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.promouvoir_mes_annonces));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        // Contact us
        Button btn_sms = findViewById(R.id.btn_sms);
        Button btn_appel = findViewById(R.id.btn_appel);
        Button btn_facebook = findViewById(R.id.btn_facebook);
        Button btn_whatsapp = findViewById(R.id.btn_whatsapp);
        // Button btn_send_diect = findViewById(R.id.send_direct_message);

        // btn_send_diect.setOnClickListener( v -> createDiscussion() );

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

    /*
    private void createDiscussion() {
        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();
        if (user.isEmpty()) {
            FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
            notConnected.show(getSupportFragmentManager(), "");
        } else {
            ProgressDialog progressDialog = new ProgressDialog(this);
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

                            Intent intent = new Intent(getApplicationContext(), ActivityMessage.class);
                            intent.putExtra("discussion_id", String.valueOf(discussion_id));
                            intent.putExtra("receiver_id", String.valueOf(admin));
                            intent.putExtra("id_ann", "0");
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(ActivityPromouvoirAnnonces.this, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ActivityPromouvoirAnnonces.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    */
}
