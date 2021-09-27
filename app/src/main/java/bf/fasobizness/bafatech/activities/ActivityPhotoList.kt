package bf.fasobizness.bafatech.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.PhotoListAdapter
import bf.fasobizness.bafatech.interfaces.OnItemListener
import java.util.*

class ActivityPhotoList : AppCompatActivity(), OnItemListener {
    private lateinit var images: ArrayList<String>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var photoListAdapter: PhotoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val intent = intent
        images = intent.getStringArrayListExtra("images") as ArrayList<String>
        // val position = intent.getIntExtra("position", 0)

        mRecyclerView = findViewById(R.id.recyclerView)
        val manager = LinearLayoutManager(this)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = manager
        photoListAdapter = PhotoListAdapter(this@ActivityPhotoList, images)
        mRecyclerView.adapter = photoListAdapter
        photoListAdapter.notifyDataSetChanged()
        photoListAdapter.setOnItemListener(this)

    }

    override fun onItemClicked(position: Int) {
        val intent = Intent(this, ActivityFullScreen::class.java)
        intent.putStringArrayListExtra("images", images)
        intent.putExtra("position", position)
        startActivity(intent)
    }
}