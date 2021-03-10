package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Announce;

public class IllustrationEditAdapter extends RecyclerView.Adapter<IllustrationEditAdapter.IllustrationHolder> {
    private final Context mContext;
    private final ArrayList<Announce.Annonce.Illustration> mIllustrations;
    private final ArrayList<Announce.Annonce.Illustration> checked;
    private OnItemListener onItemListener;

    public IllustrationEditAdapter(Context context, ArrayList<Announce.Annonce.Illustration> illustrations) {
        this.mContext = context;
        this.mIllustrations = illustrations;
        this.checked = new ArrayList<>();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public IllustrationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_illlustration_edit, viewGroup, false);
        return new IllustrationHolder(v, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull IllustrationHolder illustrationHolder, int i) {
        final Announce.Annonce.Illustration illustration = mIllustrations.get(i);

        Glide.with(mContext)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .placeholder(R.color.colorPrimary)
                                .error(R.color.colorPrimaryDark)
                                .centerCrop()
                                .override(400, 400)
                )
                .asBitmap()
                .load(illustration.getNom())
                .into(illustrationHolder.illView);
        illustrationHolder.illView.setContentDescription(illustration.getNom());

        illustrationHolder.cb_ill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checked.add(mIllustrations.get(i));
            } else {
                checked.remove(mIllustrations.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mIllustrations.size();
    }

    public void clearAll() {
        mIllustrations.clear();
    }

    public ArrayList<Announce.Annonce.Illustration> getChecked() {
        return checked;
    }

    static class IllustrationHolder extends RecyclerView.ViewHolder {
        private final ImageView illView;
        private final CheckBox cb_ill;

        IllustrationHolder(@NonNull View itemView, final OnItemListener onItemListener) {
            super(itemView);

            illView = itemView.findViewById(R.id.iv_illustration);
            cb_ill = itemView.findViewById(R.id.cb_ill);

            itemView.setOnClickListener(v -> {
                if (onItemListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemListener.onItemClicked(position);
                    }
                }
            });
        }
    }
}
