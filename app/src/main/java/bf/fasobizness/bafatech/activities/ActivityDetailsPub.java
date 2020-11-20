package bf.fasobizness.bafatech.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.Advertising;
import bf.fasobizness.bafatech.models.MyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailsPub extends AppCompatActivity {

    private static final String TAG = "ActivityDetailsPub";
    // private static final String TAG = "ActivityDetailsPub";
    private TextView description, vue, partage, tv_appel, tv_whatsapp, tv_facebook;
    private LinearLayout ad_layout, loading_indicator_ad, layout_ent_offline, layout_busy_system;
    private API api;
    private ArrayList<SlideModel> imageList;
    private ArrayList<String> images;
    private ImageSlider pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_pub);

        api = RetrofitClient.getClient().create(API.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Publicité");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        ImageView affiche = findViewById(R.id.iv_affiche_ad);
        description = findViewById(R.id.tv_description_ad);
        vue = findViewById(R.id.txt_vue);
        partage = findViewById(R.id.txt_share);

        ArrayList<String> imagesList = new ArrayList<>();
        imageList = new ArrayList<>();
        images = new ArrayList<>();

        ad_layout = findViewById(R.id.ad_layout);
        loading_indicator_ad = findViewById(R.id.loading_indicator_ad);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        pager = findViewById(R.id.flipper_affiche_annonce);

        tv_appel = findViewById(R.id.tv_appel);
        tv_facebook = findViewById(R.id.tv_facebook);
        tv_whatsapp = findViewById(R.id.tv_whatsapp);

        Intent extras = getIntent();
        if (extras.getStringExtra("id") != null) {
            getPub(extras.getStringExtra("id"));
            btn_refresh.setOnClickListener(v -> getPub(extras.getStringExtra("id")));
        }

        if (extras.getStringExtra("lien") != null) {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                    )
                    .asBitmap()
                    .load(extras.getStringExtra("lien"))
                    .thumbnail(0.1f)
                    .into(affiche);
            imagesList.add(extras.getStringExtra("lien"));

            affiche.setOnClickListener(v -> {
                Intent intent = new Intent(this, ActivityFullScreen.class);
                intent.putStringArrayListExtra("images", imagesList);
                intent.putExtra("position", 0);
                startActivity(intent);
            });
        }
    }

    private void getPub(String id) {
        ad_layout.setVisibility(View.GONE);
        loading_indicator_ad.setVisibility(View.VISIBLE);
        layout_busy_system.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);

        Call<Advertising.Ads> call = api.getAd(Integer.valueOf(id));
        call.enqueue(new Callback<Advertising.Ads>() {
            @Override
            public void onResponse(@NonNull Call<Advertising.Ads> call, @NonNull Response<Advertising.Ads> response) {
                loading_indicator_ad.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Advertising.Ads ad = response.body();
                    if (ad != null) {
                        ad_layout.setVisibility(View.VISIBLE);
                        populateData(ad);
                    }
                } else {
                    layout_busy_system.setVisibility(View.VISIBLE);
                    Toast.makeText(ActivityDetailsPub.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(@NonNull Call<Advertising.Ads> call, @NonNull Throwable t) {
                loading_indicator_ad.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
                // Toast.makeText(ActivityDetailsPub.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    private void populateData(Advertising.Ads ad) {
        String desc = ad.getDescription();
        Button btn_share = findViewById(R.id.btn_share);

        if (ad.getDescription() == null) {
            description.setVisibility(View.GONE);
        } else {
            try {
                // Spanned d = HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_COMPACT);
                Spanned d = fromHtml(desc);
                description.setText(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ad.getVue() == null) {
            vue.setVisibility(View.GONE);
        } else {
            vue.setText(getString(R.string._vues, ad.getVue()));
        }
        if (ad.getShared() == null) {
            partage.setVisibility(View.GONE);
        } else {
            partage.setText(getString(R.string._partages, ad.getShared()));
        }

        if (ad.getAppel() != null) {
            tv_appel.setText(ad.getAppel());
            tv_appel.setVisibility(View.VISIBLE);
        }
        if (ad.getFacebook() != null) {
            tv_facebook.setVisibility(View.VISIBLE);
            tv_facebook.setText(ad.getFacebook());
            tv_facebook.setOnClickListener( v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(ad.getFacebook()));
                startActivity(i);
            });
        }
        if (ad.getWhatsapp() != null) {
            String link = "https://wa.me/" + ad.getWhatsapp() + "?text=Bonjour,%20j’%20vu%20votre%20affiche%20sur%20Faso%20Biz%20Nèss%20et%20je%20voudrais%20avoir%20plus%20d’informations";
            tv_whatsapp.setVisibility(View.VISIBLE);
            tv_whatsapp.setText(ad.getWhatsapp());
            tv_whatsapp.setOnClickListener( v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            });
            /*tv_whatsapp.setOnClickListener(v -> {
                String message = "Bonjour, j’ai vu votre affiche sur Faso Biz Nèss et je voudrais avoir plus d’informations..";
                try {
                    PackageManager packageManager = getPackageManager();
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    try {
                        String url = "https://api.whatsapp.com/send?phone=" + ad.getWhatsapp() + "&text=" + URLEncoder.encode(message, "UTF-8");
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
            });*/
        }

        btn_share.setOnClickListener(v -> {
            Spanned d = HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_COMPACT);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBodyText = d + getString(R.string.telecharger_et_partager_l_application, "http://fasobizness.com/ads/" + ad.getId());
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)));
            share_ad(ad.getId());
        });

        List<Advertising.Ads.Affiche> afficheList = ad.getAffiches();
        for (Advertising.Ads.Affiche affiche : afficheList) {
            imageList.add(new SlideModel(affiche.getNom()));
            images.add(affiche.getNom());
        }
        if (imageList.size() == 0) {
            pager.setVisibility(View.GONE);
        }
        pager.setImageList(imageList, true);
        pager.setItemClickListener(i -> {
            Intent intent = new Intent(this, ActivityFullScreen.class);
            intent.putStringArrayListExtra("images", images);
            intent.putExtra("position", i);
            startActivity(intent);
        });
    }

    private void share_ad(String id) {
        if (id != null) {
            API api = RetrofitClient.getClient().create(API.class);
            Call<MyResponse> call = api.shareAd(Integer.valueOf(id));
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, response.toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    Log.d(TAG, t.toString());
                }
            });
        }

    }

    /*private void vuePub(final String id) {
        String url = Constants.HOST_URL + "vue_pub.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.v(TAG, response), error -> Log.v( "Recrutement erreur", error.toString() )) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };
        requestQueue.add(request);
    }*/

}
