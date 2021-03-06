package bf.fasobizness.bafatech.activities.annonce

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
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
import bf.fasobizness.bafatech.helper.GifSizeFilter
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.IllustrationInterface
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.Announce.Annonce.Illustration
import bf.fasobizness.bafatech.models.MyResponse
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.utils.PathUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class ActivityAnnonceEditPhotos : AppCompatActivity(), OnItemListener, UploadCallbacks {
    private lateinit var images: ArrayList<Illustration>

    //private lateinit var uri_images: ArrayList<Uri>
    private var localImages: ArrayList<Image> = ArrayList()

    // private UriAdapter mAdapter;
    private var id_annonce = ""
    private lateinit var adapter: IllustrationEditAdapter

    // private RequestQueue requestQueue;
    private lateinit var no_picture: LinearLayout
    private lateinit var progressDoalog: ProgressDialog
    private lateinit var illustrationInterface: IllustrationInterface
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
        localImages = ArrayList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_photos)
        adapter = IllustrationEditAdapter(this, images)
        val manager: LinearLayoutManager = GridLayoutManager(applicationContext, 2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = manager
        adapter.setOnItemListener(this)
        val extras = intent
        val annonce = extras.getSerializableExtra("annonce") as Annonce?
        val arrayList: List<Illustration>
        if (annonce != null) {
            arrayList = annonce.illustrations
            images.addAll(arrayList)
            adapter.notifyDataSetChanged()
            id_annonce = annonce.id_ann
        }
        countPictures()
    }

    private fun countPictures() {
        if (images.size == 0) {
            no_picture.visibility = View.VISIBLE
        } else {
            no_picture.visibility = View.GONE
        }
    }

    override fun onItemClicked(position: Int) {
        val imagesList = ArrayList<String>()
        for (i in images.indices) {
            val illustration = images[i]
            imagesList.add(illustration.nom)
        }
        val intent = Intent(this, ActivityFullScreen::class.java)
        intent.putStringArrayListExtra("images", imagesList)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_illustration_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_illustration_add) {
            requestMultiplePermissions()
        } else if (id == R.id.nav_illustration_remove) {
            val checked = adapter.checked
            val idList = StringBuilder()
            for (i in checked.indices) {
                idList.append(checked[i].id_illustration).append(",")
                images.remove(checked[i])
                adapter.notifyDataSetChanged()
            }
            delete(idList.toString())
            checked.clear()
            adapter.notifyDataSetChanged()
            countPictures()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun delete(ids: String) {
        Log.d("Activity", ids)
        val call = illustrationInterface.deleteIllustrations(ids)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                Log.d("Activity", response.toString())
                Toast.makeText(this@ActivityAnnonceEditPhotos, "Super", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d("Activty", t.toString())
            }
        })
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
                .setSelectedImages(localImages)
                .setRequestCode(100)
                .setShowCamera(false)
                .start()
        /*Matisse.from(this@ActivityAnnonceEditPhotos)
                .choose(MimeType.ofAll(), false) // .choose(MimeType.ofImage(), false)
                .theme(R.style.Matisse_Dracula)
                .countable(true)
                .capture(false)
                .captureStrategy(
                        CaptureStrategy(true, "bf.fasobizness.bafatech.fileprovider", "Pictures"))
                .maxSelectable(9)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                        resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(GlideEngine())
                .showSingleMediaType(true)
                .originalEnable(true)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .forResult(REQUEST_CODE)*/
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            images.clear()
            adapter.clearAll()
            adapter.notifyDataSetChanged()
            localImages.addAll(ImagePicker.getImages(data))

            images.clear()
            adapter.clearAll()
            // uploadPictures()
            // adapter.notifyDataSetChanged()
        }
        /*if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                uri_images.addAll(Matisse.obtainResult(data))
                uploadPictures()
            }
        }*/
    }

    override fun onProgressUpdate(percentage: Int) {
        // Toast.makeText(this, percentage+"%", Toast.LENGTH_SHORT).show();
        progressDoalog.setMessage("$percentage%")
    }

    override fun onError() {}
    override fun onFinish() {
        progressDoalog.dismiss()
    }

    private fun allPictures() {
        localImages.clear()
        images.clear()
        adapter.clearAll()
        adapter.notifyDataSetChanged()
        if (id_annonce.isNotEmpty()) {
            val illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface::class.java)
            val call = illustrationInterface.getIllustrations(id_annonce)
            call.enqueue(object : Callback<Annonce?> {
                override fun onResponse(call: Call<Annonce?>, response: Response<Annonce?>) {
                    if (response.isSuccessful) {
                        val annonce = response.body()
                        var arrayList: List<Illustration>? = null
                        if (annonce != null) {
                            arrayList = annonce.illustrations
                        }
                        if (arrayList != null) {
                            images.addAll(arrayList)
                        }
                    } else {
                        Toast.makeText(this@ActivityAnnonceEditPhotos, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                    }
                    countPictures()
                    adapter!!.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<Annonce?>, t: Throwable) {
                    Toast.makeText(this@ActivityAnnonceEditPhotos, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun uploadStart() {}
    private fun prepareFilePart(part: String, uri: Uri): MultipartBody.Part {
        val file = getFile(this, uri)!!
        // FileCompressingUtil fileCompressingUtil = new FileCompressingUtil();
        // File compressedFile = fileCompressingUtil.saveBitmapToFile(file);
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
        localImages.clear()
        images.clear()
        adapter.clearAll()
        adapter.notifyDataSetChanged()
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        for (i in localImages.indices) {
            parts.add(prepareFilePart("image$i", localImages[i].uri))
        }
        val id_ann = createPart(id_annonce)
        val size = createPart(parts.size.toString() + "")
        val call = illustrationInterface.uploadPhotos(id_ann, size, parts)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("ActivityEdit", response.toString())
                if (response.isSuccessful) {
                    progressDoalog.dismiss()
                    allPictures()
                } else {
                    Toast.makeText(this@ActivityAnnonceEditPhotos, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                progressDoalog.dismiss()
                Toast.makeText(this@ActivityAnnonceEditPhotos, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val REQUEST_CODE = 23
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
    }
}