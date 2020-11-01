package bf.fasobizness.bafatech.activities.entreprise;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.activities.annonce.ActivityUserProfile;
import bf.fasobizness.bafatech.activities.user.LoginActivity;
import bf.fasobizness.bafatech.adapters.CommentaireAdapter;
import bf.fasobizness.bafatech.fragments.FragmentSignaler;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Entreprise;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailsEntreprise extends AppCompatActivity
        implements
        // OnImageListener,
        OnItemListener {
    private static final String TAG = "ActivityDetailsEntr";
    private CommentaireAdapter mCommentaireAdapter;
    private ArrayList<Entreprise.Entreprises.Comment> mCommentaires;
    private String id_ent, id;
    private ImageView ic_logo;
    private TextView tv_comment, tv_presentation, tv_nom_entreprise, txt_email, txt_tel, txt_adresse, txt_site, txt_domaine, txt_location;
    private RecyclerView mRecycleView;

    // private ViewPager pager;
    private ImageView imageViewEnt;
    private ArrayList<String> imagesList;
    // private ImagesAdapter imagesAdapter;
    private int counter = 0;
    private StoriesProgressView storiesProgressView;
    private long pressTime = 0L;
    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            long limit = 500L;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };
    private View reverse;
    private View skip;
    private View see;
    private LinearLayout loading_indicator, detail_layout, layout_ent_offline, layout_busy_system;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_entreprise);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        mCommentaires = new ArrayList<>();
        mRecycleView = findViewById(R.id.recyclerview_commentaire);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));

        mCommentaireAdapter = new CommentaireAdapter(this, mCommentaires);
        mRecycleView.setAdapter(mCommentaireAdapter);
        mCommentaireAdapter.setOnItemListener(this);

        tv_presentation = findViewById(R.id.txt_presentation_entreprise);
        tv_nom_entreprise = findViewById(R.id.txt_nom_entreprise);
        ic_logo = findViewById(R.id.ci_logo_entreprise);
        Button ib_send = findViewById(R.id.btn_comment_send);
        txt_email = findViewById(R.id.txt_email_util);

        txt_tel = findViewById(R.id.txt_tel_entreprise);
        txt_adresse = findViewById(R.id.txt_adresse_entreprise);
        txt_location = findViewById(R.id.txt_location_entreprise);
        txt_site = findViewById(R.id.txt_site_entreprise);
        txt_domaine = findViewById(R.id.txt_domaine_ent);
        tv_comment = findViewById(R.id.tv_commentaire);
        // pager = findViewById(R.id.flipper_affiche_entreprise);
        imageViewEnt = findViewById(R.id.imageview_ent);
        storiesProgressView = findViewById(R.id.stories);
        reverse = findViewById(R.id.reverse);
        skip = findViewById(R.id.skip);
        see = findViewById(R.id.see);

        detail_layout = findViewById(R.id.detail_layout);
        loading_indicator = findViewById(R.id.loading_indicator);
        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        Button btn_refresh = findViewById(R.id.btn_refresh);

        btn_refresh.setOnClickListener(v -> getDetails());

        MySharedManager sharedManager = new MySharedManager(ActivityDetailsEntreprise.this);
        id = sharedManager.getUser();

        Intent extras = getIntent();
        if (extras.getStringExtra("id_ent") != null) {
            id_ent = extras.getStringExtra("id_ent");
        } else {
            finish();
        }

        imagesList = new ArrayList<>();
        /*imagesAdapter = new ImagesAdapter(this, imagesList);
        pager.setAdapter(imagesAdapter);
        imagesAdapter.setOnImageListener(this);*/

        ib_send.setOnClickListener(v -> ajouterCommentaire(id_ent));

        getDetails();
    }

    private void getDetails() {
        detail_layout.setVisibility(View.GONE);
        loading_indicator.setVisibility(View.VISIBLE);
        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        Log.d(TAG, id_ent);

        API api = RetrofitClient.getClient().create(API.class);
        Call<Entreprise.Entreprises> call = api.getEnterprise(id_ent);
        call.enqueue(new Callback<Entreprise.Entreprises>() {
            @Override
            public void onResponse(@NonNull Call<Entreprise.Entreprises> call, @NonNull Response<Entreprise.Entreprises> response) {
                detail_layout.setVisibility(View.VISIBLE);
                loading_indicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Entreprise.Entreprises entreprises = response.body();
                    if (entreprises != null) {
                        populateData(entreprises);
                    }
                } else {
                    Toast.makeText(ActivityDetailsEntreprise.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<Entreprise.Entreprises> call, @NonNull Throwable t) {
                loading_indicator.setVisibility(View.GONE);
                layout_ent_offline.setVisibility(View.VISIBLE);
                Log.d(TAG, t.toString());
            }
        });
    }

    private void populateData(Entreprise.Entreprises entreprises) {
        if (entreprises != null) {
            String desc = String.valueOf(HtmlCompat.fromHtml(entreprises.getPresentation(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            tv_presentation.setText(HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_LEGACY));
            tv_nom_entreprise.setText(entreprises.getNom());
            Glide.with(ActivityDetailsEntreprise.this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .centerCrop()
                    )
                    .asBitmap()
                    .load(entreprises.getLogo())
                    .thumbnail(0.1f)
                    .into(ic_logo);

            String email = entreprises.getEmail();
            String tel = entreprises.getTel();
            String site = entreprises.getSite();

            if (email.isEmpty()) {
                txt_email.setVisibility(View.GONE);
            } else {
                txt_email.setText(email);
                txt_email.setOnClickListener(v -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", email, null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Entreprise à la une");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre message");
                    startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."));
                });
            }
            if (tel.isEmpty()) {
                txt_tel.setVisibility(View.GONE);
            } else {
                txt_tel.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null));
                    startActivity(intent);
                });
                txt_tel.setText(tel);
            }
            if (entreprises.getLocation().isEmpty()) {
                txt_location.setVisibility(View.GONE);
            }

            txt_adresse.setText(entreprises.getAdresse());
            txt_site.setText(site);
            txt_site.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(site));
                startActivity(i);
            });
            txt_domaine.setText(entreprises.getDomaine());
            txt_location.setText(entreprises.getLocation());

            List<Entreprise.Entreprises.Pictures> list = entreprises.pictures;
            for (Entreprise.Entreprises.Pictures pictures : list) {
                imagesList.add(pictures.getNom());
            }
            List<Entreprise.Entreprises.Comment> commentList = entreprises.commentaires;
            mCommentaires.addAll(commentList);
            mCommentaireAdapter.notifyDataSetChanged();
            if (imagesList.size() > 0) {
                showStories();
            }
        }
    }

    private void showStories() {
        imageViewEnt.setVisibility(View.GONE);
        storiesProgressView.setStoriesCount(imagesList.size());
        storiesProgressView.setStoryDuration(4000L);
        storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
            @Override
            public void onNext() {
                Glide.with(ActivityDetailsEntreprise.this)
                        .setDefaultRequestOptions(
                                new RequestOptions()
                                        .placeholder(R.color.colorPrimary)
                                        .error(R.color.colorPrimaryDark)
                                        .centerCrop()
                        )
                        .asBitmap()
                        .load(imagesList.get(++counter))
                        .thumbnail(0.1f)
                        .into(imageViewEnt);
            }

            @Override
            public void onPrev() {
                if ((counter - 1) < 0) return;
                Glide.with(ActivityDetailsEntreprise.this)
                        .setDefaultRequestOptions(
                                new RequestOptions()
                                        .placeholder(R.color.colorPrimary)
                                        .error(R.color.colorPrimaryDark)
                                        .centerCrop()
                        )
                        .asBitmap()
                        .load(imagesList.get(--counter))
                        .thumbnail(0.1f)
                        .into(imageViewEnt);
            }

            @Override
            public void onComplete() {
                counter = 0;
                storiesProgressView.startStories();
            }
        });
        storiesProgressView.startStories();
        see.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityFullScreen.class);
            intent.putStringArrayListExtra("images", imagesList);
            intent.putExtra("position", counter);
            startActivity(intent);
        });
        Glide.with(this)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                )
                .asBitmap()
                .load(imagesList.get(counter))
                .thumbnail(0.1f)
                .into(imageViewEnt);

        reverse.setOnClickListener(v -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);

        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);


    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    public void onItemClicked(final int position) {
        final Entreprise.Entreprises.Comment commentaire = mCommentaires.get(position);
        final CharSequence[] items;

        if (commentaire.getId_personne().equals(id)) {
            items = new CharSequence[]{"Supprimer"};
        } else {
            items = new CharSequence[]{"Voir profil", "Signaler"};
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetailsEntreprise.this);
        builder.setTitle("Action ");
        builder.setItems(items, (dialog, i) -> {
            if (items[i].equals("Supprimer")) {
                supprimerCommentaire(commentaire, position);
            } else if (items[i].equals("Voir profil")) {
                Intent intent = new Intent(this, ActivityUserProfile.class);
                intent.putExtra("id", commentaire.getId_personne());
                startActivity(intent);
            } else if (items[i].equals("Signaler")) {
                signalerCommentaire(commentaire.getId_comment());
            } else {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void signalerCommentaire(String id) {
        FragmentSignaler fragmentSignaler = FragmentSignaler.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("element", "commentaire");
        bundle.putString("id_element", id);
        fragmentSignaler.setArguments(bundle);
        fragmentSignaler.show(getSupportFragmentManager(), "");
    }

    private void supprimerCommentaire(final Entreprise.Entreprises.Comment commentaire, final int position) {

        if (id.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            ProgressDialog progressDoalog = new ProgressDialog(this);
            progressDoalog.setTitle(R.string.chargement_en_cours);
            progressDoalog.show();

            API api = RetrofitClient.getClient().create(API.class);
            Call<MyResponse> call = api.deleteComment(commentaire.getId_comment());
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    progressDoalog.dismiss();
                    if (response.isSuccessful()) {
                        mCommentaires.remove(commentaire);
                        mCommentaireAdapter.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    progressDoalog.dismiss();
                    Toast.makeText(ActivityDetailsEntreprise.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void ajouterCommentaire(final String id_ent) {

        if (id.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            if (tv_comment.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.ajouter_un_commentaire, Toast.LENGTH_SHORT).show();
            } else {
                API api = RetrofitClient.getClient().create(API.class);
                Call<Entreprise.Entreprises.Comment> call = api.createComment(
                        tv_comment.getText().toString(),
                        id,
                        id_ent
                );
                call.enqueue(new Callback<Entreprise.Entreprises.Comment>() {
                    @Override
                    public void onResponse(@NonNull Call<Entreprise.Entreprises.Comment> call, @NonNull Response<Entreprise.Entreprises.Comment> response) {
                        Log.d(TAG, response.toString());
                        if (response.isSuccessful()) {
                            mCommentaires.add(response.body());
                            mCommentaireAdapter.notifyDataSetChanged();
                            if (mCommentaireAdapter.getItemCount() > 1) {
                                Objects.requireNonNull(mRecycleView.getLayoutManager()).smoothScrollToPosition(mRecycleView, null, mCommentaireAdapter.getItemCount() - 1);
                            }
                            tv_comment.setText("");
                        } else {
                            Toast.makeText(ActivityDetailsEntreprise.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Entreprise.Entreprises.Comment> call, @NonNull Throwable t) {
                        Toast.makeText(ActivityDetailsEntreprise.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                    }
                });

                /*StringRequest request = new StringRequest(Request.Method.POST, url,
                        response -> {
                    Log.v(TAG, response);
                    Log.v(TAG, id_ent+" Id enterdprise");
                            if (response.contains("id_comment")) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    Commentaire commentaire = new Commentaire();
                                    JSONObject data = jsonObject.getJSONObject("comment");
                                    setComments(commentaire, data);
                                    mCommentaireAdapter.notifyDataSetChanged();
                                    if (mCommentaireAdapter.getItemCount() > 1) {
                                        Objects.requireNonNull(mRecycleView.getLayoutManager()).smoothScrollToPosition(mRecycleView, null, mCommentaireAdapter.getItemCount() - 1);
                                    }
                                    tv_comment.setText("");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, error -> {
                    Log.v(TAG, Objects.requireNonNull(error.toString()));
                    Toast.makeText(getApplicationContext(), getString(R.string.pas_d_acces_internet), Toast.LENGTH_LONG).show();
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("commentaire", tv_comment.getText().toString());
                        params.put("id_pers_fk", id);
                        params.put("id_articl_fk", id_ent);

                        return params;
                    }
                };
                mRequestQueue.add(request);*/
            }
        }

    }

    /*@Override
    public void onImageClicked(int position) {
        Intent intent = new Intent(this, ActivityFullScreen.class);
        intent.putStringArrayListExtra("images", imagesList);
        intent.putExtra("position", position);
        startActivity(intent);
    }*/
}
