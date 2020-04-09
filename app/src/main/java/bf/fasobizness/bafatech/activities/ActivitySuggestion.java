package bf.fasobizness.bafatech.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.utils.Constants;

public class ActivitySuggestion extends AppCompatActivity {

    private final String TAG = "ActivitySuggestion";
    // private LinearLayout layout_success;
    private Button btn_envoyer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.suggestions));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_delete);
        toolbar.setNavigationOnClickListener(view -> finish());

        btn_envoyer = findViewById(R.id.btn_envoyer);
        // layout_success = findViewById(R.id.layout_succes);
        progressBar = findViewById(R.id.progress_bar);

        btn_envoyer.setOnClickListener(v -> send_suggestion());
    }

    private void send_suggestion() {
        String url = Constants.HOST_URL + "v1/suggestions";
        TextInputLayout txt_nom = findViewById(R.id.ed_nom);
        TextInputLayout txt_phone = findViewById(R.id.ed_phone);
        TextInputLayout txt_texte = findViewById(R.id.ed_texte);

        if (!Objects.requireNonNull(txt_nom.getEditText()).getText().toString().isEmpty()
                && !Objects.requireNonNull(txt_texte.getEditText()).getText().toString().isEmpty()
                && !Objects.requireNonNull(txt_phone.getEditText()).getText().toString().isEmpty()) {

            btn_envoyer.setEnabled(false);
            btn_envoyer.setText(getString(R.string.envoi_en_cours));
            progressBar.setVisibility(View.VISIBLE);

            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                btn_envoyer.setText(getString(R.string.envoyer));
                progressBar.setVisibility(View.GONE);
                AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.votre_suggestion_a_bien_t_envoy_e));
                builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                    txt_nom.getEditText().setText("");
                    txt_phone.getEditText().setText("");
                    txt_texte.getEditText().setText("");
                });
                builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
                androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();

                // layout_success.setVisibility(View.VISIBLE);
                btn_envoyer.setEnabled(true);

            }, error -> {
                btn_envoyer.setText(getString(R.string.envoyer));
                btn_envoyer.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                Log.d(TAG, error.toString());
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("nom", txt_nom.getEditText().getText().toString());
                    params.put("tel", txt_phone.getEditText().getText().toString());
                    params.put("texte", txt_texte.getEditText().getText().toString());
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(request);
        } else {
            Toast.makeText(this, R.string.verifier_les_champs, Toast.LENGTH_LONG).show();
        }

    }
}
