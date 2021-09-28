package bf.fasobizness.bafatech.activities.annonce

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.activities.ActivityPhotoList
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity
import bf.fasobizness.bafatech.fragments.FragmentNotConnected
import bf.fasobizness.bafatech.fragments.FragmentSignaler.Companion.newInstance
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityDetailsAnnonce : AppCompatActivity() {
    private var ajouterFavori: FloatingActionButton? = null

    // private ArrayList<String> imagesList;
    private lateinit var images: ArrayList<String>
    private lateinit var imageList: ArrayList<SlideModel>

    // private ImagesAdapter imagesAdapter;
    private lateinit var user: String
    private lateinit var token: String
    private lateinit var txtTitreAnnonce: TextView
    private lateinit var txtText: TextView
    private lateinit var txtPrix: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtTel1: TextView
    private lateinit var txtTel2: TextView
    private lateinit var txtTel: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtNom: TextView
    private lateinit var txtCategorie: TextView
    private lateinit var sendMessage: Button
    private lateinit var sendWhatsappMessage: Button
    private lateinit var btnShare: Button
    private lateinit var txtUpdatedat: TextView
    private lateinit var txtDatePub: TextView

    // private ImageView iv_affiche;
    private lateinit var txtPhotoUtil: ImageView
    private lateinit var txtVue: TextView
    private lateinit var ann: LinearLayout
    private lateinit var layoutNoAnnonce: LinearLayout
    private lateinit var loadingIndicatorAnn: LinearLayout
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var bottomButtons: LinearLayout
    private lateinit var seeMoreAnnonce: RelativeLayout
    private lateinit var fav: String
    private lateinit var pager: ImageSlider

    // private ProgressBar progress_bar_discussion;
    private lateinit var api: API
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_annonce)
        api = RetrofitClient.getClient().create(API::class.java)
        val mySharedManager = MySharedManager(this@ActivityDetailsAnnonce)
        user = mySharedManager.user
        token = "Bearer " + mySharedManager.token
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.title = ""
        // toolbar.setTitle(R.string.details);
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        txtPhotoUtil = findViewById(R.id.txt_username_logo_ann)
        txtTitreAnnonce = findViewById(R.id.txt_titre_annonce)
        txtText = findViewById(R.id.txt_texte_ann)
        txtPrix = findViewById(R.id.txt_prix_annonce)
        txtEmail = findViewById(R.id.txt_email_util)
        txtTel1 = findViewById(R.id.txt_tel1_annonce)
        txtTel2 = findViewById(R.id.txt_tel2_annonce)
        txtTel = findViewById(R.id.txt_tel_annonce)
        txtLocation = findViewById(R.id.txt_location_annonce)
        txtNom = findViewById(R.id.txt_username_ann)
        txtVue = findViewById(R.id.txt_vue)
        txtCategorie = findViewById(R.id.txt_categorie_annonce)
        sendMessage = findViewById(R.id.send_direct_message)
        btnShare = findViewById(R.id.btn_share)
        ajouterFavori = findViewById(R.id.favorite)
        txtUpdatedat = findViewById(R.id.txt_date_modification)
        txtDatePub = findViewById(R.id.txt_date_pub)
        pager = findViewById(R.id.flipper_affiche_annonce)
        sendWhatsappMessage = findViewById(R.id.send_whatsapp_message)
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.GONE
        loadingIndicatorAnn = findViewById(R.id.loading_indicator_ann)
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        layoutNoAnnonce = findViewById(R.id.layout_no_annonce)
        ann = findViewById(R.id.ann)
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        bottomButtons = findViewById(R.id.bottom_buttons)
        seeMoreAnnonce = findViewById(R.id.see_more_annonce)
        layoutEntOffline.visibility = View.GONE
        ann.visibility = View.GONE
        loadingIndicatorAnn.visibility = View.VISIBLE
        layoutNoAnnonce.visibility = View.GONE
        layoutBusySystem.visibility = View.GONE
        images = ArrayList()
        imageList = ArrayList()
        val extras = intent
        if (extras.getStringExtra("id_ann") != null) {
            getAnnounce(extras.getStringExtra("id_ann"))
            btnRefresh.setOnClickListener { getAnnounce(extras.getStringExtra("id_ann")) }
        } else {
            finish()
        }
    }

    private fun getAnnounce(id_ann: String?) {
        layoutEntOffline.visibility = View.GONE
        loadingIndicatorAnn.visibility = View.VISIBLE
        bottomButtons.visibility = View.GONE
        val call = api.getAnnounce(id_ann, user)
        call.enqueue(object : Callback<Annonce?> {
            override fun onResponse(call: Call<Annonce?>, response: Response<Annonce?>) {
                loadingIndicatorAnn.visibility = View.GONE
                if (response.isSuccessful) {
                    bottomButtons.visibility = View.VISIBLE
                    // Log.d(TAG, response.toString());
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
                Log.v(TAG, t.toString())
                Toast.makeText(this@ActivityDetailsAnnonce, getString(R.string.pas_d_acces_internet), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateData(annonce: Annonce) {
        try {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            RequestOptions()
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(annonce.photo)
                    .thumbnail(0.1f)
                    .into(txtPhotoUtil)
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
        if (annonce.texte != null) {
            txtText.text = annonce.texte
        }
        txtTitreAnnonce.text = annonce.titre
        if (annonce.prix.isEmpty() || annonce.prix == null || annonce.prix == "0") {
            txtPrix.setText(R.string.prix_sur_demande)
        } else {
            val prix = annonce.prix + " F CFA"
            txtPrix.text = prix
        }
        // txt_prix.setText(annonce.getPrix());
        txtEmail.text = annonce.email
        txtTel.text = annonce.tel
        txtTel1.text = annonce.tel1
        txtTel2.text = annonce.tel2
        txtLocation.text = annonce.location
        txtCategorie.text = annonce.categorie
        txtNom.text = annonce.username
        val strShare = this.getString(R.string.partager_annonce, annonce.share)
        btnShare.text = strShare
        val btnSignaler = findViewById<Button>(R.id.signaler_annonce)
        btnSignaler.setOnClickListener {
            val fragmentSignaler = newInstance()
            val bundle = Bundle()
            bundle.putString("element", "annonce")
            bundle.putString("id_element", annonce.id_ann)
            fragmentSignaler.arguments = bundle
            fragmentSignaler.show(supportFragmentManager, "")
        }
        seeMoreAnnonce.setOnClickListener {
            val intent = Intent(this, ActivityUserProfile::class.java)
            intent.putExtra("id", annonce.id_per_fk)
            startActivity(intent)
        }
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
        val dtStr = getString(R.string.publiee_le, annonce.date_pub)
        txtDatePub.text = dtStr
        val txtVuelocale = getString(R.string._vues, annonce.vue)
        txtVue.text = txtVuelocale
        if (annonce.categorie == null || annonce.categorie.isEmpty()) {
            txtCategorie.setText(R.string.aucune_categorie_renseignee)
        } else {
            txtCategorie.setOnClickListener {
                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("categorie", annonce.categorie)
                    val intent = Intent(this, ActivityAnnounceFilter::class.java)
                    intent.putExtra("filter", "multiple")
                    intent.putExtra("params", jsonObject.toString())
                    intent.putExtra("title", annonce.categorie)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (annonce.location == null || annonce.location.isEmpty()) {
            txtLocation.visibility = View.GONE
        } else {
            txtLocation.setOnClickListener {
                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("location", annonce.location)
                    val intent = Intent(this, ActivityAnnounceFilter::class.java)
                    intent.putExtra("filter", "multiple")
                    intent.putExtra("params", jsonObject.toString())
                    intent.putExtra("title", annonce.location)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (annonce.favori != null) {
            fav = annonce.favori
            if (fav == "1") {
                ajouterFavori!!.setImageResource(R.drawable.ic_star_yellow)
            }
        }

        if (annonce.tel.isEmpty()) {
            txtTel.visibility = View.GONE
        } else {
            try {
                txtTel.setOnClickListener { action(annonce.tel) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (annonce.tel1.isEmpty()) {
            txtTel1.visibility = View.GONE
        } else {
            try {
                txtTel1.setOnClickListener { action(annonce.tel1) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (annonce.tel2.isEmpty()) {
            txtTel2.visibility = View.GONE
        } else {
            try {
                txtTel2.setOnClickListener { action(annonce.tel2) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        val titre = annonce.titre
        val idAnn = annonce.id_ann
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
        ajouterFavori!!.setOnClickListener {
            if (user.isEmpty()) {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            } else {
                addFavorite(idAnn)
            }
        }
        sendMessage.setOnClickListener {
            if (user.isEmpty()) {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            } else {
                createDiscussion(annonce.id_per_fk, idAnn)
            }
        }
        if (annonce.whatsapp != null) {
            sendWhatsappMessage.setOnClickListener {
                var numero = annonce.whatsapp
                if (!annonce.whatsapp.contains("226")) {
                    numero = "+226" + annonce.whatsapp
                }
                var waMessage = "Bonjour, je suis intéressé par votre annonce publiée sur *Faso Biz Nèss* intitulée « " + annonce.titre + " ». Merci de m’envoyer plus d’informations. L’annonce se trouve ici https://fasobizness.com/annonce/" + annonce.id_ann
                waMessage = waMessage.replace(" ", "%20")
                val link = "https://wa.me/$numero?text=$waMessage"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(link)
                startActivity(i)
            }
        }
        /*else {
            String numero = annonce.getTel();
            if (!annonce.getTel().contains("226")) {
                numero = "+226" + annonce.getTel();
            }
            String link = "https://wa.me/" + numero + "?text=Bonjour,%20j’%20vu%20votre%20affiche%20sur%20Faso%20Biz%20Nèss%20et%20je%20voudrais%20avoir%20plus%20d’informations";
            send_whatsapp_message.setOnClickListener( v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            });
        }*/try {
            val arrayList = annonce.illustrations
            for (data in arrayList) {
                imageList.add(SlideModel(data.nom))
                images.add(data.nom)
            }
            if (arrayList.size == 0) {
                pager.visibility = View.GONE
            }
            pager.setImageList(imageList, true)
            pager.setItemClickListener(object : ItemClickListener {
                override fun onItemSelected(position: Int) {
                    if (images.size > 1) {
                        val intent = Intent(this@ActivityDetailsAnnonce, ActivityPhotoList::class.java)
                        intent.putStringArrayListExtra("images", images)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@ActivityDetailsAnnonce, ActivityFullScreen::class.java)
                        intent.putStringArrayListExtra("images", images)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDiscussion(receiver_id: String, id_ann: String) {
        if (user.isEmpty()) {
            val notConnected = FragmentNotConnected.newInstance()
            notConnected.show(supportFragmentManager, "")
        } else {
            progressBar.visibility = View.VISIBLE
            sendMessage.isEnabled = false
            val call = api.createDiscussion(receiver_id, id_ann, token)
            call.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                    progressBar.visibility = View.GONE
                    sendMessage.isEnabled = true
                    if (response.isSuccessful) {
                        val discussionId: Int
                        if (response.body() != null) {
                            discussionId = response.body()!!.id
                            val intent = Intent(applicationContext, DefaultMessagesActivity::class.java)
                            intent.putExtra("discussion_id", discussionId.toString())
                            intent.putExtra("receiver_id", receiver_id)
                            intent.putExtra("id_ann", id_ann)
                            intent.putExtra("new", "1")
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@ActivityDetailsAnnonce, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    sendMessage.isEnabled = true
                    Toast.makeText(this@ActivityDetailsAnnonce, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun action(numero: String) {
        try {
            val items = arrayOf<CharSequence>("Composer numéro", "Envoyer SMS")
            val builder = AlertDialog.Builder(applicationContext)
            builder.setTitle(getString(R.string.choisir_action))
            builder.setItems(items) { dialog: DialogInterface, i: Int ->
                when {
                    items[i] == "Composer numéro" -> {
                        compose(numero)
                    }
                    items[i] == "Envoyer SMS" -> {
                        sendSMS()
                    }
                    else -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.show()
        } catch (e: Exception) {
            compose(numero)
        }
    }

    private fun compose(numero: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null))
        startActivity(intent)
    }

    private fun sendSMS() {
        val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this) // Need to change the build to API 19
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "text")
        if (defaultSmsPackageName != null) {
            sendIntent.setPackage(defaultSmsPackageName)
        }
        startActivity(sendIntent)
    }

    /*private void sendEmail(String emailTo, String titre) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailTo, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titre);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre message");
        startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."));
    }*/
    private fun shareAnn(id_ann: String) {
        val call = api.setAnnouncesActions("share", id_ann, user)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    Snackbar.make(ann, "Shared response $response", Snackbar.LENGTH_SHORT)
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d(TAG, t.toString())
                Toast.makeText(this@ActivityDetailsAnnonce, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFavorite(id_ann: String) {
        fav = if (fav == "1") {
            ajouterFavori!!.setImageResource(R.drawable.ic_star_white)
            "0"
        } else {
            ajouterFavori!!.setImageResource(R.drawable.ic_star_yellow)
            "1"
        }
        val call = api.setAnnouncesActions("favorite", id_ann, user)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                Log.d(TAG, response.toString())
                if (response.isSuccessful) {
                    Snackbar.make(ann, getString(R.string.favoris_ajoute), Snackbar.LENGTH_SHORT)
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d(TAG, t.toString())
                Toast.makeText(this@ActivityDetailsAnnonce, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val TAG = "ActivityDetailsAnnonce"
    }
}