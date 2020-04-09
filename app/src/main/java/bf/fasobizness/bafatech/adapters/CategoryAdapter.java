package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.interfaces.OnItemListener;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.holder> {
    private final Context context;
    private final String[] categories;
    private final int[] icons;
    private OnItemListener onItemListener;

    public CategoryAdapter(Context context, String[] categorie) {
        this.context = context;
        categories = categorie;
        icons = new int[]{R.drawable.ic_home, R.drawable.ic_balero, R.drawable.ic_shirt, R.drawable.ic_dress, R.drawable.ic_pill, R.drawable.ic_monitor, R.drawable.ic_ipad, R.drawable.ic_motocross, R.drawable.ic_microwave_oven, R.drawable.ic_sofa, R.drawable.ic_gamer, R.drawable.ic_solar_panel, R.drawable.ic_diet, R.drawable.ic_runer_silhouette_running_fast, R.drawable.ic_education, R.drawable.ic_brick, R.drawable.ic_shop, R.drawable.ic_category};
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_category, parent, false);
        return new holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        holder.tv_category.setText(categories[position]);
        holder.iv_categorie.setBackgroundResource(icons[position]);
        holder.cardView.setOnClickListener(v -> onItemListener.onItemClicked(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }


    class holder extends RecyclerView.ViewHolder {
        private final TextView tv_category;
        private final ImageView iv_categorie;
        private final CardView cardView;

        holder(@NonNull View itemView) {
            super(itemView);
            tv_category = itemView.findViewById(R.id.txt_categorie);
            iv_categorie = itemView.findViewById(R.id.iv_category);
            cardView = itemView.findViewById(R.id.card);
        }
    }
}
