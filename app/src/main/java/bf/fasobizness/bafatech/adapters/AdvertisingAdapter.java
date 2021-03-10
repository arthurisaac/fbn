package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.models.Advertising;

class AdvertisingAdapter extends BaseAdapter {

    private final ArrayList<Advertising.Ads> arrayList;
    private final Context mContext;

    public AdvertisingAdapter(Context context, ArrayList<Advertising.Ads> advertisings) {
        this.mContext = context;
        this.arrayList = advertisings;
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Advertising.Ads ad = arrayList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.swipe_layout_pager, parent, false);

            ImageView affiche = convertView.findViewById(R.id.iv_affiche_ad);
            final ProgressBar progressBar = convertView.findViewById(R.id.progress_bar);
            Glide.with(mContext)
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
                    .load(ad.getLien())
                    .thumbnail(0.1f)
                    .into(affiche);
            affiche.setContentDescription(ad.getLien());
        }

        return convertView;
    }
}
