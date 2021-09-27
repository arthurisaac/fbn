package bf.fasobizness.bafatech.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import bf.fasobizness.bafatech.R
import java.net.URLEncoder
import java.util.*

class ActivityPromouvoirAnnonces : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promouvoir_annonces)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.promouvoir_mes_annonces)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { view: View? -> finish() }

        // Contact us
        val btn_whatsapp = findViewById<CardView>(R.id.btn_whatsapp)
        val btn_appel = findViewById<CardView>(R.id.btn_appel)
        val btn_facebook = findViewById<CardView>(R.id.btn_facebook)
        val btn_sms = findViewById<CardView>(R.id.btn_sms)
        val cgu = findViewById<TextView>(R.id.txt_en_effet)
        val cguTxt = "En effet, vous apparaissez en tête de votre Catégorie de produits et services, ainsi qu’en tête de liste des résultats des Recherches. <strong>Cette visibilité accrue vous permet de vendre plus vite et en plus grande quantité.</strong>"
        cgu.text = HtmlCompat.fromHtml(cguTxt, HtmlCompat.FROM_HTML_MODE_LEGACY)


        // btn_send_diect.setOnClickListener( v -> createDiscussion() );
        btn_sms.setOnClickListener {
            try {
                val uri = Uri.parse("smsto:+22666691915")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        btn_appel.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "+22666691915", null))
            startActivity(intent)
        }
        btn_facebook.setOnClickListener {
            val url = "https://www.facebook.com/Fasobizness/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        btn_whatsapp.setOnClickListener {
            val phone = "+22666691915"
            val message = getString(R.string.bonjour_je_souhaite_recourir_a_vos_services)
            try {
                val packageManager = packageManager
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    val url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(message, "UTF-8")
                    i.setPackage("com.whatsapp")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } /*
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