package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.models.Announce;


public class AnnounceAdapter extends RecyclerView.Adapter<AnnounceAdapter.AnnonceHolder> {

    final private Context mContext;
    private final ArrayList<Announce.Annonce> mAnnonces;
    private OnAnnonceListener onAnnonceListener;
    private OnBottomReachedListener onBottomReachedListener;

    public AnnounceAdapter(Context context, ArrayList<Announce.Annonce> annonces) {
        this.mContext = context;
        this.mAnnonces = annonces;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void setOnItemListener(OnAnnonceListener onItemListener) {
        this.onAnnonceListener = onItemListener;
    }

    @NonNull
    @Override
    public AnnonceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_annonces, viewGroup, false);
        return new AnnonceHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AnnonceHolder annonceHolder, int i) {
        final Announce.Annonce annonce = mAnnonces.get(i);

        String vip = annonce.getVip();

        annonceHolder.TexteView.setText(annonce.getTitre().toUpperCase());
        annonceHolder.btnVueView.setText(annonce.getVue());
        annonceHolder.AfficheView.setContentDescription(annonce.getTitre());

        try {

            Glide.with(mContext)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                                    .centerCrop()
                                    .override(400, 400)
                    )
                    .asBitmap()
                    .load(annonce.getAffiche())
                    .thumbnail(0.1f)
                    .into(annonceHolder.AfficheView);
            annonceHolder.AfficheView.setCornerRadius(8);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (vip.equals("1")) {
            annonceHolder.vipView.setVisibility(View.VISIBLE);
        } else {
            annonceHolder.vipView.setVisibility(View.GONE);
        }

        ViewCompat.setTransitionName(annonceHolder.AfficheView, annonce.getId_ann());

        annonceHolder.itemView.setOnClickListener(view -> onAnnonceListener.onAnnonceClicked(annonceHolder.getAdapterPosition()));

        if (i == mAnnonces.size() - 1) {
            onBottomReachedListener.onBottomReached();
        }
    }

    @Override
    public int getItemCount() {
        return mAnnonces.size();
    }

    public void clearAll() {
        mAnnonces.clear();
    }

    public interface OnBottomReachedListener {

        void onBottomReached();

    }

    class AnnonceHolder extends RecyclerView.ViewHolder {

        final TextView TexteView;
        final TextView btnVueView;
        final ImageView vipView;
        final RoundedImageView AfficheView;

        AnnonceHolder(@NonNull View itemView) {
            super(itemView);

            TexteView = itemView.findViewById(R.id.txt_texte_ann);
            btnVueView = itemView.findViewById(R.id.txt_nb_view_ann);
            AfficheView = itemView.findViewById(R.id.txt_affiche_ann);
            vipView = itemView.findViewById(R.id.vip);
        }
    }

}
