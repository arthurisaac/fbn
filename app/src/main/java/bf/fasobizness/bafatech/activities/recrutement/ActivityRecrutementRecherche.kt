package bf.fasobizness.bafatech.activities.recrutement

import android.content.Intent
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.RecrutementAdapter
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.models.Recruit
import bf.fasobizness.bafatech.models.Recruit.Recrutement
import bf.fasobizness.bafatech.utils.DatabaseManager
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ActivityRecrutementRecherche : AppCompatActivity(), OnItemListener {
    private lateinit var layoutNoRecruit: LinearLayout
    private lateinit var mRecrutementAdapter: RecrutementAdapter
    private lateinit var mRecrutements: ArrayList<Recrutement>
    private lateinit var resultats: TextView
    private lateinit var query: String
    private lateinit var layoutEntOffline: LinearLayout
    private lateinit var layoutBusySystem: LinearLayout
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recrutement_recherche)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        val edRechercher = findViewById<EditText>(R.id.ed_rechercher)
        resultats = findViewById(R.id.resultats)
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
        mRecrutements = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview_recrutements)
        mRecrutementAdapter = RecrutementAdapter(applicationContext, mRecrutements)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mRecrutementAdapter
        mRecrutementAdapter.setOnItemListener(this)
        if (edRechercher.text.toString().isEmpty()) {
            resultats.setText(R.string.que_recherchez_vous)
        }
        layoutNoRecruit = findViewById(R.id.layout_no_recruit)
        layoutEntOffline = findViewById(R.id.layout_ent_offline)
        layoutBusySystem = findViewById(R.id.layout_busy_system)
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container)
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { jsonParse() }
        /*val databaseManager = DatabaseManager(this)
        val recrutementList = databaseManager.recruits
        mRecrutements.addAll(recrutementList)
        mRecrutementAdapter.notifyDataSetChanged()*/
    }

    private fun jsonParse() {
        /*mRecrutementAdapter.filter.filter(query)
        Handler(Looper.getMainLooper()).postDelayed({
            if (mRecrutements.size == 0) {
                layoutNoRecruit.visibility = View.VISIBLE
            } else {
                resultats.visibility = View.VISIBLE
                val reslt = getString(R.string.resultats, mRecrutementAdapter.itemCount.toString() + "")
                resultats.text = reslt
            }
        }, 200)*/

        mRecrutements.clear()
        mRecrutementAdapter.notifyDataSetChanged()
        layoutBusySystem.visibility = View.GONE
        layoutEntOffline.visibility = View.GONE
        layoutNoRecruit.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mRecrutements.clear()
        mRecrutementAdapter.notifyDataSetChanged()

        val api = RetrofitClient.getClient().create(API::class.java)
        val call = api.searchRecruits(query)
        call.enqueue(object : Callback<Recruit?> {
            override fun onResponse(call: Call<Recruit?>, response: Response<Recruit?>) {
                mShimmerViewContainer.visibility = View.GONE
                val recruit = response.body()
                var recrutements: List<Recrutement>? = null
                if (recruit != null) {
                    recrutements = recruit.recrutements
                }
                if (recrutements != null) {
                    mRecrutements.addAll(recrutements)
                }
                mRecrutementAdapter.notifyDataSetChanged()
                if (mRecrutements.size == 0) {
                    layoutNoRecruit.visibility = View.VISIBLE
                } else {
                    resultats.visibility = View.VISIBLE
                    resultats.text = getString(R.string.resultats, mRecrutements.size.toString() + "")
                }
            }

            override fun onFailure(call: Call<Recruit?>, t: Throwable) {
                mShimmerViewContainer.visibility = View.GONE
                layoutEntOffline.visibility = View.VISIBLE
            }
        })
    }

    override fun onItemClicked(position: Int) {
        val recrutementArrayList = mRecrutementAdapter.recruit
        val recrutement = recrutementArrayList[position]
        val intent = Intent(applicationContext, ActivityDetailsRecrutement::class.java)
        intent.putExtra("recrutement", recrutement)
        startActivity(intent)
    }
}