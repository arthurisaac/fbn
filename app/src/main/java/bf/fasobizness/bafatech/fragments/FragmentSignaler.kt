package bf.fasobizness.bafatech.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.MyResponse
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FragmentSignaler : AppCompatDialogFragment() {
    private lateinit var raison: TextInputLayout
    private lateinit var element: String
    private lateinit var id_element: String
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        if (bundle != null) {
            element = bundle.getString("element").toString()
            id_element = bundle.getString("id_element").toString()
        }
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.fragment_signaler, null)
        val btn_signaler = view.findViewById<Button>(R.id.btn_signaler)
        raison = view.findViewById(R.id.til_raison)
        btn_signaler.setOnClickListener { signaler() }
        builder.setView(view)
        return builder.create()
    }

    private fun signaler() {
        /*val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(R.string.veuillez_patienter_pendant_le_chargement)
        progressDialog.setMessage("Chargement ...")
        progressDialog.show()*/
        val api: API = RetrofitClient.getClient().create(API::class.java)
        val txt_raison = raison.editText!!.text.toString()
        val call: Call<MyResponse> = api.createReport(
                 txt_raison + "",
                element + "",
                id_element + ""
        )
        call.enqueue(object : Callback<MyResponse> {
            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                val builder = AlertDialog.Builder(context)
                        .setMessage(getString(R.string.votre_requete_a_bien_ete_envoyee))
                        .setPositiveButton(R.string.ok) { _, _ -> dismiss() }
                        .create()
                builder.show()
            }

            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
            }
        })


        /*String url = Constants.HOST.api_server_url() + "v1/reports";
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
            // Toast.makeText(getContext(), R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
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
        Volley.newRequestQueue(requireContext()).add(request);*/
    }

    companion object {
        @JvmStatic
        fun newInstance(): FragmentSignaler {
            return FragmentSignaler()
        }
    }
}