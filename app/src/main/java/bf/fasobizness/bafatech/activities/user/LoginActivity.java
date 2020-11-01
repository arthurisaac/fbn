package bf.fasobizness.bafatech.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.auth0.android.jwt.JWT;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import bf.fasobizness.bafatech.MainActivity;
import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputLayout password;
    private TextInputLayout email;
    private TextView failed_login_error, txt_login;
    private ProgressBar progressBar;
    private LinearLayout btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_login = findViewById(R.id.layout_login);
        progressBar = findViewById(R.id.progress_bar_login);

        Button forgot_password = findViewById(R.id.forgot_password);
        TextView btn_register = findViewById(R.id.btn_register);
        txt_login = findViewById(R.id.txt_login);

        initBtn();
        failed_login_error = findViewById(R.id.failed_login_error);

        forgot_password.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ActivityForgetPasswd.class)));

        btn_register.setOnClickListener(v -> action());
        btn_login.setClickable(true);
        btn_login.setOnClickListener(view -> {
            if (checkEmail() | checkPassword()) {
                Toast.makeText(LoginActivity.this, R.string.verifier_les_champs, Toast.LENGTH_SHORT).show();
            } else {
                failed_login_error.setVisibility(View.GONE);
                txt_login.setText(R.string.chargement_en_cours);
                progressBar.setVisibility(View.VISIBLE);
                btn_login.setClickable(false);

                logIn();
            }
        });
    }

    private void logIn() {

        final String txt_email = Objects.requireNonNull(email.getEditText()).getText().toString();
        final String txt_password = Objects.requireNonNull(password.getEditText()).getText().toString();

        API api = RetrofitClient.createService(API.class, txt_email, txt_password);
        Call<User> call = api.login();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                Log.d(TAG, response.toString());
                initBtn();

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        User user = response.body();

                        if (user != null) {

                            JWT jwt = new JWT(user.getAuthorization());

                            MySharedManager shared = new MySharedManager(LoginActivity.this);
                            shared.setUsername(jwt.getClaim("username").asString());
                            shared.setUser(jwt.getClaim("sub").asString());
                            shared.setPhoto(jwt.getClaim("photo").asString());
                            shared.setEmail(jwt.getClaim("email").asString());
                            shared.setToken(user.getAuthorization());


                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    }
                } else {
                    if (response.code() == 400) {
                        String message = getString(R.string.nom_d_utilisateur_ou_mot_de_passe_incorrect);
                        Log.v(TAG, message);
                        failed_login_error.setText(message);
                        failed_login_error.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                initBtn();
                Toast.makeText(LoginActivity.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initBtn() {
        txt_login.setText(R.string.se_connecter);
        progressBar.setVisibility(View.GONE);
        btn_login.setClickable(true);
    }

    private boolean checkEmail() {
        String txt_email = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(txt_email).matches();

        if (txt_email.isEmpty()) {
            email.setError(getString(R.string.adresse_email_requise));
            return true;
        } else if (!isValid) {
            email.setError(getString(R.string.adresse_email_invalide));
            return true;
        } else {
            email.setError(null);
            return false;
        }
    }

    private boolean checkPassword() {
        String txt_mdp = Objects.requireNonNull(password.getEditText()).getText().toString().trim();

        if (txt_mdp.isEmpty()) {
            password.setError(getString(R.string.mot_de_passe_requis));
            return true;
        } else if (txt_mdp.length() < 4) {
            password.setError(getString(R.string.mot_de_passe_invalide));
            return true;
        } else {
            password.setError(null);
            return false;
        }
    }

    private void action() {
        final CharSequence[] items = {getString(R.string.entreprise), getString(R.string.particulier)};
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Type de compte");
        builder.setItems(items, (dialog, i) -> {
            if (items[i].equals(getString(R.string.entreprise))) {
                Intent intent = new Intent(this, ActivitySignUp.class);
                intent.putExtra("type", "entreprise");
                startActivity(intent);
            } else if (items[i].equals(getString(R.string.particulier))) {
                Intent intent = new Intent(this, ActivitySignUp.class);
                intent.putExtra("type", "particulier");
                startActivity(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
