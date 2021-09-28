package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Announce;

public class AnnoncePhotoListAdapter extends RecyclerView.Adapter<AnnoncePhotoListAdapter.PhotoListHolder> {
    private final List<Announce.Annonce.Illustration> images;
    private final Context context;
    private OnItemListener onItemListener;

    public AnnoncePhotoListAdapter(Context context, List<Announce.Annonce.Illustration> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public AnnoncePhotoListAdapter.PhotoListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_photo_list_item, parent, false);
        return new AnnoncePhotoListAdapter.PhotoListHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnoncePhotoListAdapter.PhotoListHolder holder, int position) {
        Announce.Annonce.Illustration image = images.get(position);
        Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                                .override(400, 400)
                )
                .asBitmap()
                .load(image.getNom())
                .thumbnail(0.1f)
                .into(holder.photoListImage);
        holder.photoListImage.setOnClickListener(v -> onItemListener.onItemClicked(position));

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public class PhotoListHolder extends RecyclerView.ViewHolder {
        final ImageView photoListImage;

        public PhotoListHolder(@NonNull View itemView) {
            super(itemView);
            photoListImage = itemView.findViewById(R.id.photoListImage);
        }
    }
}
