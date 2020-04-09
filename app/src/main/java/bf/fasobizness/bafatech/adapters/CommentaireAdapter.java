package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Entreprise;

public class CommentaireAdapter extends RecyclerView.Adapter<CommentaireAdapter.CommentaireHolder> {

    private final Context mContext;
    private final ArrayList<Entreprise.Entreprises.Comment> mComments;
    private OnItemListener onItemListener;

    public CommentaireAdapter(Context context, ArrayList<Entreprise.Entreprises.Comment> commentaires) {
        this.mContext = context;
        this.mComments = commentaires;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CommentaireHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_commentaire, viewGroup, false);
        return new CommentaireHolder(v, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentaireHolder commentaireHolder, int i) {
        Entreprise.Entreprises.Comment commentaire = mComments.get(i);

        String comments = commentaire.getCommentaire();
        String date = commentaire.getDate();
        String heure = commentaire.getHeure();
        String username = commentaire.getUsername();
        String photo = commentaire.getPhoto();

        commentaireHolder.tv_username.setText(username);
        commentaireHolder.tv_commentaire.setText(comments);
        commentaireHolder.tv_date.setText(date);
        commentaireHolder.tv_heure.setText(heure);

        try {

            Glide.with(mContext)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.user).error(R.drawable.user))
                    .load(photo)
                    .into(commentaireHolder.ImPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    class CommentaireHolder extends RecyclerView.ViewHolder {

        private final TextView tv_username;
        private final TextView tv_commentaire;
        private final TextView tv_heure;
        private final TextView tv_date;
        private final ImageView ImPhoto;

        CommentaireHolder(@NonNull View itemView, final OnItemListener onItemListener) {
            super(itemView);

            tv_username = itemView.findViewById(R.id.txt_username_commentaire);
            tv_commentaire = itemView.findViewById(R.id.txt_commentaire);
            tv_date = itemView.findViewById(R.id.txt_date_pub_commentaire);
            tv_heure = itemView.findViewById(R.id.txt_heure_commentaire);
            ImPhoto = itemView.findViewById(R.id.txt_username_logo_commentaire);

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
