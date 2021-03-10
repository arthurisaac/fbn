package bf.fasobizness.bafatech.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.Advertising.Ads
import bf.fasobizness.bafatech.models.MyResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "id"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAdvertising.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAdvertising : Fragment() {
    private var id: String? = null

    // private static final String TAG = "ActivityDetailsPub";
    private lateinit var wb_description_ad: WebView
    private lateinit var description: TextView
    private lateinit var vue: TextView
    private lateinit var partage: TextView
    private lateinit var tv_appel: ImageView
    private lateinit var tv_whatsapp: ImageView
    private lateinit var tv_facebook: ImageView
    private lateinit var ad_layout: LinearLayout
    private lateinit var loading_indicator_ad: LinearLayout
    private lateinit var layout_ent_offline: LinearLayout
    private lateinit var layout_busy_system: LinearLayout
    private var api: API = RetrofitClient.getClient().create(API::class.java)
    private lateinit var imageList: ArrayList<SlideModel>
    private lateinit var images: ArrayList<String>
    private lateinit var pager: ImageSlider
    private lateinit var btn_share: Button
    private lateinit var btn_refresh: Button
    private lateinit var affiche: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_advertising, container, false)
        val rootView = inflater
                .inflate(R.layout.fragment_advertising, container,
                        false) as ViewGroup

        affiche = rootView.findViewById(R.id.iv_affiche_ad)
        description = rootView.findViewById(R.id.tv_description_ad)
        vue = rootView.findViewById(R.id.txt_vue)
        partage = rootView.findViewById(R.id.txt_share)
        wb_description_ad = rootView.findViewById(R.id.wb_description_ad)
        ad_layout = rootView.findViewById(R.id.ad_layout)
        loading_indicator_ad = rootView.findViewById(R.id.loading_indicator_ad)
        layout_busy_system = rootView.findViewById(R.id.layout_busy_system)
        layout_ent_offline = rootView.findViewById(R.id.layout_ent_offline)
        btn_refresh = rootView.findViewById(R.id.btn_refresh)
        btn_share = rootView.findViewById(R.id.btn_share)
        pager = rootView.findViewById(R.id.flipper_affiche_annonce)

        tv_appel = rootView.findViewById(R.id.tv_appel)
        tv_facebook = rootView.findViewById(R.id.tv_facebook)
        tv_whatsapp = rootView.findViewById(R.id.tv_whatsapp)

        //val imagesList = ArrayList<String?>()
        imageList = ArrayList()
        images = ArrayList()

        /*if (extras.getStringExtra("lien") != null) {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                    )
                    .asBitmap()
                    .load(extras.getStringExtra("lien"))
                    .thumbnail(0.1f)
                    .into(affiche)
            imagesList.add(extras.getStringExtra("lien"))
            affiche.setOnClickListener { v: View? ->
                val intent = Intent(this, ActivityFullScreen::class.java)
                intent.putStringArrayListExtra("images", imagesList)
                intent.putExtra("position", 0)
                startActivity(intent)
            }
        }*/

        id?.let { getPub(it) }
        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param id Parameter 1.
         * @return A new instance of fragment FragmentAdvertising.
         */
        @JvmStatic
        fun newInstance(id: String) =
                FragmentAdvertising().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, id)
                    }
                }
    }

    private fun getPub(id: String) {
        if(isAdded) {
            ad_layout.visibility = View.GONE
            loading_indicator_ad.visibility = View.VISIBLE
            layout_busy_system.visibility = View.GONE
            layout_ent_offline.visibility = View.GONE
            val call = api.getAd(Integer.valueOf(id))
            call.enqueue(object : Callback<Ads?> {
                override fun onResponse(call: Call<Ads?>, response: Response<Ads?>) {
                    loading_indicator_ad.visibility = View.GONE
                    if (response.isSuccessful) {
                        val ad = response.body()
                        if (ad != null) {
                            ad_layout.visibility = View.VISIBLE
                            populateData(ad)
                        }
                    } else {
                        layout_busy_system.visibility = View.VISIBLE
                        Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Ads?>, t: Throwable) {
                    loading_indicator_ad.visibility = View.GONE
                    layout_ent_offline.visibility = View.VISIBLE
                    // Toast.makeText(ActivityDetailsPub.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            })
        }
    }

    private fun populateData(ad: Ads) {
        val desc = ad.description
        if (ad.description == null) {
            description.visibility = View.GONE
        } else {
            try {
                val d = HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_LEGACY)
                description.text = d
                val encodedHtml = ad.description + "<style type=\"text/css\">@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@200;500&display=swap'); body {font-family: 'Raleway';}</style>"
                wb_description_ad.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        view.context.startActivity(intent)
                        return true
                    }
                }
                wb_description_ad.loadData(encodedHtml, "text/html", "UTF-8")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (ad.lien != null) {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                    )
                    .asBitmap()
                    .load(ad.lien)
                    .thumbnail(0.1f)
                    .into(affiche)
        }
        if (ad.vue == null) {
            vue.visibility = View.GONE
        } else {
            vue.text = getString(R.string._vues, ad.vue)
        }
        if (ad.shared == null) {
            partage.visibility = View.GONE
        } else {
            partage.text = getString(R.string._partages, ad.shared)
        }
        if (ad.appel != null) {
            // tv_appel.setText(ad.getAppel());
            tv_appel.visibility = View.VISIBLE
            tv_appel.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", ad.appel, null))
                startActivity(intent)
            }
        }
        if (ad.facebook != null) {
            tv_facebook.visibility = View.VISIBLE
            // tv_facebook.setText(ad.getFacebook());
            tv_facebook.setOnClickListener {
                try {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(ad.facebook)
                    startActivity(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (ad.whatsapp != null) {
            try {
                var waMessage = "Bonjour, j’ai vu votre affiche sur *Faso Biz Nèss* et je voudrais avoir plus d’informations…"
                waMessage = waMessage.replace(" ", "%20")
                val link = "https://wa.me/" + ad.whatsapp + "?text=" + waMessage
                tv_whatsapp.visibility = View.VISIBLE
                // tv_whatsapp.setText(ad.getWhatsapp());
                tv_whatsapp.setOnClickListener {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(link)
                    startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
            }
        }
        btn_share.setOnClickListener {
            try {
                val d = HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_COMPACT)
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                // val shareBodyText = d  getString(R.string.telecharger_et_partager_l_application) TODO
                val shareBodyText = getString(R.string.telecharger_et_partager_l_application)
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "")
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.partager_avec)))
                shareAd(ad.id)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show()
            }
        }
        val afficheList = ad.getAffiches()
        try {
            for (affiche in afficheList) {
                imageList.add(SlideModel(affiche.nom))
                images.add(affiche.nom)
            }
            if (imageList.size == 0) {
                pager.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pager.setImageList(imageList, true)
        pager.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                val intent = Intent(context, ActivityFullScreen::class.java)
                intent.putStringArrayListExtra("images", images)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        })
    }

    private fun shareAd(id: String?) {
        if (id != null) {
            val api = RetrofitClient.getClient().create(API::class.java)
            val call = api.shareAd(Integer.valueOf(id))
            call.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                    if (response.isSuccessful) {
                        Log.d(tag, response.toString())
                    }
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.d(tag, t.toString())
                }
            })
        }
    }
}