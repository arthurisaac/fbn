package bf.fasobizness.bafatech.activities.user.messaging

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce
import bf.fasobizness.bafatech.adapters.MessagesList
import bf.fasobizness.bafatech.adapters.MessagesListCustomAdapter
import bf.fasobizness.bafatech.helper.ProgressRequestBody
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.interfaces.UploadCallbacks
import bf.fasobizness.bafatech.models.Message
import bf.fasobizness.bafatech.models.User
import bf.fasobizness.bafatech.utils.AppUtils
import bf.fasobizness.bafatech.utils.DatabaseManager
import bf.fasobizness.bafatech.utils.FileCompressingUtil
import bf.fasobizness.bafatech.utils.MySharedManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessageInput.*
import com.zhihu.matisse.internal.utils.PathUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

open class DefaultMessagesActivity : AppCompatActivity(), UploadCallbacks, InputListener, AttachmentsListener, TypingListener, MessagesListCustomAdapter.SelectionListener, MessagesListCustomAdapter.OnLoadMoreListener {
    private lateinit var messagesList: MessagesList
    private lateinit var menu: Menu
    private var selectionCount = 0
    private lateinit var imageLoader: ImageLoader
    private val api = RetrofitClient.getClient().create(API::class.java)
    private var discussion_id: String? = null
    private var token: String? = null
    private var senderId: String? = null
    private lateinit var iv_affiche: ImageView
    private lateinit var txt_username_logo_ann: ImageView
    private lateinit var txt_titre_annonce: TextView
    private lateinit var txt_username: TextView
    private lateinit var txt_no_annonce_error: TextView
    private lateinit var btn_see_annonce: Button
    private lateinit var lltitre: LinearLayout
    private lateinit var databaseManager: DatabaseManager
    private var images: ArrayList<Image> = ArrayList()
    // private val tv_message: TextView

