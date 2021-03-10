package bf.fasobizness.bafatech.activities.annonce

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.AnnouncesPagerAdapter
import bf.fasobizness.bafatech.fragments.FragmentAnnounce
import bf.fasobizness.bafatech.models.Announce
import java.util.*

class ActivityDetailsAnnonces : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_annonces)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        val list: MutableList<Fragment> = ArrayList()
        val extras = intent
        val position = extras.getIntExtra("position", 0)
        val announces: ArrayList<Announce.Annonce> = extras.getSerializableExtra("annonces") as ArrayList<Announce.Annonce>

        if (extras.getStringExtra("id_ann") != null) {
            for (announce in announces) {
                list.add(FragmentAnnounce.newInstance(announce.id_ann))
            }
        } else {
            finish()
        }
        val viewPager = findViewById<ViewPager>(R.id.pager)
        val pagerAdapter: PagerAdapter = AnnouncesPagerAdapter(supportFragmentManager, list)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = position
    }
}