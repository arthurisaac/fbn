package bf.fasobizness.bafatech.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnImageListener;

public class ImagesAdapter extends PagerAdapter {
    private final Context context;
    private final ArrayList<String> Images;
    private final LayoutInflater layoutInflater;
    private OnImageListener onImageListener;

    public ImagesAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        Images = images;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setOnImageListener(OnImageListener onImageListener) {
        this.onImageListener = onImageListener;
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
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.swipe_layout_pager, null);

        ImageView affiche = view.findViewById(R.id.iv_affiche_ad);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                )
                .addDefaultRequestListener(new RequestListener<Object>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .asBitmap()
                .load(Images.get(position))
                .thumbnail(0.1f)
                .into(affiche);
        affiche.setContentDescription(Images.get(position));

        affiche.setOnClickListener(v -> onImageListener.onImageClicked(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
