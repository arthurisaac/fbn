package bf.fasobizness.bafatech.activities.annonce

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailAnnonceUser
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import bf.fasobizness.bafatech.interfaces.OnLongItemListener
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityAnnoncesPublished : AppCompatActivity(), OnAnnonceListener, OnLongItemListener {
    private lateinit var layout_ent_offline: LinearLayout
    private lateinit var layout_busy_system: LinearLayout
    private lateinit var layout_no_annonce: LinearLayout
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmer_view_container: ShimmerFrameLayout
    private var deleteMenu: MenuItem? = null
    private val api = RetrofitClient.getClient().create(API::class.java)
    private var mAnnonceAdapter: AnnounceAdapter? = null
    private lateinit var mAnnonces: ArrayList<Annonce>
    private lateinit var mRecyclerView: RecyclerView
    private var user: String? = null
    private var isList = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annonces_publiees)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.mes_annonces)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val btn_add_post = findViewById<Button>(R.id.btn_add_post)
        // btn_add_post.setOnClickListener(v -> startActivity(new Intent(this, ActivityNouvelleAnnonce.class)));
        btn_add_post.setOnClickListener { startActivity(Intent(this, ActivityNewAnnounce::class.java)) }
        mAnnonces = ArrayList()
        val fab_up = findViewById<FloatingActionButton>(R.id.fab_up)
        mRecyclerView = findViewById(R.id.recyclerview_annonce)
        val managerCard =  LinearLayoutManager(this)
        val managerListe = GridLayoutManager(this, 2)

        mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
        if (isList) {
            mRecyclerView.layoutManager = managerCard
        } else {
            mRecyclerView.layoutManager = managerListe
        }
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter!!.setOnItemListener(this)
        mAnnonceAdapter!!.setOnLongItemListener(this)
        val mySharedManager = MySharedManager(this)
        user = mySharedManager.user

        //Loading more data
        mAnnonceAdapter!!.setOnBottomReachedListener {}
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    fab_up.visibility = View.VISIBLE
                } else if (dy > 0) {
                    fab_up.visibility = View.GONE
                }
            }
        })
        fab_up.setOnClickListener { mRecyclerView.layoutManager!!.smoothScrollToPosition(mRecyclerView, null, 0) }

        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer)
        mSwipeRefreshLayout.setOnRefreshListener { refresh() }
        layout_ent_offline = findViewById(R.id.layout_ent_offline)
        layout_ent_offline.visibility = View.GONE
        val btn_refresh = findViewById<Button>(R.id.btn_refresh)
        btn_refresh.setOnClickListener { refresh() }
        layout_no_annonce = findViewById(R.id.layout_no_annonce)
        layout_no_annonce.visibility = View.GONE
        layout_busy_system = findViewById(R.id.layout_busy_system)
        layout_busy_system.visibility = View.GONE
        shimmer_view_container = findViewById(R.id.shimmer_view_container)
        shimmer_view_container.visibility = View.VISIBLE
        fetchAnnounces()
    }

    private fun refresh() {
        mAnnonceAdapter!!.clearAll()
        mAnnonces!!.clear()
        mAnnonceAdapter!!.notifyDataSetChanged()
        mAnnonceAdapter!!.clearSelectedStated()
        mAnnonceAdapter!!.isInChoiceMode = false
        shimmer_view_container!!.visibility = View.VISIBLE
        shimmer_view_container!!.startShimmer()
        fetchAnnounces()
    }

    private fun fetchAnnounces() {
        layout_ent_offline.visibility = View.GONE
        layout_busy_system.visibility = View.GONE
        layout_no_annonce.visibility = View.GONE
        val call = api.getUsersAnnounces(user)
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                shimmer_view_container.visibility = View.GONE
                shimmer_view_container.stopShimmer()
                mSwipeRefreshLayout.isRefreshing = false
                val announce = response.body()
                var annonces: List<Annonce>? = null
                if (announce != null) {
                    annonces = announce.annonces
                }
                if (annonces != null) {
                    mAnnonces!!.addAll(annonces)
                }
                if (mAnnonces!!.size == 0) {
                    layout_no_annonce.visibility = View.VISIBLE
                    layout_ent_offline.visibility = View.GONE
                }
                mAnnonceAdapter!!.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Announce?>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                layout_ent_offline.visibility = View.VISIBLE
                shimmer_view_container.visibility = View.GONE
                shimmer_view_container.stopShimmer()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_my_announces, menu)
        val item = menu.findItem(R.id.nav_recherche)
        deleteMenu = menu.findItem(R.id.nav_supprimer)
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Rechercher dans mes annonces"
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mAnnonceAdapter!!.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mAnnonceAdapter!!.filter.filter(newText)

                // TODO
                /*if (mAnnonceAdapter.getItemCount() == 0) {
                    layout_no_annonce.setVisibility(View.VISIBLE);
                } else {
                    layout_no_annonce.setVisibility(View.GONE);
                }*/return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_supprimer) {
            deleteAnnoncePublished()
        }
        if (item.itemId == R.id.nav_change) {
            isList = !isList
            mRecyclerView = findViewById(R.id.recyclerview_annonce)
            val managerCard =  LinearLayoutManager(this)
            val managerListe = GridLayoutManager(this, 2)

            mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
            if (isList) {
                item.setIcon(R.drawable.ic_baseline_crop_square_24)
                mRecyclerView.layoutManager = managerCard
            } else {
                item.setIcon(R.drawable.ic_baseline_view_list_24)
                mRecyclerView.layoutManager = managerListe
            }
            mRecyclerView.setHasFixedSize(true)
            mRecyclerView.adapter = mAnnonceAdapter
            mAnnonceAdapter!!.setOnItemListener(this)
            mAnnonceAdapter!!.setOnLongItemListener(this)
            mAnnonceAdapter!!.setOnBottomReachedListener {}
            mRecyclerView.setHasFixedSize(true)
            mRecyclerView.adapter = mAnnonceAdapter
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAnnonceClicked(position: Int) {
        if (mAnnonceAdapter!!.isInChoiceMode) {
            mAnnonceAdapter!!.switchSelectedState(position)
            deleteMenu!!.isVisible = mAnnonceAdapter!!.selectedItemCount > 0
            if (mAnnonceAdapter!!.selectedItemCount <= 0) {
                mAnnonceAdapter!!.isInChoiceMode = false
            }
        } else {
            val annonce = mAnnonces[position]
            val intent = Intent(this, ActivityDetailAnnonceUser::class.java)
            intent.putExtra("id_ann", annonce.id_ann)
            intent.putExtra("affiche", annonce.affiche)
            startActivity(intent)
        }
    }

    override fun onLongItemClicked(position: Int): Boolean {
        mAnnonceAdapter!!.beginChoiceMode(position)
        deleteMenu!!.isVisible = mAnnonceAdapter!!.selectedItemCount > 0
        return true
    }

    private fun deleteAnnoncePublished() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Etes vous sûr de vouloir supprimer ces annonces? Cette action est irréversible.")
        builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
            val positions = mAnnonceAdapter!!.selectedItems
            val annonceArrayList = ArrayList<Annonce>()
            for (position in positions) {
                val annonce = mAnnonces[position]
                annonceArrayList.add(annonce)
                Log.d("annonce", "" + position)
            }
            for (annonce in annonceArrayList) {
                mAnnonces.remove(annonce)
                deleteAnnounce(annonce.id_ann)
            }
            mAnnonceAdapter!!.notifyDataSetChanged()
            mAnnonceAdapter!!.clearSelectedStated()
            mAnnonceAdapter!!.isInChoiceMode = false
            val _builder = AlertDialog.Builder(this@ActivityAnnoncesPublished)
            _builder.setMessage(R.string.annonces_supprimees_avec_succes)
            val _dialog = _builder.create()
            _dialog.show()
        }
        builder.setNegativeButton(R.string.annuler) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    // TODO Bulk delete
    private fun deleteAnnounce(id_ann: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.suppresion_en_cours))
        val call = api.deleteAnnounce(id_ann)
        call.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                /*if (response.isSuccessful()) {

                } else {
                    Toast.makeText(ActivityAnnoncesPublished.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }*/
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                Log.d("activity", t.message!!)
            }
        })
    }
}