package bf.fasobizness.bafatech.activities.annonce

import androidx.appcompat.app.AppCompatActivity
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener
import android.widget.TextView
import bf.fasobizness.bafatech.adapters.AnnounceAdapter
import bf.fasobizness.bafatech.models.Announce.Annonce
import bf.fasobizness.bafatech.interfaces.API
import android.os.Bundle
import bf.fasobizness.bafatech.R
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import bf.fasobizness.bafatech.helper.RetrofitClient
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import bf.fasobizness.bafatech.models.Announce
import bf.fasobizness.bafatech.activities.annonce.ActivityUserProfile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import bf.fasobizness.bafatech.activities.ActivityFullScreen
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce
import bf.fasobizness.bafatech.models.User
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

class ActivityUserProfile : AppCompatActivity(), OnAnnonceListener {
    private var iv_user_photo: CircleImageView? = null
    private var iv_big: ImageView? = null

    // private RequestQueue requestQueue;
    private var tv_user_username: TextView? = null
    private var tv_user_account_type: TextView? = null
    private lateinit var tv_publish_count: TextView
    private lateinit var mAnnonceAdapter: AnnounceAdapter
    private lateinit var mAnnonces: ArrayList<Annonce>
    private var api: API? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val intent = intent
        val id = intent.getStringExtra("id")
        api = RetrofitClient.getClient().create(API::class.java)

        // requestQueue = Volley.newRequestQueue(this);
        tv_user_username = findViewById(R.id.tv_user_username)
        tv_user_account_type = findViewById(R.id.tv_user_account_type)
        tv_publish_count = findViewById(R.id.tv_publish_count)
        iv_user_photo = findViewById(R.id.iv_user_photo)
        iv_big = findViewById(R.id.iv_big)
        val count = getString(R.string.publications_x, "0")
        tv_publish_count.setText(count)

        // init recyclerview
        mAnnonces = ArrayList()
        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val manager = LinearLayoutManager(this)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        mAnnonceAdapter = AnnounceAdapter(this, mAnnonces)
        mRecyclerView.adapter = mAnnonceAdapter
        mAnnonceAdapter!!.setOnItemListener(this)
        mAnnonceAdapter!!.setOnBottomReachedListener {}
        getData(id)
    }

    private fun getData(id: String?) {
        val call = api!!.getUsersAnnounces(id)
        call.enqueue(object : Callback<Announce?> {
            override fun onResponse(call: Call<Announce?>, response: Response<Announce?>) {
                Log.d(TAG, response.toString())
                try {
                    val announce = response.body()
                    var annonces: List<Annonce>? = null
                    val user: User
                    if (announce != null) {
                        annonces = announce.annonces
                        user = announce.user
                        Glide.with(this@ActivityUserProfile)
                            .setDefaultRequestOptions(
                                RequestOptions()
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                            )
                            .load(user.photo)
                            .into(iv_user_photo!!)
                        iv_user_photo!!.setOnClickListener { v: View? ->
                            val imagesList = ArrayList<String>()
                            val intent =
                                Intent(this@ActivityUserProfile, ActivityFullScreen::class.java)
                            intent.putStringArrayListExtra("images", imagesList)
                            intent.putExtra("position", 0)
                            startActivity(intent)
                        }
                        if (!user.photo.contains("user.png")) {
                            Glide.with(this@ActivityUserProfile)
                                .setDefaultRequestOptions(
                                    RequestOptions()
                                        .placeholder(R.color.colorPrimaryDark)
                                        .error(R.color.colorPrimaryDark)
                                )
                                .load(user.photo)
                                .into(iv_big!!)
                        }
                        tv_user_username!!.text = user.username
                        if (user.type == "entreprise") {
                            tv_user_account_type!!.text = "Entreprise"
                        } else {
                            tv_user_account_type!!.text = "Particulier"
                        }
                        val count =
                            getString(R.string.publications_x, annonces.size.toString() + "")
                        tv_publish_count!!.text = count
                    }
                    if (annonces != null) {
                        mAnnonces!!.addAll(annonces)
                    }
                    mAnnonceAdapter!!.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<Announce?>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@ActivityUserProfile,
                    R.string.pas_d_acces_internet,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        /*String url = Constants.HOST_URL + "/annonce/user/" + id;
        Log.d(TAG, url);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject user = jsonObject.getJSONObject("user");
                JSONArray annonce = jsonObject.getJSONArray("annonce");

                String photo = user.getString("photo");
                Glide.with(this)
                        .setDefaultRequestOptions(
                                new RequestOptions()
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user))
                        .load(photo)
                        .into(iv_user_photo);

                iv_user_photo.setOnClickListener( v -> {
                    ArrayList<String> imagesList = new ArrayList<>();
                    imagesList.add(photo);
                    Intent intent = new Intent(this, ActivityFullScreen.class);
                    intent.putStringArrayListExtra("images", imagesList);
                    intent.putExtra("position", 0);
                    startActivity(intent);
                });

                if (!photo.contains("user.png")) {
                    Glide.with(this)
                            .setDefaultRequestOptions(
                                    new RequestOptions()
                                            .placeholder(R.color.colorPrimaryDark)
                                            .error(R.color.colorPrimaryDark))
                            .load(photo)
                            .into(iv_big);
                }

                // populate(annonce);
                tv_user_username.setText(user.getString("username"));
                tv_user_account_type.setText(user.getString("type"));
                String count = getString(R.string.publications_x, annonce.length()+"");
                tv_publish_count.setText(count);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
            Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);*/
    }

    /*private void populate(JSONArray jsonArray) {
        try {
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
            mAnnonceAdapter.notifyDataSetChanged();
        } catch (JSONException j) {
            j.printStackTrace();
        }

    }*/
    override fun onAnnonceClicked(position: Int) {
        val annonce = mAnnonces[position]
        //val intent = Intent(this, ActivityDetailsAnnonce::class.java)
        //intent.putExtra("id_ann", annonce.id_ann)
        //intent.putExtra("affiche", annonce.affiche)
        val intent = Intent(this, ActivityDetailsAnnonces::class.java)
        intent.putExtra("id_ann", annonce.id_ann)
        intent.putExtra("affiche", annonce.affiche)
        intent.putExtra("annonces", mAnnonces)
        intent.putExtra("position", position)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ActivityUserProfile"
    }
}