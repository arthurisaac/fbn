package bf.fasobizness.bafatech.activities.recrutement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.ActivityBoutique
import bf.fasobizness.bafatech.activities.annonce.ActivityAnnonceCategory
import bf.fasobizness.bafatech.activities.annonce.ActivityOffreOr
import bf.fasobizness.bafatech.activities.entreprise.ActivityEntreprisesUne
import bf.fasobizness.bafatech.activities.user.messaging.ActivityDiscussions
import bf.fasobizness.bafatech.adapters.RecrutementAdapter
import bf.fasobizness.bafatech.fragments.FragmentNotConnected
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.OnItemListener
import bf.fasobizness.bafatech.models.Recruit
import bf.fasobizness.bafatech.models.Recruit.Recrutement
import bf.fasobizness.bafatech.utils.DatabaseManager
import bf.fasobizness.bafatech.utils.MySharedManager
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActivityRecrutements : AppCompatActivity(), OnItemListener {
    private lateinit var mRecrutements: ArrayList<Recrutement>
    private lateinit var mRecrutementAdapter: RecrutementAdapter

    // private RequestQueue requestQueue;
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    // private LinearLayout offline_layout;
    private var databaseManager: DatabaseManager? = null

    // private MaterialSearchView searchView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recrutements)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Espace emplois"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container)
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer)
        mSwipeRefreshLayout.setOnRefreshListener { refresh() }
        val sharedManager = MySharedManager(this)
        val user = sharedManager.user

        // offline_layout = findViewById(R.id.layout_ent_offline);
        // searchView = findViewById(R.id.search_view);
        val refresh = findViewById<Button>(R.id.btn_refresh)
        refresh.setOnClickListener { recruits }
        mRecrutements = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview_recrutements)
        mRecrutementAdapter = RecrutementAdapter(this, mRecrutements)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mRecrutementAdapter
        mRecrutementAdapter.setOnItemListener(this)
        val tabChat = findViewById<TextView>(R.id.tab_chat)
        val tabBoutiques = findViewById<TextView>(R.id.tab_boutiques)
        val tabOffreEnOr = findViewById<TextView>(R.id.tab_offre_en_or)
        val tabEntreprise = findViewById<TextView>(R.id.tab_entrepise)
        val tabCategorie = findViewById<TextView>(R.id.tab_categorie)
        tabBoutiques.setOnClickListener { startActivity(Intent(this, ActivityBoutique::class.java)) }
        tabOffreEnOr.setOnClickListener { startActivity(Intent(this, ActivityOffreOr::class.java)) }
        tabEntreprise.setOnClickListener { startActivity(Intent(this, ActivityEntreprisesUne::class.java)) }
        tabCategorie.setOnClickListener { startActivity(Intent(this, ActivityAnnonceCategory::class.java)) }
        tabChat.setOnClickListener {
            if (user.isNotEmpty()) {
                startActivity(Intent(this, ActivityDiscussions::class.java))
            } else {
                val notConnected = FragmentNotConnected.newInstance()
                notConnected.show(supportFragmentManager, "")
            }
        }
        databaseManager = DatabaseManager(this)
        recruits
        // searchView.setOnSearchViewListener(this);
    }

    private fun refresh() {
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.stopShimmer()
        mRecrutementAdapter.clearAll()
        mRecrutements.clear()
        mRecrutementAdapter.notifyDataSetChanged()
        recruits
    }

    /*private void refresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mRecrutementAdapter.clearAll();
        mRecrutements.clear();
        mRecrutementAdapter.notifyDataSetChanged();

        jsonParse();
    }*/
    /*private void jsonParse() {
              offline_layout.setVisibility(View.GONE);
              mShimmerViewContainer.setVisibility(View.VISIBLE);
              API api = RetrofitClient.getClient().create(API.class);
              Call<Recruit> call = api.getRecruits();
              call.enqueue(new Callback<Recruit>() {
                  @Override
                  public void onResponse(@NonNull Call<Recruit> call, @NonNull Response<Recruit> response) {
                      mShimmerViewContainer.setVisibility(View.GONE);
                      mSwipeRefreshLayout.setRefreshing(false);
                      Log.d(TAG, response.toString());
                      Recruit recruit = response.body();
                      List<Recruit.Recrutement> recrutements = null;
                      if (recruit != null) {
                          recrutements = recruit.recrutements;
                      }
                      if (recrutements != null) {
                          mRecrutements.addAll(recrutements);
      
                          for (Recruit.Recrutement recrutement: recrutements) {
                              databaseManager.insertRecrutement(
                                      recrutement.getNom_ent(),
                                      recrutement.getDomaine(),
                                      recrutement.getDescription(),
                                      recrutement.getDesc(),
                                      recrutement.getDate_pub(),
                                      recrutement.getDate_fin(),
                                      recrutement.getHeure_fin(),
                                      recrutement.getNom_r(),
                                      recrutement.getVue(),
                                      recrutement.getLien(),
                                      recrutement.getShare()
                              );
      
                              for (Recruit.Recrutement.Affiche affiche: recrutement.affiches)
                              databaseManager.insertRecruitAttachment(
                                      affiche.getNom(),
                                      affiche.getThumbnail()
                              );
                          }
                          databaseManager.close();
                      }
                      mRecrutementAdapter.notifyDataSetChanged();
                  }
      
                  @Override
                  public void onFailure(@NonNull Call<Recruit> call, @NonNull Throwable t) {
                      mShimmerViewContainer.setVisibility(View.GONE);
                      mSwipeRefreshLayout.setRefreshing(false);
                      offline_layout.setVisibility(View.VISIBLE);
                      Log.d(TAG, t.toString());
                  }
              });
      
          }*/

    private val recruits: Unit
        get() {
            val recrutementList = databaseManager!!.recruits
            mRecrutements.addAll(recrutementList)
            mRecrutementAdapter.notifyDataSetChanged()
            if (mRecrutements.size == 0) {
                mShimmerViewContainer.visibility = View.VISIBLE
            }
            val api = RetrofitClient.getClient().create(API::class.java)
            val call = api.recruits
            call.enqueue(object : Callback<Recruit?> {
                override fun onResponse(call: Call<Recruit?>, response: Response<Recruit?>) {
                    mShimmerViewContainer.visibility = View.GONE
                    mShimmerViewContainer.stopShimmer()
                    mSwipeRefreshLayout.isRefreshing = false
                    Log.d(TAG, response.toString())
                    val recruit = response.body()
                    var recrutements: List<Recrutement>? = null
                    if (recruit != null) {
                        recrutements = recruit.recrutements
                    }
                    if (recrutements != null) {
                        mRecrutementAdapter.clearAll()
                        mRecrutements.addAll(recrutements)
                        databaseManager!!.truncateRecruits()
                        for (recrutement in recrutements) {
                            databaseManager!!.insertRecrutement(
                                    recrutement.id_recr,
                                    recrutement.nom_ent,
                                    recrutement.domaine,
                                    recrutement.description,
                                    recrutement.desc,
                                    recrutement.date_pub,
                                    recrutement.date_fin,
                                    recrutement.heure_fin,
                                    recrutement.nom_r,
                                    recrutement.vue,
                                    recrutement.lien,
                                    recrutement.share
                            )
                            for (affiche in recrutement.affiches) databaseManager!!.insertRecruitAttachment(
                                    affiche.nom,
                                    affiche.thumbnail,
                                    recrutement.id_recr
                            )
                        }
                        databaseManager!!.close()
                    }
                    mRecrutementAdapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<Recruit?>, t: Throwable) {
                    Log.d(TAG, t.toString())
                    mShimmerViewContainer.visibility = View.GONE
                    mShimmerViewContainer.stopShimmer()
                    mSwipeRefreshLayout.isRefreshing = false
                }
            })
        }

    override fun onItemClicked(position: Int) {
        val sharedManager = MySharedManager(this@ActivityRecrutements)
        if (sharedManager.user.isNotEmpty()) {
            val recrutement = mRecrutements[position]
            val intent = Intent(applicationContext, ActivityDetailsRecrutement::class.java)
            intent.putExtra("recrutement", recrutement)
            startActivity(intent)
        } else {
            val notConnected = FragmentNotConnected.newInstance()
            notConnected.show(supportFragmentManager, "")
            // startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recruit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_recherche) {
            startActivity(Intent(this, ActivityRecrutementRecherche::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "ActivityRecrutements"
    }
}