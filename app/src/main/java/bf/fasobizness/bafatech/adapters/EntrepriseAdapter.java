package bf.fasobizness.bafatech.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.fragments.FragmentNotConnected;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.OnEntrepriseListener;
import bf.fasobizness.bafatech.models.Entreprise;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntrepriseAdapter extends RecyclerView.Adapter<EntrepriseAdapter.EntrepriseHolder> {

    private static final String TAG = "EntrepriseAdapter";
    private final Context mContext;
    private final ArrayList<Entreprise.Entreprises> mEntreprise;
    private OnEntrepriseListener onEntrepriseListener;
    private String id_pers_fk, token;


    public EntrepriseAdapter(Context context, ArrayList<Entreprise.Entreprises> entreprises) {
        this.mContext = context;
        this.mEntreprise = entreprises;
    }

    public void setOnEntrepriseListener(OnEntrepriseListener onEntrepriseListener) {
        this.onEntrepriseListener = onEntrepriseListener;
    }

    @NonNull
    @Override
    public EntrepriseHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_entreprise_une, viewGroup, false);
        return new EntrepriseHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final EntrepriseHolder entrepriseHolder, int i) {
        final Entreprise.Entreprises entreprise = mEntreprise.get(i);

        MySharedManager sharedManager = new MySharedManager(mContext);
        id_pers_fk = sharedManager.getUser();
        token = sharedManager.getToken();

        final String nom = entreprise.getNom();
        String date_pub = entreprise.getDate_pub();
        String affiche = entreprise.getAffiche();

        final String desc = entreprise.getDesc();

        String vue = entreprise.getNbVue();
        String like = entreprise.getNbLike();
        String nbComment = entreprise.getNbComment();

        entrepriseHolder.nom_ent.setText(nom);
        entrepriseHolder.description_ent.setText(desc);

        entrepriseHolder.date_pub_ent.setText(date_pub);
        entrepriseHolder.nb_vue.setText(vue);
        entrepriseHolder.nb_comment.setText(nbComment);
        entrepriseHolder.nb_like.setText(like);


        Spannable spannable = new SpannableString("... Lire la suite");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        entrepriseHolder.description_ent.append(spannable);

        try {
            Glide.with(mContext)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.color.colorPrimary).error(R.color.colorPrimaryDark))
                    .load(affiche)
                    .into(entrepriseHolder.txt_affiche);
        } catch (Exception e) {
            e.printStackTrace();
        }

        entrepriseHolder.btn_share_ent.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBodyText = desc + ".. Téléchargez ou parcourez l'application Faso Biz Nèss pour en savoir plus. Pour android: http://bit.ly/AndroidFBN. Pour Iphone: http://bit.ly/IphoneFBN";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Decouvre l'entreprise " + nom);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            mContext.startActivity(Intent.createChooser(sharingIntent, "Partager avec"));
        });

        if (entreprise.getAimer().equals("1")) {
            entrepriseHolder.like.setBackgroundResource(R.drawable.ic_like_red);
        } else {
            entrepriseHolder.like.setBackgroundResource(R.drawable.ic_like_gray);
        }

        entrepriseHolder.like.setOnClickListener(v -> {
            if (!id_pers_fk.isEmpty()) {

                if (entreprise.getAimer().equals("1")) {
                    jaime(entreprise.getId());
                    entrepriseHolder.like.setBackgroundResource(R.drawable.ic_like_gray);
                    entreprise.setAimer("0");
                } else {
                    jaime(entreprise.getId());
                    entrepriseHolder.like.setBackgroundResource(R.drawable.ic_like_red);
                    entreprise.setAimer("1");
                }
            } else {
                FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                FragmentNotConnected notConnected = FragmentNotConnected.newInstance();
                notConnected.show(fragmentManager, "");
            }
        });

        entrepriseHolder.itemView.setOnClickListener(view -> onEntrepriseListener.onEntrepriseClicked(entrepriseHolder.getAdapterPosition()));
    }

    private void jaime(final String id_articl_fk) {
        API api = RetrofitClient.getClient().create(API.class);
        Call<MyResponse> call = api.like(id_articl_fk, id_pers_fk, token);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                Log.d(TAG, response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
        /*String url = Constants.HOST_URL + "v1/likes";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, response);
                    //Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(mContext, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("id_articl_fk", id_articl_fk);
                params.put("id_pers_fk", id_pers_fk);
                return params;
            }
        };
        Volley.newRequestQueue(mContext).add(request);*/
    }

    @Override
    public int getItemCount() {
        return mEntreprise.size();
    }

    class EntrepriseHolder extends RecyclerView.ViewHolder {

        private final TextView nom_ent;
        private final TextView description_ent;
        private final TextView date_pub_ent;
        private final TextView nb_vue;
        private final TextView nb_comment;
        private final TextView nb_like;
        private final ImageView txt_affiche;
        private final ImageButton like;
        private final Button btn_share_ent;

        EntrepriseHolder(@NonNull View itemView) {
            super(itemView);
            nom_ent = itemView.findViewById(R.id.txt_nom_entreprise);
            description_ent = itemView.findViewById(R.id.txt_description_entreprise);
            date_pub_ent = itemView.findViewById(R.id.txt_date_pub);
            btn_share_ent = itemView.findViewById(R.id.btn_share_entreprise);

            nb_vue = itemView.findViewById(R.id.txt_nb_view);
            nb_comment = itemView.findViewById(R.id.txt_nb_comment);
            nb_like = itemView.findViewById(R.id.btn_like);
            like = itemView.findViewById(R.id.like);

            txt_affiche = itemView.findViewById(R.id.txt_affiche);
        }
    }


}
