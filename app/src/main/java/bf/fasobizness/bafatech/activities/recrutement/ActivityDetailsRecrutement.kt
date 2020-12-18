package bf.fasobizness.bafatech.activities.recrutement

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.adapters.IllustrationRecAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.models.Recruit.Recrutement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityDetailsRecrutement : AppCompatActivity(), OnItemListener {
    private lateinit var mIllustrations: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_recrutement)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val extras = intent
        val recrutement = extras.getSerializableExtra("recrutement") as Recrutement?
        val tvNomR = findViewById<TextView>(R.id.tv_recrutement_nom_r)
        val tvDomaine = findViewById<TextView>(R.id.tv_recrutement_domaine2)
        val tvDescription = findViewById<TextView>(R.id.tv_recrutement_description)
        val tvDatePub = findViewById<TextView>(R.id.tv_recrutement_date_pub_2)
        val tvDateFin = findViewById<TextView>(R.id.tv_recrutement_date_depot)
        val tvHeureFin = findViewById<TextView>(R.id.tv_recrutement_heure_depot)
        val tvNomEnt = findViewById<TextView>(R.id.tv_recrutement_nom_ent)
        val btnShare = findViewById<Button>(R.id.btn_share)
        val btnPostuler = findViewById<Button>(R.id.btn_postuler)
        try {
            if (recrutement != null) {
                if (recrutement.lien == null || recrutement.lien.isEmpty() || recrutement.lien == "null") {
                    btnPostuler.visibility = View.GONE
                } else {
                    btnPostuler.visibility = View.VISIBLE
                }
                val datePub = getString(R.string.publi_le, recrutement.date_pub)
                tvNomR.text = recrutement.nom_r
                tvDomaine.text = recrutement.domaine
                tvDescription.text = recrutement.description
                tvDatePub.text = datePub
                tvDateFin.text = recrutement.date_fin
                tvHeureFin.text = recrutement.heure_fin
                tvNomEnt.text = recrutement.nom_ent
                btnShare.setOnClickListener {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    val shareBodyText = "Je partage cette offre d’emploi pour " + recrutement.nom_r + ". Tous les détails sont sur l’application Faso Biz Nèss, à télécharger sur Playstore, http://bit.ly/AndroidFBN."
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Offre d'emploi " + recrutement.nom_r)
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
                    startActivity(Intent.createChooser(sharingIntent, "Partager avec"))
                }
                btnPostuler.setOnClickListener {
                    var url = recrutement.lien
                    if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                seen(recrutement.id_recr)
                jsonParse(recrutement)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
        }
    }

    private fun seen(id_recr: String) {
        /*String url = Constants.HOST_URL + "recrutement/vue/"+id_recr;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> Log.v(TAG, response), error -> {
                    Log.v(TAG, error.toString());
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(request);*/
        val api = RetrofitClient.getClient().create(API::class.java)
        val call = api.markAsReadRecruit(id_recr)
        call.enqueue(object : Callback<Int?> {
            override fun onResponse(call: Call<Int?>, response: Response<Int?>) {
                Log.d(TAG, response.toString())
            }

            override fun onFailure(call: Call<Int?>, t: Throwable) {
                Log.d(TAG, t.toString())
            }
        })
    }

    private fun jsonParse(recrutement: Recrutement) {
        mIllustrations = ArrayList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_piece_jointe)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        try {
            val affiches = recrutement.affiches
            for (i in affiches.indices) {
                mIllustrations.add(affiches[i].nom)
            }
            val mIllustrationAdapter = IllustrationRecAdapter(this, mIllustrations)
            recyclerView.adapter = mIllustrationAdapter
            mIllustrationAdapter.setOnItemListener(this)
            mIllustrationAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getExtension(filePath: String): String? {
        val strLenght = filePath.lastIndexOf(".")
        return if (strLenght > 0) {
            filePath.substring(strLenght + 1).toLowerCase(Locale.ROOT)
        } else null
    }

    override fun onItemClicked(position: Int) {
        val nom = mIllustrations[position]
        if (Objects.requireNonNull(getExtension(nom)) == "pdf") {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(nom))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                val uri = Uri.parse(nom)
                Toast.makeText(this, R.string.telecharement_en_cours, Toast.LENGTH_SHORT).show()
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(uri)
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.setTitle("Faso Biz Ness")
                request.setDescription("Téléchargement")
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "faso_biz_ness_" + System.currentTimeMillis() + ".jpg")
                request.setMimeType("*/*")
                downloadManager.enqueue(request)
            }
        } else {
            val intent = Intent(this, ActivityFullScreen::class.java)
            intent.putExtra("position", position)
            intent.putExtra("images", mIllustrations)
            startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "ActivityDetailsRecrut"
    }
}