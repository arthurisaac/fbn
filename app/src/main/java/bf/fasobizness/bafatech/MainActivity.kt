package bf.fasobizness.bafatech

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bf.fasobizness.bafatech.activities.*
import bf.fasobizness.bafatech.activities.annonce.*
import bf.fasobizness.bafatech.activities.entreprise.ActivityEntreprisesUne
import bf.fasobizness.bafatech.activities.recrutement.ActivityRecrutements
import bf.fasobizness.bafatech.activities.user.ActivityProfile
import bf.fasobizness.bafatech.activities.user.LoginActivity
import bf.fasobizness.bafatech.activities.user.messaging.ActivityDiscussions
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.fragments.FragmentMaintenance
import bf.fasobizness.bafatech.fragments.FragmentNotConnected
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import bf.fasobizness.bafatech.interfaces.OnImageListener
import bf.fasobizness.bafatech.interfaces.OnLongItemListener
import bf.fasobizness.bafatech.models.Advertising
import bf.fasobizness.bafatech.models.Advertising.Ads
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.models.User
import bf.fasobizness.bafatech.utils.MySharedManager
import bf.fasobizness.bafatech.utils.RemoteConfigUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnAnnonceListener, OnImageListener, OnLongItemListener {
    private val tag = "MainActivity"
    private lateinit var drawer: DrawerLayout
    private lateinit var badgeDiscussions: TextView
    private lateinit var user: String
    private var page = 1
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var loadingIndicator: LinearLayout
    private lateinit var layoutNoAnnonce: LinearLayout
    private var filtre = "id"
    private var arguments = " "
    private lateinit var mAnnonceAdapter: AnnounceAdapter
    private lateinit var mAnnonces: ArrayList<Annonce>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private lateinit var imageSlider: ImageSlider
    private lateinit var images: ArrayList<Ads>
    private lateinit var imageList: ArrayList<SlideModel>

    // private ArrayList<String> imageList;
    // private ImagesAdapter imagesAdapter;
    private var api: API = RetrofitClient.getClient().create(API::class.java)
    private lateinit var sharedManager: MySharedManager
    private var doubleBackToExitPressedOnce = false
    override fun onStart() {
        super.onStart()
        if (intent.extras != null) {
            if (intent.getStringExtra("discussion_id") != null) {
                val receiverId = intent.getStringExtra("receiver_id")
                val discussionId = intent.getStringExtra("discussion_id")
                val intent = Intent(this, DefaultMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("discussion_id", discussionId)
                intent.putExtra("receiver_id", receiverId)
                startActivity(intent)
            }
            api = RetrofitClient.getClient().create(API::class.java)
            checkUser()
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d(tag, "Key = " + key + "value = " + value)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.annonces)
        setSupportActionBar(toolbar)
        val fab = findViewById<Button>(R.id.fab)
        val fabUp = findViewById<FloatingActionButton>(R.id.fab_up)
        fab.setOnClickListener {
            if (user.isEmpty()) {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            } else {
                startActivity(Intent(this@MainActivity, ActivityNewAnnounce::class.java))
            }
        }
        fabUp.setOnClickListener {
            mRecyclerView.layoutManager?.smoothScrollToPosition(mRecyclerView, null, 0)
        }
        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        sharedManager = MySharedManager(this)
        toggle.syncState()
        val btnFiltrer = findViewById<Button>(R.id.btn_filtrer)
        val btnReset = findViewById<Button>(R.id.btn_reset)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val navigationView2 = findViewById<NavigationView>(R.id.nav_view2)
        val btnValider = navigationView2.findViewById<Button>(R.id.btn_valider)
        val edMin = navigationView2.findViewById<EditText>(R.id.ed_min)
        val edMax = navigationView2.findViewById<EditText>(R.id.ed_max)
        val spVille = findViewById<Spinner>(R.id.sp_ville_annonce)
        val spCategorie = findViewById<Spinner>(R.id.sp_categorie_annonce)
        spVille.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.villes))
        spCategorie.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.categories))
        btnReset.setOnClickListener {
            edMin.editableText.clear()
            edMax.editableText.clear()
            spCategorie.setSelection(0)
            spVille.setSelection(0)
        }
        btnValider.setOnClickListener {
            val txtMin = edMin.text.toString()
            val txtMax = edMax.text.toString()
            var cat = spCategorie.selectedItem.toString()
            var ville = spVille.selectedItem.toString()

            // Advanced request
            if (ville == "Toutes les villes") {
                ville = ""
            }
            if (cat == "Toutes les catégories") {
                cat = ""
            }
            filtre = "multiple"
            val jsonObject = JSONObject()
            if (ville.isNotEmpty()) {
                try {
                    jsonObject.put("location", ville)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (cat.isNotEmpty()) {
                try {
                    jsonObject.put("categorie", cat)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (txtMin.isNotEmpty() && txtMax.isNotEmpty()) {
                try {
                    val jo = JSONObject()
                    jo.put("min", txtMin)
                    jo.put("max", txtMax)
                    jsonObject.put("prix", jo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            arguments = jsonObject.toString()
            val intent = Intent(this, ActivityAnnounceFilter::class.java)
            intent.putExtra("filter", filtre)
            intent.putExtra("params", arguments)
            startActivity(intent)
        }
        btnFiltrer.setOnClickListener { openDrawer() }
        navigationView.setNavigationItemSelectedListener(this)
        val hv = navigationView.getHeaderView(0)
        val menu = navigationView.menu
        val navPost = menu.findItem(R.id.nav_my_post)
        // MenuItem nav_chat = menu.findItem(R.id.nav_chat);
        val photoProfil = hv.findViewById<ImageView>(R.id.photoProfilNav)
        val txtLoggedUsername = hv.findViewById<TextView>(R.id.txt_logged_username)
        val txtLoggedEmail = hv.findViewById<TextView>(R.id.txt_logged_email)
        val btnPromouvoir = findViewById<RelativeLayout>(R.id.promouvoir_btn)

        val username = sharedManager.username
        val email = sharedManager.email

        btnPromouvoir.setOnClickListener {
            if (user.isNotEmpty()) {
                startActivity(Intent(this, ActivityPromouvoirAnnonces::class.java))
            } else {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        }
        drawer.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                if (user.isNotEmpty()) {

                    txtLoggedEmail.text = email
                    txtLoggedEmail.visibility = View.VISIBLE
                    txtLoggedUsername.text = username
                    val photo = sharedManager.photo
                    navPost.isVisible = true
                    Glide.with(this@MainActivity)
                            .setDefaultRequestOptions(
                                    RequestOptions()
                                            .placeholder(R.drawable.user)
                                            .error(R.drawable.user)
                                            .centerCrop()
                            )
                            .asBitmap()
                            .load(photo)
                            .thumbnail(0.85f)
                            .into(photoProfil)
                } else {
                    navPost.isVisible = false
                    txtLoggedEmail.visibility = View.GONE
                    txtLoggedUsername.text = getString(R.string.connectez_vous_ici)
                }
                photoProfil.setOnClickListener {
                    if (user.isEmpty()) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, ActivityProfile::class.java))
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
        val tabChat = findViewById<TextView>(R.id.tab_chat)
        val tabBoutiques = findViewById<TextView>(R.id.tab_boutiques)
        val tabOffreEnOr = findViewById<TextView>(R.id.tab_offre_en_or)
        val tabEntreprise = findViewById<TextView>(R.id.tab_entrepise)
        val tabCategorie = findViewById<TextView>(R.id.tab_categorie)
        badgeDiscussions = findViewById(R.id.badge_discussions)
        tabBoutiques.setOnClickListener { startActivity(Intent(this, ActivityBoutique::class.java)) }
        tabOffreEnOr.setOnClickListener { startActivity(Intent(this, ActivityOffreOr::class.java)) }
        tabEntreprise.setOnClickListener { startActivity(Intent(this, ActivityEntreprisesUne::class.java)) }
        tabCategorie.setOnClickListener { startActivity(Intent(this, ActivityAnnonceCategory::class.java)) }
        imageSlider = findViewById(R.id.flipper)
        images = ArrayList()
        imageList = ArrayList()

        // AdapterViewFlipper pager = findViewById(R.id.flipper);
        // pager = findViewById(R.id.flipper);
        // imagesAdapter = new ImagesAdapter(this, imageList);
        // advertisingAdapter = new AdvertisingAdapter(this, images);
        // pager.setAdapter(advertisingAdapter);

        // pager.setAdapter(imagesAdapter);
        // imagesAdapter.setOnImageListener(this);
        tabChat.setOnClickListener {
            if (user.isNotEmpty()) {
                badgeDiscussions.visibility = View.GONE
                startActivity(Intent(this@MainActivity, ActivityDiscussions::class.java))
            } else {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
                // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        }

        // Fetch annonces
        mAnnonces = ArrayList()
        mRecyclerView = findViewById(R.id.annonces_card_view)
        val manager: LinearLayoutManager = GridLayoutManager(applicationContext, 2)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mAnnonceAdapter = AnnounceAdapter(this@MainActivity, mAnnonces)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter.setOnItemListener(this)
        mAnnonceAdapter.setOnLongItemListener(this)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    fab.visibility = View.VISIBLE
                    fabUp.visibility = View.VISIBLE
                } else if (dy > 0) {
                    fab.visibility = View.GONE
                    fabUp.visibility = View.GONE
                }
            }
        })
        loadingIndicator = findViewById(R.id.loading_indicator)
        loadingIndicator.visibility = View.GONE

        //Loading more data
        mAnnonceAdapter.setOnBottomReachedListener {
            loadingIndicator.visibility = View.VISIBLE
            jsonParse()
        }

        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer)
        mSwipeRefreshLayout.setOnRefreshListener { refresh() }
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        layoutEntOffline.visibility = View.GONE
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { refresh() }
        layoutNoAnnonce = findViewById(R.id.layout_no_annonce)
        layoutNoAnnonce.visibility = View.GONE
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        layoutBusySystem.visibility = View.GONE
        shimmerViewContainer = findViewById(R.id.shimmer_view_container)
        shimmerViewContainer.visibility = View.VISIBLE
        val searchLayout = findViewById<LinearLayout>(R.id.search_layout)
        searchLayout.setOnClickListener { startActivity(Intent(this, ActivitySearchAnnonce::class.java)) }

        user = sharedManager.user
        checkUser()
        jsonParse()
        ads()
        if (user.isNotEmpty()) {
            checkNewMessage()
        }
        RemoteConfigUtils.init()
        checkVersion()
        // showMaintenance()
    }

    private fun checkUser() {
        val token = sharedManager.token
        val id = sharedManager.user
        user = sharedManager.user
        getNewJWTToken(id, token)
    }

    private fun checkVersion() {
        try {
            val pInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            val url = RemoteConfigUtils.getUpdateUrl()
            val version = pInfo.versionName
            if (version != RemoteConfigUtils.getMinVersion()) {
                val dialog: AlertDialog = AlertDialog.Builder(this)
                        .setTitle("Nouvelle version disponible")
                        .setMessage("Nous vous recommandons d'installer la dernière version de l'application Faso Biz Nèss.")
                        .setPositiveButton("Mettre à jour") { _, _ -> redirectStore(url) }
                        .create()
                dialog.show()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun redirectStore(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showMaintenance() {
        val fragmentMaintenance = FragmentMaintenance.newInstance("100")
        fragmentMaintenance.show(supportFragmentManager, "")
    }

    private fun getNewJWTToken(id: String, token: String) {
        val call = api.updateToken(id, "Bearer $token")
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.status) {
                            user = ""
                            sharedManager.username = ""
                            sharedManager.email = ""
                            sharedManager.photo = ""
                            sharedManager.type = ""
                            sharedManager.user = ""
                            sharedManager.user = ""
                            sharedManager.token = ""
                        } else {
                            sharedManager.token = response.body()!!.authorization
                        }
                    }
                } else {
                    Log.d(tag, "Failed refresh token")
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                Log.d(tag, t.toString())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    private fun openDrawer() {
        when {
            drawer.isDrawerOpen(GravityCompat.END) -> {
                drawer.closeDrawer(GravityCompat.END)
            }
            else -> {
                drawer.openDrawer(GravityCompat.END)
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_annonce -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.nav_faq -> {
                startActivity(Intent(this, ActivityInfos::class.java))
                return true
            }
            R.id.nav_recrutement -> {
                startActivity(Intent(this, ActivityRecrutements::class.java))
                return true
            }
            R.id.nav_suggestion -> {
                startActivity(Intent(this, ActivitySuggestion::class.java))
                return true
            }
            R.id.nav_my_post -> {
                startActivity(Intent(this, ActivityAnnoncesPublished::class.java))
                return true
            }
            R.id.nav_partager -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.telecharger_et_partager_l_application_recommender))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                return true
            }
            else -> {
                val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_favori -> if (user.isNotEmpty()) {
                startActivity(Intent(this, ActivityFavorite::class.java))
                return true
            } else {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            }
            R.id.navActualiser -> refresh()
            R.id.navHautPage -> mRecyclerView.layoutManager?.smoothScrollToPosition(mRecyclerView, null, 1)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refresh() {
        page = 1
        mAnnonceAdapter.clearAll()
        mAnnonces.clear()
        mAnnonceAdapter.notifyDataSetChanged()
        shimmerViewContainer.visibility = View.VISIBLE
        shimmerViewContainer.startShimmer()
        jsonParse()
        // ads();
        if (user.isNotEmpty()) {
            checkNewMessage()
        }
    }

    private fun checkNewMessage() {
        val call = api.getUnread("Bearer " + sharedManager.token)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    val myResponse = response.body()
                    if (myResponse != null) {
                        val count = myResponse.count
                        if (count > 0) {
                            badgeDiscussions.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.d(tag, response.toString())
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d(tag, t.toString())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 6) {
            if (resultCode == RESULT_CANCELED) {
                refresh()
            }
        }
    }

    private fun jsonParse() {
        layoutEntOffline.visibility = View.GONE
        layoutBusySystem.visibility = View.GONE
        layoutNoAnnonce.visibility = View.GONE
        val call = api.getAnnounces(page.toString(), user)
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                shimmerViewContainer.visibility = View.GONE
                shimmerViewContainer.stopShimmer()
                mSwipeRefreshLayout.isRefreshing = false
                loadingIndicator.visibility = View.GONE
                val announce = response.body()
                var annonces: List<Annonce>? = null
                if (announce != null) {
                    annonces = announce.annonces
                }
                if (annonces != null) {
                    mAnnonces.addAll(annonces)
                }
                if (mAnnonces.size == 0) {
                    layoutNoAnnonce.visibility = View.VISIBLE
                    layoutEntOffline.visibility = View.GONE
                    loadingIndicator.visibility = View.GONE
                }
                page++
                mAnnonceAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Announce?>, t: Throwable) {

                if (mAnnonces.size == 0) {
                    layoutEntOffline.visibility = View.VISIBLE
                }
                mSwipeRefreshLayout.isRefreshing = false
                shimmerViewContainer.visibility = View.GONE
                shimmerViewContainer.stopShimmer()
                loadingIndicator.visibility = View.GONE
            }
        })
    }

    private fun ads() {
        val call = api.ads
        call.enqueue(object : Callback<Advertising?> {
            override fun onResponse(call: Call<Advertising?>, response: Response<Advertising?>) {
                val advertising = response.body()
                var ads: List<Ads>? = null
                if (advertising != null) {
                    ads = advertising.adsList
                }
                if (advertising != null) {
                    for (ad in ads!!) {
                        images.add(ad)
                        imageList.add(SlideModel(ad.lien))
                    }
                    imageSlider.setImageList(imageList, true)
                    imageSlider.setItemClickListener(object : ItemClickListener {
                        override fun onItemSelected(position: Int) {
                            val advertising1 = images[position]
                            val intent = Intent(this@MainActivity, AdvertisingActivity::class.java)
                            intent.putExtra("id", advertising1.id)
                            intent.putExtra("lien", advertising1.lien)
                            intent.putExtra("description", advertising1.description)
                            intent.putExtra("ads", images)
                            intent.putExtra("position", position)
                            startActivity(intent)
                        }
                    })
                }
            }

            override fun onFailure(call: Call<Advertising?>, t: Throwable) {}
        })
    }

    override fun onAnnonceClicked(position: Int) {
        val annonce = mAnnonces[position]
        // val intent = Intent(this, ActivityDetailsAnnonce::class.java)
        val intent = Intent(this, ActivityDetailsAnnonces::class.java)
        intent.putExtra("id_ann", annonce.id_ann)
        intent.putExtra("affiche", annonce.affiche)
        intent.putExtra("annonces", mAnnonces)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.appuyer_sur_retour_pour_quitter, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onImageClicked(position: Int) {
        val advertising1 = images[position]
        val intent = Intent(this@MainActivity, AdvertisingActivity::class.java)
        intent.putExtra("id", advertising1.id)
        intent.putExtra("lien", advertising1.lien)
        intent.putExtra("description", advertising1.description)
        intent.putExtra("ads", images)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    override fun onLongItemClicked(position: Int): Boolean {
        return false
    }
}