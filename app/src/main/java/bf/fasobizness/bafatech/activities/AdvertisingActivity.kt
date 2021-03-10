package bf.fasobizness.bafatech.activities

    import android.os.Bundle
    import android.widget.Button
    import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.adapters.AdvertisingPagerAdapter
import bf.fasobizness.bafatech.fragments.FragmentAdvertising
import bf.fasobizness.bafatech.models.Advertising
    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import java.util.*

class AdvertisingActivity : AppCompatActivity() {
    private lateinit var nextBtn : FloatingActionButton
    private lateinit var previousBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advertising)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }

        nextBtn = findViewById(R.id.nextAd)
        previousBtn = findViewById(R.id.previousAd)

        val list: MutableList<Fragment> = ArrayList()
        val extras = intent
        val position = extras.getIntExtra("position", 0)
        val ads: ArrayList<Advertising.Ads> = extras.getSerializableExtra("ads") as ArrayList<Advertising.Ads>
        if (extras.getStringExtra("id") != null) {
            for (ad in ads) {
                list.add(FragmentAdvertising.newInstance(ad.id))
            }
        } else {
            finish()
        }
        val viewPager = findViewById<ViewPager>(R.id.pager)
        val pagerAdapter: PagerAdapter = AdvertisingPagerAdapter(supportFragmentManager, list)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = position

        nextBtn.setOnClickListener {
            if ((viewPager.currentItem + 1) <= ads.size) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }

        previousBtn.setOnClickListener {
            if ((viewPager.currentItem - 1) >= 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }
}