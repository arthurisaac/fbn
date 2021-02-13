package bf.fasobizness.bafatech.activities.annonce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.models.Announce.Annonce
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityAnnounceFilter : AppCompatActivity(), OnAnnonceListener {
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var layoutNoAnnonce: LinearLayout
    private lateinit var mAnnonceAdapter: AnnounceAdapter
    private lateinit var mAnnonces: ArrayList<Annonce>
    private var params: String? = ""
    private var filter: String? = "id"
    private var page = 1
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private lateinit var tvCountAnnounce: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annonce_from_category)
        val extras = intent
        if (extras.getStringExtra("params") != null) {
            params = extras.getStringExtra("params")
        }
        if (extras.getStringExtra("filter") != null) {
            filter = extras.getStringExtra("filter")
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        var title: String? = getString(R.string.vos_resultats)
        if (extras.getStringExtra("title") != null) {
            title = extras.getStringExtra("title")
        }
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        tvCountAnnounce = findViewById(R.id.tv_count_announce)
        val fabUp = findViewById<FloatingActionButton>(R.id.fab_up)

        // Fetch annonces
        mAnnonces = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.annonces_card_view)
        val manager: LinearLayoutManager = GridLayoutManager(applicationContext, 2)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter.setOnItemListener(this)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    fabUp.visibility = View.VISIBLE
                } else if (dy > 0) {
                    fabUp.visibility = View.GONE
                }
            }
        })

        fabUp.setOnClickListener {
            mRecyclerView.layoutManager?.smoothScrollToPosition(mRecyclerView, null, 0)
        }
        mAnnonceAdapter.setOnBottomReachedListener {}
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { refresh() }
        layoutNoAnnonce = findViewById(R.id.layout_no_annonce)
        layoutNoAnnonce.visibility = View.GONE
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        layoutBusySystem.visibility = View.GONE
        shimmerViewContainer = findViewById(R.id.shimmer_view_container)
        shimmerViewContainer.visibility = View.VISIBLE

        //SwipeRefresh
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer)
        mSwipeRefreshLayout.setOnRefreshListener { refresh() }
        jsonParse()
    }

    private fun jsonParse() {
        val api = RetrofitClient.getClient().create(API::class.java)
        layoutEntOffline.visibility = View.GONE
        layoutBusySystem.visibility = View.GONE
        layoutNoAnnonce.visibility = View.GONE
        tvCountAnnounce.visibility = View.GONE
        val call = api.filterAnnounce(filter, params)
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                Log.d("ActivityAnnounce", params.toString())
                shimmerViewContainer.visibility = View.GONE
                mSwipeRefreshLayout.isRefreshing = false
                val announce = response.body()
                var annonces: List<Annonce>? = null
                if (announce != null) {
                    annonces = announce.annonces
                }
                if (annonces != null) {
                    mAnnonces.addAll(annonces)
                    tvCountAnnounce.text = getString(R.string._0_annonces, mAnnonces.size.toString())
                    tvCountAnnounce.visibility = View.VISIBLE
                }
                if (mAnnonces.size == 0) {
                    layoutNoAnnonce.visibility = View.VISIBLE
                    layoutEntOffline.visibility = View.GONE
                }
                page++
                mAnnonceAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Announce?>, t: Throwable) {

                // Log.d("ActivityFilter", t.toString());
                mSwipeRefreshLayout.isRefreshing = false
                layoutEntOffline.visibility = View.VISIBLE
                shimmerViewContainer.visibility = View.GONE
            }
        })
    }

    /*private void jsonParse() {
        layout_ent_offline.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);
        layout_no_annonce.setVisibility(View.GONE);

        String url = Constants.HOST_URL + "annonce/all";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            shimmer_view_container.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
             Log.v(TAG, response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (!jsonObject.get("total").equals("null")) {
                    total = jsonObject.getInt("total");
                }
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
                    layout_no_annonce.setVisibility(View.VISIBLE);
                    layout_ent_offline.setVisibility(View.GONE);
                }
                page++;
                mAnnonceAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                if (page <= 1) {
                    layout_busy_system.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Fin", Toast.LENGTH_SHORT).show();
                }
            }

        }, error -> {
            Log.d(TAG, "" + error.toString());
            error.printStackTrace();
            mSwipeRefreshLayout.setRefreshing(false);
            layout_ent_offline.setVisibility(View.VISIBLE);
            shimmer_view_container.setVisibility(View.GONE);
            // page++;
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_pers_fk", user);
                params.put("page", page+"");
                params.put("filtre", filtre);
                params.put("params", params);
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
    private fun refresh() {
        page = 1
        mAnnonceAdapter.clearAll()
        mAnnonces.clear()
        mAnnonceAdapter.notifyDataSetChanged()
        shimmerViewContainer.visibility = View.VISIBLE
        jsonParse()
    }

    override fun onAnnonceClicked(position: Int) {
        val annonce = mAnnonces[position]
        val intent = Intent(this, ActivityDetailsAnnonce::class.java)
        intent.putExtra("id_ann", annonce.id_ann)
        intent.putExtra("affiche", annonce.affiche)
        startActivity(intent)
    }
}