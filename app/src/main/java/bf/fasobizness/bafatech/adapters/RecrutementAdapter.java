package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.models.Recruit;

public class RecrutementAdapter extends RecyclerView.Adapter<RecrutementAdapter.RecrutementHolder>
 implements Filterable {

    private final Context mContext;
    private final ArrayList<Recruit.Recrutement> mRecrutements;
    private ArrayList<Recruit.Recrutement> mRecrutementsFiltre;
    private OnItemListener onItemListener;

    public RecrutementAdapter(Context context, ArrayList<Recruit.Recrutement> recrutements) {
        this.mContext = context;
        this.mRecrutements = recrutements;
        this.mRecrutementsFiltre = recrutements;
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

        final Recruit.Recrutement recrutement = mRecrutementsFiltre.get(i);

        String titre = recrutement.getNom_r();
        String date_pub = this.mContext.getString(R.string.publie_le, recrutement.getDate_pub());
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
        return mRecrutementsFiltre.size();
    }

    public void clearAll() {
        mRecrutements.clear();
        mRecrutementsFiltre.clear();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString();
                if (query.isEmpty()) {
                    mRecrutementsFiltre = mRecrutements;
                } else {
                    ArrayList<Recruit.Recrutement> recrutements = new ArrayList<>();
                    for (Recruit.Recrutement recrutement: mRecrutements) {
                        if (recrutement.getNom_r().toLowerCase().contains(query)){
                            recrutements.add(recrutement);
                        }
                    }
                    mRecrutementsFiltre = recrutements;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mRecrutementsFiltre;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mRecrutementsFiltre = (ArrayList<Recruit.Recrutement>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class RecrutementHolder extends RecyclerView.ViewHolder {

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
