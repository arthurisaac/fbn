package bf.fasobizness.bafatech.activities.user.messaging;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce;
import bf.fasobizness.bafatech.adapters.MessageAdapter;
import bf.fasobizness.bafatech.helper.GifSizeFilter;
import bf.fasobizness.bafatech.helper.ProgressRequestBody;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnMessageListener;
import bf.fasobizness.bafatech.interfaces.UploadCallbacks;
import bf.fasobizness.bafatech.models.Message;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.FileCompressingUtil;
import bf.fasobizness.bafatech.utils.MySharedManager;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zhihu.matisse.internal.utils.PathUtils.getPath;

public class ActivityMessage extends AppCompatActivity
        implements OnMessageListener, UploadCallbacks {
    private static final String TAG = "ActivityMessage";
    private static final int REQUEST_CODE = 11;
    private String discussion_id;
    private final Handler handler = new Handler();
    private RecyclerView recyclerView;
    private ArrayList<Message.Messages> mMessages;
    private MessageAdapter mAdapter;
    private ImageView iv_affiche, txt_username_logo_ann;
    private EditText inputMessage;
    private TextView txt_titre_annonce, txt_username, txt_no_annonce_error;
    private LinearLayout no_message, layout_ent_offline, loading_indicator, layout_busy_system, lltitre;
    private String token;
    private Button btn_see_annonce;
    private ArrayList<Uri> images;
    // private Bitmap bitmap;
    private API api;

    private MySharedManager sharedManager;
    private final Runnable fetchNewMessages = new Runnable() {
        @Override
        public void run() {
            Call<Message> call = api.getMessages(discussion_id, token);
            call.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                    loading_indicator.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Message message = response.body();
                        List<Message.Messages> messages = null;

                        if (message != null) {
                            messages = message.messages;
                        }
                        if (messages != null) {
                            if (messages.size() > mMessages.size()) {
                                mMessages.clear();
                                mAdapter.notifyDataSetChanged();
                                mMessages.addAll(messages);
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                                }
                            }
                        }

                    } else {
                        Log.d(TAG, response.toString());
                        layout_busy_system.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                    layout_ent_offline.setVisibility(View.VISIBLE);
                    loading_indicator.setVisibility(View.GONE);
                }
            });

            try {
                handler.postDelayed(this, 5000);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ActivityMessage.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private ProgressBar progressBar;

    private static File getFile(Context context, Uri uri) {
        String path = getPath(context, uri);
        if (path != null) {
            if (isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    private static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        api = RetrofitClient.getClient().create(API.class);
        sharedManager = new MySharedManager(this);

        progressBar = findViewById(R.id.iv_send_progress);
        recyclerView = findViewById(R.id.recycler_view);
        ImageButton btnSend = findViewById(R.id.iv_send);
        ImageButton btn_attachment = findViewById(R.id.btn_attachment);
        txt_titre_annonce = findViewById(R.id.txt_titre_annonce);
        txt_username = findViewById(R.id.txt_username);
        inputMessage = findViewById(R.id.message);
        iv_affiche = findViewById(R.id.affiche);
        txt_username_logo_ann = findViewById(R.id.txt_username_logo_ann);
        txt_no_annonce_error = findViewById(R.id.txt_no_annonce_error);

        layout_ent_offline = findViewById(R.id.layout_ent_offline);
        no_message = findViewById(R.id.no_message);
        loading_indicator = findViewById(R.id.loading_indicator);
        layout_busy_system = findViewById(R.id.layout_busy_system);
        lltitre = findViewById(R.id.lltitre);
        Button btn_refresh = findViewById(R.id.btn_refresh);
        btn_see_annonce = findViewById(R.id.btn_see_annonce);

        layout_ent_offline.setVisibility(View.GONE);
        no_message.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

        btn_refresh.setOnClickListener(v -> fetchMessages());
        btn_attachment.setOnClickListener(v -> requestMultiplePermissions());

        images = new ArrayList<>();
        mMessages = new ArrayList<>();

        MySharedManager sharedManager = new MySharedManager(this);
        String userId = sharedManager.getUser();
        token = "Bearer " + sharedManager.getToken();

        mAdapter = new MessageAdapter(this, mMessages, userId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnMessageListener(this);


        Intent intent = getIntent();
        if (intent.getStringExtra("discussion_id") != null) {
            discussion_id = intent.getStringExtra("discussion_id");
            // fetchMessages();
            handler.post(fetchNewMessages);
        } else {
            finish();
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void fetchMessages() {
        loading_indicator.setVisibility(View.VISIBLE);
        layout_busy_system.setVisibility(View.GONE);
        no_message.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);
        Log.d(TAG, token);

        Call<Message> call = api.getMessages(discussion_id, token);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                // Log.d(TAG, response.toString());
                loading_indicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Message message = response.body();
                    List<Message.Messages> messages = null;
                    User user = new User();

                    if (message != null) {
                        messages = message.messages;
                        user = message.user;
                    }
                    if (messages != null) {
                        mMessages.addAll(messages);
                    }
                    if (mMessages.size() == 0) no_message.setVisibility(View.VISIBLE);

                    if (message != null) {

                        iv_affiche.setVisibility(View.VISIBLE);
                        txt_titre_annonce.setVisibility(View.VISIBLE);
                        btn_see_annonce.setVisibility(View.VISIBLE);
                        String id_ann = message.getId_ann();
                        Intent intent = new Intent(ActivityMessage.this, ActivityDetailsAnnonce.class);
                        intent.putExtra("id_ann", id_ann);
                        iv_affiche.setOnClickListener(v -> startActivity(intent));
                        btn_see_annonce.setOnClickListener(v -> startActivity(intent));
                        txt_titre_annonce.setOnClickListener(v -> startActivity(intent));

                        String titre = message.getTitre();
                        txt_titre_annonce.setText(titre);
                        String affiche = message.getAffiche();
                        Glide.with(ActivityMessage.this)
                                .setDefaultRequestOptions(
                                        new RequestOptions()
                                                .placeholder(R.color.colorPrimary)
                                                .error(R.color.colorPrimaryDark)
                                                .centerCrop()
                                                .override(400, 400)
                                )
                                .asBitmap()
                                .load(affiche)
                                .thumbnail(0.1f)
                                .into(iv_affiche);
                        iv_affiche.setContentDescription(affiche);

                        String username = user.getUsername();
                        String photo = user.getPhoto();
                        Glide.with(ActivityMessage.this)
                                .setDefaultRequestOptions(
                                        new RequestOptions()
                                                .placeholder(R.color.colorPrimaryDark)
                                                .error(R.drawable.user)
                                                .centerCrop()
                                                .override(400, 400)
                                )
                                .asBitmap()
                                .load(photo)
                                .thumbnail(0.1f)
                                .into(txt_username_logo_ann);

                        txt_username.setText(username);

                        if (mAdapter.getItemCount() > 1) {
                            // Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                            recyclerView.scrollToPosition(mMessages.size() - 1);
                        }
                    }
                    if (message != null && message.getTitre() == null) {
                        iv_affiche.setVisibility(View.GONE);
                        txt_titre_annonce.setVisibility(View.GONE);
                        if (message.getId_ann() != null && message.getId_ann().equals("0")) {
                            txt_no_annonce_error.setVisibility(View.GONE);
                        } else {
                            txt_no_annonce_error.setVisibility(View.VISIBLE);
                            lltitre.setVisibility(View.GONE);
                        }
                    }
                } else {
                    layout_busy_system.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
                layout_ent_offline.setVisibility(View.VISIBLE);
                loading_indicator.setVisibility(View.GONE);
            }
        });

    }

    private void sendMessage() {

        no_message.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);
        String str_message = this.inputMessage.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        String str_type = "text";
        Call<ResponseBody> call;

        if (images.size() > 0) {
            str_type = "image";

            API api = RetrofitClient.getClient().create(API.class);
            List<MultipartBody.Part> parts = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                parts.add(prepareFilePart("image" + i, images.get(i)));
            }
            RequestBody message = createPart(str_message);
            RequestBody type = createPart(str_type);
            RequestBody discussion = createPart(discussion_id);
            RequestBody size = createPart(parts.size() + "");

            call = api.createMessagesWithPictures(
                    message,
                    type,
                    discussion,
                    size,
                    parts,
                    "Bearer " + sharedManager.getToken()
            );
        } else {
            if (!str_message.isEmpty()) {
                call = api.createMessagesWithoutPictures(
                        str_message,
                        str_type,
                        discussion_id,
                        0,
                        null,
                        "Bearer " + sharedManager.getToken()
                );
            } else {
                Toast.makeText(this, R.string.veuillez_entrer_un_message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

        }


        Toast.makeText(this, R.string.envoi_en_cours, Toast.LENGTH_SHORT).show();

        /*Call<ResponseBody> call = api.createMessages(
                message,
                type,
                discussion,
                size,
                parts,
                "Bearer " + sharedManager.getToken()
        );*/
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                inputMessage.setText("");
                images.clear();
                if (response.isSuccessful()) {
                    Log.d("ActivityMessage ", response.toString());
                } else {
                    Toast.makeText(ActivityMessage.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ActivityMessage.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void supprimerDiscussion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.supprimer_la_discussion));
        builder.setPositiveButton(R.string.ok, (dialog, id) -> confirmSuppress());
        builder.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void confirmSuppress() {
        Call<MyResponse> call = api.deleteDiscussion(discussion_id, "Bearer" + token);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                Log.d(TAG, response.toString());
                if (response.isSuccessful()) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMessage.this);
                    builder.setMessage(getString(R.string.conversation_supprimee));
                    builder.setPositiveButton(R.string.ok, (dialog, id) -> finish());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(ActivityMessage.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    @Override
    public void onMessageClicked(int position) {

        ArrayList<String> images = new ArrayList<>();
        Message.Messages messages = mMessages.get(position);

        images.add(messages.getMessage());
        Intent intent = new Intent(this, ActivityFullScreen.class);
        intent.putStringArrayListExtra("images", images);
        intent.putExtra("postion", 0);
        startActivity(intent);
    }

    private void showChooser() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .theme(R.style.Matisse_Dracula)
                .countable(true)
                .capture(true)
                .captureStrategy(
                        new CaptureStrategy(true, "bf.fasobizness.bafatech.fileprovider", "Pictures"))
                .maxSelectable(9)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                        getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showSingleMediaType(true)
                .originalEnable(false)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .forResult(REQUEST_CODE);
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            showChooser();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }

    private RequestBody createPart(String id_ann) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, id_ann);
    }

    private MultipartBody.Part prepareFilePart(String part, Uri uri) {
        File file = getFile(this, uri);
        // FileCompressingUtil fileCompressingUtil = new FileCompressingUtil();
        // File compressedFile = fileCompressingUtil.saveBitmapToFile(file);
        ProgressRequestBody requestFile = new ProgressRequestBody(file, this);
        assert file != null;
        return MultipartBody.Part.createFormData(part, file.getName(), requestFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                images.addAll(Matisse.obtainResult(data));
            }
            // mAdapter.notifyDataSetChanged();
            sendMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add_photo) {
            requestMultiplePermissions();
        } else if (id == R.id.nav_delete_discussion) {
            supprimerDiscussion();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchNewMessages);
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void uploadStart() {

    }
}
