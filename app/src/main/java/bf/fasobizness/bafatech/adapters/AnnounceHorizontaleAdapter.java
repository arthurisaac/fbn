package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonces;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnAnnonceListener;
import bf.fasobizness.bafatech.interfaces.OnLongItemListener;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AnnounceHorizontaleAdapter extends RecyclerView.Adapter<AnnounceHorizontaleAdapter.AnnonceHolder> implements Filterable {

    final private Context mContext;
    private final ArrayList<Announce.Annonce> mAnnonces;
    private ArrayList<Announce.Annonce> mAnnoncesFiltre;
    // private SparseBooleanArray mSelectedItems;
    private final SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private final API api = RetrofitClient.getClient().create(API.class);
    private final MySharedManager sharedManager;

    private boolean mIsInChoiceMode;

    public void switchSelectedState(int position) {
        if (mSelectedItems.get(position)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyDataSetChanged();
    }

    public void clearSelectedStated() {
        List<Integer> selection = getSelectedItems();
        mSelectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public void setIsInChoiceMode(boolean isInChoiceMode) {
        this.mIsInChoiceMode = isInChoiceMode;
    }

    public boolean getIsInChoiceMode() {
        return mIsInChoiceMode;
    }

    public void beginChoiceMode(int position) {
        // mSelectedItems = new SparseBooleanArray();
        clearSelectedStated();
        setIsInChoiceMode(true);
        switchSelectedState(position);
    }

    public AnnounceHorizontaleAdapter(Context context, ArrayList<Announce.Annonce> annonces) {
        this.mContext = context;
        this.mAnnonces = annonces;
        this.mAnnoncesFiltre = annonces;
        sharedManager = new MySharedManager(context);
    }

    @NonNull
    @Override
    public AnnonceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_annonces_horizontal, viewGroup, false);
        return new AnnonceHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AnnonceHolder annonceHolder, int i) {
        final Announce.Annonce annonce = mAnnoncesFiltre.get(i);

        String vip = annonce.getVip();

        annonceHolder.TexteView.setText(annonce.getTitre().toUpperCase());
        if (annonce.getPrix().isEmpty() || annonce.getPrix() == null || annonce.getPrix().equals("0")) {
            annonceHolder.PrixView.setText(R.string.prix_sur_demande);
        } else {
            String prix = annonce.getPrix() + " F CFA";
            annonceHolder.PrixView.setText(prix);
        }
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
            annonceHolder.AfficheView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ActivityDetailsAnnonces.class);
                intent.putExtra("id_ann", annonce.getId_ann());
                intent.putExtra("affiche", annonce.getAffiche());
                intent.putExtra("annonces", mAnnonces);
                intent.putExtra("position", i);
                mContext.startActivity(intent);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (vip.equals("1")) {
            annonceHolder.vipView.setVisibility(View.VISIBLE);
        } else {
            annonceHolder.vipView.setVisibility(View.GONE);
        }

        if (annonce.getFavori() != null && annonce.getFavori().equals("1")) {
            annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_yellow);
            annonceHolder.favoriteView.setTag(R.drawable.ic_star_yellow);
        } else {
            annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_white);
            annonceHolder.favoriteView.setTag(R.drawable.ic_star_white);
        }

        if (mIsInChoiceMode) {
            annonceHolder.checkBox.setVisibility(View.VISIBLE);
            annonceHolder.checkBox.setChecked(mSelectedItems.get(i));
        } else {
            annonceHolder.checkBox.setChecked(false);
            annonceHolder.checkBox.setVisibility(View.GONE);
        }

        annonceHolder.favoriteView.setOnClickListener(v -> {
            if (!sharedManager.getUser().isEmpty()) {
                Integer resource = (Integer) annonceHolder.favoriteView.getTag();
                if ( (resource != null) && resource == R.drawable.ic_star_white) {
                    annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_yellow);
                    annonceHolder.favoriteView.setTag(R.drawable.ic_star_yellow);
                } else {
                    annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_white);
                    annonceHolder.favoriteView.setTag(R.drawable.ic_star_white);
                }

                Call<MyResponse> call = api.setAnnouncesActions("favorite", annonce.getId_ann(), sharedManager.getUser());
                call.enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                        if (response.isSuccessful()) {
                            if (annonce.getFavori() != null && annonce.getFavori().equals("1")) {
                                // annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_white);
                                annonce.setFavoris("0");
                            } else {
                                // annonceHolder.favoriteView.setImageResource(R.drawable.ic_star_yellow);
                                annonce.setFavoris("1");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                        //Toast.makeText(mContext, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "");
            }

        });
    }

    @Override
    public int getItemCount() {
        return mAnnoncesFiltre.size();
    }

    public void clearAll() {
        mAnnonces.clear();
        mAnnoncesFiltre.clear();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString();
                if (query.isEmpty()) {
                    mAnnoncesFiltre = mAnnonces;
                } else {
                    ArrayList<Announce.Annonce> annonces = new ArrayList<>();
                    for (Announce.Annonce annonce : mAnnonces) {
                        if (annonce.getTitre().toLowerCase().contains(query) ||
                                annonce.getTexte().toLowerCase().contains(query)
                        ) {
                            annonces.add(annonce);
                        }
                    }
                    mAnnoncesFiltre = annonces;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mAnnoncesFiltre;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mAnnoncesFiltre = (ArrayList<Announce.Annonce>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface OnBottomReachedListener {

        void onBottomReached();

    }

    static class AnnonceHolder extends RecyclerView.ViewHolder {

        final TextView TexteView;
        final TextView PrixView;
        // final TextView btnVueView;
        final ImageView vipView;
        private final ImageButton favoriteView;
        final RoundedImageView AfficheView;
        final CheckBox checkBox;

        AnnonceHolder(@NonNull View itemView) {
            super(itemView);

            TexteView = itemView.findViewById(R.id.txt_texte_ann);
            PrixView = itemView.findViewById(R.id.txt_prix_ann);
            // btnVueView = itemView.findViewById(R.id.txt_nb_view_ann);
            AfficheView = itemView.findViewById(R.id.txt_affiche_ann);
            vipView = itemView.findViewById(R.id.vip);
            favoriteView = itemView.findViewById(R.id.favorite);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

}
