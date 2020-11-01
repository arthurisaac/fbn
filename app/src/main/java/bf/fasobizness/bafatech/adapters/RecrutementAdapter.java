package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recruit;

public class RecrutementAdapter extends RecyclerView.Adapter<RecrutementAdapter.RecrutementHolder> {

    private final Context mContext;
    private final ArrayList<Recruit.Recrutement> mRecrutements;
    private OnItemListener onItemListener;

    public RecrutementAdapter(Context context, ArrayList<Recruit.Recrutement> recrutements) {
        this.mContext = context;
        this.mRecrutements = recrutements;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public RecrutementHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_recrutement, viewGroup, false);
        return new RecrutementHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecrutementHolder recrutementHolder, int i) {

        final Recruit.Recrutement recrutement = mRecrutements.get(i);

        String titre = recrutement.getNom_r();
        String date_pub = recrutement.getDate_pub();
        // String desc = recrutement.getDesc();
        String domaine = recrutement.getDomaine();
        String vue = recrutement.getVue();

        recrutementHolder.nom_entV.setText(titre);
        recrutementHolder.dateV.setText(date_pub);
        // recrutementHolder.descriptionV.setText(desc);
        recrutementHolder.domaineV.setText(domaine);
        recrutementHolder.vueV.setText(vue);

        /*Spannable spannable = new SpannableString("... Lire la suite");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        recrutementHolder.descriptionV.append(spannable);*/

        ViewCompat.setTransitionName(recrutementHolder.viewStart, recrutement.getId_recr());
        recrutementHolder.itemView.setOnClickListener(view -> onItemListener.onItemClicked(recrutementHolder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mRecrutements.size();
    }

    class RecrutementHolder extends RecyclerView.ViewHolder {

        private final TextView nom_entV;
        private final TextView domaineV;
        // private final TextView descriptionV;
        private final TextView dateV;
        private final TextView vueV;
        private final View viewStart;

        RecrutementHolder(@NonNull View itemView) {
            super(itemView);

            nom_entV = itemView.findViewById(R.id.tv_recrutement_titre);
            domaineV = itemView.findViewById(R.id.tv_recrutement_domaine);
            // descriptionV = itemView.findViewById(R.id.tv_recrutement_desc);
            dateV = itemView.findViewById(R.id.tv_recrutement_date_pub);
            vueV = itemView.findViewById(R.id.txt_vue);
            viewStart = itemView.findViewById(R.id.viewStart);
        }
    }
}
