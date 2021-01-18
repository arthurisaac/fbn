package bf.fasobizness.bafatech.activities.annonce

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.user.LoginActivity
import bf.fasobizness.bafatech.adapters.AnnoncePhotosAdapter
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.IllustrationInterface
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.FileCompressingUtil
import bf.fasobizness.bafatech.utils.MySharedManager
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.zhihu.matisse.internal.utils.PathUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class ActivityNewAnnounce : AppCompatActivity(), OnItemListener, UploadCallbacks {
    private var tag = "ActivtyAnnonce"
    private var images: ArrayList<Image> = ArrayList()
    // private var pictures: ArrayList<Uri>? = null

    private lateinit var tilTitreAnnonce: TextInputLayout
    private lateinit var tilDescAnnonce: TextInputLayout
    private lateinit var tilTelAnnonce: TextInputLayout
    private lateinit var tilTel1Annonce: TextInputLayout
    private lateinit var tilTel2Annonce: TextInputLayout
    private lateinit var spCategorie: Spinner
    private lateinit var spVille: Spinner

    private lateinit var edPrix: TextInputLayout
    private lateinit var tvVilleError: TextView
    private lateinit var tvCategorieError: TextView
    private lateinit var tvErrorNoImage: TextView
    private lateinit var pourcent: TextView
    private lateinit var btnPublishOffer: Button
    private var user: String? = null
    private lateinit var overbox: RelativeLayout
    private lateinit var rlUploadPicture: RelativeLayout
    private lateinit var mAdapter: AnnoncePhotosAdapter
    private var idAnnonceFk = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var linearUploading: LinearLayout
    private lateinit var linearSucces: LinearLayout
    private var api: API? = null
    private lateinit var sharedManager: MySharedManager

    private fun getFile(context: Context, uri: Uri): File? {
        val path = PathUtils.getPath(context, uri)
        if (path != null) {
            if (isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_announce)

        /*
         * Init
         */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        pourcent = findViewById(R.id.pourcent)
        edPrix = findViewById(R.id.ed_prix_annonce)
        val btnCloseOverbox = findViewById<Button>(R.id.btn_close_overbox)
        tilTitreAnnonce = findViewById(R.id.til_titre_annonce)
        tilDescAnnonce = findViewById(R.id.til_description_annonce)
        tilTelAnnonce = findViewById(R.id.til_tel_annonce)
        tilTel1Annonce = findViewById(R.id.til_tel1_annonce)
        tilTel2Annonce = findViewById(R.id.til_tel2_annonce)
        spCategorie = findViewById(R.id.sp_categorie_annonce)
        spVille = findViewById(R.id.sp_ville_annonce)
        overbox = findViewById(R.id.overbox)
        rlUploadPicture = findViewById(R.id.rl_upload_picture)


        tvVilleError = findViewById(R.id.tv_error_ville)
        tvCategorieError = findViewById(R.id.tv_error_catégorie)
        tvErrorNoImage = findViewById(R.id.tv_error_no_image)
        progressBar = findViewById(R.id.progress_bar)
        linearUploading = findViewById(R.id.linear_uploading)
        linearSucces = findViewById(R.id.linear_succes)
        btnPublishOffer = findViewById(R.id.btn_publish)


        /*
         *
         */
        api = RetrofitClient.getClient().create(API::class.java)
        sharedManager = MySharedManager(this)

        toolbar.title = getString(R.string.nouvelle_annonce)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        overbox.visibility = View.GONE
        rlUploadPicture.visibility = View.GONE

        spVille.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.villes_2))
        spCategorie.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.categories_2))

        btnPublishOffer.setOnClickListener {
            if (!checkTitreInput() or !checkDescInput() or !checkTelInput() or !checkVilleInput() or !checkCatInput() or !checkImage()) {
                return@setOnClickListener
            }
            publierAnnonce()
        }
        btnCloseOverbox.setOnClickListener { finish() }

        mAdapter = AnnoncePhotosAdapter(this, images)
        val recyclerView = findViewById<RecyclerView>(R.id.file_list)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = mAdapter
        recyclerView.setHasFixedSize(true)
        mAdapter.setOnItemListener(this)

        user = sharedManager.user

        if (sharedManager.tel.isNotEmpty()) {
            tilTelAnnonce.editText?.setText(sharedManager.tel)
        }
        if (sharedManager.tel1.isNotEmpty()) {
            tilTel1Annonce.editText?.setText(sharedManager.tel1)
        }
        if (sharedManager.tel2.isNotEmpty()) {
            tilTel2Annonce.editText?.setText(sharedManager.tel2)
        }
        if (sharedManager.ville.isNotEmpty()) {
            for (i in 0 until spVille.adapter.count) {
                if (spVille.adapter.getItem(i).toString().contains(sharedManager.ville)) {
                    spVille.setSelection(i)
                }
            }
        }
        if (sharedManager.categorie.isNotEmpty()) {
            for (i in 0 until spCategorie.adapter.count) {
                if (spCategorie.adapter.getItem(i).toString().contains(sharedManager.categorie)) {
                    spCategorie.setSelection(i)
                }
            }
        }

        val btnAddPicturesAnnounce = findViewById<Button>(R.id.btn_add_pictures_annonce)
        btnAddPicturesAnnounce.setOnClickListener { requestMultiplePermissions() }
    }

    private fun requestMultiplePermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            showChooser()
                            // Log.v(tag, "All permissions are granted by user!")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Erreur de permission! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun showChooser() {
        ImagePicker.with(this)
                .setFolderMode(true)
                .setFolderTitle("Photos")
                .setRootDirectoryName(Config.ROOT_DIR_DCIM)
                .setDirectoryName("Faso Biz Ness")
                .setMultipleMode(true)
                .setShowNumberIndicator(true)
                .setMaxSize(10)
                .setLimitMessage("Vous pouvez selectionner 10 images")
                .setSelectedImages(images)
                .setRequestCode(100)
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            // images = ImagePicker.getImages(data)
            images.clear()
            images.addAll(ImagePicker.getImages(data))
            mAdapter.notifyDataSetChanged()
            /*val photos = ImagePicker.getImages(data)
            for (photon photos) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // path
                    pictures!!.add(photo.uri)
                } else {
                    // uri
                    Log.d(tag, photo.path)
                }

            }*/
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkTitreInput(): Boolean {
        val titreInput = tilTitreAnnonce.editText!!.text.toString().trim { it <= ' ' }
        return when {
            titreInput.isEmpty() -> {
                tilTitreAnnonce.error = getString(R.string.titre_annonce_requis)
                false
            }
            titreInput.length > 35 -> {
                tilTitreAnnonce.error = getString(R.string.titre_annonce_trop_long)
                false
            }
            else -> {
                tilTitreAnnonce.error = null
                true
            }
        }
    }

    private fun checkDescInput(): Boolean {
        try {
            val descInput = tilDescAnnonce.editText!!.text.toString().trim { it <= ' ' }
            return when {
                descInput.isEmpty() -> {
                    tilDescAnnonce.error = getString(R.string.description_annonce_requis)
                    false
                }
                descInput.length > 600 -> {
                    tilDescAnnonce.error = getString(R.string.description_de_lannonce_trop_longue)
                    false
                }
                else -> {
                    tilDescAnnonce.error = null
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun checkTelInput(): Boolean {
        try {
            val telInput = tilTelAnnonce.editText!!.text.toString().trim { it <= ' ' }
            return when {
                telInput.isEmpty() -> {
                    tilTelAnnonce.error = getString(R.string.no_de_tel_requis)
                    false
                }
                telInput.length > 13 -> {
                    tilTelAnnonce.error = getString(R.string.no_de_tel_invalide)
                    false
                }
                else -> {
                    tilTelAnnonce.error = null
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun checkVilleInput(): Boolean {
        val villeInput = spVille.selectedItem.toString()
        return when {
            villeInput.isEmpty() -> {
                tvVilleError.visibility = View.VISIBLE
                false
            }
            (villeInput == "Choisir ville" || villeInput == "Toutes les villes") -> {
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
            (catInput == "Toutes les catégories" || catInput == "Choisir catégorie") -> {
                tvCategorieError.visibility = View.VISIBLE
                false
            }
            else -> {
                tvCategorieError.visibility = View.GONE
                true
            }
        }
    }

    private fun checkImage(): Boolean {
        return if (images.size == 0) {
            tvErrorNoImage.visibility = View.VISIBLE
            false
        } else {
            tvErrorNoImage.visibility = View.GONE
            true
        }
    }

    override fun onItemClicked(position: Int) {
        //val image = images[position]
        //images.remove(image)
        mAdapter.remove(position)
        mAdapter.notifyItemRemoved(position)
    }

    override fun onFinish() {
        pourcent.text = getString(R.string.chargement_en_cours)
    }

    override fun onProgressUpdate(percentage: Int) {
        val percent = getString(R.string._pourcent, percentage.toString())
        pourcent.text = percent
        progressBar.isIndeterminate = false
        progressBar.max = 100
        progressBar.progress = percentage
    }

    override fun uploadStart() {
        pourcent.text = getString(R.string.chargement_en_cours)
        pourcent.visibility = View.VISIBLE
    }

    override fun onError() {
        btnPublishOffer.isEnabled = true
        pourcent.text = getString(R.string.pas_d_acces_internet)
    }

    private fun publierAnnonce() {
        if (currentFocus != null) {
            val inputManager = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        btnPublishOffer.isEnabled = false
        overbox.visibility = View.VISIBLE
        if (user!!.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val titre = tilTitreAnnonce.editText!!.text.toString().trim { it <= ' ' }
            val description = tilDescAnnonce.editText!!.text.toString()
            val prix = edPrix.editText!!.text.toString()
            val tel = tilTelAnnonce.editText!!.text.toString()
            val tel1 = tilTel1Annonce.editText!!.text.toString()
            val tel2 = tilTel2Annonce.editText!!.text.toString()
            val ville = spVille.selectedItem.toString()
            val categorie = spCategorie.selectedItem.toString()
            sharedManager.tel = tel
            sharedManager.tel1 = tel1
            sharedManager.tel2 = tel2
            sharedManager.ville = ville
            sharedManager.categorie = categorie
            val call = api!!.postAnnounce(
                    idAnnonceFk,
                    user,
                    description,
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
                    try {
                        Log.d(tag, response.body()!!.status.toString())
                        val status = response.body()!!.status
                        if (!status) {
                            overbox.visibility = View.GONE
                            btnPublishOffer.isEnabled = true
                        } else {
                            idAnnonceFk = response.body()!!.id
                            uploadPictures()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        overbox.visibility = View.GONE
                        btnPublishOffer.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.v(tag, t.toString())
                    btnPublishOffer.isEnabled = true
                    btnPublishOffer.setText(R.string.ressayer)
                    overbox.visibility = View.GONE
                }
            })
        }
    }

    private fun uploadPictures() {
        rlUploadPicture.visibility = View.VISIBLE
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        val illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)
        for (i in images.indices) {
            parts.add(prepareFilePart("image$i", images[i]))
        }
        val idAnn = createPart(idAnnonceFk.toString())
        val size = createPart(parts.size.toString() + "")
        val call = illustrationInterface.uploadPhotos(idAnn, size, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d(tag, response.toString())
                if (response.isSuccessful) {
                    linearUploading.visibility = View.GONE
                    linearSucces.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                rlUploadPicture.visibility = View.GONE
                overbox.visibility = View.GONE
                btnPublishOffer.isEnabled = true
                btnPublishOffer.setText(R.string.ressayer)
                Log.d(tag, t.toString())
            }
        })
    }

    private fun prepareFilePart(part: String, image: Image): MultipartBody.Part {
        val file = getFile(this, image.uri)
        val fileCompressingUtil = FileCompressingUtil()
        val compressedFile = fileCompressingUtil.saveBitmapToFile(file)
        val requestFile = ProgressRequestBody(compressedFile, this)
        return MultipartBody.Part.createFormData(part, file!!.name, requestFile)
    }

    private fun createPart(id_ann: String): RequestBody {
        return RequestBody.create(
                MultipartBody.FORM, id_ann)
    }
}