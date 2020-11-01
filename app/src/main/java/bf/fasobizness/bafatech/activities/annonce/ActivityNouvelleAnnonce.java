package bf.fasobizness.bafatech.activities.annonce;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.user.LoginActivity;
import bf.fasobizness.bafatech.adapters.UriAdapter;
import bf.fasobizness.bafatech.helper.GifSizeFilter;
import bf.fasobizness.bafatech.helper.ProgressRequestBody;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.interfaces.IllustrationInterface;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.interfaces.UploadCallbacks;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.FileCompressingUtil;
import bf.fasobizness.bafatech.utils.MySharedManager;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zhihu.matisse.internal.utils.PathUtils.getPath;

public class ActivityNouvelleAnnonce extends AppCompatActivity implements OnItemListener, UploadCallbacks {
    private static final String TAG = "ActivityNouvelleAnnonce";
    private static final int REQUEST_CODE = 23;
    private TextInputLayout til_titre_annonce, til_desc_annonce, til_tel_annonce, til_tel1_annonce, til_tel2_annonce, ed_prix;
    private Spinner sp_ville, sp_categorie;
    private TextView tv_ville_error, tv_categorie_error, tv_error_no_image, pourcent;
    private Button btn_publish_offer;
    private String user;
    private RelativeLayout overbox, rl_upload_picture;
    private ArrayList<Uri> images;
    private UriAdapter mAdapter;
    private int id_annonce_fk = 0;
    // private RequestQueue requestQueue;
    //private ImageView progressImage;
    private ProgressBar progressBar;
    private LinearLayout linear_uploading, linear_succes;
    private API api;
    private MySharedManager sharedManager;

    private static File getFile(Context context, Uri uri) {
        String path = getPath(context, uri);
        if (path != null) {
            if (isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    private static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouvelle_offre);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.nouvelle_annonce));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        api = RetrofitClient.getClient().create(API.class);
        sharedManager = new MySharedManager(this);

        overbox = findViewById(R.id.overbox);
        overbox.setVisibility(View.GONE);
        rl_upload_picture = findViewById(R.id.rl_upload_picture);
        rl_upload_picture.setVisibility(View.GONE);

        pourcent = findViewById(R.id.pourcent);
        til_titre_annonce = findViewById(R.id.til_titre_annonce);
        til_desc_annonce = findViewById(R.id.til_description_annonce);
        til_tel_annonce = findViewById(R.id.til_tel_annonce);
        til_tel1_annonce = findViewById(R.id.til_tel1_annonce);
        til_tel2_annonce = findViewById(R.id.til_tel2_annonce);
        ed_prix = findViewById(R.id.ed_prix_annonce);
        Button btn_add_pictures_annonce = findViewById(R.id.btn_add_pictures_annonce);
        btn_publish_offer = findViewById(R.id.btn_publish);
        Button btn_close_overbox = findViewById(R.id.btn_close_overbox);


        tv_ville_error = findViewById(R.id.tv_error_ville);
        tv_categorie_error = findViewById(R.id.tv_error_catégorie);
        tv_error_no_image = findViewById(R.id.tv_error_no_image);
        // progressImage = findViewById(R.id.progressImage);
        progressBar = findViewById(R.id.progress_bar);
        linear_uploading = findViewById(R.id.linear_uploading);
        linear_succes = findViewById(R.id.linear_succes);

        sp_ville = findViewById(R.id.sp_ville_annonce);
        sp_categorie = findViewById(R.id.sp_categorie_annonce);

