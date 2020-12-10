package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;

public class AnnoncePhotosAdapter extends RecyclerView.Adapter<AnnoncePhotosAdapter.ImageViewHolder> {

    private final List<Image> mImages;
    private final Context mContext;
    private OnItemListener onItemListener;

    public AnnoncePhotosAdapter(Context context, List<Image> Images) {
        mImages = Images;
        mContext = context;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Glide.with(mContext)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                                .override(400, 400)
                )
                .load(mImages.get(position).getUri())
                .into(holder.imageView);

        holder.supp.setOnClickListener(v -> {
            if (onItemListener != null) {
                onItemListener.onItemClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages == null ? 0 : mImages.size();
    }

    public void remove(int position) {
        mImages.remove(position);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageButton supp;

        ImageViewHolder(View contentView) {
            super(contentView);
            imageView = contentView.findViewById(R.id.image_view);
            supp = contentView.findViewById(R.id.btn_remove);
        }
    }
}
