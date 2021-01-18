package bf.fasobizness.bafatech.activities

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.utils.Constants.HOST.api_server_url
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class ActivitySuggestion : AppCompatActivity() {
    private val tag = "ActivitySuggestion"

    // private LinearLayout layout_success;
    private lateinit var btnEnvoyer: Button
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestion)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.suggestions)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_delete)
        toolbar.setNavigationOnClickListener { finish() }
        btnEnvoyer = findViewById(R.id.btn_envoyer)
        // layout_success = findViewById(R.id.layout_succes);
        progressBar = findViewById(R.id.progress_bar)
        btnEnvoyer.setOnClickListener { sendSuggestion() }
    }

    private fun sendSuggestion() {
        val url = api_server_url() + "v1/suggestions"
        val txtNom = findViewById<TextInputLayout>(R.id.ed_nom)
        val txtPhone = findViewById<TextInputLayout>(R.id.ed_phone)
        val txtTexte = findViewById<TextInputLayout>(R.id.ed_texte)
        if (txtNom.editText?.text.toString().isNotEmpty()
                && txtTexte.editText?.text.toString().isNotEmpty()
                && txtPhone.editText?.text.toString().isNotEmpty()) {
            btnEnvoyer.isEnabled = false
            btnEnvoyer.text = getString(R.string.envoi_en_cours)
            progressBar.visibility = View.VISIBLE
            val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener {
                btnEnvoyer.text = getString(R.string.envoyer)
                progressBar.visibility = View.GONE
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.votre_suggestion_a_bien_t_envoy_e))
                builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    txtNom.editText!!.setText("")
                    txtPhone.editText!!.setText("")
                    txtTexte.editText!!.setText("")
                }
                builder.setNegativeButton(R.string.annuler) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()

                // layout_success.setVisibility(View.VISIBLE);
                btnEnvoyer.isEnabled = true
            }, Response.ErrorListener { error: VolleyError ->
                btnEnvoyer.text = getString(R.string.envoyer)
                btnEnvoyer.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
                error.printStackTrace()
                Log.d(tag, error.toString())
            }) {
                override fun getBodyContentType(): String {
                    return "application/x-www-form-urlencoded; charset=UTF-8"
                }

                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["nom"] = txtNom.editText!!.text.toString()
                    params["tel"] = txtPhone.editText!!.text.toString()
                    params["texte"] = txtTexte.editText!!.text.toString()
                    return params
                }
            }
            Volley.newRequestQueue(this).add(request)
        } else {
            Toast.makeText(this, R.string.verifier_les_champs, Toast.LENGTH_LONG).show()
        }
    }
}