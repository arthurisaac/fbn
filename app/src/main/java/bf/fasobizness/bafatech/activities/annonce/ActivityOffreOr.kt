package bf.fasobizness.bafatech.activities.annonce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityPromouvoirAnnonces
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.fragments.FragmentNotConnected
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.utils.MySharedManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityOffreOr : AppCompatActivity(), OnAnnonceListener {
    private lateinit var layout_ent_offline: LinearLayout
    private lateinit var layout_busy_system: LinearLayout
    private lateinit var layout_no_annonce: LinearLayout
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private var shimmer_view_container: ShimmerFrameLayout? = null
    private var mAnnonceAdapter: AnnounceAdapter? = null
    private var mAnnonces: ArrayList<Annonce>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offre_or)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.offres_en_or)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        mAnnonces = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview_annonce)
        val manager: LinearLayoutManager = GridLayoutManager(this, 2)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter!!.setOnItemListener(this)


        //Loading more data
        mAnnonceAdapter!!.setOnBottomReachedListener {}
        val fabUp = findViewById<FloatingActionButton>(R.id.fab_up)
        fabUp.setOnClickListener {
            mRecyclerView.layoutManager?.smoothScrollToPosition(mRecyclerView, null, 0)
        }

        val fab = findViewById<Button>(R.id.fab)
        fab.setOnClickListener {
            val sharedManager = MySharedManager(this)
            if (sharedManager.user.isEmpty()) {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            } else {
                startActivity(Intent(this, ActivityPromouvoirAnnonces::class.java))
            }
        }
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


        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer)
        mSwipeRefreshLayout.setOnRefreshListener(OnRefreshListener { refresh() })
        layout_ent_offline = findViewById(R.id.layout_ent_offline)
        layout_ent_offline.visibility = View.GONE
        val btn_refresh = findViewById<Button>(R.id.btn_refresh)
        btn_refresh.setOnClickListener { v: View? -> refresh() }
        layout_no_annonce = findViewById(R.id.layout_no_annonce)
        layout_no_annonce.visibility = View.GONE
        layout_busy_system = findViewById(R.id.layout_busy_system)
        layout_busy_system.visibility = View.GONE
        shimmer_view_container = findViewById(R.id.shimmer_view_container)
        jsonParse()
    }

    private fun refresh() {
        mAnnonceAdapter!!.clearAll()
        mAnnonces!!.clear()
        mAnnonceAdapter!!.notifyDataSetChanged()
        shimmer_view_container!!.visibility = View.VISIBLE
        jsonParse()
    }

    private fun jsonParse() {
        val api = RetrofitClient.getClient().create(API::class.java)
        layout_ent_offline!!.visibility = View.GONE
        layout_busy_system!!.visibility = View.GONE
        layout_no_annonce!!.visibility = View.GONE
        shimmer_view_container!!.visibility = View.VISIBLE
        val call = api.filterAnnounce("vip", "1")
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                shimmer_view_container!!.visibility = View.GONE
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
                shimmer_view_container!!.visibility = View.GONE
                Log.d(TAG, t.message!!)
            }
        })
    }

    /*private void jsonParse() {
//        layout_ent_offline.setVisibility(View.GONE);
//        layout_busy_system.setVisibility(View.GONE);
//        layout_no_annonce.setVisibility(View.GONE);
//        shimmer_view_container.setVisibility(View.VISIBLE);

        String url = Constants.HOST_URL + "annonce/premium/all";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);

                    String titre = data.getString("titre");
                    String vue = data.getString("vue");
                    String id_ann = data.getString("id_ann");
                    String categorie = data.getString("categorie");
                    String vip = data.getString("vip");

                    String affiche = data.getString("illNom");

                    Annonce annonce = new Annonce();
                    annonce.setAffiche(affiche);
                    annonce.setVue(vue);
                    annonce.setId_ann(id_ann);
                    annonce.setCategorie(categorie);
                    annonce.setTitre(titre);
                    annonce.setVip(vip);

                    mAnnonces.add(annonce);
                }
                if (mAnnonces.size() == 0) {
                    layout_no_annonce.setVisibility(View.GONE);
                }
                mAnnonceAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                layout_busy_system.setVisibility(View.VISIBLE);
            }

        }, error -> {
            Log.d(TAG, "" + error.toString());
            error.printStackTrace();
            layout_ent_offline.setVisibility(View.VISIBLE);
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_pers_fk", user);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2)
        );
        requestQueue.add(request);
    }*/
    override fun onAnnonceClicked(position: Int) {
        val annonce = mAnnonces!![position]
        val intent = Intent(this, ActivityDetailsAnnonce::class.java)
        intent.putExtra("id_ann", annonce.id_ann)
        intent.putExtra("affiche", annonce.affiche)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ActivityOffreOr"
    }
}