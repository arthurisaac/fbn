package bf.fasobizness.bafatech.activities.user

import android.Manifest
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.lifecycle.lifecycleScope
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.fragments.FragmentPasswordUpdate
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.User
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
import com.zhihu.matisse.internal.utils.PathUtils
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*


class ActivityProfile : AppCompatActivity(), UploadCallbacks {

    private val CAMERA = 2
    private val GALLERY = 1

    private lateinit var username: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var telephone: TextInputLayout
    private lateinit var nom: TextInputLayout
    private lateinit var prenom: TextInputLayout
    private lateinit var sectActivite: TextInputLayout
    // private lateinit var dateNaissance: TextInputLayout
    private lateinit var dateNaissance: br.com.sapereaude.maskedEditText.MaskedEditText
    private lateinit var dateNaissanceTI: TextInputLayout
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
    private lateinit var dateNaissanceLayout: RelativeLayout
    private var mCurrentPhotoPath: String = ""
    private val myCalendar = Calendar.getInstance()

    private lateinit var genreLayout: LinearLayout
    private lateinit var txtGenreErreur: TextView
    private lateinit var spGenre: Spinner
    // private var images: ArrayList<Image> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedManager = MySharedManager(this)
        user = sharedManager.user
        val type = sharedManager.type

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

        photoProfile = findViewById(R.id.photo_de_profil)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        telephone = findViewById(R.id.telephone)
        nom = findViewById(R.id.nom)
        prenom = findViewById(R.id.prenom)
        sectActivite = findViewById(R.id.sect_activite)
        val updateMdp = findViewById<Button>(R.id.btn_update_mdp)
        dateNaissance = findViewById(R.id.dateNaissance)

