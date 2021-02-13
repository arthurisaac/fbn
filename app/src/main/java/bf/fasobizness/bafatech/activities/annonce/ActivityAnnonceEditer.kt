package bf.fasobizness.bafatech.activities.annonce

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityAnnonceEditer : AppCompatActivity() {
    private lateinit var til_titre_annonce: TextInputLayout
    private lateinit var til_desc_annonce: TextInputLayout
    private lateinit var til_tel_annonce: TextInputLayout
    private lateinit var til_tel1_annonce: TextInputLayout
    private lateinit var til_tel2_annonce: TextInputLayout
    private lateinit var ed_prix: TextInputLayout
    private lateinit var spVille: Spinner
    private lateinit var spCategorie: Spinner
    private lateinit var btn_publish_offer: Button
    private lateinit var user: String
    private lateinit var ll_base: CoordinatorLayout
    private lateinit var tvVilleError: TextView
    private lateinit var tvCategorieError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annonce_editer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.modification)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val mySharedManager = MySharedManager(this)
        user = mySharedManager.user
        til_titre_annonce = findViewById(R.id.til_titre_annonce)
        til_desc_annonce = findViewById(R.id.til_description_annonce)
        til_tel_annonce = findViewById(R.id.til_tel_annonce)
        til_tel1_annonce = findViewById(R.id.til_tel1_annonce)
        til_tel2_annonce = findViewById(R.id.til_tel2_annonce)
        tvVilleError = findViewById(R.id.tv_error_ville)
        tvCategorieError = findViewById(R.id.tv_error_catégorie)
        ed_prix = findViewById(R.id.ed_prix_annonce)
        btn_publish_offer = findViewById(R.id.btn_publish)
        ll_base = findViewById(R.id.ll_base)
        spVille = findViewById(R.id.sp_ville_annonce)
        spCategorie = findViewById(R.id.sp_categorie_annonce)
        spVille.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.villes_2))
        spCategorie.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.categories_2))
        val extras = intent
        val annonce = extras.getSerializableExtra("annonce") as Annonce?
        annonce?.let { populateData(it) }
    }

    private fun updatePost(id_ann: String) {
        val progressDoalog = ProgressDialog(this)
        progressDoalog.setTitle(R.string.chargement_en_cours)
        progressDoalog.show()
        val api = RetrofitClient.getClient().create(API::class.java)
        val titre = til_titre_annonce.editText?.text.toString()
        val texte = til_desc_annonce.editText?.text.toString()
        val tel1 = til_tel1_annonce.editText?.text.toString()
        val tel2 = til_tel2_annonce.editText?.text.toString()
        val tel = til_tel_annonce.editText?.text.toString()
        val prix = ed_prix.editText?.text.toString()
        val ville = spVille.selectedItem.toString()
        val categorie = spCategorie.selectedItem.toString()
        val call = api.updateAnnounce(
                id_ann,
                user,
                texte,
                prix,
                ville,
                tel,
                tel1,
                tel2,
                titre,
                categorie
        )
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                btn_publish_offer!!.isEnabled = true
                btn_publish_offer!!.setText(R.string.publier)
                progressDoalog.dismiss()
                Log.d("Activity", response.toString())
                if (response.isSuccessful) {
                    val builder = AlertDialog.Builder(this@ActivityAnnonceEditer)
                    builder.setMessage(R.string.l_annonce_a_ete_modifiee_avec_succes)
                    builder.setPositiveButton(R.string.ok) { dialog: DialogInterface?, id: Int -> finish() }
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    Toast.makeText(this@ActivityAnnonceEditer, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Snackbar.make(ll_base, R.string.pas_d_acces_internet, Snackbar.LENGTH_SHORT).show()
                btn_publish_offer.isEnabled = true
                btn_publish_offer.setText(R.string.publier)
                progressDoalog.dismiss()
            }
        })

        /*String url = Constants.HOST_URL + "annonce/update";
        StringRequest request1 = new StringRequest(Request.Method.POST, url, response -> {
            btn_publish_offer.setEnabled(true);
            btn_publish_offer.setText(R.string.publier);
            Log.d("activity", response );
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean status = jsonObject.getBoolean("status");
                if (status) {
                    Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(ll_base, getString(R.string.l_annonce_a_ete_modifiee_avec_succes), Snackbar.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();                }
        }, error -> {
            btn_publish_offer.setEnabled(true);
            btn_publish_offer.setText(R.string.publier);
            Toast.makeText(this, "Pas d'acces internet", Toast.LENGTH_SHORT).show();
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_per_fk", user);
                params.put("texte", texte);
                params.put("prix", prix);
                params.put("ville", ville);
                params.put("tel", tel);
                params.put("tel1", tel1);
                params.put("tel2", tel2);
                params.put("titre", titre);
                params.put("categorie", categorie);
                params.put("id_ann", id_ann);

                return params;
            }
        };
        request1.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2)
        );
        Volley.newRequestQueue(this).add(request1);*/
    }

    private fun populateData(annonce: Annonce) {
        til_titre_annonce.editText?.setText(annonce.titre)
        til_desc_annonce.editText?.setText(annonce.texte)
        til_tel1_annonce.editText?.setText(annonce.tel1)
        til_tel2_annonce.editText?.setText(annonce.tel2)
        til_tel_annonce.editText?.setText(annonce.tel)
        ed_prix.editText?.setText(annonce.prix)
        for (i in 0 until spVille.adapter.count) {
            if (spVille.adapter.getItem(i).toString().contains(annonce.location)) {
                spVille.setSelection(i)
            }
        }
        for (i in 0 until spCategorie.adapter.count) {
            if (spCategorie.adapter.getItem(i).toString().contains(annonce.categorie)) {
                spCategorie.setSelection(i)
            }
        }
        btn_publish_offer.setOnClickListener {
            btn_publish_offer.setText(R.string.publication_en_cours)
            btn_publish_offer.isEnabled = false
            if (!checkVilleInput() or !checkCatInput()) {
                return@setOnClickListener
            }
            updatePost(annonce.id_ann)
        }
    }

    private fun checkVilleInput(): Boolean {
        val villeInput = spVille.selectedItem.toString()
        return when {
            villeInput.isEmpty() -> {
                tvVilleError.visibility = View.VISIBLE
                false
            }
            (villeInput == "Choisir ville *") -> {
                tvVilleError.visibility = View.VISIBLE
                false
            }
            else -> {
                tvVilleError.visibility = View.GONE
                true
            }
        }
    }

    private fun checkCatInput(): Boolean {
        val catInput = spCategorie.selectedItem.toString()
        return when {
            catInput.isEmpty() -> {
                tvCategorieError.visibility = View.VISIBLE
                false
            }
            (catInput == "Choisir catégorie *") -> {
                tvCategorieError.visibility = View.VISIBLE
                false
            }
            else -> {
                tvCategorieError.visibility = View.GONE
                true
            }
        }
    }

}