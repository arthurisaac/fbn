package bf.fasobizness.bafatech.fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import bf.fasobizness.bafatech.MainActivity;
import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.utils.Constants;
import bf.fasobizness.bafatech.utils.MySharedManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPasswordUpdate extends AppCompatDialogFragment {
    private TextInputLayout ancien, mdp, confirm;
    private Button btn_update;
    private String id;
    private String type;

    public FragmentPasswordUpdate() {
        // Required empty public constructor
    }

    public static FragmentPasswordUpdate newInstance() {
        return new FragmentPasswordUpdate();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_password_update, null);
        ancien = view.findViewById(R.id.oldpassword);
        mdp = view.findViewById(R.id.password);
        confirm = view.findViewById(R.id.passwordConfirm);
        btn_update = view.findViewById(R.id.btn_update_mdp);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getString("type");
        }
        if (type != null && type.equals("recovery")) {
            if (bundle != null) {
                id = bundle.getString("id");
                ancien.setVisibility(View.GONE);
            }
        } else {
            MySharedManager sharedManager = new MySharedManager(getContext());
            id = sharedManager.getUser();
        }
        Log.d("activity", "id: " + id);

        btn_update.setOnClickListener(view1 -> {
            try {
                if (type.equals("recovery")) {
                    if (checkPasswd()) {
                        updateMdp();
                    }
                } else {
                    if (checkPasswd() && checkOld()) {
                        updateMdp();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.setView(view)
                .setTitle(getString(R.string.mot_de_passe));

        return builder.create();
    }

    private boolean checkPasswd() {
        String passwd = Objects.requireNonNull(mdp.getEditText()).getText().toString().trim();
        String conf = Objects.requireNonNull(confirm.getEditText()).getText().toString().trim();

        if (passwd.isEmpty()) {
            mdp.setError(getString(R.string.mot_de_passe_requis));
            return false;
        } else if (passwd.length() < 6) {
            mdp.setError(getString(R.string.mot_de_passe_superieur_6_caracteres));
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

    private boolean checkOld() {
        String oldPass = Objects.requireNonNull(ancien.getEditText()).getText().toString().trim();

        if (oldPass.isEmpty()) {
            ancien.setError(getString(R.string.mot_de_passe_requis));
            return false;
        } else {
            ancien.setError(null);
            return true;
        }
    }

    private void updateMdp() {
        btn_update.setEnabled(false);
        btn_update.setText(R.string.chargement_en_cours);

        String url = Constants.HOST_URL + "v1/users/password/update";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    btn_update.setText(R.string.mettre_a_jour);
                    btn_update.setEnabled(true);
                    try {
                        Log.d("ActivityPasswd", response);
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = jsonObject.getBoolean("status");
                        String message = jsonObject.getString("message");
                        if (status) {

                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
                            builder.setMessage(getString(R.string.mot_de_passe_modifie_avec_succes));
                            builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                                Objects.requireNonNull(getDialog()).dismiss();
                                if (type.equals("recovery")) {

                                    try {
                                        String auth = jsonObject.getString("authorization");
                                        JWT jwt = new JWT(auth);
                                        MySharedManager sharedManager = new MySharedManager(getContext());

                                        sharedManager.setUsername(jwt.getClaim("username").asString());
                                        sharedManager.setUser(jwt.getClaim("sub").asString());
                                        sharedManager.setPhoto(jwt.getClaim("photo").asString());
                                        sharedManager.setEmail(jwt.getClaim("email").asString());
                                        sharedManager.setToken(auth);

                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
                            androidx.appcompat.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            ancien.setError(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, error -> {
            Log.d("activity", error.toString());
            Toast.makeText(getContext(), R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            btn_update.setText(R.string.mettre_a_jour);
            btn_update.setEnabled(true);
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("ancien", Objects.requireNonNull(ancien.getEditText()).getText().toString());
                params.put("mdp", Objects.requireNonNull(mdp.getEditText()).getText().toString());
                params.put("id", id);
                params.put("type", type);
                return params;
            }
        };
        Volley.newRequestQueue(Objects.requireNonNull(getContext())).add(request);
    }

}
