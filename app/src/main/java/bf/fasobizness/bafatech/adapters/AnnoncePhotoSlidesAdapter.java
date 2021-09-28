package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.ActivityAnnoncePhotoFullScreen;
import bf.fasobizness.bafatech.activities.ActivityAnnoncePhotoList;
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.activities.ActivityPhotoList;
import bf.fasobizness.bafatech.models.Announce;

public class AnnoncePhotoSlidesAdapter extends
        SliderViewAdapter<AnnoncePhotoSlidesAdapter .SliderAdapterVH> {

    private Context context;
    private ArrayList<Announce.Annonce.Illustration> mSliderItems = new ArrayList<>();

    public AnnoncePhotoSlidesAdapter(Context context) {
        this.context = context;
    }

    public void renewItems(ArrayList<Announce.Annonce.Illustration> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(Announce.Annonce.Illustration sliderItem) {
        this.mSliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_annonce_slider, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        Announce.Annonce.Illustration sliderItem = mSliderItems.get(position);

        viewHolder.textViewDescription.setText("");
        viewHolder.textViewDescription.setTextSize(16);
        viewHolder.textViewDescription.setTextColor(Color.WHITE);
        Glide.with(viewHolder.itemView)
                .load(sliderItem.getNom())
                .fitCenter()
                .into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(v -> {
            if (mSliderItems.size() > 1) {
                Intent intent = new Intent(context, ActivityAnnoncePhotoList.class);
                intent.putExtra("images", mSliderItems);
                intent.putExtra("position", position);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, ActivityAnnoncePhotoFullScreen.class);
                intent.putExtra("images", mSliderItems);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}

