package bf.fasobizness.bafatech.activities.annonce;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.adapters.CategoryAdapter;
import bf.fasobizness.bafatech.interfaces.OnItemListener;

public class ActivityAnnonceCategory extends AppCompatActivity implements OnItemListener {
    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonce_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.categories));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        // LinearLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        categories = getResources().getStringArray(R.array.categories_3);
        CategoryAdapter adapter = new CategoryAdapter(this, categories);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemListener(this);

    }

    @Override
    public void onItemClicked(int position) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("categorie", categories[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // String arguments = "{\"ville\": \"\", \"categorie\": \"" + categories[position] + "\"}";

        Intent intent = new Intent(this, ActivityAnnounceFilter.class);
        intent.putExtra("filter", "multiple");
        intent.putExtra("title", categories[position]);
        intent.putExtra("params", jsonObject.toString());
        startActivity(intent);

    }
}
