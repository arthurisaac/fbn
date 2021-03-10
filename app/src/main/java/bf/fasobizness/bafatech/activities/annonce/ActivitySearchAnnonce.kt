package bf.fasobizness.bafatech.activities.annonce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.models.Announce.Annonce
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivitySearchAnnonce : AppCompatActivity(), OnAnnonceListener {
    private lateinit var resultats: TextView
    private lateinit var query: String
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private lateinit var mAnnonceAdapter: AnnounceAdapter
    private lateinit var mAnnonces: ArrayList<Annonce>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recherche_annonce)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        val edRechercher = findViewById<EditText>(R.id.ed_rechercher)
        resultats = findViewById(R.id.resultats)

        // Init annonce recyclerview
        mAnnonces = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview_annonce)
        val manager: LinearLayoutManager = GridLayoutManager(applicationContext, 2)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter.setOnItemListener(this)
        mAnnonceAdapter.setOnBottomReachedListener {}
        edRechercher.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                query = v.text.toString()
                resultats.setText(R.string.recherche_en_cours)
                val `in` = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                `in`.hideSoftInputFromWindow(edRechercher.windowToken, 0)
                handled = true
                jsonParse()
            }
            handled
        }
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        layoutEntOffline.visibility = View.GONE
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { jsonParse() }
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        layoutBusySystem.visibility = View.GONE
        shimmerViewContainer = findViewById(R.id.shimmer_view_container)
        shimmerViewContainer.visibility = View.GONE
    }

    private fun jsonParse() {
        mAnnonces.clear()
        mAnnonceAdapter.notifyDataSetChanged()
        resultats.setText(R.string.recherche_en_cours)
        resultats.visibility = View.VISIBLE
        layoutEntOffline.visibility = View.GONE
        layoutBusySystem.visibility = View.GONE
        shimmerViewContainer.visibility = View.VISIBLE
        val api = RetrofitClient.getClient().create(API::class.java)
        val call = api.searchAnnounces(query)
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                shimmerViewContainer.visibility = View.GONE
                Log.d(TAG, response.toString())
                if (response.isSuccessful) {
                    val announce = response.body()
                    var annonces: List<Annonce>? = null
                    if (announce != null) {
                        annonces = announce.annonces
                    }
                    if (annonces != null) {
                        mAnnonces.addAll(annonces)
                    }
                    if (mAnnonces.size == 0) {
                        layoutEntOffline.visibility = View.GONE
                    }
                    mAnnonceAdapter.notifyDataSetChanged()
                    val reslt = getString(R.string.resultats, mAnnonces.size.toString() + "")
                    if (mAnnonces.size == 0) {
                        resultats.setText(R.string.aucun_resultat)
                    }
                    resultats.text = reslt
                } else {
                    layoutBusySystem.visibility = View.VISIBLE
                    resultats.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Announce?>, t: Throwable) {
                shimmerViewContainer.visibility = View.GONE
                layoutEntOffline.visibility = View.VISIBLE
            }
        })
    }

    override fun onAnnonceClicked(position: Int) {
        val annonce = mAnnonces[position]
        val intent = Intent(this, ActivityDetailsAnnonce::class.java)
        intent.putExtra("id_ann", annonce.id_ann)
        intent.putExtra("affiche", annonce.affiche)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ActivityRecherche"
    }
}