package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity;
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

    private String user, token;
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
    private Button send_message, send_whatsapp_message;
    private Button btn_share;
    private TextView txt_updatedAt;
    private TextView txt_date_pub;
    // private ImageView iv_affiche;
    private ImageView txt_photo_util;
    private TextView txt_vue;
    private LinearLayout ann, layout_no_annonce, loading_indicator_ann, layout_ent_offline, layout_busy_system, bottom_buttons;
    private RelativeLayout see_more_annonce;
    private String fav;
    private ImageSlider pager;
    // private ProgressBar progress_bar_discussion;
    private API api;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_annonce);

        api = RetrofitClient.getClient().create(API.class);
        MySharedManager mySharedManager = new MySharedManager(ActivityDetailsAnnonce.this);
        user = mySharedManager.getUser();
        token = "Bearer " + mySharedManager.getToken();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
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
        txt_vue = findViewById(R.id.txt_vue);
        txt_categorie = findViewById(R.id.txt_categorie_annonce);
        send_message = findViewById(R.id.send_direct_message);
        btn_share = findViewById(R.id.btn_share);
        ajouterFavori = findViewById(R.id.favorite);
        txt_updatedAt = findViewById(R.id.txt_date_modification);
        txt_date_pub = findViewById(R.id.txt_date_pub);
        pager = findViewById(R.id.flipper_affiche_annonce);
        send_whatsapp_message = findViewById(R.id.send_whatsapp_message);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        loading_indicator_ann = findViewById(R.id.loading_indicator_ann);
        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        ann = findViewById(R.id.ann);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        bottom_buttons = findViewById(R.id.bottom_buttons);

        see_more_annonce = findViewById(R.id.see_more_annonce);
        layout_ent_offline.setVisibility(View.GONE);

        ann.setVisibility(View.GONE);
        loading_indicator_ann.setVisibility(View.VISIBLE);
        layout_no_annonce.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

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
        bottom_buttons.setVisibility(View.GONE);

        Call<Announce.Annonce> call = api.getAnnounce(id_ann, user);
        call.enqueue(new Callback<Announce.Annonce>() {
            @Override
            public void onResponse(@NonNull Call<Announce.Annonce> call, @NonNull Response<Announce.Annonce> response) {
                loading_indicator_ann.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    bottom_buttons.setVisibility(View.VISIBLE);
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
            txt_text.setText(annonce.getTexte());
        }

        txt_titre_annonce.setText(annonce.getTitre());
        if (annonce.getPrix().isEmpty() || annonce.getPrix() == null || annonce.getPrix().equals("0")) {
            txt_prix.setText(R.string.prix_sur_demande);
        } else {
            String prix = annonce.getPrix() + " F CFA";
            txt_prix.setText(prix);
        }
        // txt_prix.setText(annonce.getPrix());
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

        String dt_str = getString(R.string.publiee_le, annonce.getDate_pub());
        txt_date_pub.setText(dt_str);

        String _txt_vue = getString(R.string._vues, annonce.getVue());
        txt_vue.setText(_txt_vue);

        if (annonce.getCategorie() == null || annonce.getCategorie().isEmpty()) {
            txt_categorie.setText(R.string.aucune_categorie_renseignee);
        } else {
            txt_categorie.setOnClickListener(v -> {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("categorie", annonce.getCategorie());
                    Intent intent = new Intent(this, ActivityAnnounceFilter.class);
                    intent.putExtra("filter", "multiple");
                    intent.putExtra("params", jsonObject.toString());
                    intent.putExtra("title", annonce.getCategorie());
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (annonce.getLocation() == null || annonce.getLocation().isEmpty()) {
            txt_location.setVisibility(View.GONE);
        } else {
            txt_location.setOnClickListener(v -> {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("location", annonce.getLocation());
                    Intent intent = new Intent(this, ActivityAnnounceFilter.class);
                    intent.putExtra("filter", "multiple");
                    intent.putExtra("params", jsonObject.toString());
                    intent.putExtra("title", annonce.getLocation());
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
        /*if (annonce.getEmail().length() == 0) {
            txt_email.setVisibility(View.GONE);
        } else {
            try {
                txt_email.setOnClickListener(v -> sendEmail(annonce.getEmail(), annonce.getTitre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
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
            // String shareBodyText = annonce.getTitre() + "\n" +annonce.getTexte() + "\n" + getString(R.string.telecharger_et_partager_l_application);
            String shareBodyText = "Salut, voici une annonce intéressante que je viens de découvrir sur Faso Biz Nèss : " + annonce.getTitre() + " " + annonce.getTexte() +
                    "\n\nPour en savoir plus, clique ici : https://fasobizness.com/annonce/" + annonce.getId_ann() + ". \n" +
                    "\n\nSi tu n’as pas encore l’application tu peux la télécharger gratuitement sur Playstore : http://bit.ly/AndroidFBN";
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
                createDiscussion(annonce.getId_per_fk(), id_ann);
            }
        });

        if (annonce.getWhatsapp() != null) {
            send_whatsapp_message.setOnClickListener( v -> {
                String numero = annonce.getWhatsapp();
                if (!annonce.getWhatsapp().contains("226")) {
                    numero = "+226" + annonce.getWhatsapp();
                }
                String waMessage = "Bonjour, je suis intéressé par votre annonce publiée sur *Faso Biz Nèss* intitulée « " + annonce.getTitre() + " ». Merci de m’envoyer plus d’informations. L’annonce se trouve ici https://fasobizness.com/annonce/"+annonce.getId_ann();
                waMessage = waMessage.replace(" ", "%20");
                String link = "https://wa.me/" + numero + "?text=" + waMessage;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            });
        }
        /*else {
            String numero = annonce.getTel();
            if (!annonce.getTel().contains("226")) {
                numero = "+226" + annonce.getTel();
            }
            String link = "https://wa.me/" + numero + "?text=Bonjour,%20j’%20vu%20votre%20affiche%20sur%20Faso%20Biz%20Nèss%20et%20je%20voudrais%20avoir%20plus%20d’informations";
            send_whatsapp_message.setOnClickListener( v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            });
        }*/

        try {
            List<Announce.Annonce.Illustration> arrayList = annonce.illustrations;
            for (Announce.Annonce.Illustration data : arrayList) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createDiscussion(String receiver_id, String id_ann) {

        if (user.isEmpty()) {
            FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
            notConnected.show(getSupportFragmentManager(), "");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            send_message.setEnabled(false);
            Call<MyResponse> call = api.createDiscussion(receiver_id, id_ann, token);
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    send_message.setEnabled(true);
                    if (response.isSuccessful()) {
                        int discussion_id;
                        if (response.body() != null) {
                            discussion_id = response.body().getId();

                            Intent intent = new Intent(getApplicationContext(), DefaultMessagesActivity.class);
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
                    progressBar.setVisibility(View.GONE);
                    send_message.setEnabled(true);
                    Toast.makeText(ActivityDetailsAnnonce.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });

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

    /*private void sendEmail(String emailTo, String titre) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailTo, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titre);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre message");
        startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."));
    }*/

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
    }
}
