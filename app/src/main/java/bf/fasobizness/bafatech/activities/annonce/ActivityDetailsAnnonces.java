package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.AnnouncesPagerAdapter;
import bf.fasobizness.bafatech.fragments.FragmentAnnonce;
import bf.fasobizness.bafatech.utils.MySharedManager;

public class ActivityDetailsAnnonces extends AppCompatActivity {
    // private static final String TAG = "ActivityDetailsAnnonce";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_annonces);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        List<Fragment> list = new ArrayList<>();

        Intent extras = getIntent();
        if (extras.getStringExtra("id_ann") != null) {
            Bundle bundle = new Bundle();
            bundle.putString("id_ann", extras.getStringExtra("id_ann"));
            FragmentAnnonce fragmentAnnonce = new FragmentAnnonce();
            fragmentAnnonce.setArguments(bundle);
            list.add(fragmentAnnonce);
        } else {

            finish();
        }

        ViewPager viewPager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new AnnouncesPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(pagerAdapter);
    }

}
