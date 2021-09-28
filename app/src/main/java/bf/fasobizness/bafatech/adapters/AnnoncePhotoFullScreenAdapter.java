package bf.fasobizness.bafatech.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;

import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Announce;

public class AnnoncePhotoFullScreenAdapter extends PagerAdapter {
    private final Context context;
    private final List<Announce.Annonce.Illustration> Images;
    private final LayoutInflater layoutInflater;
    private OnItemListener onItemListener;

    public AnnoncePhotoFullScreenAdapter(Context context, List<Announce.Annonce.Illustration> images) {
        this.context = context;
        Images = images;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public int getCount() {
        return Images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        BigImageViewer.initialize(GlideImageLoader.with(context));
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.swipe_layout_pager, null);

        //TouchImageView affiche = view.findViewById(R.id.iv_affiche_ad);
        /*Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                )
                .addDefaultRequestListener(new RequestListener<Object>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .asBitmap()
                .load(Images.get(position).getNom())
                .thumbnail(0.1f)
                .into(affiche);*/
        BigImageView affiche = view.findViewById(R.id.iv_affiche_ad);
        affiche.showImage(Uri.parse(Images.get(position).getNom()));
        //final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        affiche.setContentDescription(Images.get(position).getNom());

        affiche.setOnClickListener(v -> onItemListener.onItemClicked(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
