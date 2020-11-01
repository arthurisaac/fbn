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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
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
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.Constants;
import bf.fasobizness.bafatech.utils.MySharedManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySignUp extends AppCompatActivity {
    private static final String TAG = "ActivitySignUp";
    private final int GALLERY = 1;
    private final int CAMERA = 2;
    private Bitmap bitmap;
    private CircleImageView photo_profil;
    private TextInputLayout username, email, telephone, nom, prenom, sect_activite, mdp, confirm;
    private MySharedManager sharedManager;
    private RequestQueue requestQueue;
    private LinearLayout linear_sign_up_base;
    private String type;
    private Button btn_sign_up;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.enregistrement));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        RelativeLayout sect = findViewById(R.id.sect);
        RelativeLayout pre = findViewById(R.id.pre);
        RelativeLayout us = findViewById(R.id.us);

        photo_profil = findViewById(R.id.photo_de_profil);
        photo_profil.setOnClickListener(view -> requestMultiplePermissions());

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        telephone = findViewById(R.id.telephone);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        sect_activite = findViewById(R.id.sect_activite);
        mdp = findViewById(R.id.password);
        confirm = findViewById(R.id.passwordConfirm);

        btn_sign_up = findViewById(R.id.btn_sign_up);
        sharedManager = new MySharedManager(this);

        requestQueue = Volley.newRequestQueue(this);
        linear_sign_up_base = findViewById(R.id.linear_sign_up_base);

        Intent extras = getIntent();
        type = extras.getStringExtra("type");

        if (type != null) {
            if (type.equals("entreprise")) {
                pre.setVisibility(View.GONE);
                us.setVisibility(View.GONE);
            } else {
                sect.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
        }

        btn_sign_up.setOnClickListener(v -> {
            if (type != null) {
                if (type.equals("entreprise")) {
                    if (checkEmail() && checkSect() && checkNom() && checkTel() && checkPass()) {
                        signup();
                    }
                } else {
                    if (checkUsername() && checkNom() && checkEmail() && checkPrenom() && checkTel() && checkPass()) {
                        signup();
                    }
                }

            }
        });

        TextView cgu = findViewById(R.id.txt_cgu);
        cgu.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://fasobizness.com/cgu.pdf"));
            startActivity(intent);
        });

        api = RetrofitClient.getClient().create(API.class);
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
        final CharSequence[] items = {"Camera", "Galerie"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choisir_la_source);
        builder.setItems(items, (dialog, i) -> {
            if (items[i].equals("Camera")) {
                pickFromCamera();
            } else if (items[i].equals("Galerie")) {
                pickFromGallerie();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    Uri contentURI = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        photo_profil.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == CAMERA) {
                try {
                    bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    photo_profil.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void signup() {

        String tx_email = Objects.requireNonNull(email.getEditText().getText().toString());
        String tx_nom_pers = Objects.requireNonNull(username.getEditText().getText().toString());
        String tx_tel = Objects.requireNonNull(telephone.getEditText().getText().toString());
        String tx_nom = Objects.requireNonNull(nom.getEditText().getText().toString());
        String tx_prenom = Objects.requireNonNull(prenom.getEditText().getText().toString());
        String tx_sect_activity = Objects.requireNonNull(sect_activite.getEditText().getText().toString());
        String tx_mdp = Objects.requireNonNull(mdp.getEditText().getText().toString());

        btn_sign_up.setEnabled(false);
        btn_sign_up.setText(R.string.enregistrement_en_cours);

        Call<User> call = api.createUser(
                tx_email,
                tx_tel,
                tx_nom,
                tx_prenom,
                tx_nom_pers,
                tx_sect_activity,
                tx_mdp,
                type
        );
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                btn_sign_up.setEnabled(true);
                btn_sign_up.setText(R.string.enregistrer);

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
                                Toast.makeText(ActivitySignUp.this, R.string.votre_compte_a_ete_supprime, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            String auth = user.getAuthorization();
                            JWT jwt = new JWT(auth);
                            sharedManager.setUsername(jwt.getClaim("username").asString());
                            sharedManager.setUser(jwt.getClaim("sub").asString());
                            sharedManager.setPhoto(jwt.getClaim("photo").asString());
                            sharedManager.setEmail(jwt.getClaim("email").asString());
                            sharedManager.setToken(auth);

                            if (bitmap != null) {
                                addPhoto(sharedManager.getUser());
                            } else {
                                Intent intent = new Intent(ActivitySignUp.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                } else {
                    Snackbar.make(linear_sign_up_base, response.message(), Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, response.errorBody().contentType().toString());
                }

            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(ActivitySignUp.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                btn_sign_up.setEnabled(true);
                btn_sign_up.setText(getString(R.string.enregistrer));
            }
        });

    }

    private void addPhoto(String user) {
        btn_sign_up.setEnabled(false);
        btn_sign_up.setText(R.string.enregistrement_en_cours);

        String url = Constants.HOST_URL + "v1/users/avatar/";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            btn_sign_up.setEnabled(true);
            btn_sign_up.setText(getString(R.string.enregistrer));
            if (response.contains("photo")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String photo = jsonObject.getString("photo");
                    sharedManager.setPhoto(photo);

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, error -> {
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            btn_sign_up.setEnabled(true);
            btn_sign_up.setText(getString(R.string.enregistrer));
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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

        requestQueue.add(request);
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
            prenom.setError(getString(R.string.prenom_requis));
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

    private boolean checkTel() {
        String txt_sect_act = Objects.requireNonNull(telephone.getEditText()).getText().toString().trim();

        if (txt_sect_act.isEmpty()) {
            telephone.setError(getString(R.string.numero_de_telephone_requis));
            return false;
        } else {
            telephone.setError(null);
            return true;
        }
    }

    private boolean checkPass() {
        String passwd = Objects.requireNonNull(mdp.getEditText()).getText().toString().trim();
        String conf = Objects.requireNonNull(confirm.getEditText()).getText().toString().trim();

        if (passwd.isEmpty()) {
            mdp.setError(getString(R.string.mot_de_passe_requis));
            return false;
        } else if (passwd.length() < 6) {
            mdp.setError(getString(R.string.minimum_6_caracteres));
            return false;
        } else if (conf.isEmpty()) {
            confirm.setError(getString(R.string.mot_de_passe_requis));
            return false;
        } else if (!conf.equals(passwd)) {
            confirm.setError(getString(R.string.les_mots_de_passe_ne_se_correspondent_pas));
            return false;
        } else {
            mdp.setError(null);
            confirm.setError(null);
            return true;
        }
    }
}

