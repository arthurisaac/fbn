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

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;

public class IllustrationRecAdapter extends RecyclerView.Adapter<IllustrationRecAdapter.IllustrationHolder> {
    private final Context mContext;
    private final ArrayList<String> mIllustrations;
    private OnItemListener onItemListener;

    public IllustrationRecAdapter(Context context, ArrayList<String> illustrations) {
        this.mContext = context;
        this.mIllustrations = illustrations;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public IllustrationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_illlustration, viewGroup, false);
        return new IllustrationHolder(v, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull IllustrationHolder illustrationHolder, int i) {
        final String nom = mIllustrations.get(i);

        String ext = getExtension(nom);
        if (ext != null && ext.equals("pdf")) {
            illustrationHolder.illView.setImageResource(R.drawable.pdf);
        } else {
            Glide.with(mContext)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.color.colorPrimary).error(R.color.colorPrimaryDark))
                    .load(nom)
                    .into(illustrationHolder.illView);
            illustrationHolder.illView.setContentDescription(nom);
        }
    }

    @Override
    public int getItemCount() {
        return mIllustrations.size();
    }

    private String getExtension(String filePath) {
        int strLenght = filePath.lastIndexOf(".");
        if (strLenght > 0) {
            return filePath.substring(strLenght + 1).toLowerCase();
        }
        return null;
    }

    class IllustrationHolder extends RecyclerView.ViewHolder {
        private final ImageView illView;

        IllustrationHolder(@NonNull View itemView, final OnItemListener onItemListener) {
            super(itemView);

            illView = itemView.findViewById(R.id.iv_annonce_illustation);
            //nomView = itemView.findViewById( R.id.ill_nom );

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
