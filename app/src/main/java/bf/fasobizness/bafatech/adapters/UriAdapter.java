package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;

public class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

    private final List<Uri> mUris;
    private final Context mContext;
    private OnItemListener onItemListener;

    public UriAdapter(Context context, List<Uri> uris) {
        mUris = uris;
        mContext = context;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UriViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
    }

    @Override
    public void onBindViewHolder(UriViewHolder holder, int position) {
        Glide.with(mContext)
                .load(mUris.get(position))
                .into(holder.imageView);

        holder.supp.setOnClickListener(v -> {
            if (onItemListener != null) {
                onItemListener.onItemClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUris == null ? 0 : mUris.size();
    }

    public void remove(int position) {
        mUris.remove(position);
    }

    class UriViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageButton supp;

        UriViewHolder(View contentView) {
            super(contentView);
            imageView = contentView.findViewById(R.id.image_view);
            supp = contentView.findViewById(R.id.btn_remove);
        }
    }
}
