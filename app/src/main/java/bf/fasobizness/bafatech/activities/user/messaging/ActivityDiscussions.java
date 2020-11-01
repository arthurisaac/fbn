package bf.fasobizness.bafatech.activities.user.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.user.LoginActivity;
import bf.fasobizness.bafatech.adapters.DiscussionAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Discussion;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDiscussions extends AppCompatActivity
        implements OnItemListener {

    private String token;
    private ArrayList<Discussion.Discussions> mDiscussion;
    private DiscussionAdapter discussionAdapter;
    private LinearLayout no_message;
    private LinearLayout layout_ent_offline;
    private LinearLayout loading_indicator;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussions);

        api = RetrofitClient.getClient().create(API.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.discussions));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        no_message = findViewById(R.id.no_message);
        loading_indicator = findViewById(R.id.loading_indicator);
        LinearLayout layout_busy_system = findViewById(R.id.layout_busy_system);
        Button btn_refresh = findViewById(R.id.btn_refresh);

        layout_ent_offline.setVisibility(View.GONE);
        no_message.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

        btn_refresh.setOnClickListener(v -> getDiscussions());

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDiscussion = new ArrayList<>();

        discussionAdapter = new DiscussionAdapter(ActivityDiscussions.this, mDiscussion);
        recyclerView.setAdapter(discussionAdapter);
        discussionAdapter.setOnItemListener(this);

        MySharedManager sharedManager = new MySharedManager(ActivityDiscussions.this);
        String user = sharedManager.getUser();
        token = "Bearer " + sharedManager.getToken();

        if (user.isEmpty()) {
            startActivity(new Intent(ActivityDiscussions.this, LoginActivity.class));
        } else {
            getDiscussions();
        }

    }

    private void getDiscussions() {
        layout_ent_offline.setVisibility(View.GONE);
        loading_indicator.setVisibility(View.VISIBLE);

        Call<Discussion> call = api.getDiscussions(token);
        call.enqueue(new Callback<Discussion>() {
            @Override
            public void onResponse(@NonNull Call<Discussion> call, @NonNull Response<Discussion> response) {
                loading_indicator.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Discussion discussion = response.body();
                    List<Discussion.Discussions> discussions = null;

                    if (discussion != null) {
                        discussions = discussion.discussions;
                    }
                    if (discussion != null) {
                        mDiscussion.addAll(discussions);
                    }

                    if (mDiscussion.size() == 0) no_message.setVisibility(View.VISIBLE);
                    discussionAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ActivityDiscussions.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<Discussion> call, @NonNull Throwable t) {

                Log.d("ActivityDiscussions ", t.toString());
                layout_ent_offline.setVisibility(View.VISIBLE);
                no_message.setVisibility(View.GONE);
                loading_indicator.setVisibility(View.GONE);

            }
        });

        /*StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    loading_indicator.setVisibility(View.GONE);
                    //Log.v(TAG, response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            Discussion discussion = new Discussion();
                            discussion.setTitre(data.getString("titre"));
                            discussion.setMessage(data.getString("message"));
                            discussion.setDiscussion_id(data.getString("discussion_id"));
                            discussion.setReceiver_id(data.getString("receiver_id"));
                            discussion.setNom(data.getString("nom"));
                            discussion.setId_ann(data.getString("id_ann"));
                            discussion.setId_user( data.getString("id_user"));
                            discussion.setReceiver_id( data.getString("receiver_id"));
                            discussion.setCount( data.getInt("read"));
                            discussion.setTimestamp(data.getString("created_at"));
                            mDiscussion.add(discussion);
                        }
                        if (mDiscussion.size() == 0) no_message.setVisibility(View.VISIBLE);

                        discussionAdapter.notifyDataSetChanged();
                        discussionAdapter.setOnItemListener(ActivityDiscussions.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                        layout_busy_system.setVisibility(View.VISIBLE);
                    }
                }, error -> {
            Log.v(TAG, error.toString());
            layout_ent_offline.setVisibility(View.VISIBLE);
            no_message.setVisibility(View.GONE);
            loading_indicator.setVisibility(View.GONE);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 40,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 4,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT * 2)
        );
        requestQueue.add(request);*/
    }

    @Override
    public void onItemClicked(int position) {
        Discussion.Discussions discussion = mDiscussion.get(position);

        int index = mDiscussion.indexOf(discussion);
        discussion.setMessage(discussion.getMessage());
        discussion.setCount(0);
        mDiscussion.remove(index);
        mDiscussion.add(index, discussion);
        discussionAdapter.notifyDataSetChanged();

        Intent intent = new Intent(ActivityDiscussions.this, ActivityMessage.class);
        //String receiver = null;
        /*if (discussion.getReceiver_id().equals(user)) {
            receiver = discussion.getId_user();
        } else if (discussion.getId_user().equals(user)) {
            receiver = discussion.getReceiver_id();
        }*/
        intent.putExtra("discussion_id", discussion.getDiscussion_id());
        //intent.putExtra("receiver_id", receiver);
        // intent.putExtra("id_ann", discussion.getId_ann());
        startActivity(intent);
    }
}

