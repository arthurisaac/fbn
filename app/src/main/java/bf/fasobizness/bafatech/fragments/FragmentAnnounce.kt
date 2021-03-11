package bf.fasobizness.bafatech.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.activities.ActivityPhotoList
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnounceFilter
import bf.fasobizness.bafatech.activities.annonce.ActivityUserProfile
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.AppUtils
import bf.fasobizness.bafatech.utils.MySharedManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "id_ann"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAnnounce.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class FragmentAnnounce : Fragment() {
    private var id_ann: String? = null
    // private var param2: String? = null

    private lateinit var ajouterFavori: FloatingActionButton
    private lateinit var images: ArrayList<String>
    private lateinit var imageList: ArrayList<SlideModel>

    private var user: String = ""
    private lateinit var txt_titre_annonce: TextView
    private lateinit var txt_text: TextView
    private lateinit var txt_prix: TextView
    private lateinit var txt_email: TextView
    private lateinit var txt_tel1: TextView
    private lateinit var txt_tel2: TextView
    private lateinit var txt_tel: TextView
    private lateinit var txt_location: TextView
    private lateinit var txt_nom: TextView
    private lateinit var txt_categorie: TextView
    private lateinit var send_message: Button
    private lateinit var send_whatsapp_message: Button
    private lateinit var btn_share: Button
    private lateinit var btn_signaler: Button
    private lateinit var txt_updatedAt: TextView
    private lateinit var txt_date_pub: TextView
    private lateinit var txt_vue: TextView

    // private ImageView iv_affiche;
    private lateinit var txt_photo_util: ImageView
    private lateinit var see_more_annonce: RelativeLayout
    private lateinit var ann: LinearLayout
    private lateinit var layout_no_annonce: LinearLayout
    private lateinit var loading_indicator_ann: LinearLayout
    private lateinit var layout_busy_system: LinearLayout
    private lateinit var layout_ent_offline: LinearLayout
    private lateinit var bottom_buttons: LinearLayout
    private lateinit var btnPlayAudio: ImageButton
    private lateinit var seek_bar: SeekBar
    // private lateinit var audioProgress: ProgressBar

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    //private val mediaPlayer: MediaPlayer
    private var pause: Boolean = false
    private lateinit var audioLayout: LinearLayout

    // layout_ent_offline
    private lateinit var fav: String  // layout_ent_offline
    private lateinit var token: String  // layout_ent_offline
    private lateinit var pager: ImageSlider
    private lateinit var api: API
    private lateinit var audioFile: String
    private val mediaPlayer = MediaPlayer()
    private lateinit var lottieAudio: LottieAnimationView

    val MediaPlayer.seconds:Int
        get() {
            return this.duration / 100
        }

    val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/100
        }

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id_ann = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_announce, container, false)

        api = RetrofitClient.getClient().create(API::class.java)
        val mySharedManager = MySharedManager(context)
        user = mySharedManager.user
        token = "Bearer " + mySharedManager.token

        val rootView = inflater
                .inflate(R.layout.fragment_annonce, container,
                        false) as ViewGroup
        txt_photo_util = rootView.findViewById(R.id.txt_username_logo_ann)
        txt_titre_annonce = rootView.findViewById(R.id.txt_titre_annonce)
        txt_text = rootView.findViewById(R.id.txt_texte_ann)
        txt_prix = rootView.findViewById(R.id.txt_prix_annonce)
        txt_email = rootView.findViewById(R.id.txt_email_util)
        txt_tel1 = rootView.findViewById(R.id.txt_tel1_annonce)
        txt_tel2 = rootView.findViewById(R.id.txt_tel2_annonce)
        txt_tel = rootView.findViewById(R.id.txt_tel_annonce)
        txt_location = rootView.findViewById(R.id.txt_location_annonce)
        txt_nom = rootView.findViewById(R.id.txt_username_ann)
        txt_vue = rootView.findViewById(R.id.txt_vue)
        txt_categorie = rootView.findViewById(R.id.txt_categorie_annonce)
        send_message = rootView.findViewById(R.id.send_direct_message)
        btn_share = rootView.findViewById(R.id.btn_share)
        ajouterFavori = rootView.findViewById(R.id.favorite)
        txt_updatedAt = rootView.findViewById(R.id.txt_date_modification)
        txt_date_pub = rootView.findViewById(R.id.txt_date_pub)
        pager = rootView.findViewById(R.id.flipper_affiche_annonce)
        send_whatsapp_message = rootView.findViewById(R.id.send_whatsapp_message)
        progressBar = rootView.findViewById(R.id.progress_bar)
        progressBar.visibility = View.GONE

        loading_indicator_ann = rootView.findViewById(R.id.loading_indicator_ann)
        // layout_ent_offline = rootView.findViewById(R.id.layout_ent_offline);
        layout_ent_offline = rootView.findViewById(R.id.layout_ent_offline)
        layout_no_annonce = rootView.findViewById(R.id.layout_no_annonce)
        ann = rootView.findViewById(R.id.ann)
        layout_busy_system = rootView.findViewById(R.id.layout_busy_system)
        val btn_refresh = rootView.findViewById<Button>(R.id.btn_refresh)
        btn_refresh.setOnClickListener { getAnnounce(id_ann.toString()) }
        btn_signaler = rootView.findViewById(R.id.signaler_annonce)
        bottom_buttons = rootView.findViewById(R.id.bottom_buttons)
        btnPlayAudio = rootView.findViewById(R.id.btn_play_audio)
        seek_bar = rootView.findViewById(R.id.seek_bar)
        // audioProgress = rootView.findViewById(R.id.audioProgress)

        see_more_annonce = rootView.findViewById(R.id.see_more_annonce)
        audioLayout = rootView.findViewById(R.id.audioLayout)
        audioLayout.visibility = View.GONE

        layout_ent_offline.visibility = View.GONE
        ann.visibility = View.GONE
        loading_indicator_ann.visibility = View.VISIBLE
        layout_no_annonce.visibility = View.GONE
        layout_busy_system.visibility = View.GONE
        ajouterFavori.visibility = View.GONE
        // audioProgress.visibility = View.GONE
        lottieAudio = rootView.findViewById(R.id.audio_lottie)
        lottieAudio.progress = 0.0f
        lottieAudio.visibility = View.GONE

        images = ArrayList()
        imageList = ArrayList()

        if (!id_ann.isNullOrEmpty()) {
            getAnnounce(id_ann.toString())
        }
        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param id_ann Parameter 1.
         * @return A new instance of fragment FragmentAnnounce.
         */
        @JvmStatic
        fun newInstance(id_ann: String) =
                FragmentAnnounce().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, id_ann)
                    }
                }
    }

    private fun getAnnounce(id_ann: String) {
        if (isAdded) {
            layout_ent_offline.visibility = View.GONE
            loading_indicator_ann.visibility = View.VISIBLE
            bottom_buttons.visibility = View.GONE
            val call = api.getAnnounce(id_ann, user)
            call.enqueue(object : Callback<Annonce?> {
                override fun onResponse(call: Call<Annonce?>, response: Response<Annonce?>) {
                    loading_indicator_ann.visibility = View.GONE
                    if (response.isSuccessful) {
                        bottom_buttons.visibility = View.VISIBLE
                        // Log.d(TAG, response.toString());
                        loading_indicator_ann.visibility = View.GONE
                        ann.visibility = View.VISIBLE
                        val announce = response.body()
                        if (announce == null) {
                            layout_ent_offline.visibility = View.GONE
                            ann.visibility = View.GONE
                            layout_no_annonce.visibility = View.VISIBLE
                        } else {
                            populateData(announce)
                        }
                    } else {
                        layout_busy_system.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<Annonce?>, t: Throwable) {
                    layout_ent_offline.visibility = View.VISIBLE
                    ann.visibility = View.GONE
                    layout_no_annonce.visibility = View.GONE
                    loading_indicator_ann.visibility = View.GONE
                    Toast.makeText(context, getString(R.string.pas_d_acces_internet), Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    private fun populateData(annonce: Annonce) {
        if (isAdded) {
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
                        .into(txt_photo_util)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            ajouterFavori.visibility = View.VISIBLE
            if (annonce.texte != null) {
                txt_text.text = annonce.texte
            }
            txt_titre_annonce.text = annonce.titre
            if (annonce.prix.isEmpty() || annonce.prix == null || annonce.prix == "0") {
                txt_prix.setText(R.string.prix_sur_demande)
            } else {
                val prix = annonce.prix + " F CFA"
                txt_prix.text = prix
            }
            // txt_prix.setText(annonce.getPrix());
            txt_email.text = annonce.email
            txt_tel.text = annonce.tel
            txt_tel1.text = annonce.tel1
            txt_tel2.text = annonce.tel2
            txt_location.text = annonce.location
            txt_categorie.text = annonce.categorie
            txt_nom.text = annonce.username
            try {
                val str_share = this.getString(R.string.partager_annonce, annonce.share)
                btn_share.text = str_share
            } catch (e: Exception) {
                e.printStackTrace()
            }

            btn_signaler.setOnClickListener {
                val fragmentSignaler = FragmentSignaler.newInstance()
                val bundle = Bundle()
                bundle.putString("element", "annonce")
                bundle.putString("id_element", annonce.id_ann)
                fragmentSignaler.arguments = bundle
                fragmentManager?.let { fragmentSignaler.show(it, "") }
            }
            see_more_annonce.setOnClickListener { v: View? ->
                val intent = Intent(context, ActivityUserProfile::class.java)
                intent.putExtra("id", annonce.id_per_fk)
                startActivity(intent)
            }
            val updatedAt = annonce.updatedAt
            val str_updatedAt = this.getString(R.string.derni_re_mise_jour_le_1_s, updatedAt)
            if (updatedAt != null) {
                if (!updatedAt.isEmpty()) {
                    txt_updatedAt.text = str_updatedAt
                    txt_updatedAt.visibility = View.VISIBLE
                } else {
                    txt_updatedAt.visibility = View.GONE
                }
            }
            val dt_str = getString(R.string.publiee_le, annonce.date_pub)
            txt_date_pub.text = dt_str
            val _txt_vue = getString(R.string._vues, annonce.vue)
            txt_vue.text = _txt_vue
            if (annonce.categorie == null || annonce.categorie.isEmpty()) {
                txt_categorie.setText(R.string.aucune_categorie_renseignee)
            } else {
                txt_categorie.setOnClickListener { v: View? ->
                    try {
                        val jsonObject = JSONObject()
                        jsonObject.put("categorie", annonce.categorie)
                        val intent = Intent(context, ActivityAnnounceFilter::class.java)
                        intent.putExtra("filter", "multiple")
                        intent.putExtra("params", jsonObject.toString())
                        intent.putExtra("title", annonce.categorie)
                        startActivity(intent)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (annonce.location == null || annonce.location.isEmpty()) {
                txt_location.visibility = View.GONE
            } else {
                txt_location.setOnClickListener {
                    try {
                        val jsonObject = JSONObject()
                        jsonObject.put("location", annonce.location)
                        val intent = Intent(context, ActivityAnnounceFilter::class.java)
                        intent.putExtra("filter", "multiple")
                        intent.putExtra("params", jsonObject.toString())
                        intent.putExtra("title", annonce.location)
                        startActivity(intent)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            fav = if (annonce.favori == null) "0" else annonce.favori
            if (fav == "1") {
                ajouterFavori.setImageResource(R.drawable.ic_star_yellow)
            }
            if (annonce.tel.isEmpty()) {
                txt_tel.visibility = View.GONE
            } else {
                try {
                    txt_tel.setOnClickListener { action(annonce.tel) }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (annonce.tel1.isEmpty()) {
                txt_tel1.visibility = View.GONE
            } else {
                try {
                    txt_tel1.setOnClickListener { action(annonce.tel1) }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (annonce.tel2.isEmpty()) {
                txt_tel2.visibility = View.GONE
            } else {
                try {
                    txt_tel2.setOnClickListener { v: View? -> action(annonce.tel2) }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (annonce.prix.isEmpty()) {
                txt_prix.setText(R.string.prix_sur_demande)
            }
            if (annonce.location.isEmpty()) {
                txt_location.visibility = View.GONE
            }
            if (annonce.location.isEmpty()) {
                txt_location.visibility = View.GONE
            }
            val titre = annonce.titre
            val id_ann = annonce.id_ann
            btn_share.setOnClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBodyText = """Salut, voici une annonce intéressante que je viens de découvrir sur Faso Biz Nèss : 

${annonce.titre} 
                    
${annonce.texte}

Pour en savoir plus, clique ici : https://fasobizness.com/annonce/${annonce.id_ann}.

Si tu n’as pas encore l’application tu peux la télécharger gratuitement sur Playstore : http://bit.ly/AndroidFBN"""
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, titre)
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)))
                share_ann(id_ann)
            }
            ajouterFavori.setOnClickListener {
                if (user.isEmpty()) {
                    val notConnected = FragmentNotConnected.newInstance()
                    fragmentManager?.let { notConnected.show(it, "") }
                } else {
                    addFavorite(id_ann)
                }
            }
            send_message.setOnClickListener {
                if (user.isEmpty()) {
                    val notConnected = FragmentNotConnected.newInstance()
                    fragmentManager?.let { notConnected.show(it, "") }
                } else {
                    createDiscussion(annonce.id_per_fk, id_ann)
                }
            }
            if (annonce.whatsapp != null) {
                send_whatsapp_message.setOnClickListener {
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
            try {
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
                        // val intent = Intent(context, ActivityFullScreen::class.java)
                        if (images.size > 1) {
                            val intent = Intent(context, ActivityPhotoList::class.java)
                            intent.putStringArrayListExtra("images", images)
                            intent.putExtra("position", position)
                            startActivity(intent)
                        } else {
                            val intent = Intent(context, ActivityFullScreen::class.java)
                            intent.putStringArrayListExtra("images", images)
                            intent.putExtra("position", position)
                            startActivity(intent)
                        }
                    }
                })
                if (annonce.audio != null) {
                    lottieAudio.visibility = View.VISIBLE
                    // audioProgress.visibility = View.VISIBLE
                    // Enabling database for resume support even after the application is killed:

                    // Enabling database for resume support even after the application is killed:
                    val config = PRDownloaderConfig.newBuilder()
                            .setDatabaseEnabled(true)
                            .build()
                    PRDownloader.initialize(context, config)

                    val uri = Uri.parse(annonce.audio)
                    val dirPath = AppUtils.getRootDirPath(context) + "/"
                    //val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/"
                    Dexter.withContext(context)
                            .withPermissions(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                            .withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                    if (report.areAllPermissionsGranted()) {
                                        PRDownloader.download(annonce.audio, dirPath, uri.lastPathSegment)
                                                .build()
                                                .setOnStartOrResumeListener { }
                                                .setOnPauseListener { }
                                                .setOnCancelListener {}
                                                .setOnProgressListener {
                                                    val progressPercent: Long = it.currentBytes * 100 / it.totalBytes
                                                    // audioProgress.progress = progressPercent.toInt()
                                                    lottieAudio.progress = progressPercent.toFloat()
                                                }
                                                .start(object : OnDownloadListener {

                                                    override fun onDownloadComplete() {
                                                        audioFile = dirPath + uri.lastPathSegment
                                                        Log.d("Lien de audio", audioFile)
                                                        mediaPlayer.setDataSource(audioFile)
                                                        mediaPlayer.prepare()
                                                        audioPermission()
                                                    }

                                                    override fun onError(error: com.downloader.Error?) {
                                                        //Log.d("audio downloading", error.toString())
                                                        Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                                    token.continuePermissionRequest()
                                }
                            }).withErrorListener { Toast.makeText(context, "Erreur de permission! ", Toast.LENGTH_SHORT).show() }
                            .onSameThread()
                            .check()



//                    try {
//                        audioProgress.visibility = View.VISIBLE
//                        val uri = Uri.parse(annonce.audio)
//                        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
//                        val request = DownloadManager.Request(uri)
//                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                        request.setTitle("Faso Biz Ness")
//                        request.setDescription("Téléchargement")
//                        request.allowScanningByMediaScanner()
//                        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                        //request.setDestinationInExternalPublicDir("/FasoBizNess", uri.lastPathSegment)
//                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
//                        request.setMimeType("audio/*")
//                        downloadManager?.enqueue(request)
//                        audioFile = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + uri.lastPathSegment
//                        mediaPlayer.setDataSource(audioFile)
//                        mediaPlayer.prepare()
//
//                        context?.registerReceiver(attachmentDownloadCompleteReceive, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//                    } catch (e: java.lang.Exception) {
//                        audioProgress.visibility = View.GONE
//                        e.printStackTrace()
//                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

//    private var attachmentDownloadCompleteReceive: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
//                audioProgress.visibility = View.GONE
//                audioPermission()
//            }
//        }
//    }

    fun audioPermission() {
        btnPlayAudio.visibility = View.VISIBLE
        audioLayout.visibility = View.VISIBLE
        lottieAudio.visibility = View.GONE
        // audioProgress.visibility = View.GONE
        btnPlayAudio.setOnClickListener {
            Dexter.withContext(context)
                    .withPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                playAudio()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }
                    }).withErrorListener { Toast.makeText(context, "Erreur de permission! ", Toast.LENGTH_SHORT).show() }
                    .onSameThread()
                    .check()
        }
    }

    fun playAudio() {
        mediaPlayer.start()
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer.seekTo(i * 100)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        seek_bar.max = mediaPlayer.seconds
        runnable = Runnable {
            seek_bar.progress = mediaPlayer.currentSeconds
            handler.postDelayed(runnable, 100)
        }
        handler.postDelayed(runnable, 100)
    }

    private fun createDiscussion(receiver_id: String, id_ann: String) {
        if (user.isEmpty()) {
            val notConnected = FragmentNotConnected.newInstance()
            fragmentManager?.let { notConnected.show(it, "") }
        } else {
            progressBar.visibility = View.VISIBLE
            send_message.isEnabled = false
            val call = api.createDiscussion(receiver_id, id_ann, token)
            call.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                    progressBar.visibility = View.GONE
                    send_message.isEnabled = true
                    if (response.isSuccessful) {
                        val discussion_id: Int
                        if (response.body() != null) {
                            discussion_id = response.body()!!.id
                            val intent = Intent(context, DefaultMessagesActivity::class.java)
                            intent.putExtra("discussion_id", discussion_id.toString())
                            intent.putExtra("receiver_id", receiver_id)
                            intent.putExtra("id_ann", id_ann)
                            intent.putExtra("new", "1")
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(context, R.string.le_serveur_est_occupe, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    send_message.isEnabled = true
                    Toast.makeText(context, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun action(numero: String) {
        try {
            val items = arrayOf<CharSequence>("Composer numéro", "Envoyer SMS")
            val builder = context?.let { AlertDialog.Builder(it) }
            builder?.setTitle(getString(R.string.choisir_action))
            builder?.setItems(items) { dialog: DialogInterface, i: Int ->
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
                builder.show()
            }

        } catch (e: Exception) {
            compose(numero)
        }
    }

    private fun compose(numero: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null))
        startActivity(intent)
    }

    private fun sendSMS() {
        val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context) // Need to change the build to API 19
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "text")
        if (defaultSmsPackageName != null) {
            sendIntent.setPackage(defaultSmsPackageName)
        }
        startActivity(sendIntent)
    }

    private fun sendEmail(emailTo: String, titre: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailTo, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titre)
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre message")
        startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."))
    }

    private fun share_ann(id_ann: String) {
        val call: Call<MyResponse> = api.setAnnouncesActions("share", id_ann, user)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    Snackbar.make(ann, "Shared response $response", Snackbar.LENGTH_SHORT)
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Toast.makeText(context, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFavorite(id_ann: String) {
        fav = when (fav) {
            "1" -> {
                ajouterFavori.setImageResource(R.drawable.ic_star_white)
                "0"
            }
            else -> {
                ajouterFavori.setImageResource(R.drawable.ic_star_yellow)
                "1"
            }
        }
        val call: Call<MyResponse> = api.setAnnouncesActions("favorite", id_ann, user)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    Snackbar.make(ann, getString(R.string.favoris_ajoute), Snackbar.LENGTH_SHORT)
                } else {
                    Snackbar.make(ann, getString(R.string.une_erreur_sest_produite), Snackbar.LENGTH_SHORT)
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Toast.makeText(context, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }
}