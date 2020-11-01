package bf.fasobizness.bafatech.fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.utils.Constants;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSignaler extends AppCompatDialogFragment {
    private TextInputLayout raison;
    private String element = "", id_element = "";


    public FragmentSignaler() {
        // Required empty public constructor
    }

    public static FragmentSignaler newInstance() {
        return new FragmentSignaler();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            element = bundle.getString("element");
            id_element = bundle.getString("id_element");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_signaler, null);
        Button btn_signaler = view.findViewById(R.id.btn_signaler);
        raison = view.findViewById(R.id.til_raison);
        btn_signaler.setOnClickListener(v -> signaler());
        builder.setView(view);
        return builder.create();
    }

    private void signaler() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(R.string.veuillez_patienter_pendant_le_chargement);
        progressDialog.setMessage("Chargement ...");
        progressDialog.show();

        String txt_raison = Objects.requireNonNull(raison.getEditText()).getText().toString();
        String url = Constants.HOST_URL + "v1/reports";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {

            progressDialog.dismiss();
            try {
                JSONObject object = new JSONObject(response);
                boolean status = object.getBoolean("status");
                if (status) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
                    builder.setMessage(getString(R.string.votre_requete_a_bien_ete_envoyee));
                    builder.setPositiveButton(R.string.ok, (dialog, id) -> Objects.requireNonNull(getDialog()).dismiss());
                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("raison", txt_raison);
                params.put("element", element);
                params.put("id_element", id_element);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);

    }
}
