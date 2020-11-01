package bf.fasobizness.bafatech;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.activities.ActivityDetailsPub;
import bf.fasobizness.bafatech.activities.ActivityInfos;
import bf.fasobizness.bafatech.activities.ActivityPromouvoirAnnonces;
import bf.fasobizness.bafatech.activities.ActivitySuggestion;
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnonceCategory;
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnoncesPublished;
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnounceFilter;
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonces;
import bf.fasobizness.bafatech.activities.annonce.ActivityNouvelleAnnonce;
import bf.fasobizness.bafatech.activities.annonce.ActivityOffreOr;
import bf.fasobizness.bafatech.activities.annonce.ActivitySearchAnnonce;
import bf.fasobizness.bafatech.activities.entreprise.ActivityEntreprisesUne;
import bf.fasobizness.bafatech.activities.recrutement.ActivityRecrutements;
import bf.fasobizness.bafatech.activities.user.ActivityFavorite;
import bf.fasobizness.bafatech.activities.user.ActivityProfile;
import bf.fasobizness.bafatech.activities.user.LoginActivity;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityDiscussions;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityMessage;
import bf.fasobizness.bafatech.adapters.AnnounceAdapter;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.interfaces.OnImageListener;
import bf.fasobizness.bafatech.models.Advertising;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnAnnonceListener, OnImageListener {

    private final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private TextView badge_discussions;
    private String user;
    private int page = 1;
    private LinearLayout layout_ent_offline, layout_busy_system, loading_indicator, layout_no_annonce;
    private String filtre = "id";
    private String arguments = " ";

    private AnnounceAdapter mAnnonceAdapter;
    private ArrayList<Announce.Annonce> mAnnonces;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShimmerFrameLayout shimmer_view_container;

    private ImageSlider imageSlider;
    private ArrayList<Advertising.Ads> images;
    private ArrayList<SlideModel> imageList;
    //private ArrayList<String> imageList;
    // private ImagesAdapter imagesAdapter;
    private API api;
    private MySharedManager sharedManager;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("discussion_id") != null) {
                String receiver_id = getIntent().getStringExtra("receiver_id");
                String discussion_id = getIntent().getStringExtra("discussion_id");
                Intent intent = new Intent(this, ActivityMessage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("discussion_id", discussion_id);
                intent.putExtra("receiver_id", receiver_id);
                startActivity(intent);
            }

            api = RetrofitClient.getClient().create(API.class);
            checkUser();
            /*
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key = " + key + "value = " + value);
            }
            */
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.annonces));
        setSupportActionBar(toolbar);
        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (user.isEmpty()) {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
            } else {
                startActivity(new Intent(MainActivity.this, ActivityNouvelleAnnonce.class));
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        NavigationView navigationView2 = findViewById(R.id.nav_view2);
        Button btn_valider = navigationView2.findViewById(R.id.btn_valider);
        EditText ed_min = navigationView2.findViewById(R.id.ed_min);
        EditText ed_max = navigationView2.findViewById(R.id.ed_max);

        Spinner sp_ville = findViewById(R.id.sp_ville_annonce);
        Spinner sp_categorie = findViewById(R.id.sp_categorie_annonce);

        sp_ville.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.villes)));
        sp_categorie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categories)));

        btn_valider.setOnClickListener(v -> {
            String txt_min = ed_min.getText().toString();
            String txt_max = ed_max.getText().toString();
            String cat = sp_categorie.getSelectedItem().toString();
            String ville = sp_ville.getSelectedItem().toString();

            // Advanced request
            if (ville.equals("Choisir ville")) {
                ville = "";
            }
            if (cat.equals("Choisir Categorie")) {
                cat = "";
            }
            filtre = "multiple";
            JSONObject jsonObject = new JSONObject();
            if (!ville.isEmpty()) {
                try {
                    jsonObject.put("location", ville);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!cat.isEmpty()) {
                try {
                    jsonObject.put("categorie", cat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!txt_min.isEmpty() && !txt_max.isEmpty()) {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("min", txt_min);
                    jo.put("max", txt_max);
                    jsonObject.put("prix", jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // arguments = "{\"location\": \"" + ville + "\", \"categorie\": \"" + cat + "\", \"prix\": {\"min\": " + txt_min + ", \"max\": " + txt_max + "}}";
            arguments = jsonObject.toString();

            // openDrawer();
            Intent intent = new Intent(this, ActivityAnnounceFilter.class);
            intent.putExtra("filter", filtre);
            intent.putExtra("params", arguments);
            startActivity(intent);

        });

        navigationView.setNavigationItemSelectedListener(this);
        View hv = navigationView.getHeaderView(0);

        Menu menu = navigationView.getMenu();
        MenuItem nav_post = menu.findItem(R.id.nav_my_post);
        // MenuItem nav_chat = menu.findItem(R.id.nav_chat);

        ImageView photoProfil = hv.findViewById(R.id.photoProfilNav);
        TextView txt_logged_username = hv.findViewById(R.id.txt_logged_username);
        TextView txt_logged_email = hv.findViewById(R.id.txt_logged_email);

        RelativeLayout btn_promouvoir = findViewById(R.id.promouvoir_btn);
        btn_promouvoir.setOnClickListener(v -> {
            if (!user.isEmpty()) {
                startActivity(new Intent(this, ActivityPromouvoirAnnonces.class));
            } else {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

                if (user != null) {
                    if (!user.isEmpty()) {
                        String username = sharedManager.getUsername(),
                                email = sharedManager.getEmail(),
                                photo = sharedManager.getPhoto();

                        txt_logged_email.setText(email);
                        txt_logged_email.setVisibility(View.VISIBLE);
                        txt_logged_username.setText(username);
                        nav_post.setVisible(true);

                        Glide.with(MainActivity.this)
                                .setDefaultRequestOptions(
                                        new RequestOptions()
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                                .centerCrop()
                                )
                                .asBitmap()
                                .load(photo)
                                .thumbnail(0.85f)
                                .into(photoProfil);

                    } else {
                        nav_post.setVisible(false);
                        txt_logged_email.setVisibility(View.GONE);
                        txt_logged_username.setText(getString(R.string.connectez_vous_ici));
                    }
                } else {
                    nav_post.setVisible(false);
                    txt_logged_email.setVisibility(View.GONE);
                    txt_logged_username.setText(getString(R.string.connectez_vous_ici));
                }

                photoProfil.setOnClickListener(v -> {
                    if (user.isEmpty()) {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, ActivityProfile.class));
                    }
                });


            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        TextView tab_chat = findViewById(R.id.tab_chat);
        TextView tab_boutiques = findViewById(R.id.tab_boutiques);
        TextView tab_offre_en_or = findViewById(R.id.tab_offre_en_or);
        TextView tab_entreprise = findViewById(R.id.tab_entrepise);
        TextView tab_categorie = findViewById(R.id.tab_categorie);
        badge_discussions = findViewById(R.id.badge_discussions);

        tab_boutiques.setOnClickListener(v -> startActivity(new Intent(this, ActivityBoutique.class)));
        tab_offre_en_or.setOnClickListener(v -> startActivity(new Intent(this, ActivityOffreOr.class)));
        tab_entreprise.setOnClickListener(v -> startActivity(new Intent(this, ActivityEntreprisesUne.class)));
        tab_categorie.setOnClickListener(v -> startActivity(new Intent(this, ActivityAnnonceCategory.class)));

        imageSlider = findViewById(R.id.flipper);
        images = new ArrayList<>();
        imageList = new ArrayList<>();

        // AdapterViewFlipper pager = findViewById(R.id.flipper);
        // pager = findViewById(R.id.flipper);
        // imagesAdapter = new ImagesAdapter(this, imageList);
        // advertisingAdapter = new AdvertisingAdapter(this, images);
        // pager.setAdapter(advertisingAdapter);

        // pager.setAdapter(imagesAdapter);
        // imagesAdapter.setOnImageListener(this);

        tab_chat.setOnClickListener(v -> {
            if (!user.isEmpty()) {
                badge_discussions.setVisibility(View.GONE);
                startActivity(new Intent(MainActivity.this, ActivityDiscussions.class));
            } else {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(getSupportFragmentManager(), "");
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        // Fetch annonces
        mAnnonces = new ArrayList<>();

        mRecyclerView = findViewById(R.id.annonces_card_view);
        LinearLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAnnonceAdapter = new AnnounceAdapter(MainActivity.this, mAnnonces);
        mRecyclerView.setAdapter(mAnnonceAdapter);
        mAnnonceAdapter.setOnItemListener(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    fab.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fab.setVisibility(View.GONE);
                }
            }
        });

        loading_indicator = findViewById(R.id.loading_indicator);
        loading_indicator.setVisibility(View.GONE);

        //Loading more data
        mAnnonceAdapter.setOnBottomReachedListener(() -> {
            loading_indicator.setVisibility(View.VISIBLE);
            jsonParse();
        });

        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        layout_ent_offline.setVisibility(View.GONE);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> refresh());

        layout_no_annonce = findViewById(R.id.layout_no_annonce);
        layout_no_annonce.setVisibility(View.GONE);

        layout_busy_system = findViewById(R.id.layout_busy_system);
        layout_busy_system.setVisibility(View.GONE);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        shimmer_view_container.setVisibility(View.VISIBLE);

        sharedManager = new MySharedManager(this);
        api = RetrofitClient.getClient().create(API.class);
        user = sharedManager.getUser();

        checkUser();
        jsonParse();
        ads();
        if (!user.isEmpty()) {
            checkNewMessage();
        }

    }

    private void checkUser() {
        String token = sharedManager.getToken();
        String id = sharedManager.getUser();
        user = sharedManager.getUser();

        getNewJWTToken(id, token);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String fcm = Objects.requireNonNull(task.getResult()).getToken();
                if (!user.isEmpty() && !token.isEmpty()) {
                    Call<MyResponse> call = api.updateFCM(fcm, "Bearer " + token);
                    call.enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                            // Log.d(TAG, response.toString());
                        }

                        @Override
                        public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                            Log.d(TAG, t.toString());
                        }
                    });
                }
            }
        });

    }

    private void getNewJWTToken(String id, String token) {
        Call<User> call = api.updateToken(id, "Bearer " + token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getStatus()) {
                            user = "";
                            sharedManager.setUsername("");
                            sharedManager.setEmail("");
                            sharedManager.setPhoto("");
                            sharedManager.setType("");
                            sharedManager.setUser("");
                            sharedManager.setUser("");
                            sharedManager.setToken("");
                        } else {
                            sharedManager.setToken(response.body().getAuthorization());
                        }
                    }
                } else {
                    Log.d(TAG, "Failed refresh token");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    private void openDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            drawer.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_annonce) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (id == R.id.nav_faq) {
            startActivity(new Intent(this, ActivityInfos.class));
            return true;
        } else if (id == R.id.nav_recrutement) {
            startActivity(new Intent(this, ActivityRecrutements.class));
            return true;
        } else if (id == R.id.nav_suggestion) {
            startActivity(new Intent(this, ActivitySuggestion.class));
            return true;
        } else if (id == R.id.nav_my_post) {
            startActivity(new Intent(this, ActivityAnnoncesPublished.class));
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.navFiltrer:
                openDrawer();
                break;
            case R.id.nav_favori:
                if (user != null) {
                    if (user.length() > 0) {
                        startActivity(new Intent(this, ActivityFavorite.class));
                        return true;
                    } else {
                        FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                        notConnected.show(getSupportFragmentManager(), "");
                        // startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                }
                break;
            case R.id.nav_ann_search:
                startActivity(new Intent(this, ActivitySearchAnnonce.class));
                break;
            case R.id.navActualiser:
                refresh();
                break;
            case R.id.navHautPage:
                Objects.requireNonNull(mRecyclerView.getLayoutManager()).smoothScrollToPosition(mRecyclerView, null, 1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        page = 1;
        mAnnonceAdapter.clearAll();
        mAnnonces.clear();
        mAnnonceAdapter.notifyDataSetChanged();

        shimmer_view_container.setVisibility(View.VISIBLE);

        jsonParse();
        // ads();
        if (!user.isEmpty()) {
            checkNewMessage();
        }
    }

    private void checkNewMessage() {
        Call<MyResponse> call = api.getUnread("Bearer " + sharedManager.getToken());
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                if (response.isSuccessful()) {
                    MyResponse myResponse = response.body();
                    if (myResponse != null) {
                        int count = myResponse.getCount();
                        if (count > 0) {
                            badge_discussions.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 6) {
            if (resultCode == RESULT_CANCELED) {
                refresh();
            }
        }
    }

    private void jsonParse() {
        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);

        Call<Announce> call = api.getAnnounces(String.valueOf(page));
        call.enqueue(new Callback<Announce>() {
            @Override
            public void onResponse(@NonNull Call<Announce> call, @NonNull Response<Announce> response) {
                shimmer_view_container.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                loading_indicator.setVisibility(View.GONE);
                Log.v(TAG, response.toString());

                Announce announce = response.body();
                List<Announce.Annonce> annonces = null;
                if (announce != null) {
                    annonces = announce.annonces;
                }
                if (annonces != null) {
                    mAnnonces.addAll(annonces);
                }
                if (mAnnonces.size() == 0) {
                    layout_no_annonce.setVisibility(View.VISIBLE);
                    layout_ent_offline.setVisibility(View.GONE);
                    loading_indicator.setVisibility(View.GONE);
                }
                page++;
                mAnnonceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<Announce> call, @NonNull Throwable t) {

                // Log.d(TAG, t.toString());
                if (mAnnonces.size() == 0) {
                    layout_ent_offline.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
                mSwipeRefreshLayout.setRefreshing(false);
                shimmer_view_container.setVisibility(View.GONE);
                loading_indicator.setVisibility(View.GONE);

            }
        });

    }

    private void ads() {

        Call<Advertising> call = api.getAds();
        call.enqueue(new Callback<Advertising>() {
            @Override
            public void onResponse(@NonNull Call<Advertising> call, @NonNull Response<Advertising> response) {
                Advertising advertising = response.body();
                List<Advertising.Ads> ads = null;
                if (advertising != null) {
                    ads = advertising.adsList;
                }
                if (advertising != null) {
                    for (Advertising.Ads ad : ads) {
                        images.add(ad);
                        imageList.add(new SlideModel(ad.getLien()));
                    }
                    imageSlider.setImageList(imageList, true);
                    imageSlider.setItemClickListener(i -> {
                        Advertising.Ads advertising1 = images.get(i);

                        Intent intent = new Intent(MainActivity.this, ActivityDetailsPub.class);
                        intent.putExtra("id", advertising1.getId());
                        intent.putExtra("lien", advertising1.getLien());
                        intent.putExtra("description", advertising1.getDescription());
                        startActivity(intent);
                    });
                }

            }

            @Override
            public void onFailure(@NonNull Call<Advertising> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    public void onAnnonceClicked(int position) {

        Announce.Annonce annonce = mAnnonces.get(position);
        // Intent intent = new Intent(this, ActivityDetailsAnnonce.class);
        Intent intent = new Intent(this, ActivityDetailsAnnonces.class);
        intent.putExtra("id_ann", annonce.getId_ann());
        intent.putExtra("affiche", annonce.getAffiche());
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.appuyer_sur_retour_pour_quitter, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public void onImageClicked(int position) {
        Advertising.Ads advertising1 = images.get(position);

        Intent intent = new Intent(MainActivity.this, ActivityDetailsPub.class);
        intent.putExtra("id", advertising1.getId());
        intent.putExtra("lien", advertising1.getLien());
        intent.putExtra("description", advertising1.getDescription());
        startActivity(intent);
    }
}
