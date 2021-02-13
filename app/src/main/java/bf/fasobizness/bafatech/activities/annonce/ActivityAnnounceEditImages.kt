package bf.fasobizness.bafatech.activities.annonce

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.adapters.IllustrationEditAdapter
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.IllustrationInterface
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.Announce.Annonce.Illustration
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.FileCompressingUtil
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

class ActivityAnnounceEditImages : AppCompatActivity(), OnItemListener, UploadCallbacks {

    private var tag = "ActivtyAnnounceEdit"
    private lateinit var images: ArrayList<Image>
    private var illustration: ArrayList<Illustration> = ArrayList()
    private var pictures: ArrayList<Uri> = ArrayList()

    private var idAnnonce = ""
    private lateinit var adapter: IllustrationEditAdapter
    private lateinit var no_picture: LinearLayout
    private lateinit var progressDoalog: ProgressDialog
    private lateinit var illustrationInterface: IllustrationInterface

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

        setContentView(R.layout.activity_annonce_edit_photos)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.modification)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        no_picture = findViewById(R.id.layout_no_picture)
        no_picture.setOnClickListener { requestMultiplePermissions() }

        illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)

        images = ArrayList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_photos)
        adapter = IllustrationEditAdapter(this, illustration)
        val manager: LinearLayoutManager = GridLayoutManager(applicationContext, 2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = manager
        adapter.setOnItemListener(this)

        val extras = intent
        val annonce = extras.getSerializableExtra("annonce") as Annonce

        val arrayList: List<Illustration>
        arrayList = annonce.illustrations
        illustration.addAll(arrayList)
        adapter.notifyDataSetChanged()
        idAnnonce = annonce.id_ann
        countPictures()

    }

    private fun countPictures() {
        if (illustration.size == 0) {
            no_picture.visibility = View.VISIBLE
        } else {
            no_picture.visibility = View.GONE
        }
    }

    private fun getAllPictures() {
        images.clear()
        adapter.notifyDataSetChanged()
        if (idAnnonce.isNotEmpty()) {
            val illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)
            val call = illustrationInterface.getIllustrations(idAnnonce)
            call.enqueue(object : Callback<Annonce?> {
                override fun onResponse(call: Call<Annonce?>, response: Response<Annonce?>) {
                    if (response.isSuccessful) {
                        val annonce = response.body()
                        var arrayList: List<Illustration>? = null
                        if (annonce != null) {
                            arrayList = annonce.illustrations
                        }
                        if (arrayList != null) {
                            illustration.addAll(arrayList)
                        }
                    } else {
                        Toast.makeText(this@ActivityAnnounceEditImages, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                    }
                    countPictures()
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<Annonce?>, t: Throwable) {
                    Toast.makeText(this@ActivityAnnounceEditImages, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun prepareFilePart(part: String, image: Image): MultipartBody.Part {
        val file = getFile(this, image.uri)!!
        // val fileCompressingUtil = FileCompressingUtil()
        // val compressedFile = fileCompressingUtil.saveBitmapToFile(file)
        val requestFile = ProgressRequestBody(file, this)
        return MultipartBody.Part.createFormData(part, file.name, requestFile)
    }

    private fun createPart(id_ann: String): RequestBody {
        return RequestBody.create(
                MultipartBody.FORM, id_ann)
    }

    private fun uploadPictures() {
        progressDoalog = ProgressDialog(this)
        progressDoalog.setTitle(R.string.chargement_en_cours)
        progressDoalog.show()
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        for (i in images.indices) {
            parts.add(prepareFilePart("image$i", images[i]))
        }
        val id_ann = createPart(idAnnonce)
        val size = createPart(parts.size.toString() + "")
        val call = illustrationInterface.uploadPhotos(id_ann, size, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d(tag, response.toString())
                if (response.isSuccessful) {
                    progressDoalog.dismiss()
                    Toast.makeText(this@ActivityAnnounceEditImages, R.string.succes, Toast.LENGTH_SHORT).show()
                    getAllPictures()
                } else {
                    Toast.makeText(this@ActivityAnnounceEditImages, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                progressDoalog.dismiss()
                Toast.makeText(this@ActivityAnnounceEditImages, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
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
                    pictures.add(photo.uri)
                } else {
                    // uri
                    Log.d(tag, photo.path)
                }

            }
            uploadPictures()
        }
        super.onActivityResult(requestCode, resultCode, data)   // This line is REQUIRED in fragment mode
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
                }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    override fun onItemClicked(position: Int) {
        val imagesList = ArrayList<String>()
        for (i in illustration.indices) {
            val illustration: Illustration = illustration[i]
            imagesList.add(illustration.nom)
        }
        val intent = Intent(this, ActivityFullScreen::class.java)
        intent.putStringArrayListExtra("images", imagesList)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    override fun onFinish() {
        progressDoalog.dismiss()
    }

    override fun onProgressUpdate(percentage: Int) {
        progressDoalog.setMessage("$percentage%")
    }

    override fun uploadStart() {
        Log.d(tag, "Upload started")
    }

    override fun onError() {
        Log.d(tag, "Failed to upload")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_illustration_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_illustration_add -> requestMultiplePermissions()
            R.id.nav_illustration_remove -> {
                val checked = adapter.checked
                val idList = StringBuilder()
                var i = 0
                while (i < checked.size) {
                    idList.append(checked[i].id_illustration).append(",")
                    illustration.remove(checked[i])
                    adapter.notifyDataSetChanged()
                    i++
                }
                delete(idList.toString())
                checked.clear()
                adapter.notifyDataSetChanged()
                countPictures()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun delete(ids: String) {
        Log.d(tag, ids)
        val call = illustrationInterface.deleteIllustrations(ids)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                Log.d(tag, response.toString())
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d(tag, t.toString())
            }
        })
    }
}