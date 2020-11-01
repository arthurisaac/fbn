package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityMessage;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.fragments.FragmentSignaler;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailsAnnonce extends AppCompatActivity {
    private static final String TAG = "ActivityDetailsAnnonce";
    private FloatingActionButton ajouterFavori;
    // private ArrayList<String> imagesList;
    private ArrayList<String> images;
    private ArrayList<SlideModel> imageList;
    // private ImagesAdapter imagesAdapter;

    private String user;
    private TextView txt_titre_annonce;
    private TextView txt_text;
    private TextView txt_prix;
    private TextView txt_email;
    private TextView txt_tel1;
    private TextView txt_tel2;
    private TextView txt_tel;
    private TextView txt_location;
    private TextView txt_nom;
    private TextView txt_categorie;
    private Button send_message;
    private Button btn_share;
    private TextView txt_updatedAt;
    private TextView txt_date_pub;
    // private ImageView iv_affiche;
    private ImageView txt_photo_util;
    private LinearLayout ann, layout_no_annonce, loading_indicator_ann, layout_ent_offline, layout_busy_system;
    private RelativeLayout see_more_annonce;
    private String fav, token;
    private ImageSlider pager;
    // private ProgressBar progress_bar_discussion;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_annonce);

        api = RetrofitClient.getClient().create(API.class);
        MySharedManager mySharedManager = new MySharedManager(ActivityDetailsAnnonce.this);
        user = mySharedManager.getUser();
        token = "Bearer " + mySharedManager.getToken();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        // toolbar.setTitle(R.string.details);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        txt_photo_util = findViewById(R.id.txt_username_logo_ann);
        txt_titre_annonce = findViewById(R.id.txt_titre_annonce);
        txt_text = findViewById(R.id.txt_texte_ann);
        txt_prix = findViewById(R.id.txt_prix_annonce);
        txt_email = findViewById(R.id.txt_email_util);
        txt_tel1 = findViewById(R.id.txt_tel1_annonce);
        txt_tel2 = findViewById(R.id.txt_tel2_annonce);
        txt_tel = findViewById(R.id.txt_tel_annonce);
        txt_location = findViewById(R.id.txt_location_annonce);
        txt_nom = findViewById(R.id.txt_username_ann);
        txt_categorie = findViewById(R.id.txt_categorie_annonce);
        send_message = findViewById(R.id.send_direct_message);
        btn_share = findViewById(R.id.btn_share);
        ajouterFavori = findViewById(R.id.favorite);
        txt_updatedAt = findViewById(R.id.txt_date_modification);
        txt_date_pub = findViewById(R.id.txt_date_pub);
        // iv_affiche = findViewById(R.id.iv_affiche_annonce);
        pager = findViewById(R.id.flipper_affiche_annonce);

        loading_indicator_ann = findViewById(R.id.loading_indicator_ann);
        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        ann = findViewById(R.id.ann);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        Button btn_refresh = findViewById(R.id.btn_refresh);

        see_more_annonce = findViewById(R.id.see_more_annonce);
        layout_ent_offline.setVisibility(View.GONE);
        // progress_bar_discussion = findViewById(R.id.progress_bar_discussion);
        // iv_chat = findViewById(R.id.iv_chat);

        ann.setVisibility(View.GONE);
        loading_indicator_ann.setVisibility(View.VISIBLE);
        layout_no_annonce.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

        /*imagesList = new ArrayList<>();
        imagesAdapter = new ImagesAdapter(this, imagesList);*/
        /*pager.setAdapter(imagesAdapter);
        imagesAdapter.setOnImageListener(this);*/
        images = new ArrayList<>();
        imageList = new ArrayList<>();

        Intent extras = getIntent();
        if (extras.getStringExtra("id_ann") != null) {
            getAnnounce(extras.getStringExtra("id_ann"));
            btn_refresh.setOnClickListener(v -> getAnnounce(extras.getStringExtra("id_ann")));
        } else {
            finish();
        }

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
                    // Log.d(TAG, response.toString());
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

                Log.v(TAG, t.toString());
                Toast.makeText(ActivityDetailsAnnonce.this, getString(R.string.pas_d_acces_internet), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void populateData(final Announce.Annonce annonce) {

        /*Glide.with(this)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark))
                .asBitmap()
                .load(annonce.getAffiche())
                .thumbnail(0.1f)
                .into(iv_affiche);

        iv_affiche.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityFullScreen.class);
            intent.putStringArrayListExtra("images", images);
            intent.putExtra("position", 1);
            startActivity(intent);
        });*/

        try {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(annonce.getPhoto())
                    .thumbnail(0.1f)
                    .into(txt_photo_util);
        } catch (Exception e) {
            finish();
            e.printStackTrace();
        }


        if (annonce.getTexte() != null) {
            // txt_text.setText(HtmlCompat.fromHtml(annonce.getTexte(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            txt_text.setText(annonce.getTexte());
        }

        txt_titre_annonce.setText(annonce.getTitre());
        txt_prix.setText(annonce.getPrix());
        txt_email.setText(annonce.getEmail());
        txt_tel.setText(annonce.getTel());
        txt_tel1.setText(annonce.getTel1());
        txt_tel2.setText(annonce.getTel2());
        txt_location.setText(annonce.getLocation());
        txt_categorie.setText(annonce.getCategorie());
        txt_nom.setText(annonce.getUsername());
        String str_share = this.getString(R.string.partager_annonce, annonce.getShare());
        btn_share.setText(str_share);

        Button btn_signaler = findViewById(R.id.signaler_annonce);
        btn_signaler.setOnClickListener(v -> {
            FragmentSignaler fragmentSignaler = FragmentSignaler.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("element", "annonce");
            bundle.putString("id_element", annonce.getId_ann());
            fragmentSignaler.setArguments(bundle);
            fragmentSignaler.show(getSupportFragmentManager(), "");
        });

        see_more_annonce.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityUserProfile.class);
            intent.putExtra("id", annonce.getId_per_fk());
            startActivity(intent);
        });

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

        String dt_str = getString(R.string.publie_le, annonce.getDate_pub());
        txt_date_pub.setText(dt_str);

        if (annonce.getCategorie() == null || annonce.getCategorie().isEmpty()) {
            txt_categorie.setText(R.string.aucune_categorie_renseignee);
        } else {
            txt_categorie.setOnClickListener(v -> {

                //String arguments = "{\"ville\": \"\", \"categorie\": \"" + annonce.getCategorie() + "\"}";
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("categorie", annonce.getCategorie());
                    Intent intent = new Intent(this, ActivityAnnounceFilter.class);
                    intent.putExtra("filter", "multiple");
                    intent.putExtra("params", jsonObject.toString());
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        fav = annonce.getFavori();

        if (fav != null) {
            if (fav.equals("1")) {
                ajouterFavori.setImageResource(R.drawable.ic_star_yellow);
            }
        } else {
            ajouterFavori.setImageResource(R.drawable.ic_star_white);
        }

        if (annonce.getTel().isEmpty()) {
            txt_tel.setVisibility(View.GONE);
        } else {
            try {
                txt_tel.setOnClickListener(v -> action(annonce.getTel()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (annonce.getTel1().isEmpty()) {
            txt_tel1.setVisibility(View.GONE);
        } else {
            try {
                txt_tel1.setOnClickListener(v -> action(annonce.getTel1()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (annonce.getTel2().isEmpty()) {
            txt_tel2.setVisibility(View.GONE);
        } else {
            try {
                txt_tel2.setOnClickListener(v -> action(annonce.getTel2()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (annonce.getEmail().length() == 0) {
            txt_email.setVisibility(View.GONE);
        } else {
            try {
                txt_email.setOnClickListener(v -> sendEmail(annonce.getEmail(), annonce.getTitre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        final String titre = annonce.getTitre();

        final String id_ann = annonce.getId_ann();

        btn_share.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBodyText = annonce.getTexte() + "\n" + getString(R.string.telecharger_et_partager_l_application, "http://fasobizness.com/annonces/" + id_ann);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, titre);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)));
            share_ann(id_ann);
        });

        ajouterFavori.setOnClickListener(v -> {
            if (user.isEmpty()) {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
            } else {
                addFavorite(id_ann);
            }
        });

        send_message.setOnClickListener(v -> {
            if (user.isEmpty()) {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
            } else {
                // send_message.setEnabled(false);
                // send_message.setText(R.string.chargement_en_cours);
                // iv_chat.setVisibility(View.GONE);
                // progress_bar_discussion.setVisibility(View.VISIBLE);
                createDiscussion(annonce.getId_per_fk(), id_ann);
            }
        });

        try {
            // JSONArray jsonArray = new JSONArray(annonce.getIllustrations());
            List<Announce.Annonce.Illustration> arrayList = annonce.illustrations;
            for (Announce.Annonce.Illustration data : arrayList) {
                // imagesList.add(data.getNom());
                // Log.d(TAG, data.toString());
                imageList.add(new SlideModel(data.getNom()));
                images.add(data.getNom());
            }
            if (arrayList.size() == 0) {
                pager.setVisibility(View.GONE);
            }
            pager.setImageList(imageList, true);
            pager.setItemClickListener(i -> {
                Intent intent = new Intent(this, ActivityFullScreen.class);
                intent.putStringArrayListExtra("images", images);
                intent.putExtra("position", i);
                startActivity(intent);
            });


            /*
            CircleIndicator indicator = findViewById(R.id.indicator);
            indicator.setViewPager(pager);
            */

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createDiscussion(String receiver_id, String id_ann) {

        if (user.isEmpty()) {
            FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
            notConnected.show(getSupportFragmentManager(), "");
        } else {

            Intent intent = new Intent(getApplicationContext(), ActivityMessage.class);
            intent.putExtra("receiver_id", receiver_id);
            intent.putExtra("id_ann", id_ann);
            intent.putExtra("new", "1");
            startActivity(intent);

            /*Call<MyResponse> call = api.createDiscussion(receiver_id, id_ann, token);
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    //  Log.d(TAG, response.toString());

                    // send_message.setEnabled(true);
                    // send_message.setText(R.string.discuter_avec_le_vender);
                    // iv_chat.setVisibility(View.VISIBLE);
                    // progress_bar_discussion.setVisibility(View.GONE);

                    if (response.isSuccessful()) {
                        int discussion_id;
                        if (response.body() != null) {
                            discussion_id = response.body().getId();

                            Intent intent = new Intent(getApplicationContext(), ActivityMessage.class);
                            intent.putExtra("discussion_id", String.valueOf(discussion_id));
                            intent.putExtra("receiver_id", receiver_id);
                            intent.putExtra("id_ann", id_ann);
                            intent.putExtra("new", "1");
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(ActivityDetailsAnnonce.this, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    // send_message.setEnabled(true);
                    // send_message.setText(R.string.discuter_avec_le_vender);
                    // iv_chat.setVisibility(View.VISIBLE);
                    // progress_bar_discussion.setVisibility(View.GONE);
                    Toast.makeText(ActivityDetailsAnnonce.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });*/

            /*
            String url = Constants.HOST_URL + "discussion/create?id_user=" + user + "&id_ann=" + id_ann + "&receiver_id=" + receiver_id;
            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
                send_message.setEnabled(true);
                send_message.setText(R.string.discuter_avec_le_vender);
                iv_chat.setVisibility(View.VISIBLE);
                progress_bar_discussion.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String discussion_id = jsonObject.getString("id");
                    Intent intent = new Intent(getApplicationContext(), ActivityMessage.class);
                    intent.putExtra("discussion_id", discussion_id);
                    intent.putExtra("receiver_id", receiver_id);
                    intent.putExtra("id_ann", id_ann);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                send_message.setEnabled(true);
                send_message.setText(R.string.discuter_avec_le_vender);
                iv_chat.setVisibility(View.VISIBLE);
                progress_bar_discussion.setVisibility(View.GONE);
                Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                // Log.v(TAG, error.toString());
            });
            requestQueue.add(request);
            */
        }
    }

    private void action(final String numero) {
        try {

            final CharSequence[] items = {"Composer numéro", "Envoyer SMS"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle(getString(R.string.choisir_action));
            builder.setItems(items, (dialog, i) -> {
                if (items[i].equals("Composer numéro")) {
                    compose(numero);
                } else if (items[i].equals("Envoyer SMS")) {
                    sendSMS();
                } else {
                    dialog.dismiss();
                }
            });
            builder.show();
        } catch (Exception e) {
            compose(numero);
        }
    }

    private void compose(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null));
        startActivity(intent);
    }

    private void sendSMS() {
        String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); // Need to change the build to API 19

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "text");

        if (defaultSmsPackageName != null) {
            sendIntent.setPackage(defaultSmsPackageName);
        }
        startActivity(sendIntent);
    }

    private void sendEmail(String emailTo, String titre) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailTo, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titre);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre message");
        startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."));
    }

    private void share_ann(final String id_ann) {
        Call<MyResponse> call = api.setAnnouncesActions("share", id_ann, user);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(ann, "Shared response " + response.toString(), Snackbar.LENGTH_SHORT);
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(ActivityDetailsAnnonce.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
        /*
        String url = Constants.HOST_URL + "annonce/share";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.v(TAG, "Shared response "+response), error -> Log.v(TAG, error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id_ann);

                return params;
            }
        };
        requestQueue.add(request);
         */
    }

    private void addFavorite(String id_ann) {

        if (fav != null) {
            if (fav.equals("1")) {
                ajouterFavori.setImageResource(R.drawable.ic_star_white);
                fav = "0";
            } else {
                ajouterFavori.setImageResource(R.drawable.ic_star_yellow);
                fav = "1";
            }
        } else {
            ajouterFavori.setImageResource(R.drawable.ic_star_yellow);
            fav = "1";
        }
        Call<MyResponse> call = api.setAnnouncesActions("favorite", id_ann, user);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                Log.d(TAG, response.toString());
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Snackbar.make(ann, getString(R.string.favoris_ajoute), Snackbar.LENGTH_SHORT);
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(ActivityDetailsAnnonce.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
        /*
        String url = Constants.HOST_URL + "annonce/favori/create?id_pers_fk=" + user + "&id_annonce_fk=" + id_ann + "&favori=" + favori + "&id_fav=" + id_fav;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Snackbar.make(ann, getString(R.string.favoris_ajoute), Snackbar.LENGTH_SHORT);
                }, error -> {
                    Log.v(TAG, error.toString());
                    Toast.makeText(getApplicationContext(), R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
         */
    }
}
