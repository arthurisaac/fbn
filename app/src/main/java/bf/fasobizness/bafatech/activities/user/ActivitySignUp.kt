package bf.fasobizness.bafatech.activities.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.User
import bf.fasobizness.bafatech.utils.MySharedManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.auth0.android.jwt.JWT
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

class ActivitySignUp : AppCompatActivity(), UploadCallbacks {
    private val tag = "ActivityProfile"
    private lateinit var photoProfile: CircleImageView
    private lateinit var username: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var telephone: TextInputLayout
    private lateinit var nom: TextInputLayout
    private lateinit var prenom: TextInputLayout
    private lateinit var sectActivite: TextInputLayout
    private lateinit var mdp: TextInputLayout
    private lateinit var confirm: TextInputLayout
    private lateinit var dateNaissance: br.com.sapereaude.maskedEditText.MaskedEditText
    private lateinit var dateNaissanceTI: TextInputLayout
    private lateinit var sharedManager: MySharedManager
    private lateinit var requestQueue: RequestQueue
    private lateinit var linearSignUpBase: LinearLayout
    private lateinit var dateNaissanceLayout: RelativeLayout
    private lateinit var genreLayout: LinearLayout
    private lateinit var txtGenreErreur: TextView

    private lateinit var type: String
    private lateinit var btnSignUp: Button
    private var api: API = RetrofitClient.getClient().create(API::class.java)
    private var images: ArrayList<Image> = ArrayList()
    // private val myCalendar = Calendar.getInstance()

    private lateinit var spGenre: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Inscription"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        val sect = findViewById<RelativeLayout>(R.id.sect)
        val pre = findViewById<RelativeLayout>(R.id.pre)
        val us = findViewById<RelativeLayout>(R.id.us)

        photoProfile = findViewById(R.id.photo_de_profil)
        photoProfile.setOnClickListener { requestMultiplePermissions() }

        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        telephone = findViewById(R.id.telephone)
        nom = findViewById(R.id.nom)
        prenom = findViewById(R.id.prenom)
        sectActivite = findViewById(R.id.sect_activite)
        mdp = findViewById(R.id.password)
        confirm = findViewById(R.id.passwordConfirm)
        dateNaissance = findViewById(R.id.dateNaissance)
        dateNaissanceTI = findViewById(R.id.dateNaissanceTI)
        genreLayout = findViewById(R.id.genreLayout)
        txtGenreErreur = findViewById(R.id.txt_genre_erreur)

        /*val date = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            updateDate()
        }*/

        /*dateNaissance.setOnClickListener {
            DatePickerDialog(this@ActivitySignUp, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]).show()
        }*/
        // dateNaissance.addTextChangedListener(textWatcher)

        btnSignUp = findViewById(R.id.btn_sign_up)
        sharedManager = MySharedManager(this)

        requestQueue = Volley.newRequestQueue(this)
        linearSignUpBase = findViewById(R.id.linear_sign_up_base)
        dateNaissanceLayout = findViewById(R.id.dateNaissanceLayout)
        /*dateNaissanceLayout.setOnClickListener {
            DatePickerDialog(this@ActivitySignUp, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]).show()
        }*/

        val extras = intent
        type = extras.getStringExtra("type").toString()

