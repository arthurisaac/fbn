package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import bf.fasobizness.bafatech.helper.MessagesListStyleCustom;
import bf.fasobizness.bafatech.helper.RecyclerScrollMoreListener;
import bf.fasobizness.bafatech.interfaces.IMessage;

public class MessagesList extends RecyclerView {
    private MessagesListStyleCustom messagesListStyle;

    public MessagesList(Context context) {
        super(context);
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseStyle(context, attrs);
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseStyle(context, attrs);
    }

    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to MessagesList. Use #setAdapter(MessagesListAdapter) instead.");
    }

    public <MESSAGE extends IMessage> void setAdapter(MessagesListCustomAdapter<MESSAGE> adapter) {
        this.setAdapter(adapter, true);
    }

    public <MESSAGE extends IMessage> void setAdapter(MessagesListCustomAdapter<MESSAGE> adapter, boolean reverseLayout) {
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, reverseLayout);
        this.setItemAnimator(itemAnimator);
        this.setLayoutManager(layoutManager);
        adapter.setLayoutManager(layoutManager);
        adapter.setStyle(this.messagesListStyle);
        this.addOnScrollListener(new RecyclerScrollMoreListener(layoutManager, adapter));
        super.setAdapter(adapter);
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        this.messagesListStyle = MessagesListStyleCustom.parse(context, attrs);
    }
}
