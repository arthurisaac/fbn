package bf.fasobizness.bafatech.activities.annonce

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import bf.fasobizness.bafatech.adapters.UriAdapter
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
    private var images: ArrayList<Image>? = null
    private var pictures: ArrayList<Uri>? = null

    private lateinit var til_titre_annonce: TextInputLayout
    private lateinit var til_desc_annonce: TextInputLayout
    private lateinit var til_tel_annonce: TextInputLayout
    private lateinit var til_tel1_annonce: TextInputLayout
    private lateinit var til_tel2_annonce: TextInputLayout
    private lateinit var sp_categorie: Spinner
    private lateinit var sp_ville: Spinner

    private lateinit var ed_prix: TextInputLayout
    private lateinit var tv_ville_error: TextView
    private lateinit var tv_categorie_error: TextView
    private lateinit var tv_error_no_image: TextView
    private lateinit var pourcent: TextView
    private lateinit var btn_publish_offer: Button
    private var user: String? = null
    private lateinit var overbox: RelativeLayout
    private lateinit var rlUploadPicture: RelativeLayout
    private var mAdapter: UriAdapter? = null
    private var id_annonce_fk = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var linear_uploading: LinearLayout
    private lateinit var linear_succes: LinearLayout
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
        ed_prix = findViewById(R.id.ed_prix_annonce)
        val btn_close_overbox = findViewById<Button>(R.id.btn_close_overbox)
        til_titre_annonce = findViewById(R.id.til_titre_annonce)
        til_desc_annonce = findViewById(R.id.til_description_annonce)
        til_tel_annonce = findViewById(R.id.til_tel_annonce)
        til_tel1_annonce = findViewById(R.id.til_tel1_annonce)
        til_tel2_annonce = findViewById(R.id.til_tel2_annonce)
        sp_categorie = findViewById(R.id.sp_categorie_annonce)
        sp_ville = findViewById(R.id.sp_ville_annonce)
        overbox = findViewById(R.id.overbox)
        rlUploadPicture = findViewById(R.id.rl_upload_picture)


        tv_ville_error = findViewById(R.id.tv_error_ville)
        tv_categorie_error = findViewById(R.id.tv_error_catégorie)
        tv_error_no_image = findViewById(R.id.tv_error_no_image)
        progressBar = findViewById(R.id.progress_bar)
        linear_uploading = findViewById(R.id.linear_uploading)
        linear_succes = findViewById(R.id.linear_succes)
        btn_publish_offer = findViewById(R.id.btn_publish)


        /*
         *
         */
        api = RetrofitClient.getClient().create(API::class.java)
        sharedManager = MySharedManager(this)

        images = ArrayList()
        pictures = ArrayList()
        toolbar.title = getString(R.string.nouvelle_annonce)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        overbox.visibility = View.GONE
        rlUploadPicture.visibility = View.GONE

        sp_ville.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.villes))
        sp_categorie.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.categories))

        btn_publish_offer.setOnClickListener {
            if (!checkTitreInput() or !checkDescInput() or !checkTelInput() or !checkVilleInput() or !checkCatInput() or !checkImage()) {
                return@setOnClickListener
            }
            publierAnnonce()
        }
        btn_close_overbox.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.file_list)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = UriAdapter(this, pictures).also { mAdapter = it }
        recyclerView.setHasFixedSize(true)
        mAdapter?.setOnItemListener(this)

        user = sharedManager.user

        if (sharedManager.tel.isNotEmpty()) {
            til_tel_annonce.editText?.setText(sharedManager.tel)
        }
        if (sharedManager.tel1.isNotEmpty()) {
            til_tel1_annonce.editText?.setText(sharedManager.tel1)
        }
        if (sharedManager.tel2.isNotEmpty()) {
            til_tel2_annonce.editText?.setText(sharedManager.tel2)
        }
        if (sharedManager.ville.isNotEmpty()) {
            for (i in 0 until sp_ville.adapter.count) {
                if (sp_ville.adapter.getItem(i).toString().contains(sharedManager.ville)) {
                    sp_ville.setSelection(i)
                }
            }
        }
        if (sharedManager.categorie.isNotEmpty()) {
            for (i in 0 until sp_categorie.adapter.count) {
                if (sp_categorie.adapter.getItem(i).toString().contains(sharedManager.categorie)) {
                    sp_categorie.setSelection(i)
                }
            }
        }

        val btnAddPicturesAnnounce = findViewById<Button>(R.id.btn_add_pictures_annonce)
        btnAddPicturesAnnounce.setOnClickListener { v: View? -> requestMultiplePermissions() }
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
                .setFolderTitle("Album")
                .setRootDirectoryName(Config.ROOT_DIR_DCIM)
                .setDirectoryName("Image Picker")
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
            images = ImagePicker.getImages(data)
            val photos = ImagePicker.getImages(data)
            for (photo in photos) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // path
                    pictures!!.add(photo.uri)
                } else {
                    // uri
                    Log.d(tag, photo.path)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)   // This line is REQUIRED in fragment mode
    }

    private fun checkTitreInput(): Boolean {
        val titreInput = til_titre_annonce.editText!!.text.toString().trim { it <= ' ' }
        return when {
            titreInput.isEmpty() -> {
                til_titre_annonce.error = getString(R.string.titre_annonce_requis)
                false
            }
            titreInput.length > 35 -> {
                til_titre_annonce.error = getString(R.string.titre_annonce_trop_long)
                false
            }
            else -> {
                til_titre_annonce.error = null
                true
            }
        }
    }

    private fun checkDescInput(): Boolean {
        try {
            val descInput = til_desc_annonce.editText!!.text.toString().trim { it <= ' ' }
            return when {
                descInput.isEmpty() -> {
                    til_desc_annonce.error = getString(R.string.description_annonce_requis)
                    false
                }
                descInput.length > 600 -> {
                    til_desc_annonce.error = getString(R.string.description_de_lannonce_trop_longue)
                    false
                }
                else -> {
                    til_desc_annonce.error = null
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
            val telInput = til_tel_annonce.editText!!.text.toString().trim { it <= ' ' }
            return when {
                telInput.isEmpty() -> {
                    til_tel_annonce.error = getString(R.string.no_de_tel_requis)
                    false
                }
                telInput.length > 13 -> {
                    til_tel_annonce.error = getString(R.string.no_de_tel_invalide)
                    false
                }
                else -> {
                    til_tel_annonce.error = null
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun checkVilleInput(): Boolean {
        val villeInput = sp_ville.selectedItem.toString()
        return when {
            villeInput.isEmpty() -> {
                tv_ville_error.visibility = View.VISIBLE
                false
            }
            villeInput == "Choisir ville" -> {
                tv_ville_error.visibility = View.VISIBLE
                false
            }
            else -> {
                tv_ville_error.visibility = View.GONE
                true
            }
        }
    }

    private fun checkCatInput(): Boolean {
        val catInput = sp_categorie.selectedItem.toString()
        return when {
            catInput.isEmpty() -> {
                tv_categorie_error.visibility = View.VISIBLE
                false
            }
            catInput == "Choisir Categorie" -> {
                tv_categorie_error.visibility = View.VISIBLE
                false
            }
            else -> {
                tv_categorie_error.visibility = View.GONE
                true
            }
        }
    }

    private fun checkImage(): Boolean {
        return if (images!!.size == 0) {
            tv_error_no_image.visibility = View.VISIBLE
            false
        } else {
            tv_error_no_image.visibility = View.GONE
            true
        }
    }

    override fun onItemClicked(position: Int) {
        mAdapter!!.remove(position)
        mAdapter!!.notifyItemRemoved(position)
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
        btn_publish_offer.isEnabled = true
        pourcent.text = getString(R.string.pas_d_acces_internet)
    }

    private fun publierAnnonce() {
        // Cacher clavier
        if (currentFocus != null) {
            val inputManager = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        btn_publish_offer.isEnabled = false
        overbox.visibility = View.VISIBLE
        if (user!!.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val titre = til_titre_annonce.editText!!.text.toString().trim { it <= ' ' }
            val description = til_desc_annonce.editText!!.text.toString()
            val prix = ed_prix.editText!!.text.toString()
            val tel = til_tel_annonce.editText!!.text.toString()
            val tel1 = til_tel1_annonce.editText!!.text.toString()
            val tel2 = til_tel2_annonce.editText!!.text.toString()
            val ville = sp_ville.selectedItem.toString()
            val categorie = sp_categorie.selectedItem.toString()
            sharedManager.tel = tel
            sharedManager.tel1 = tel1
            sharedManager.tel2 = tel2
            sharedManager.ville = ville
            sharedManager.categorie = categorie
            val call = api!!.postAnnounce(
                    id_annonce_fk,
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
                            btn_publish_offer.isEnabled = true
                        } else {
                            id_annonce_fk = response.body()!!.id
                            uploadPictures()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        overbox.visibility = View.GONE
                        btn_publish_offer.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.v(tag, t.toString())
                    btn_publish_offer.isEnabled = true
                    btn_publish_offer.setText(R.string.ressayer)
                    overbox.visibility = View.GONE
                }
            })
        }
    }

    private fun uploadPictures() {
        rlUploadPicture.visibility = View.VISIBLE
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        val illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)
        for (i in images!!.indices) {
            parts.add(prepareFilePart("image$i", images!![i]))
        }
        val id_ann = createPart(id_annonce_fk.toString())
        val size = createPart(parts.size.toString() + "")
        val call = illustrationInterface.uploadPhotos(id_ann, size, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d(tag, response.toString())
                if (response.isSuccessful) {
                    linear_uploading.visibility = View.GONE
                    linear_succes.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                rlUploadPicture.visibility = View.GONE
                overbox.visibility = View.GONE
                btn_publish_offer.isEnabled = true
                btn_publish_offer.setText(R.string.ressayer)
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