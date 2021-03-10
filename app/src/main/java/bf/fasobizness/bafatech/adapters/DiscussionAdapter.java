package bf.fasobizness.bafatech.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Discussion;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionHolder> {

    private static String today;
    private final Context mContext;
    private final ArrayList<Discussion.Discussions> mDiscussion;
    private OnItemListener onItemListener;

    public DiscussionAdapter(Context context, ArrayList<Discussion.Discussions> discussions) {
        this.mContext = context;
        mDiscussion = discussions;

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @SuppressLint("SimpleDateFormat")
    private static String getTimeStamp(String dateStr) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            assert date != null;
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm") : new SimpleDateFormat("dd LLL, HH:mm");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public DiscussionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_discussion, viewGroup, false);
        return new DiscussionHolder(v, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionHolder discussionHolder, int i) {
        Discussion.Discussions discussion = mDiscussion.get(i);

        discussionHolder.v_username.setText(discussion.getTitre());
        discussionHolder.v_message.setText(discussion.getMessage());

        if (discussion.getCount() > 0) {
            discussionHolder.v_count.setText(String.valueOf(discussion.getCount()));
            discussionHolder.v_count.setVisibility(View.VISIBLE);
        } else {
            discussionHolder.v_count.setVisibility(View.GONE);
        }

        if (discussion.getTimestamp() != null) {
            discussionHolder.v_timestamp.setText(getTimeStamp(discussion.getTimestamp()));
        }

    }

    @Override
    public int getItemCount() {
        return mDiscussion.size();
    }

    class DiscussionHolder extends RecyclerView.ViewHolder {
        final TextView v_username;
        final TextView v_message;
        final TextView v_count;
        final TextView v_timestamp;

        DiscussionHolder(@NonNull View itemView, final OnItemListener onItemListener) {
            super(itemView);

            v_username = itemView.findViewById(R.id.username);
            v_message = itemView.findViewById(R.id.message);
            v_count = itemView.findViewById(R.id.count);
            v_timestamp = itemView.findViewById(R.id.timestamp);

            itemView.setOnClickListener(v -> {
                if (onItemListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemListener.onItemClicked(position);
                    }
                }
            });
        }
    }
}
