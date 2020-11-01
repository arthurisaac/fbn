package bf.fasobizness.bafatech.activities.user;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bf.fasobizness.bafatech.MainActivity;
import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.fragments.FragmentPasswordUpdate;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.Constants;
import bf.fasobizness.bafatech.utils.MySharedManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityProfile extends AppCompatActivity {
    private static final String TAG = "ActivityProfile";
    private final int GALLERY = 1;
    private final int CAMERA = 2;
    private TextInputLayout username, email, telephone, nom, prenom, sect_activite;
    private Bitmap bitmap;
    private CircleImageView photo_profil;
    private MySharedManager sharedManager;
    private String user;
    private LinearLayout layout_loading, layout_profile, layout_busy, layout_offline;
    private ProgressBar progressBar, progress_bar_head;
    private Button btn_update_profil;
    private API api;
    private RelativeLayout sect, pre, us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedManager = new MySharedManager(this);
        user = sharedManager.getUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.profil));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        sect = findViewById(R.id.sect);
        pre = findViewById(R.id.pre);
        us = findViewById(R.id.us);

        layout_loading = findViewById(R.id.layout_loading);
        layout_profile = findViewById(R.id.layout_profile);
        layout_busy = findViewById(R.id.layout_busy_system);
        layout_offline = findViewById(R.id.layout_ent_offline);
        layout_offline.setVisibility(View.GONE);
        layout_busy.setVisibility(View.GONE);
        layout_profile.setVisibility(View.GONE);
        layout_loading.setVisibility(View.VISIBLE);

        final String type = sharedManager.getType();
        if (type.equals("entreprise")) {
            pre.setVisibility(View.GONE);
            us.setVisibility(View.GONE);
        } else {
            sect.setVisibility(View.GONE);
        }

        photo_profil = findViewById(R.id.photo_de_profil);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        telephone = findViewById(R.id.telephone);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        sect_activite = findViewById(R.id.sect_activite);
        Button update_mdp = findViewById(R.id.btn_update_mdp);

        progressBar = findViewById(R.id.progress_bar_photo);
        progress_bar_head = findViewById(R.id.progress_bar_head);

        photo_profil.setOnClickListener(view -> requestMultiplePermissions());

        update_mdp.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentPasswordUpdate dialogFragment = FragmentPasswordUpdate.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("type", "update");
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fragmentManager, "");
        });
        btn_update_profil = findViewById(R.id.btn_update_profil);
        btn_update_profil.setOnClickListener(v -> {
            if (type.equals("entreprise")) {
                if (checkEmail() | checkSect() | checkNom()) {
                    update_profil();
                }
            } else {
                if (checkUsername() && checkNom() && checkEmail() && checkPrenom()) {
                    update_profil();
                } else {
                    Toast.makeText(ActivityProfile.this, "erreur", Toast.LENGTH_SHORT).show();
                }
            }
        });

        api = RetrofitClient.getClient().create(API.class);
        getUserProfile();

        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(v -> getUserProfile());
    }

    private void update_profil() {
        progress_bar_head.setVisibility(View.VISIBLE);
        btn_update_profil.setText(R.string.chargement_en_cours);
        btn_update_profil.setEnabled(false);

        email.setError(null);
        nom.setError(null);
        prenom.setError(null);
        sect_activite.setError(null);
        telephone.setError(null);

        String txtemail = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        String txtusername = Objects.requireNonNull(username.getEditText()).getText().toString().trim();
        String txttel = Objects.requireNonNull(telephone.getEditText()).getText().toString().trim();
        String txtnom = Objects.requireNonNull(nom.getEditText()).getText().toString().trim();
        String txtprenom = Objects.requireNonNull(prenom.getEditText()).getText().toString().trim();
        String txtsect_activite = Objects.requireNonNull(sect_activite.getEditText()).getText().toString().trim();

        Call<User> call = api.updateUser(
                txtemail,
                txtusername,
                txttel,
                txtnom,
                txtprenom,
                txtsect_activite,
                user,
                "Bearer " + sharedManager.getToken()
        );

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                btn_update_profil.setText(R.string.enregistrer);
                btn_update_profil.setEnabled(true);
                progress_bar_head.setVisibility(View.GONE);
                if (response.isSuccessful()) {

                    User user = response.body();

                    if (user != null) {

                        if (user.getMessage() != null) {
                            if (user.getMessage().equals("email")) {
                                email.setError(getString(R.string.cet_email_existe_deja));
                            }
                            if (user.getMessage().equals("tel")) {
                                telephone.setError(getString(R.string.ce_numero_de_telephone_existe));
                            }
                            if (user.getMessage().equals("username")) {
                                username.setError(getString(R.string.ce_nom_d_utilisateur_existe_deja));
                            }
                            if (user.getMessage().equals("nom")) {
                                nom.setError(getString(R.string.ce_nom_existe_deja));
                            }
                            if (user.getMessage().equals("user")) {
                                Toast.makeText(ActivityProfile.this, R.string.votre_compte_a_ete_supprime, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String type = user.getType();

                            if (type.equals("entreprise")) {
                                pre.setVisibility(View.GONE);
                                us.setVisibility(View.GONE);
                            } else {
                                sect.setVisibility(View.GONE);
                            }

                            sharedManager.setEmail(user.getEmail());
                            sharedManager.setUsername(user.getUsername());
                            sharedManager.setPhoto(user.getPhoto());
                            Snackbar.make(layout_profile, getString(R.string.profil_modifie_avec_succes), Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivityProfile.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ActivityProfile.this, response.message(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                btn_update_profil.setText(R.string.enregistrer);
                btn_update_profil.setEnabled(true);
                progress_bar_head.setVisibility(View.GONE);
                Toast.makeText(ActivityProfile.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserProfile() {

        layout_loading.setVisibility(View.VISIBLE);
        layout_offline.setVisibility(View.GONE);
        layout_busy.setVisibility(View.GONE);
        layout_profile.setVisibility(View.GONE);

        Call<User> call = api.getUser(user, "Bearer " + sharedManager.getToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                layout_loading.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    layout_profile.setVisibility(View.VISIBLE);
                    User user = response.body();

                    if (user != null) {
                        Objects.requireNonNull(username.getEditText()).setText(user.getUsername());
                        Objects.requireNonNull(email.getEditText()).setText(user.getEmail());
                        Objects.requireNonNull(prenom.getEditText()).setText(user.getPrenom());
                        Objects.requireNonNull(telephone.getEditText()).setText(user.getTel());
                        Objects.requireNonNull(sect_activite.getEditText()).setText(user.getSect_activite());
                        Objects.requireNonNull(nom.getEditText()).setText(user.getNom());

                        sharedManager.setUsername(user.getUsername());
                        sharedManager.setEmail(user.getEmail());
                        sharedManager.setPhoto(user.getPhoto());
                        sharedManager.setType(user.getType());

                        try {
                            Glide.with(ActivityProfile.this)
                                    .setDefaultRequestOptions(
                                            new RequestOptions()
                                                    .placeholder(R.drawable.user)
                                                    .error(R.drawable.user)
                                                    .centerCrop()
                                                    .override(400, 400)
                                    )
                                    .asBitmap()
                                    .load(user.getPhoto())
                                    .thumbnail(0.1f)
                                    .into(photo_profil);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    layout_busy.setVisibility(View.VISIBLE);
                    layout_profile.setVisibility(View.GONE);
                    Toast.makeText(ActivityProfile.this, response.message(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

                layout_loading.setVisibility(View.GONE);
                layout_busy.setVisibility(View.GONE);
                layout_offline.setVisibility(View.VISIBLE);
                layout_profile.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.pas_d_acces_internet), Toast.LENGTH_LONG).show();
                Log.v(TAG, Objects.requireNonNull(t.getMessage()));

            }
        });
    }

    private void deconnect() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.vous_etes_sur_le_point_de_vous_deconnecter));
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            sharedManager.setUsername("");
            sharedManager.setEmail("");
            sharedManager.setPhoto("");
            sharedManager.setType("");
            sharedManager.setUser("");

            Intent intent = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            selectImage();
                            Log.v(TAG, "All permissions are granted by user!");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }

    private void selectImage() {
        final CharSequence[] items = {"Camera", "Galerie", "Supprimer"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfile.this);
        builder.setTitle(R.string.choisir_la_source);
        builder.setItems(items, (dialog, i) -> {
            if (items[i].equals("Camera")) {
                pickFromCamera();
            } else if (items[i].equals("Galerie")) {
                pickFromGallerie();
            } else if (items[i].equals("Supprimer")) {
                supprimer();
            }
        });
        builder.show();
    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void pickFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void pickFromGallerie() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY);
    }

    private void supprimer() {
        updatePhoto();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    Uri contentURI = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        updatePhoto();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == CAMERA) {
                try {
                    bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    updatePhoto();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void updatePhoto() {
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.HOST_URL + "v1/users/avatar";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            photo_profil.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
            if (response.contains("photo")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String photo = jsonObject.getString("photo");
                    sharedManager.setPhoto(photo);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, error -> {
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", user);
                if (bitmap != null) {
                    String file = getStringImage(bitmap);
                    params.put("file", file);
                } else {
                    params.put("file", "");
                }
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2));
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private boolean checkEmail() {
        String txt_email = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(txt_email).matches();

        if (txt_email.isEmpty()) {
            email.setError(getString(R.string.adresse_email_requise));
            return false;
        } else if (!isValid) {
            email.setError(getString(R.string.adresse_email_invalide));
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean checkUsername() {
        String txt_username = Objects.requireNonNull(username.getEditText()).getText().toString().trim();

        if (txt_username.isEmpty()) {
            username.setError(getString(R.string.nom_d_utlisateur_requis));
            return false;
        } else if (txt_username.length() < 4) {
            username.setError(getString(R.string.entrer_un_nom_d_utilisateur_plus_long));
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    private boolean checkPrenom() {
        String txt_prenom = Objects.requireNonNull(prenom.getEditText()).getText().toString().trim();

        if (txt_prenom.isEmpty()) {
            prenom.setError(getString(R.string.nom_d_utlisateur_requis));
            return false;
        } else {
            prenom.setError(null);
            return true;
        }
    }

    private boolean checkNom() {
        String txt_nom = Objects.requireNonNull(nom.getEditText()).getText().toString().trim();

        if (txt_nom.isEmpty()) {
            nom.setError(getString(R.string.nom_requis));
            return false;
        } else {
            nom.setError(null);
            return true;
        }
    }

    private boolean checkSect() {
        String txt_sect_act = Objects.requireNonNull(sect_activite.getEditText()).getText().toString().trim();

        if (txt_sect_act.isEmpty()) {
            sect_activite.setError(getString(R.string.secteut_d_activite_requis));
            return false;
        } else {
            sect_activite.setError(null);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            deconnect();
        }

        return super.onOptionsItemSelected(item);
    }

}
