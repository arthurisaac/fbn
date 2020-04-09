package bf.fasobizness.bafatech.activities.user.messaging;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce;
import bf.fasobizness.bafatech.adapters.MessageAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnMessageListener;
import bf.fasobizness.bafatech.models.Message;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityMessage extends AppCompatActivity
        implements OnMessageListener {
    private static final String TAG = "ActivityMessage";
    private final int GALLERY = 1;
    private final int CAMERA = 2;
    // private RequestQueue requestQueue;
    private String discussion_id;
    private String selfUserId, receiver_id, token, id_ann;
    private RecyclerView recyclerView;
    private ArrayList<Message.Messages> mMessages;
    private MessageAdapter mAdapter;
    private ImageView iv_affiche, txt_username_logo_ann;
    private EditText inputMessage;
    private TextView txt_titre_annonce, txt_username, txt_no_annonce_error;
    private LinearLayout no_message, layout_ent_offline, loading_indicator, layout_busy_system, lltitre;
    private Bitmap bitmap;
    private API api;

    private MySharedManager sharedManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        // requestQueue = Volley.newRequestQueue(this);
        api = RetrofitClient.getClient().create(API.class);
        sharedManager = new MySharedManager(this);

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

        layout_ent_offline.setVisibility(View.GONE);
        no_message.setVisibility(View.GONE);
        layout_busy_system.setVisibility(View.GONE);

        btn_refresh.setOnClickListener(v -> fetchMessages());
        btn_attachment.setOnClickListener(v -> showSheetDialog());

        mMessages = new ArrayList<>();
        MySharedManager sharedManager = new MySharedManager(this);
        selfUserId = sharedManager.getUser();
        token = "Bearer " + sharedManager.getToken();

        mAdapter = new MessageAdapter(this, mMessages, selfUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnMessageListener(this);


        Intent intent = getIntent();
        if (intent.getStringExtra("discussion_id") != null) {
            discussion_id = intent.getStringExtra("discussion_id");
            receiver_id = intent.getStringExtra("receiver_id");
            id_ann = intent.getStringExtra("id_ann");
            fetchMessages();
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

        /*Log.d(TAG, "disc " + discussion_id);
        Log.d(TAG, "id_ann " + id_ann);
        Log.d(TAG, "receiver " + receiver_id);*/

        Call<Message> call = api.getMessages(discussion_id, id_ann, receiver_id, token);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
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
                        String id_ann = message.getId_ann();
                        Intent intent = new Intent(ActivityMessage.this, ActivityDetailsAnnonce.class);
                        intent.putExtra("id_ann", id_ann);
                        iv_affiche.setOnClickListener(v -> startActivity(intent));
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
                            Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }
                    }
                    if (message != null && message.getTitre() == null) {
                        iv_affiche.setVisibility(View.GONE);
                        txt_titre_annonce.setVisibility(View.GONE);
                        if (id_ann != null && id_ann.equals("0")) {
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
                layout_ent_offline.setVisibility(View.VISIBLE);
                loading_indicator.setVisibility(View.GONE);
            }
        });

    }

    private void sendMessage() {

        no_message.setVisibility(View.GONE);
        layout_ent_offline.setVisibility(View.GONE);
        String message = this.inputMessage.getText().toString().trim();
        String type = "text";
        if (bitmap != null) {
            message = getStringImage(bitmap);
            type = "image";
        }

        if (!message.isEmpty()) {
            Toast.makeText(this, R.string.envoi_en_cours, Toast.LENGTH_SHORT).show();
            Call<Message> call = api.createMessages(
                    selfUserId,
                    receiver_id,
                    message,
                    type,
                    discussion_id,
                    "Bearer " + sharedManager.getToken()
            );
            call.enqueue(new Callback<Message>() {
                @Override
                public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {

                    // Log.d(TAG, response.toString());
                    if (response.isSuccessful()) {
                        inputMessage.setText("");

                        Message message = response.body();
                        List<Message.Messages> messages = null;

                        if (message != null) {
                            messages = message.messages;
                        }
                        if (messages != null) {
                            mMessages.addAll(messages);
                        }
                        if (mMessages.size() == 0) no_message.setVisibility(View.VISIBLE);

                        mAdapter.notifyDataSetChanged();

                        if (mAdapter.getItemCount() > 1) {
                            Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(ActivityMessage.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                    Toast.makeText(ActivityMessage.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    finish();
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

    private void showSheetDialog() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_attachment, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();

        LinearLayout layout_camera = dialog.findViewById(R.id.layout_camera);
        LinearLayout layout_galerie = dialog.findViewById(R.id.layout_galerie);

        if (layout_camera != null) {
            layout_camera.setOnClickListener(v -> {
                requestMultiplePermissions("camera");
                dialog.dismiss();
            });
        }

        if (layout_galerie != null) {
            layout_galerie.setOnClickListener(v -> {
                requestMultiplePermissions("galerie");
                dialog.dismiss();
            });
        }
    }

    private void pickGalerie() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY);
    }

    private void pickCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void requestMultiplePermissions(String type) {
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
                            if (type.equals("camera")) {
                                pickCamera();
                            } else if (type.equals("galerie")) {
                                pickGalerie();
                            }
                            Log.v(TAG, "All permissions are granted by user!");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    Uri contentURI = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        sendMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == CAMERA) {
                try {
                    if (data != null) {
                        bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        sendMessage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_add_photo:
                showSheetDialog();
                break;

            case R.id.nav_delete_discussion:
                supprimerDiscussion();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