        if (type == "entreprise") {
            pre.visibility = View.GONE
            us.visibility = View.GONE
            dateNaissanceLayout.visibility = View.GONE
            genreLayout.visibility = View.GONE
        } else {
            sect.visibility = View.GONE
        }
        spGenre = findViewById(R.id.sp_genre)
        spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.genre))

        btnSignUp.setOnClickListener {
            if (type == "entreprise") {
                if (checkEmail() && checkSect() && checkNom() && checkTel() && checkPass()) {
                    signup()
                }
            } else {
                if (checkUsername() && checkNom() && checkEmail() && checkPrenom() && checkTel() && checkDateNaiss() && checkGenreInput() && checkPass() ) {
                    signup()
                }
            }
        }

        val cgu = findViewById<TextView>(R.id.txt_cgu)
        val cguTxt = "<u>En vous inscrivant vous confirmez avoir lu et accepté nos termes et conditions ainsi que notre politique de confidentialité</u>"
        cgu.text = HtmlCompat.fromHtml(cguTxt, HtmlCompat.FROM_HTML_MODE_LEGACY)
        cgu.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fasobizness.com/uploads/cgu.pdf"))
            startActivity(intent)
        }
    }

    /*private fun updateDate() {
        val format = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(format, Locale.FRENCH)
        dateNaissance.setText(simpleDateFormat.format(myCalendar.time))
//        dateNaissance.editText!!.setText(simpleDateFormat.format(myCalendar.time))
    }*/

    private fun requestMultiplePermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            selectImage()
                            Log.d(tag, "All permissions are granted by user!")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun selectImage() {
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
            Glide.with(this@ActivitySignUp)
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
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun signup() {
        val txEmail = email.editText!!.text.toString()
        val txNomPers = username.editText!!.text.toString()
        val txTel = telephone.editText!!.text.toString()
        val txNom = nom.editText!!.text.toString()
        val txPrenom = prenom.editText!!.text.toString()
        val txSectActivity = sectActivite.editText!!.text.toString()
        val txMdp: String = mdp.editText?.text.toString()
        val txtDateNaissance: String = dateNaissance.text.toString()
        btnSignUp.isEnabled = false
        btnSignUp.setText(R.string.enregistrement_en_cours)
        val call: Call<User> = api.createUser(
                txEmail,
                txTel,
                txNom,
                txPrenom,
                txNomPers,
                txSectActivity,
                txtDateNaissance,
                txMdp,
                type,
                spGenre.selectedItem.toString()
        )
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                btnSignUp.isEnabled = true
                btnSignUp.setText(R.string.enregistrer)
                if (response.isSuccessful) {
                    val user: User? = response.body()
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
                                Toast.makeText(this@ActivitySignUp, R.string.votre_compte_a_ete_supprime, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val auth = user.authorization
                            val jwt = JWT(auth)
                            sharedManager.username = jwt.getClaim("username").asString()
                            sharedManager.user = jwt.getClaim("sub").asString()
                            sharedManager.photo = jwt.getClaim("photo").asString()
                            sharedManager.email = jwt.getClaim("email").asString()
                            sharedManager.type = type
                            sharedManager.token = auth
                            if (images.size > 0) {
                                updateProfilePhoto(images[0].uri, sharedManager.user)
                            } else {
                                val intent = Intent(this@ActivitySignUp, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                    }
                } else {
                    Snackbar.make(linearSignUpBase, response.message(), Snackbar.LENGTH_SHORT).show()
                    Log.d(tag, response.errorBody()?.contentType().toString())
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                Toast.makeText(this@ActivitySignUp, t.message, Toast.LENGTH_SHORT).show()
                btnSignUp.isEnabled = true
                btnSignUp.text = getString(R.string.enregistrer)
            }
        })
    }

    private fun updateProfilePhoto(uri: Uri, id: String) {
        // val parts: MultipartBody.Part = prepareFilePart("file", uri)
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        parts.add(prepareFilePart("file", uri))
        val user = createPart(id)
        val call = api.uploadPhotos(user, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("activity", response.toString())
                if (!response.isSuccessful) {
                    Toast.makeText(this@ActivitySignUp, "Error code: " + response.code(), Toast.LENGTH_SHORT).show()
                    Log.d("activity", response.toString())
                } else {
                    val intent = Intent(this@ActivitySignUp, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("activity", t.toString())
            }
        })
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

    private fun checkEmail(): Boolean {
        val txtEmail = email.editText?.text.toString().trim()
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
        val txtUsername = username.editText?.text.toString().trim()
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
        val txtPrenom = prenom.editText?.text.toString().trim()
        return if (txtPrenom.isEmpty()) {
            prenom.error = getString(R.string.prenom_requis)
            false
        } else {
            prenom.error = null
            true
        }
    }

    private fun checkNom(): Boolean {
        val txtNom = nom.editText?.text.toString().trim()
        return if (txtNom.isEmpty()) {
            nom.error = getString(R.string.nom_requis)
            false
        } else {
            nom.error = null
            true
        }
    }

    private fun checkSect(): Boolean {
        val txtSectAct = sectActivite.editText?.text.toString().trim()
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
        return when {
            txtDateNaiss.isEmpty() -> {
                // dateNaissance.error = getString(R.string.date_de_naissance_requise)
                dateNaissanceTI.error = getString(R.string.date_de_naissance_requise)
                false
            }
            txtDateNaiss == "jj/mm/aaaa" -> {
                // dateNaissance.error = getString(R.string.date_de_naissance_requise)
                dateNaissanceTI.error = getString(R.string.date_de_naissance_requise)
                false
            }
            else -> {
                // dateNaissance.error = null
                dateNaissanceTI.error = null
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

    private fun checkPass(): Boolean {
        val passwd: String = mdp.editText?.text.toString().trim()
        val conf: String = confirm.editText?.text.toString().trim()
        return when {
            passwd.isEmpty() -> {
                mdp.error = getString(R.string.mot_de_passe_requis)
                false
            }
            passwd.length < 6 -> {
                mdp.error = getString(R.string.minimum_6_caracteres)
                false
            }
            conf.isEmpty() -> {
                confirm.error = getString(R.string.mot_de_passe_requis)
                false
            }
            conf != passwd -> {
                confirm.error = getString(R.string.les_mots_de_passe_ne_se_correspondent_pas)
                false
            }
            else -> {
                mdp.error = null
                confirm.error = null
                true
            }
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

    override fun onProgressUpdate(percentage: Int) {
        btnSignUp.setText(R.string.chargement_en_cours)
    }

    override fun onError() {
        btnSignUp.isEnabled = true
        btnSignUp.setText(R.string.enregistrer)
    }

    override fun onFinish() {
        btnSignUp.isEnabled = true
        btnSignUp.setText(R.string.enregistrer)
    }

    override fun uploadStart() {
        btnSignUp.isEnabled = false
        btnSignUp.setText(R.string.enregistrement_en_cours)
    }
}