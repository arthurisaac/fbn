package bf.fasobizness.bafatech.activities.user

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.fragments.FragmentPasswordUpdate
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.User
import bf.fasobizness.bafatech.utils.FileCompressingUtil
import bf.fasobizness.bafatech.utils.MySharedManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
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
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class ActivityProfile : AppCompatActivity(), UploadCallbacks {

    private lateinit var username: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var telephone: TextInputLayout
    private lateinit var nom: TextInputLayout
    private lateinit var prenom: TextInputLayout
    private lateinit var sectActivite: TextInputLayout
    private lateinit var photoProfile: CircleImageView
    private lateinit var sharedManager: MySharedManager
    private lateinit var user: String
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutProfile: LinearLayout
    private lateinit var layoutBusy: LinearLayout
    private lateinit var layoutOffline: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarHead: ProgressBar
    private lateinit var btnUpdateProfile: Button
    private var api = RetrofitClient.getClient().create(API::class.java)
    private lateinit var sect: RelativeLayout
    private lateinit var pre: RelativeLayout
    private lateinit var us: RelativeLayout
    private var images: ArrayList<Image> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedManager = MySharedManager(this)
        user = sharedManager.user

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.profil)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        sect = findViewById(R.id.sect)
        pre = findViewById(R.id.pre)
        us = findViewById(R.id.us)

        layoutLoading = findViewById(R.id.layout_loading)
        layoutProfile = findViewById(R.id.layout_profile)
        layoutBusy = findViewById(R.id.layout_busy_system)
        layoutOffline = findViewById(R.id.layout_ent_offline)
        layoutOffline.visibility = View.GONE
        layoutBusy.visibility = View.GONE
        layoutProfile.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE

        val type = sharedManager.type
        if (type == "entreprise") {
            pre.visibility = View.GONE
            us.visibility = View.GONE
        } else {
            sect.visibility = View.GONE
        }

        photoProfile = findViewById(R.id.photo_de_profil)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        telephone = findViewById(R.id.telephone)
        nom = findViewById(R.id.nom)
        prenom = findViewById(R.id.prenom)
        sectActivite = findViewById(R.id.sect_activite)
        val updateMdp = findViewById<Button>(R.id.btn_update_mdp)

        progressBar = findViewById(R.id.progress_bar_photo)
        progressBarHead = findViewById(R.id.progress_bar_head)

        photoProfile.setOnClickListener { options() }

        updateMdp.setOnClickListener {
            val fragmentManager: FragmentManager = supportFragmentManager
            val dialogFragment = FragmentPasswordUpdate.newInstance()
            val bundle = Bundle()
            bundle.putString("type", "update")
            dialogFragment.arguments = bundle
            dialogFragment.show(fragmentManager, "")
        }
        btnUpdateProfile = findViewById(R.id.btn_update_profil)
        btnUpdateProfile.setOnClickListener {
            if (type == "entreprise") {
                if (checkEmail() or checkSect() or checkNom()) {
                     updateProfile()
                }
            } else {
                if (checkUsername() && checkNom() && checkEmail() && checkPrenom()) {
                    updateProfile()
                }
            }
        }

        api = RetrofitClient.getClient().create(API::class.java)
        getUserProfile()

        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { getUserProfile() }
    }

    private fun getUserProfile() {
        layoutLoading.visibility = View.VISIBLE
        layoutOffline.visibility = View.GONE
        layoutBusy.visibility = View.GONE
        layoutProfile.visibility = View.GONE
        val call: Call<User> = api.getUser(user, "Bearer " + sharedManager.token)
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                layoutLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    layoutProfile.visibility = View.VISIBLE
                    val user: User? = response.body()

                    if (user != null) {
                        username.editText!!.setText(user.username)
                        email.editText?.setText(user.email)
                        prenom.editText?.setText(user.prenom)
                        telephone.editText?.setText(user.tel)
                        sectActivite.editText?.setText(user.sect_activite)
                        nom.editText?.setText(user.nom)
                        sharedManager.username = user.username
                        sharedManager.email = user.email
                        sharedManager.photo = user.photo
                        sharedManager.type = user.type
                        Glide.with(this@ActivityProfile)
                                .setDefaultRequestOptions(
                                        RequestOptions()
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                                .centerCrop()
                                                .override(400, 400)
                                )
                                .asBitmap()
                                .load(user.photo)
                                .thumbnail(0.1f)
                                .into(photoProfile)
                    }

                } else {
                    layoutBusy.visibility = View.VISIBLE
                    layoutProfile.visibility = View.GONE
                    Toast.makeText(this@ActivityProfile, response.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                layoutLoading.visibility = View.GONE
                layoutBusy.visibility = View.GONE
                layoutOffline.visibility = View.VISIBLE
                layoutProfile.visibility = View.GONE
                Toast.makeText(applicationContext, getString(R.string.pas_d_acces_internet), Toast.LENGTH_LONG).show()
            }
        })
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
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Erreur de permission! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun options() {
        val items = arrayOf<CharSequence>("Modifier", "Supprimer")
        val builder = AlertDialog.Builder(this@ActivityProfile)
        builder.setTitle(R.string.choisir_la_source)
        builder.setItems(items) { _: DialogInterface?, i: Int ->
            if (items[i] == "Modifier") {
                requestMultiplePermissions()
            } else if (items[i] == "Supprimer") {
                removeProfilePhoto()
            }
        }
        builder.show()
    }

    private fun showChooser() {
        ImagePicker.with(this)
                .setFolderMode(true)
                .setFolderTitle("Photos")
                .setRootDirectoryName(Config.ROOT_DIR_DCIM)
                .setDirectoryName("Faso Biz Ness")
                .setMultipleMode(false)
                .setShowNumberIndicator(true)
                .setMaxSize(1)
                .setLimitMessage("Vous pouvez selectionner 10 images")
                .setSelectedImages(images)
                .setRequestCode(100)
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            images = ImagePicker.getImages(data)
            updateProfilePhoto(images[0].uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateProfilePhoto(uri: Uri) {
        // val parts: MultipartBody.Part = prepareFilePart("file", uri)
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        parts.add(prepareFilePart("file", uri))
        val user = createPart(user)
        val call = api.uploadPhotos(user, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("activity", response.toString())
                Glide.with(this@ActivityProfile)
                        .setDefaultRequestOptions(
                                RequestOptions()
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .centerCrop()
                                        .override(400, 400)
                        )
                        .asBitmap()
                        .load(images[0].uri)
                        .thumbnail(0.1f)
                        .into(photoProfile)

                if (!response.isSuccessful) {
                    Toast.makeText(this@ActivityProfile, "Error code: " + response.code(), Toast.LENGTH_SHORT).show()
                    Log.d("activity", response.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("activity", t.toString())
            }
        })
    }

    private fun updateProfile() {
        progressBarHead.visibility = View.VISIBLE
        btnUpdateProfile.setText(R.string.chargement_en_cours)
        btnUpdateProfile.isEnabled = false
        email.error = null
        nom.error = null
        prenom.error = null
        sectActivite.error = null
        telephone.error = null
        val txtemail = email.editText?.text.toString().trim()
        val txtusername = username.editText?.text.toString().trim()
        val txttel = telephone.editText?.text.toString().trim()
        val txtnom = nom.editText?.text.toString().trim()
        val txtprenom = prenom.editText?.text.toString().trim()
        val txtsectActivite = sectActivite.editText?.text.toString().trim()
        val call = api.updateUser(
                txtemail,
                txtusername,
                txttel,
                txtnom,
                txtprenom,
                txtsectActivite,
                user,
                "Bearer " + sharedManager.token
        )
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                btnUpdateProfile.setText(R.string.enregistrer)
                btnUpdateProfile.isEnabled = true
                progressBarHead.visibility = View.GONE
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        if (user.message != null) {
                            if (user.message == "email") {
                                email.error = getString(R.string.cet_email_existe_deja)
                            }
                            if (user.message == "tel") {
                                telephone.error = getString(R.string.ce_numero_de_telephone_existe)
                            }
                            if (user.message == "username") {
                                username.error = getString(R.string.ce_nom_d_utilisateur_existe_deja)
                            }
                            if (user.message == "nom") {
                                nom.error = getString(R.string.ce_nom_existe_deja)
                            }
                            if (user.message == "user") {
                                Toast.makeText(this@ActivityProfile, R.string.votre_compte_a_ete_supprime, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val type = user.type
                            if (type == "entreprise") {
                                pre.visibility = View.GONE
                                us.visibility = View.GONE
                            } else {
                                sect.visibility = View.GONE
                            }
                            sharedManager.email = user.email
                            sharedManager.username = user.username
                            sharedManager.photo = user.photo
                            Snackbar.make(layoutProfile, getString(R.string.profil_modifie_avec_succes), Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ActivityProfile, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ActivityProfile, response.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                btnUpdateProfile.setText(R.string.enregistrer)
                btnUpdateProfile.isEnabled = true
                progressBarHead.visibility = View.GONE
                Toast.makeText(this@ActivityProfile, "Erreur " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun disconnect() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.vous_etes_sur_le_point_de_vous_deconnecter))
        builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
            sharedManager.username = ""
            sharedManager.email = ""
            sharedManager.photo = ""
            sharedManager.type = ""
            sharedManager.user = ""
            val intent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.annuler) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun removeProfilePhoto() {
        val call = api.removePhoto(user)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@ActivityProfile, "Error code: " + response.code(), Toast.LENGTH_SHORT).show()
                } else {
                    photoProfile.setImageResource(R.drawable.user)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Toast.makeText(this@ActivityProfile, "Error message: " + t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.nav_logout) {
            disconnect()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkEmail(): Boolean {
        val txtEmail: String = email.editText?.text.toString().trim()
        val isValid = Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()
        return if (txtEmail.isEmpty()) {
            email.error = getString(R.string.adresse_email_requise)
            false
        } else if (!isValid) {
            email.error = getString(R.string.adresse_email_invalide)
            false
        } else {
            email.error = null
            true
        }
    }

    private fun checkUsername(): Boolean {
        val txtUsername: String = username.editText?.text.toString().trim()
        return when {
            txtUsername.isEmpty() -> {
                username.error = getString(R.string.nom_d_utlisateur_requis)
                false
            }
            txtUsername.length < 4 -> {
                username.error = getString(R.string.entrer_un_nom_d_utilisateur_plus_long)
                false
            }
            else -> {
                username.error = null
                true
            }
        }
    }

    private fun checkPrenom(): Boolean {
        val txtPrenom: String = prenom.editText?.text.toString().trim()
        return if (txtPrenom.isEmpty()) {
            prenom.error = getString(R.string.nom_d_utlisateur_requis)
            false
        } else {
            prenom.error = null
            true
        }
    }

    private fun checkNom(): Boolean {
        val txtNom: String = nom.editText?.text.toString().trim()
        return if (txtNom.isEmpty()) {
            nom.error = getString(R.string.nom_requis)
            false
        } else {
            nom.error = null
            true
        }
    }

    private fun checkSect(): Boolean {
        val txtSectAct: String = sectActivite.editText?.text.toString().trim()
        return if (txtSectAct.isEmpty()) {
            sectActivite.error = getString(R.string.secteut_d_activite_requis)
            false
        } else {
            sectActivite.error = null
            true
        }
    }

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

    private fun prepareFilePart(part: String, uri: Uri): MultipartBody.Part {
        val file = getFile(this, uri)
        val fileCompressingUtil = FileCompressingUtil()
        val compressedFile = fileCompressingUtil.saveBitmapToFile(file)
        val requestFile = ProgressRequestBody(compressedFile, this)
        return MultipartBody.Part.createFormData(part, file!!.name, requestFile)
    }

    private fun createPart(id_ann: String): RequestBody {
        return RequestBody.create(
                MultipartBody.FORM, id_ann)
    }

    override fun onProgressUpdate(percentage: Int) {

    }

    override fun onError() {
        progressBar.visibility = View.GONE
    }

    override fun onFinish() {
        progressBar.visibility = View.GONE
    }

    override fun uploadStart() {
        progressBar.visibility = View.VISIBLE
    }

}