    // private WebSocket webSocket;
    protected lateinit var messagesAdapter: MessagesListCustomAdapter<Message.Messages>
    private lateinit var notificationManager: NotificationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_messages)
        val intent = intent
        if (intent.getStringExtra("discussion_id") != null) discussion_id = intent.getStringExtra("discussion_id") else finish()
        // if (intent.getStringExtra("new") != null) feedDB()
        imageLoader = ImageLoader { imageView: ImageView?, url: String?, _: Any? ->
            Glide.with(this@DefaultMessagesActivity)
                    .setDefaultRequestOptions(
                            RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(url)
                    .thumbnail(0.1f)
                    .into(imageView!!)
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        txt_username_logo_ann = findViewById(R.id.txt_username_logo_ann)
        txt_username = findViewById(R.id.txt_username)
        txt_no_annonce_error = findViewById(R.id.txt_no_annonce_error)
        txt_titre_annonce = findViewById(R.id.txt_titre_annonce)
        btn_see_annonce = findViewById(R.id.btn_see_annonce)
        lltitre = findViewById(R.id.lltitre)
        iv_affiche = findViewById(R.id.affiche)
        val recordView = findViewById<RecordView>(R.id.record_view)
        val recordButton = findViewById<View>(R.id.record_button) as RecordButton
        recordButton.setRecordView(recordView)
        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart")
            }

            override fun onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel")
            }

            override fun onFinish(recordTime: Long) {
                //Stop Recording..
                // String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish")

                // Log.d("RecordTime", time);
            }

            override fun onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond")
            }
        })
        recordButton.setOnRecordClickListener { v: View? ->
            Toast.makeText(this@DefaultMessagesActivity, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
            Log.d("RecordButton", "RECORD BUTTON CLICKED")
        }
        recordView.setOnBasketAnimationEndListener { Log.d("RecordView", "Basket Animation Finished") }
        recordView.setSoundEnabled(false)

        //if (recordButton.isListenForRecord()) {
        // recordButton.setListenForRecord(false);
        //Toast.makeText(this, "onClickEnabled", Toast.LENGTH_SHORT).show();
        //} else {
        //  recordButton.setListenForRecord(true);
        // Toast.makeText(this, "onClickDisabled", Toast.LENGTH_SHORT).show();
        //}
        databaseManager = DatabaseManager(this)
        val sharedManager = MySharedManager(this)
        token = "Bearer " + sharedManager.token
        senderId = sharedManager.user
        messagesList = findViewById(R.id.messagesList)
        try {
            initAdapter()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val input = findViewById<View>(R.id.input) as MessageInput
        input.setInputListener(this)
        input.setTypingListener(this)
        input.setAttachmentsListener(this)

        //instantiateWebSocket();
    }

    override fun onStart() {
        super.onStart()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val msg = intent.getStringExtra("msg")
                try {
                    val `object` = JSONObject(msg)
                    val messages = Message.Messages()
                    messages.message_id = `object`.getString("message_id")
                    messages.message = `object`.getString("message")
                    messages.sender = `object`.getString("sender")
                    messages.discussion_id = `object`.getString("discussion_id")
                    messages.created_at = `object`.getString("created_at")
                    messages.type = `object`.getString("type")
                    if (messages.type == "image") {
                         messages.image = Message.Messages.Image(messages.message)
                    }
                    messagesAdapter.addToStart(messages, true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, IntentFilter("MyData"))
    }

    private fun initAdapter() {
        messagesAdapter = MessagesListCustomAdapter(senderId, imageLoader)
        val mMessages = databaseManager.getMessages(discussion_id!!)
        val mMessage = databaseManager.getMessage(discussion_id!!)
        messagesAdapter.addToEnd(mMessages, true)
        messagesList.setAdapter(messagesAdapter)
        // populateUser(mMessage.getUser())
        // populateAnnounce(mMessage)

        feedDB()
    }

    private fun requestMultiplePermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            showChooser()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Erreur de permission! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun showChooser() {
        ImagePicker.with(this)
                .setFolderMode(true)
                .setFolderTitle("Photos")
                .setRootDirectoryName(Config.ROOT_DIR_DCIM)
                .setDirectoryName("Faso Biz Ness")
                .setMultipleMode(false)
                .setShowNumberIndicator(true)
                .setMaxSize(1)
                .setLimitMessage("Vous pouvez selectionner 1 images")
                .setSelectedImages(images)
                .setRequestCode(100)
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            images = ImagePicker.getImages(data)
            sendPicture()

        }
        super.onActivityResult(requestCode, resultCode, data)
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
    private fun feedDB() {
        val call = api.getMessages(discussion_id, token)
        call.enqueue(object : Callback<Message?> {
            override fun onResponse(call: Call<Message?>, response: Response<Message?>) {
                if (response.isSuccessful) {
                    val message = response.body()
                    val messages: List<Message.Messages>? = null
                    var user: User? = User()
                    if (message != null) {
                        // messages = message.messages;
                        user = message.user
                        populateAnnounce(message)
                        populateUser(user)
                    }
                    /*if (messages != null) {
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
                    }*/
                } else {
                    Toast.makeText(this@DefaultMessagesActivity, "Code " + response.code(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Message?>, t: Throwable) {
                Log.d("messaging", t.toString())
                Toast.makeText(this@DefaultMessagesActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateAnnounce(message: Message) {
        lltitre.visibility = View.GONE
        txt_no_annonce_error.visibility = View.GONE
        if (message.isAnnonce) {
            lltitre.visibility = View.VISIBLE
            if (message.affiche != null) {
                Glide.with(this@DefaultMessagesActivity)
                        .setDefaultRequestOptions(
                                RequestOptions()
                                        .placeholder(R.color.colorPrimary)
                                        .error(R.color.colorPrimaryDark)
                                        .centerCrop()
                                        .override(400, 400)
                        )
                        .asBitmap()
                        .load(message.affiche)
                        .thumbnail(0.1f)
                        .into(iv_affiche)
                iv_affiche.contentDescription = message.affiche
            }
            if (message.titre != null) {
                txt_titre_annonce.text = message.titre
            }
            if (message.id_ann != null) {
                val intent = Intent(this, ActivityDetailsAnnonce::class.java)
                intent.putExtra("id_ann", message.id_ann)
                btn_see_annonce.setOnClickListener { v: View? -> startActivity(intent) }
                lltitre.setOnClickListener { v: View? -> startActivity(intent) }
            }
        } else {
            lltitre.visibility = View.GONE
            txt_no_annonce_error.visibility = View.VISIBLE
        }
    }

    private fun populateUser(user: User?) {
        if (user != null) {
            Glide.with(this@DefaultMessagesActivity)
                    .setDefaultRequestOptions(
                            RequestOptions()
                                    .placeholder(R.color.colorPrimaryDark)
                                    .error(R.drawable.user)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(user.photo)
                    .thumbnail(0.1f)
                    .into(txt_username_logo_ann)
            txt_username.text = user.username
        }
    }

    override fun onAddAttachments() {
        requestMultiplePermissions()
    }

    private fun sendPicture() {
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        for (i in images.indices) {
            parts.add(prepareFilePart("image$i", images[i].uri))
        }
        val message = createPart(images[0].path)
        val type = createPart("image")
        val discussion = createPart(discussion_id!!)
        val size = createPart(parts.size.toString() + "")

        val cal = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS")
        val strDate = sdf.format(cal.time)
        val messages = Message.Messages()
        messages.image = Message.Messages.Image(images[0].path)
        messages.sender = senderId
        messages.message = null
        messages.created_at = strDate
        messages.type = "Image"
        messages.message_id = UUID.randomUUID().leastSignificantBits.toString()
        messagesAdapter.addToStart(messages, true)
        val call = api.createMessagesWithPictures(
                message,
                type,
                discussion,
                size,
                parts,
                token
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("ActivityMessage ", response.toString())
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("ActivityMessage ", t.toString())
            }
        })
    }

    override fun onSubmit(input: CharSequence): Boolean {
        val cal = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS")
        val strDate = sdf.format(cal.time)
        val messages = Message.Messages()
        messages.sender = senderId
        messages.message = input.toString()
        messages.created_at = strDate
        messages.type = "text"
        messages.message_id = UUID.randomUUID().leastSignificantBits.toString()
        messagesAdapter.addToStart(messages, true)
        val call = api.createMessagesWithoutPictures(
                input.toString(),
                "text",
                discussion_id,
                0,
                strDate,
                token
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("ActivityMessage ", response.toString())
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("ActivityMessage ", t.toString())
            }
        })
        return true
    }

    private fun getFile(context: Context, uri: Uri): File? {
        val path = PathUtils.getPath(context, uri)
        if (path != null) {
            if (isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    private fun prepareFilePart(part: String, uri: Uri): MultipartBody.Part {
        val file = getFile(this, uri)
        //val fileCompressingUtil = FileCompressingUtil()
        //val compressedFile = fileCompressingUtil.saveBitmapToFile(file)
        val requestFile = ProgressRequestBody(file, this)
        return MultipartBody.Part.createFormData(part, file!!.name, requestFile)
    }

    private fun createPart(id_ann: String): RequestBody {
        return RequestBody.create(
                MultipartBody.FORM, id_ann)
    }

    override fun onStartTyping() {
        Log.d("Typing listener", "Entrain d'ecrire")
    }

    override fun onStopTyping() {
        Log.d("Typing listener", "N'ecris pas")
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {}

    override fun onSelectionChanged(count: Int) {
        selectionCount = count
        menu.findItem(R.id.action_delete).isVisible = count > 0
        menu.findItem(R.id.action_copy).isVisible = count > 0
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.chat_actions_menu, menu)
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_delete) {
            messagesAdapter.deleteSelectedMessages()
        } else if (itemId == R.id.action_copy) {
            messagesAdapter.copySelectedMessagesText(this, messageStringFormatter, true)
            AppUtils.showToast(this, "Copié", true)
        }
        return true
    }

    override fun onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed()
        } else {
            messagesAdapter.unselectAllItems()
        }
    }

    private val messageStringFormatter: MessagesListCustomAdapter.Formatter<Message.Messages>
        get() = MessagesListCustomAdapter.Formatter { message: Message.Messages ->
            val createdAt = SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.created_at)
            var text = message.message
            if (text == null) text = "[attachment]"
            String.format(Locale.getDefault(), "%s: %s (%s)",
                    message.sender, text, createdAt)
        }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "bf.fasobizness.bafatech"
    }

    override fun onFinish() {

    }

    override fun onProgressUpdate(percentage: Int) {

    }

    override fun uploadStart() {

    }

    override fun onError() {

    }
}