        progressBar = findViewById(R.id.progress_bar_photo)
        progressBarHead = findViewById(R.id.progress_bar_head)
        dateNaissanceLayout = findViewById(R.id.dateNaissanceLayout)
        dateNaissanceTI = findViewById(R.id.dateNaissanceTI)
        genreLayout = findViewById(R.id.genreLayout)
        txtGenreErreur = findViewById(R.id.txt_genre_erreur)
        spGenre = findViewById(R.id.sp_genre)
        spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.genre_without_label))

        photoProfile.setOnClickListener { requestMultiplePermissions() }

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
                if (checkEmail() && checkSect() && checkNom() && checkTel()) {
                     updateProfile()
                }
            } else {
                if (checkUsername() && checkNom() && checkEmail() && checkDateNaiss() && checkPrenom() && checkTel() && checkGenreInput()) {
                    updateProfile()
                }
            }

            /*if (type == "entreprise") {
                pre.visibility = View.GONE
                us.visibility = View.GONE
                dateNaissanceLayout.visibility = View.GONE
                genreLayout.visibility = View.GONE
            } else {
                sect.visibility = View.GONE
                sectActivite.visibility = View.GONE
            }

            if (type == "particuler") {
                sect.visibility = View.GONE
                sectActivite.visibility = View.GONE
            }*/
        }

        api = RetrofitClient.getClient().create(API::class.java)
        getUserProfile()

        /*val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            updateDate()
        }*/

        /*dateNaissance.setOnClickListener {
            DatePickerDialog(this@ActivityProfile, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]).show()
        }*/

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
                    val arrayAdapter = ArrayAdapter(this@ActivityProfile, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.genre_without_label))
                    spGenre.adapter = ArrayAdapter(this@ActivityProfile, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.genre_without_label))

                    if (user != null) {
                        username.editText!!.setText(user.username)
                        email.editText?.setText(user.email)
                        prenom.editText?.setText(user.prenom)
                        telephone.editText?.setText(user.tel)
                        sectActivite.editText?.setText(user.sect_activite)
                        dateNaissance.setText(user.date_naissance)
                        nom.editText?.setText(user.nom)
                        if (user.genre != null) {
                            val spinnerPosition: Int = arrayAdapter.getPosition(user.genre)
                            spGenre.setSelection(spinnerPosition)
                        }
                        /*if (user.genre != null) {
                            spGenre.setSelection(arrayAdapter.getPosition(user.genre))
                        }*/
                        if (user.type == "particulier" || user.type == "utilisateur") {
                            sect.visibility = View.GONE
                            spGenre.visibility = View.VISIBLE
                        } else {
                            pre.visibility = View.GONE
                            sect.visibility = View.GONE
                            us.visibility = View.GONE
                            genreLayout.visibility = View.GONE
                            sect.visibility = View.VISIBLE
                            dateNaissanceLayout.visibility = View.GONE
                        }
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
                    disconnect()
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
                            // showChooser()
                            options()
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
        val items = arrayOf<CharSequence>("Camera", "Galerie", "Supprimer")
        val builder = AlertDialog.Builder(this@ActivityProfile)
        builder.setTitle(R.string.choisir_la_source)
        builder.setItems(items) { _: DialogInterface?, i: Int ->
            when {
                items[i] == "Camera" -> {
                    // pickFromCamera()
                    pickFromCamera()
                }
                items[i] == "Galerie" -> {
                    pickFromGallery()
                }
                items[i] == "Supprimer" -> {
                    removeProfilePhoto()
                }
            }
        }
        builder.show()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY)
    }

    private fun pickFromCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            mCurrentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            images = ImagePicker.getImages(data)
            updateProfilePhoto(images[0].uri)
        }*/

        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val protection = Array(1) { MediaStore.Images.Media.DATA }
                        val cursor = this.managedQuery(contentURI, protection, null, null, null)
                        cursor?.moveToFirst()
                        val photoPath = cursor?.getString(0)
                        cursor?.close()
                        val file = File(photoPath)
                        val uri = Uri.fromFile(file)
                        updateProfilePhoto(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if (requestCode == CAMERA) {
                val cursor = contentResolver.query(Uri.parse(mCurrentPhotoPath),
                        Array(1) { MediaStore.Images.ImageColumns.DATA },
                        null, null, null)
                cursor?.moveToFirst()
                val photoPath = cursor?.getString(0)
                cursor?.close()
                val file = File(photoPath)
                prepareToUpload(file)
            }
        }

    }

    private fun prepareToUpload(file: File) {
        lifecycleScope.launch{
            val compressedImageFile = Compressor.compress(this@ActivityProfile, file)
            val uri = Uri.fromFile(compressedImageFile)
            updateProfilePhoto(uri)
        }
    }

    private fun updateProfilePhoto(uri: Uri) {
        // val parts: MultipartBody.Part = prepareFilePart("file", uri)
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        parts.add(prepareFilePart("file", uri))
        val user = createPart(user)
        val call = api.uploadPhotos(user, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                //Log.d("activity", response.toString())
                sharedManager.photo = uri.path
                Glide.with(this@ActivityProfile)
                        .setDefaultRequestOptions(
                                RequestOptions()
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .centerCrop()
                                        .override(400, 400)
                        )
                        .asBitmap()
                        .load(uri)
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
        var txtGenre = ""
        if (spGenre.selectedItem != null) {
            txtGenre = spGenre.selectedItem.toString()
        }
        // val txtDateNaissance: String = dateNaissance.editText?.text.toString()
        val txtDateNaissance: String = dateNaissance.text.toString()
        val call = api.updateUser(
                txtemail,
                txtusername,
                txttel,
                txtnom,
                txtprenom,
                txtsectActivite,
                txtDateNaissance,
                txtGenre,
                user,
                "Bearer " + sharedManager.token
        )
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                btnUpdateProfile.setText(R.string.enregistrer_les_modifications)
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
                btnUpdateProfile.setText(R.string.enregistrer_les_modifications)
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

    /*private fun updateDate() {
        val format = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(format, Locale.FRENCH)
        dateNaissance.setText(simpleDateFormat.format(myCalendar.time))
    }*/

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

    private fun checkDateNaiss(): Boolean {
        val txtDateNaiss = dateNaissance.text.toString().trim()
        return if (txtDateNaiss.isEmpty()) {
            // dateNaissance.error = getString(R.string.date_de_naissance_requise)
            dateNaissanceTI.error = getString(R.string.date_de_naissance_requise)
            false
        } else if (txtDateNaiss == "jj/mm/aaaa") {
            // dateNaissance.error = getString(R.string.date_de_naissance_requise)
            dateNaissanceTI.error = getString(R.string.date_de_naissance_requise)
            false
        } else {
            // dateNaissance.error = null
            dateNaissanceTI.error = null
            true
        }
    }

    private fun checkGenreInput(): Boolean {
        val genre = spGenre.selectedItem.toString()
        return when {
            genre.isEmpty() -> {
                txtGenreErreur.visibility = View.VISIBLE
                false
            }
            (genre == "Choisir genre") -> {
                txtGenreErreur.visibility = View.VISIBLE
                false
            }
            else -> {
                txtGenreErreur.visibility = View.GONE
                true
            }
        }
    }

    private fun checkTel(): Boolean {
        val txtTel = telephone.editText?.text.toString().trim()
        return if (txtTel.isEmpty()) {
            telephone.error = getString(R.string.numero_de_telephone_requis)
            false
        } else {
            telephone.error = null
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
        //val fileCompressingUtil = FileCompressingUtil()
        //val compressedFile = fileCompressingUtil.saveBitmapToFile(file)
        val requestFile = ProgressRequestBody(file, this)
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