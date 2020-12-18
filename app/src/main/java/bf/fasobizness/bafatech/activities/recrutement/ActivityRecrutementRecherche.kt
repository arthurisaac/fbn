package bf.fasobizness.bafatech.activities.recrutement

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.models.Recruit.Recrutement
import bf.fasobizness.bafatech.utils.DatabaseManager
import java.util.*

class ActivityRecrutementRecherche : AppCompatActivity(), OnItemListener {
    private lateinit var layoutNoRecruit: LinearLayout
    private lateinit var mRecrutementAdapter: RecrutementAdapter
    private lateinit var mRecrutements: ArrayList<Recrutement>
    private lateinit var resultats: TextView
    private lateinit var query: String
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
        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener { jsonParse() }
        val databaseManager = DatabaseManager(this)
        val recrutementList = databaseManager.recruits
        mRecrutements.addAll(recrutementList)
        mRecrutementAdapter.notifyDataSetChanged()
    }

    private fun jsonParse() {
        mRecrutementAdapter.filter.filter(query)
        Handler(Looper.getMainLooper()).postDelayed({
            if (mRecrutements.size == 0) {
                layoutNoRecruit.visibility = View.VISIBLE
            } else {
                resultats.visibility = View.VISIBLE
                val reslt = getString(R.string.resultats, mRecrutementAdapter.itemCount.toString() + "")
                resultats.text = reslt
            }
        }, 200)
    }

    override fun onItemClicked(position: Int) {
        val recrutementArrayList = mRecrutementAdapter.recruit
        val recrutement = recrutementArrayList[position]
        val intent = Intent(applicationContext, ActivityDetailsRecrutement::class.java)
        intent.putExtra("recrutement", recrutement)
        startActivity(intent)
    }
}