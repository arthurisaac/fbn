package bf.fasobizness.bafatech.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnMessageListener;
import bf.fasobizness.bafatech.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    final private Context mContext;
    private final ArrayList<Message.Messages> mMessages;
    final private String userId;
    final private int SELF = 100;
    private OnMessageListener onMessageListener;


    public MessageAdapter(Context context, ArrayList<Message.Messages> messages, String userId) {
        this.mContext = context;
        this.mMessages = messages;
        this.userId = userId;
    }

    private static String getTimeStamp(String dateStr) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        try {
            Date date = format.parse(dateStr);
            @SuppressLint("SimpleDateFormat") Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            timestamp = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;

        if (i == SELF) {
            itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.chat_item_self, viewGroup, false);
        } else {
            itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.chat_item_other, viewGroup, false);
        }

        return new MessageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageHolder messageHolder, int i) {
        Message.Messages messages = mMessages.get(i);
        if (messages.getType().equals("image")) {
            messageHolder.message.setVisibility(View.GONE);
            messageHolder.imageView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(messages.getMessage())
                    .thumbnail(0.1f)
                    .into(messageHolder.imageView);

            if (messages.getCreated_at() != null) {
                messageHolder.timestamp.setText(getTimeStamp(messages.getCreated_at()));
            }

        } else {
            messageHolder.imageView.setVisibility(View.GONE);
            messageHolder.message.setText(messages.getMessage());
            messageHolder.timestamp.setText(getTimeStamp(messages.getCreated_at()));

            if (messages.getIsread_receiver().equals("1")) {
                messageHolder.readView.setText("lu");
            } else {
                messageHolder.readView.setText("envoyé");
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message.Messages messages = mMessages.get(position);
        if (messages.getSender().equals(userId)) {
            return SELF;
        }

        return position;

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        final TextView message, timestamp, readView;
        private final ImageView imageView;

        MessageHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            timestamp = itemView.findViewById(R.id.timestamp);
            imageView = itemView.findViewById(R.id.image);
            readView = itemView.findViewById(R.id.read);

            imageView.setOnClickListener(v -> {
                if (onMessageListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onMessageListener.onMessageClicked(position);
                    }
                }
            });

        }
    }
}
