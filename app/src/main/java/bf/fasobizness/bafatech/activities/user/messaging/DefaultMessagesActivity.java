package bf.fasobizness.bafatech.activities.user.messaging;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.gson.Gson;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce;
import bf.fasobizness.bafatech.adapters.MessagesList;
import bf.fasobizness.bafatech.adapters.MessagesListCustomAdapter;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.Message;
import bf.fasobizness.bafatech.models.User;
import bf.fasobizness.bafatech.utils.AppUtils;
import bf.fasobizness.bafatech.utils.Constants;
import bf.fasobizness.bafatech.utils.DatabaseManager;
import bf.fasobizness.bafatech.utils.MySharedManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DefaultMessagesActivity extends AppCompatActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener,
        MessagesListCustomAdapter.SelectionListener,
        MessagesListCustomAdapter.OnLoadMoreListener {

    private static final String TAG = "Activity";
    private MessagesList messagesList;
    private Menu menu;
    private int selectionCount;
    protected ImageLoader imageLoader;

    private final API api = RetrofitClient.getClient().create(API.class);
    private String discussion_id;
    private String token, senderId;

    private ImageView iv_affiche, txt_username_logo_ann;
    private TextView txt_titre_annonce, txt_username, txt_no_annonce_error;
    private Button btn_see_annonce;
    private LinearLayout lltitre;
    private DatabaseManager databaseManager;
    private TextView tv_message;
    // private WebSocket webSocket;

    protected MessagesListCustomAdapter<Message.Messages> messagesAdapter;
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "bf.fasobizness.bafatech";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_messages);

        Intent intent = getIntent();
        if (intent.getStringExtra("discussion_id") != null)
            discussion_id = intent.getStringExtra("discussion_id");
        else finish();

        imageLoader = (imageView, url, payload) -> Glide.with(DefaultMessagesActivity.this)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                                .override(400, 400)
                )
                .asBitmap()
                .load(url)
                .thumbnail(0.1f)
                .into(imageView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        txt_username_logo_ann = findViewById(R.id.txt_username_logo_ann);
        txt_username = findViewById(R.id.txt_username);
        txt_no_annonce_error = findViewById(R.id.txt_no_annonce_error);
        txt_titre_annonce = findViewById(R.id.txt_titre_annonce);
        btn_see_annonce = findViewById(R.id.btn_see_annonce);
        lltitre = findViewById(R.id.lltitre);
        iv_affiche = findViewById(R.id.affiche);
        RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);
        recordButton.setRecordView(recordView);
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                // String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");

                // Log.d("RecordTime", time);
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
            }
        });

        recordButton.setOnRecordClickListener(v -> {
            Toast.makeText(DefaultMessagesActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
            Log.d("RecordButton","RECORD BUTTON CLICKED");
        });

        recordView.setOnBasketAnimationEndListener(() -> Log.d("RecordView", "Basket Animation Finished"));
        recordView.setSoundEnabled(false);

        //if (recordButton.isListenForRecord()) {
            // recordButton.setListenForRecord(false);
            //Toast.makeText(this, "onClickEnabled", Toast.LENGTH_SHORT).show();
        //} else {
          //  recordButton.setListenForRecord(true);
           // Toast.makeText(this, "onClickDisabled", Toast.LENGTH_SHORT).show();
        //}

        databaseManager = new DatabaseManager(this);

        MySharedManager sharedManager = new MySharedManager(this);
        token = "Bearer " + sharedManager.getToken();
        senderId = sharedManager.getUser();

        messagesList = findViewById(R.id.messagesList);
        try {
            initAdapter();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);

        //instantiateWebSocket();

    }

    @Override
    protected void onStart() {
        super.onStart();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                try {
                    JSONObject object = new JSONObject(msg);
                    Message.Messages messages = new Message.Messages();
                    messages.setMessage_id(object.getString("message_id"));
                    messages.setMessage(object.getString("message"));
                    messages.setSender(object.getString("sender"));
                    messages.setDiscussion_id(object.getString("discussion_id"));
                    messages.setCreated_at(object.getString("created_at"));
                    messages.setType(object.getString("type"));

                    messagesAdapter.addToStart(messages, true);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (notificationManager != null) {
                            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new IntentFilter("MyData"));
    }

    private void initAdapter() throws ParseException {
        messagesAdapter = new MessagesListCustomAdapter<>(senderId, imageLoader);
        List<Message.Messages> mMessages = databaseManager.getMessages(discussion_id);
        Message mMessage = databaseManager.getMessage(discussion_id);
        messagesAdapter.addToEnd(mMessages, true);
        messagesList.setAdapter(messagesAdapter);

        if (mMessage != null) {
            populateUser(mMessage.getUser());
            populateAnnounce(mMessage);
        }

    }

    /*private void instantiateWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(Constants.HOST.api_server_url()).build();
        SockcetListener sockcetListener = new SockcetListener(this);
        webSocket = client.newWebSocket(request, sockcetListener);
    }*/

    /*public static class SockcetListener extends WebSocketListener {
        public DefaultMessagesActivity activity;

        public SockcetListener(DefaultMessagesActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
            super.onOpen(webSocket, response);
            activity.runOnUiThread(() -> Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            activity.runOnUiThread(() -> {
                Log.d("activity", text);
                JSONObject object = new JSONObject();
                try {
                    object.put("message", text);
                    object.put("byServer", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, okhttp3.Response response) {
            super.onFailure(webSocket, t, response);
        }
    }*/

    /*private void feedDB(){
        Call<Message> call = api.getMessages(discussion_id, token);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                if (response.isSuccessful()) {
                    Message message = response.body();
                    List<Message.Messages> messages = null;
                    User user = new User();

                    if (message != null) {
                        messages = message.messages;
                        user = message.user;
                    }
                    if (messages != null) {
                        // mMessages.addAll(messages);


                        Gson gson = new Gson();
                        String json = gson.toJson(user);

                        int isAnnonce = 0;
                        if (message.isAnnonce()) isAnnonce = 1;
                        databaseManager.truncateMessage();
                        databaseManager.insertMessage(
                                discussion_id,
                                json,
                                message.getId_ann(),
                                isAnnonce,
                                message.getTitre(),
                                message.getAffiche()
                        );

                        databaseManager.truncateMessages();
                        for (Message.Messages messages1: messages) {
                            databaseManager.insertMessages(
                                    messages1.getMessage_id(),
                                    messages1.getMessage(),
                                    messages1.getCreated_at(),
                                    messages1.getEtat(),
                                    messages1.getDiscussion_id(),
                                    messages1.getType(),
                                    messages1.getSender()
                            );
                        }
                    } else {
                        Toast.makeText(DefaultMessagesActivity.this, "Messages null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DefaultMessagesActivity.this, "Code " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(DefaultMessagesActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void populateAnnounce(Message message) {
        if (message.isAnnonce()) {
            lltitre.setVisibility(View.VISIBLE);
            if (message.getAffiche() != null) {
                Glide.with(DefaultMessagesActivity.this)
                        .setDefaultRequestOptions(
                                new RequestOptions()
                                        .placeholder(R.color.colorPrimary)
                                        .error(R.color.colorPrimaryDark)
                                        .centerCrop()
                                        .override(400, 400)
                        )
                        .asBitmap()
                        .load(message.getAffiche())
                        .thumbnail(0.1f)
                        .into(iv_affiche);
                iv_affiche.setContentDescription(message.getAffiche());
            }
            if (message.getTitre() != null) {
                txt_titre_annonce.setText(message.getTitre());
            }
            if (message.getId_ann() != null) {
                Intent intent = new Intent(this, ActivityDetailsAnnonce.class);
                intent.putExtra("id_ann", message.getId_ann());
                btn_see_annonce.setOnClickListener(v -> startActivity(intent));
                lltitre.setOnClickListener(v -> startActivity(intent));
            }
        } else {
            lltitre.setVisibility(View.GONE);
            txt_no_annonce_error.setVisibility(View.VISIBLE);
        }

    }

    private void populateUser(User user) {
        if (user != null) {
            Glide.with(DefaultMessagesActivity.this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.color.colorPrimaryDark)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(user.getPhoto())
                    .thumbnail(0.1f)
                    .into(txt_username_logo_ann);
            txt_username.setText(user.getUsername());
        }

    }

    @Override
    public void onAddAttachments() {

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        Calendar cal = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        String strDate = sdf.format(cal.getTime());
        Message.Messages messages = new Message.Messages();
        messages.setSender(senderId);
        messages.setMessage(input.toString());
        messages.setCreated_at(strDate);
        messages.setType("text");
        messages.setMessage_id(Long.toString(UUID.randomUUID().getLeastSignificantBits()));
        messagesAdapter.addToStart(messages, true);

        Call<ResponseBody> call = api.createMessagesWithoutPictures(
                input.toString(),
                "text",
                discussion_id,
                0,
                strDate,
                token
        );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.d("ActivityMessage ", response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("ActivityMessage ", t.toString());
            }
        });

        return true;
    }

    @Override
    public void onStartTyping() {
        Log.d("Typing listener", "Entrain d'ecrire");
    }

    @Override
    public void onStopTyping() {
        Log.d("Typing listener", "N'ecris pas");
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, "Copié", true);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    private MessagesListCustomAdapter.Formatter<Message.Messages> getMessageStringFormatter() {
        return message -> {
            String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.getCreated_at());

            String text = message.getMessage();
            if (text == null) text = "[attachment]";

            return String.format(Locale.getDefault(), "%s: %s (%s)",
                    message.getSender(), text, createdAt);
        };
    }
}