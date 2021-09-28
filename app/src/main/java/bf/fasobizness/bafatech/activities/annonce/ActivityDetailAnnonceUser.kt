package bf.fasobizness.bafatech.activities.annonce

//import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.adapters.ImagesAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnImageListener
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityDetailAnnonceUser : AppCompatActivity(), OnImageListener {
    private lateinit var imagesList: ArrayList<String>
    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var user: String
    private lateinit var txtTitreAnnonce: TextView
    private lateinit var txtText: TextView
    private lateinit var txtPrix: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtTel1: TextView
    private lateinit var txtTel2: TextView
    private lateinit var txtTel: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtCategorie: TextView
    private lateinit var txtUpdatedat: TextView
    private lateinit var txtDatePub: TextView
    private lateinit var ann: LinearLayout
    private lateinit var layoutNoAnnonce: LinearLayout
    private lateinit var loadingIndicatorAnn: LinearLayout
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var pager: ViewPager
    private lateinit var idAnn: String
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnEditPhoto: Button
    private lateinit var btnShare: Button
    private var api: API = RetrofitClient.getClient().create(API::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_annonce_utilisateur)
        api
        val mySharedManager = MySharedManager(this)
        user = mySharedManager.user
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        txtTitreAnnonce = findViewById(R.id.txt_titre_annonce)
        txtText = findViewById(R.id.txt_texte_ann)
        txtPrix = findViewById(R.id.txt_prix_annonce)
        txtEmail = findViewById(R.id.txt_email_util)
        txtTel1 = findViewById(R.id.txt_tel1_annonce)
        txtTel2 = findViewById(R.id.txt_tel2_annonce)
        txtTel = findViewById(R.id.txt_tel_annonce)
        txtLocation = findViewById(R.id.txt_location_annonce)
        txtCategorie = findViewById(R.id.txt_categorie_annonce)
        btnShare = findViewById(R.id.btn_share)
        txtUpdatedat = findViewById(R.id.txt_date_modification)
        txtDatePub = findViewById(R.id.txt_date_pub)
        pager = findViewById(R.id.flipper_affiche_annonce)
        loadingIndicatorAnn = findViewById(R.id.loading_indicator_ann)
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        layoutNoAnnonce = findViewById(R.id.layout_no_annonce)
        ann = findViewById(R.id.ann)
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        layoutEntOffline.visibility = View.GONE
        ann.visibility = View.GONE
        loadingIndicatorAnn.visibility = View.VISIBLE
        layoutNoAnnonce.visibility = View.GONE
        layoutBusySystem.visibility = View.GONE
        imagesList = ArrayList()
        imagesAdapter = ImagesAdapter(this, imagesList)
        pager.adapter = imagesAdapter
        imagesAdapter.setOnImageListener(this)
        val extras = intent
        if (extras.getStringExtra("id_ann") != null) {
            idAnn = extras.getStringExtra("id_ann")!!
            btnRefresh.setOnClickListener { getAnnounce(extras.getStringExtra("id_ann")) }
        }
        btnEdit = findViewById(R.id.btn_editer)
        btnDelete = findViewById(R.id.btn_delete)
        btnEditPhoto = findViewById(R.id.btn_editer_photo)
        btnShare = findViewById(R.id.btn_share)
    }

    private fun getAnnounce(id_ann: String?) {
        layoutEntOffline.visibility = View.GONE
        loadingIndicatorAnn.visibility = View.VISIBLE
        val call = api.getAnnounce(id_ann, user)
        call.enqueue(object : Callback<Annonce?> {
            override fun onResponse(call: Call<Annonce?>, response: Response<Annonce?>) {
                loadingIndicatorAnn.visibility = View.GONE
                if (response.isSuccessful) {
                    Log.d(TAG, response.toString())
                    loadingIndicatorAnn.visibility = View.GONE
                    ann.visibility = View.VISIBLE
                    val announce = response.body()
                    if (announce == null) {
                        layoutEntOffline.visibility = View.GONE
                        ann.visibility = View.GONE
                        layoutNoAnnonce.visibility = View.VISIBLE
                    } else {
                        populateData(announce)
                    }
                } else {
                    layoutBusySystem.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<Annonce?>, t: Throwable) {
                layoutEntOffline.visibility = View.VISIBLE
                ann.visibility = View.GONE
                layoutNoAnnonce.visibility = View.GONE
                loadingIndicatorAnn.visibility = View.GONE
                Log.d(TAG, t.toString())
                Toast.makeText(this@ActivityDetailAnnonceUser, getString(R.string.pas_d_acces_internet), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateData(annonce: Annonce) {
        txtTitreAnnonce.text = annonce.titre
        txtText.text = annonce.texte
        txtPrix.text = annonce.prix
        txtEmail.text = annonce.email
        txtTel.text = annonce.tel
        txtTel1.text = annonce.tel1
        txtTel2.text = annonce.tel2
        txtLocation.text = annonce.location
        txtCategorie.text = annonce.categorie
        val strShare = this.getString(R.string.partager_annonce, annonce.share)
        btnShare.text = strShare
        val updatedAt = annonce.updatedAt
        val strUpdatedat = this.getString(R.string.derni_re_mise_jour_le_1_s, updatedAt)
        if (updatedAt != null) {
            if (updatedAt.isNotEmpty()) {
                txtUpdatedat.text = strUpdatedat
                txtUpdatedat.visibility = View.VISIBLE
            } else {
                txtUpdatedat.visibility = View.GONE
            }
        }
        val datePub = getString(R.string.publiee_le, annonce.date_pub)
        txtDatePub.text = datePub
        if (annonce.categorie == "null" || annonce.categorie.isEmpty()) {
            txtCategorie.setText(R.string.aucune_categorie_renseignee)
        }
        if (annonce.tel.isEmpty()) {
            txtTel.visibility = View.GONE
        }
        if (annonce.tel1.isEmpty()) {
            txtTel1.visibility = View.GONE
        }
        if (annonce.tel2.isEmpty()) {
            txtTel2.visibility = View.GONE
        }
        if (annonce.email.isEmpty()) {
            txtEmail.visibility = View.GONE
        }
        if (annonce.prix.isEmpty()) {
            txtPrix.setText(R.string.prix_sur_demande)
        }
        if (annonce.location.isEmpty()) {
            txtLocation.visibility = View.GONE
        }
        if (annonce.location.isEmpty()) {
            txtLocation.visibility = View.GONE
        }
        try {
            // JSONArray jsonArray = new JSONArray(annonce.getIllustrations());
            val arrayList = annonce.illustrations
            for (data in arrayList) {
                imagesList.add(data.nom)
                // Log.d(TAG, data.toString());
            }
            if (arrayList.size == 0) {
                pager.visibility = View.GONE
            }
            imagesAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        btnEdit.setOnClickListener {
            try {
                val intent = Intent(this, ActivityAnnonceEditer::class.java)
                intent.putExtra("annonce", annonce)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        btnEditPhoto.setOnClickListener {
            try {
                val intent = Intent(this, ActivityAnnounceEditImages::class.java)
                intent.putExtra("annonce", annonce)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.confirmation_suppression_annonce)
            builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> deleteAnnonce(annonce.id_ann) }
            builder.setNegativeButton(R.string.annuler) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
        btnShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            // String shareBodyText = annonce.getTitre() + "\n" +annonce.getTexte() + "\n" + getString(R.string.telecharger_et_partager_l_application);
            val shareBodyText = """Salut, voici une annonce intéressante que je viens de découvrir sur Faso Biz Nèss : ${annonce.titre} ${annonce.texte}

Pour en savoir plus, clique ici : https://fasobizness.com/annonce/${annonce.id_ann}. 


Si tu n’as pas encore l’application tu peux la télécharger gratuitement sur Playstore : http://bit.ly/AndroidFBN"""
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, annonce.titre)
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)))
            shareAnn(idAnn)
        }
    }

    private fun deleteAnnonce(id_ann: String) {
        /*val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.suppresion_en_cours))*/
        loadingIndicatorAnn.visibility = View.VISIBLE
        val call = api.deleteAnnounce(id_ann)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    val builder = AlertDialog.Builder(this@ActivityDetailAnnonceUser)
                    builder.setMessage(R.string.annonce_supprimee_avec_succes)
                    builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> finish() }
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    Toast.makeText(this@ActivityDetailAnnonceUser, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Toast.makeText(this@ActivityDetailAnnonceUser, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onImageClicked(position: Int) {
        val intent = Intent(this, ActivityFullScreen::class.java)
        intent.putStringArrayListExtra("images", imagesList)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    private fun shareAnn(id_ann: String) {
        val call = api.setAnnouncesActions("share", id_ann, user)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                Log.d("AnnonceDetails", response.body().toString())
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Toast.makeText(this@ActivityDetailAnnonceUser, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        ann.visibility = View.GONE
        loadingIndicatorAnn.visibility = View.VISIBLE
        getAnnounce(idAnn)
    }

    companion object {
        private const val TAG = "ActivityDetailsAnnonce"
    }
}