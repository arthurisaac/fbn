package bf.fasobizness.bafatech.activities.annonce

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.helper.AudioManager
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.IllustrationInterface
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zhihu.matisse.internal.utils.PathUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class ActivityAnnonceEditer : AppCompatActivity(), UploadCallbacks {
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

    private lateinit var btnDescriptionAudio: Button
    private lateinit var btnSupprimerAudio: ImageView
    private val PERMISSIONS_REQ = 1
    private lateinit var audioManager: AudioManager
    private var isRecording: Boolean = false
    private var isPlaying: Boolean = false
    private var audioPresent: Boolean = false

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
        spVille.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.villes_2)
        )
        spCategorie.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.categories_2)
        )
        val extras = intent
        val annonce = extras.getSerializableExtra("annonce") as Annonce?
        annonce?.let { populateData(it) }

        btnDescriptionAudio = findViewById(R.id.btn_description_audio)
        btnSupprimerAudio = findViewById(R.id.btn_supprimer_audio)
        btnDescriptionAudio.setOnClickListener { requestAudioPermissions() }
        btnSupprimerAudio.setOnClickListener {
            audioPresent = false
            btnDescriptionAudio.text = getString(R.string.ajouter_description_audio)
            // btnDescriptionAudio.background = ContextCompat.getDrawable(this@ActivityNewAnnounce, R.drawable.ic_baseline_mic)
            btnDescriptionAudio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_mic, 0, 0, 0)
            btnSupprimerAudio.visibility = View.GONE
        }
        audioManager = AudioManager(this)
    }

    private fun requestAudioPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        recordAudio()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Erreur de permission! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun recordAudio() {
        val handler = Handler()
        Runnable { }
        if (isPlaying) {
            audioManager.stopPlayback()
            btnDescriptionAudio.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_play_black,
                0,
                0,
                0
            )
            btnDescriptionAudio.text = "Jouer audio"
            isPlaying = false
        } else {
            if (audioPresent) {
                isPlaying = true
                // btnDescriptionAudio.text = "Arrêter "
                btnDescriptionAudio.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_stop,
                    0,
                    0,
                    0
                )
                btnSupprimerAudio.visibility = View.VISIBLE
                audioManager.startPlayback(1)
                btnDescriptionAudio.text = "Arrêter"
                val runnable = Runnable {
                    btnDescriptionAudio.text = "Arrêter ${audioManager.getDuration()} Sec"
                }
                handler.postDelayed(runnable, 1000)

                audioManager.mediaPlayer()?.setOnCompletionListener {
                    handler.removeCallbacks(runnable)
                    isPlaying = false
                    btnDescriptionAudio.text = "Jouer audio"
                    btnDescriptionAudio.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_play_black,
                        0,
                        0,
                        0
                    )
                }
            } else {
                if (isRecording) {
                    audioManager.stopRecording()
                    // btnDescriptionAudio.text = getString(R.string.ajouter_description_audio)
                    btnDescriptionAudio.text = "Jouer audio"
                    btnDescriptionAudio.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_play_black,
                        0,
                        0,
                        0
                    )
                    btnSupprimerAudio.visibility = View.VISIBLE
                    isRecording = false
                    audioPresent = true
                } else {
                    audioManager.startRecording(PERMISSIONS_REQ)
                    btnDescriptionAudio.text = getString(R.string.arreter_l_enregistrement)
                    isRecording = true
                }
            }
        }
    }

    private fun updatePost(id_ann: String) {
        Toast.makeText(this@ActivityAnnonceEditer, "Enregistrement en cours", Toast.LENGTH_SHORT).show()
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
                btn_publish_offer.isEnabled = true
                btn_publish_offer.setText(R.string.publier)
                Log.d("Activity", response.toString())
                if (response.isSuccessful) {
                    if (audioPresent) {
                        Toast.makeText(
                            this@ActivityAnnonceEditer,
                            "enregistrement audio",
                            Toast.LENGTH_SHORT
                        ).show()
                        uploadAudio(id_ann)
                    } else {
                        val builder = AlertDialog.Builder(this@ActivityAnnonceEditer)
                        builder.setMessage(R.string.l_annonce_a_ete_modifiee_avec_succes)
                        builder.setPositiveButton(R.string.ok) { dialog: DialogInterface?, id: Int -> finish() }
                        val dialog = builder.create()
                        dialog.show()
                    }
                } else {
                    Toast.makeText(
                        this@ActivityAnnonceEditer,
                        R.string.une_erreur_sest_produite,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Snackbar.make(ll_base, R.string.pas_d_acces_internet, Snackbar.LENGTH_SHORT).show()
                btn_publish_offer.isEnabled = true
                btn_publish_offer.setText(R.string.publier)
            }
        })
    }

    private fun uploadAudio(id_ann: String) {
        val path = audioManager.filePathForId(PERMISSIONS_REQ)
        val audio = File(path)
        if (File(path).exists()) {
            val parts: MutableList<MultipartBody.Part> = ArrayList()
            val illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)
            parts.add(prepareAudioFilePart("audio", audio))

            val idAnn = createPart(id_ann)
            val call = illustrationInterface.uploadAudio(idAnn, parts)
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    Log.d("ActivityAnnonceEditer", response.toString())
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.d("AnnonceEditer", t.toString())
                }
            })
        }
    }

    private fun prepareAudioFilePart(part: String, file: File): MultipartBody.Part {
        val requestFile = ProgressRequestBody(file, this)
        return MultipartBody.Part.createFormData(part, file.name, requestFile)
    }

    private fun createPart(id_ann: String): RequestBody {
        return RequestBody.create(
            MultipartBody.FORM, id_ann
        )
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

    override fun onProgressUpdate(percentage: Int) {

    }

    override fun onError() {
    }

    override fun onFinish() {
    }

    override fun uploadStart() {
    }

}