package bf.fasobizness.bafatech.activities.annonce;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.utils.MySharedManager;

public class ActivityDetailsAnnonces extends AppCompatActivity {
    private static final String TAG = "ActivityDetailsAnnonce";
    private FloatingActionButton ajouterFavori;
    private ArrayList<String> images;
    private ArrayList<SlideModel> imageList;

    private String fav, token, user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_annonces);

        MySharedManager mySharedManager = new MySharedManager(ActivityDetailsAnnonces.this);
        user = mySharedManager.getUser();
        token = "Bearer " + mySharedManager.getToken();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        // toolbar.setTitle(R.string.details);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

    }

}