        sp_ville.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.villes)));
        sp_categorie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categories)));

        btn_publish_offer.setOnClickListener(v -> {
            if (!checkTitreInput() | !checkDescInput() | !checkTelInput() | !checkVilleInput() | !checkCatInput() | !checkImage()) {
                return;
            }
            publierAnnonce();
        });

        btn_close_overbox.setOnClickListener(v -> finish());

        btn_add_pictures_annonce.setOnClickListener(v -> requestMultiplePermissions());

        images = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.file_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(mAdapter = new UriAdapter(this, images));
        recyclerView.setHasFixedSize(true);
        mAdapter.setOnItemListener(this);

        MySharedManager mySharedManager = new MySharedManager(this);
        user = mySharedManager.getUser();

        if (!sharedManager.getTel().isEmpty()) {
            Objects.requireNonNull(til_tel_annonce.getEditText()).setText(sharedManager.getTel());
        }
        if (!sharedManager.getTel1().isEmpty()) {
            Objects.requireNonNull(til_tel1_annonce.getEditText()).setText(sharedManager.getTel1());
        }
        if (!sharedManager.getTel2().isEmpty()) {
            Objects.requireNonNull(til_tel2_annonce.getEditText()).setText(sharedManager.getTel2());
        }

        if (!sharedManager.getVille().isEmpty()) {

            for (int i = 0; i < sp_ville.getAdapter().getCount(); i++) {
                if (sp_ville.getAdapter().getItem(i).toString().contains(sharedManager.getVille())) {
                    sp_ville.setSelection(i);
                }
            }
        }
        if (!sharedManager.getCategorie().isEmpty()) {
            for (int i = 0; i < sp_categorie.getAdapter().getCount(); i++) {
                if (sp_categorie.getAdapter().getItem(i).toString().contains(sharedManager.getCategorie())) {
                    sp_categorie.setSelection(i);
                }
            }
        }
    }

    private void showChooser() {
        Matisse.from(ActivityNouvelleAnnonce.this)
                .choose(MimeType.ofImage(), false)
                .theme(R.style.Matisse_Dracula)
                .countable(true)
                .capture(true)
                .captureStrategy(
                        new CaptureStrategy(true, "bf.fasobizness.bafatech.fileprovider", "Pictures"))
                .maxSelectable(9)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                        getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showSingleMediaType(true)
                .originalEnable(false)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .forResult(REQUEST_CODE);
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showChooser();
                            Log.v(TAG, "All permissions are granted by user!");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // mAdapter.setData(this, Matisse.obtainResult(data));
            if (data != null) {
                images.addAll(Matisse.obtainResult(data));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void publierAnnonce() {
        // Cacher clavier
        if (getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        btn_publish_offer.setEnabled(false);
        overbox.setVisibility(View.VISIBLE);
        if (user.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {

            final String titre = Objects.requireNonNull(til_titre_annonce.getEditText()).getText().toString().trim();
            final String description = Objects.requireNonNull(til_desc_annonce.getEditText()).getText().toString();
            final String prix = Objects.requireNonNull(ed_prix.getEditText()).getText().toString();
            final String tel = Objects.requireNonNull(til_tel_annonce.getEditText()).getText().toString();
            final String tel1 = Objects.requireNonNull(til_tel1_annonce.getEditText()).getText().toString();
            final String tel2 = Objects.requireNonNull(til_tel2_annonce.getEditText()).getText().toString();
            final String ville = sp_ville.getSelectedItem().toString();
            final String categorie = sp_categorie.getSelectedItem().toString();

            sharedManager.setTel(tel);
            sharedManager.setTel1(tel1);
            sharedManager.setTel2(tel2);
            sharedManager.setVille(ville);
            sharedManager.setCategorie(categorie);


            Call<MyResponse> call = api.postAnnounce(
                    id_annonce_fk,
                    user,
                    description,
                    prix,
                    ville,
                    tel,
                    tel1,
                    tel2,
                    titre,
                    categorie
            );
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    try {
                        assert response.body() != null;
                        Log.d(TAG, response.body().getStatus().toString());
                        boolean status = response.body().getStatus();
                        if (!status) {
                            overbox.setVisibility(View.GONE);
                            btn_publish_offer.setEnabled(true);
                        } else {
                            id_annonce_fk = response.body().getId();
                            uploadPictures();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ActivityNouvelleAnnonce.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                        overbox.setVisibility(View.GONE);
                        btn_publish_offer.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    Log.v(TAG, t.toString());
                    btn_publish_offer.setEnabled(true);
                    btn_publish_offer.setText(R.string.ressayer);
                    overbox.setVisibility(View.GONE);
                    Toast.makeText(ActivityNouvelleAnnonce.this, "Pas d'acces internet", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void uploadPictures() {
        rl_upload_picture.setVisibility(View.VISIBLE);
        List<MultipartBody.Part> parts = new ArrayList<>();
        IllustrationInterface illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface.class);
        for (int i = 0; i < images.size(); i++) {
            parts.add(prepareFilePart("image" + i, images.get(i)));
        }
        RequestBody id_ann = createPart(String.valueOf(id_annonce_fk));
        RequestBody size = createPart(parts.size() + "");
        Call<ResponseBody> call = illustrationInterface.uploadPhotos(id_ann, size, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.v(TAG, response.toString());
                if (response.isSuccessful()) {
                    linear_uploading.setVisibility(View.GONE);
                    linear_succes.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                rl_upload_picture.setVisibility(View.GONE);
                overbox.setVisibility(View.GONE);
                Toast.makeText(ActivityNouvelleAnnonce.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                btn_publish_offer.setEnabled(true);
                btn_publish_offer.setText(R.string.ressayer);
                Log.v(TAG, t.toString());
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String part, Uri uri) {
        File file = getFile(this, uri);
        FileCompressingUtil fileCompressingUtil = new FileCompressingUtil();
        File compressedFile = fileCompressingUtil.saveBitmapToFile(file);
        ProgressRequestBody requestFile = new ProgressRequestBody(compressedFile, this);
        assert file != null;
        return MultipartBody.Part.createFormData(part, file.getName(), requestFile);
    }

    private RequestBody createPart(String id_ann) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, id_ann);
    }

    private boolean checkTitreInput() {
        String titreInput = Objects.requireNonNull(til_titre_annonce.getEditText()).getText().toString().trim();
        if (titreInput.isEmpty()) {
            til_titre_annonce.setError(getString(R.string.titre_annonce_requis));
            return false;
        } else if (titreInput.length() > 35) {
            til_titre_annonce.setError(getString(R.string.titre_annonce_trop_long));
            return false;
        } else {
            til_titre_annonce.setError(null);
            return true;
        }
    }

    private boolean checkDescInput() {
        try {
            String descInput = Objects.requireNonNull(til_desc_annonce.getEditText()).getText().toString().trim();
            if (descInput.isEmpty()) {
                til_desc_annonce.setError(getString(R.string.description_annonce_requis));
                return false;
            } else if (descInput.length() > 600) {
                til_desc_annonce.setError(getString(R.string.description_de_lannonce_trop_longue));
                return false;
            } else {
                til_desc_annonce.setError(null);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean checkTelInput() {
        try {
            String telInput = Objects.requireNonNull(til_tel_annonce.getEditText()).getText().toString().trim();
            if (telInput.isEmpty()) {
                til_tel_annonce.setError(getString(R.string.no_de_tel_requis));
                return false;
            } else if (telInput.length() > 13) {
                til_tel_annonce.setError(getString(R.string.no_de_tel_invalide));
                return false;
            } else {
                til_tel_annonce.setError(null);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean checkVilleInput() {
        String villeInput = sp_ville.getSelectedItem().toString();
        if (villeInput.isEmpty()) {
            tv_ville_error.setVisibility(View.VISIBLE);
            return false;
        } else if (villeInput.equals("Choisir ville")) {
            tv_ville_error.setVisibility(View.VISIBLE);
            return false;
        } else {
            tv_ville_error.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean checkCatInput() {

        String catInput = sp_categorie.getSelectedItem().toString();
        if (catInput.isEmpty()) {
            tv_categorie_error.setVisibility(View.VISIBLE);
            return false;

        } else if (catInput.equals("Choisir Categorie")) {
            tv_categorie_error.setVisibility(View.VISIBLE);
            return false;
        } else {
            tv_categorie_error.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean checkImage() {
        if (images.size() == 0) {
            tv_error_no_image.setVisibility(View.VISIBLE);
            return false;

        } else {
            tv_error_no_image.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    public void onItemClicked(int position) {
        mAdapter.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onProgressUpdate(int percentage) {
        String percent = getString(R.string._pourcent, String.valueOf(percentage));
        pourcent.setText(percent);
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(percentage);

        /*if (percentage == 0) {
            Glide.with(this)
                    .setDefaultRequestOptions(
                            new RequestOptions()
                                    .placeholder(R.color.colorPrimary)
                                    .error(R.color.colorPrimaryDark)
                                    .centerCrop()
                    )
                    .load(path)
                    .thumbnail(0.1f)
                    .into(progressImage);

        }*/
    }

    @Override
    public void onError() {
        btn_publish_offer.setEnabled(true);
        pourcent.setText(getString(R.string.pas_d_acces_internet));

    }

    @Override
    public void onFinish() {
        pourcent.setText(getString(R.string.chargement_en_cours));
    }

    @Override
    public void uploadStart() {
        pourcent.setText(getString(R.string.chargement_en_cours));
        pourcent.setVisibility(View.VISIBLE);

    }
}
