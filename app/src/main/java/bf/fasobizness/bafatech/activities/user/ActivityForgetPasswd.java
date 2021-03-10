package bf.fasobizness.bafatech.activities.user;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.fragments.FragmentPasswordUpdate;
import bf.fasobizness.bafatech.utils.Constants;

public class ActivityForgetPasswd extends AppCompatActivity {
    private TextInputLayout code, email;
    private LinearLayout layout_code, layout_email;
    private TextView messageText;
    private ImageView imageView_email;
    private ProgressBar progressBar;
    private String pin;
    private int user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_passwd);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.recuperation_mdp));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        code = findViewById(R.id.code);
        email = findViewById(R.id.email);
        final Button btn_continuer = findViewById(R.id.btn_continuer);
        final Button btn_continuer_code = findViewById(R.id.btn_continuer_code);
        Button btn_resend_code = findViewById(R.id.btn_resend_code);
        layout_code = findViewById(R.id.layout_code);
        layout_email = findViewById(R.id.layout_email);
        messageText = findViewById(R.id.message);
        progressBar = findViewById(R.id.progress_bar);

        imageView_email = findViewById(R.id.imageview_email);

        messageText.setText(getString(R.string.saisissez_votre_adresse_mail_svp));

        btn_continuer.setOnClickListener(view -> {
            if (checkEmail()) {
                verifyEmail();
            }
        });

        btn_continuer_code.setOnClickListener(view -> {
            if (checkCode()) {
                verifyCode();
            }
        });

        btn_resend_code.setOnClickListener(view -> {
            if (checkEmail()) {
                resend();
            }
        });
    }

    private void verifyEmail() {
        progressBar.setVisibility(View.VISIBLE);
        final String e = Objects.requireNonNull(email.getEditText()).getText().toString();
        String url = Constants.HOST.api_server_url() + "v1/users/reset-password";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject object = new JSONObject(response);
                String message = object.getString("message");
                boolean status = object.getBoolean("status");
                pin = object.getString("code");
                user = object.getInt("user");

                if (!status) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // We just sent your authentication code via email to r***********@gmail.com Resend the code..
                    String str = getString(R.string.nous_venons_d_envoyer_un_code_dauthentification, e);
                    Glide.with(this)
                            .load(R.raw.email_anim)
                            .into(imageView_email);
                    messageText.setText(str);
                    layout_email.setVisibility(View.GONE);
                    layout_code.setVisibility(View.VISIBLE);
                }
            } catch (Exception e1) {
                progressBar.setVisibility(View.GONE);
                e1.printStackTrace();
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.cet_email_nexiste_pas));
                builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
                builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
                androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", e);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 1)
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void verifyCode() {
        String codetxt = Objects.requireNonNull(code.getEditText()).getText().toString();
        if (pin != null) {
            if (codetxt.equals(pin)) {
                newPassword(user);
            } else {
                code.setError(getString(R.string.code_incorrect));
            }
        }
    }

    private void resend() {
        verifyEmail();
    }

    private void newPassword(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentPasswordUpdate dialogFragment = FragmentPasswordUpdate.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("id", String.valueOf(id));
        bundle.putString("type", "recovery");
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fragmentManager, "");
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

    private boolean checkCode() {
        String txt_code = Objects.requireNonNull(code.getEditText()).getText().toString().trim();
        if (txt_code.isEmpty()) {
            code.setError(getString(R.string.le_code_de_verification_est_requise_pour_continer));
            return false;
        } else {
            code.setError(null);
            return true;
        }
    }
}
