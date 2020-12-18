package bf.fasobizness.bafatech.activities.annonce;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import bf.fasobizness.bafatech.activities.ActivityFullScreen;
import bf.fasobizness.bafatech.adapters.IllustrationEditAdapter;
import bf.fasobizness.bafatech.helper.GifSizeFilter;
import bf.fasobizness.bafatech.helper.ProgressRequestBody;
import bf.fasobizness.bafatech.helper.RetrofitClient;
import bf.fasobizness.bafatech.interfaces.IllustrationInterface;
import bf.fasobizness.bafatech.interfaces.OnItemListener;
import bf.fasobizness.bafatech.interfaces.UploadCallbacks;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.FileCompressingUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zhihu.matisse.internal.utils.PathUtils.getPath;

public class ActivityAnnonceEditPhotos extends AppCompatActivity implements OnItemListener, UploadCallbacks {
    private static final int REQUEST_CODE = 23;
    private ArrayList<Announce.Annonce.Illustration> images;
    private ArrayList<Uri> uri_images;
    // private UriAdapter mAdapter;
    private String id_annonce = "";
    private IllustrationEditAdapter adapter;
    // private RequestQueue requestQueue;
    private LinearLayout no_picture;
    private ProgressDialog progressDoalog;
    private IllustrationInterface illustrationInterface;

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
        setContentView(R.layout.activity_annonce_edit_photos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.modification));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.left_white);
        toolbar.setNavigationOnClickListener(view -> finish());

        no_picture = findViewById(R.id.layout_no_picture);
        no_picture.setOnClickListener(v -> requestMultiplePermissions());

        illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface.class);

        images = new ArrayList<>();
        uri_images = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerview_photos);
        adapter = new IllustrationEditAdapter(this, images);
        LinearLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        adapter.setOnItemListener(this);

        Intent extras = getIntent();
        Announce.Annonce annonce = (Announce.Annonce) extras.getSerializableExtra("annonce");


        List<Announce.Annonce.Illustration> arrayList;
        if (annonce != null) {
            arrayList = annonce.illustrations;
            images.addAll(arrayList);
            adapter.notifyDataSetChanged();
            id_annonce = annonce.getId_ann();
        }
        countPictures();

    }

    private void countPictures() {
        if (images.size() == 0) {
            no_picture.setVisibility(View.VISIBLE);
        } else {
            no_picture.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClicked(int position) {
        ArrayList<String> imagesList = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            Announce.Annonce.Illustration illustration = images.get(i);
            imagesList.add(illustration.getNom());
        }
        Intent intent = new Intent(this, ActivityFullScreen.class);
        intent.putStringArrayListExtra("images", imagesList);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_illustration_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_illustration_add) {
            requestMultiplePermissions();
        } else if (id == R.id.nav_illustration_remove) {
            ArrayList<Announce.Annonce.Illustration> checked = adapter.getChecked();
            StringBuilder idList = new StringBuilder();
            for (int i = 0; i < checked.size(); i++) {
                idList.append(checked.get(i).getId_illustration()).append(",");
                images.remove(checked.get(i));
                adapter.notifyDataSetChanged();
            }
            delete(idList.toString());
            checked.clear();
            adapter.notifyDataSetChanged();
            countPictures();
        }

        return super.onOptionsItemSelected(item);
    }

    private void delete(String ids) {
        Log.d("Activity", ids);
        Call<MyResponse> call = illustrationInterface.deleteIllustrations(ids);
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                Log.d("Activity", response.toString());
                Toast.makeText(ActivityAnnonceEditPhotos.this, "Super", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.d("Activty", t.toString());
            }
        });
    }

    private void showChooser() {
        Matisse.from(ActivityAnnonceEditPhotos.this)
                .choose(MimeType.ofAll(), false)
                // .choose(MimeType.ofImage(), false)
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
                .originalEnable(true)
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
            if (data != null) {
                uri_images.addAll(Matisse.obtainResult(data));

                uploadPictures();
            }
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        // Toast.makeText(this, percentage+"%", Toast.LENGTH_SHORT).show();
        progressDoalog.setMessage(percentage + "%");
    }

    @Override
    public void onError() {
    }

    @Override
    public void onFinish() {
        progressDoalog.dismiss();
    }

    private void getAllPictures() {
        images.clear();
        adapter.notifyDataSetChanged();

        if (!id_annonce.isEmpty()) {
            IllustrationInterface illustrationInterface = RetrofitClient.getClient().create(IllustrationInterface.class);
            Call<Announce.Annonce> call = illustrationInterface.getIllustrations(id_annonce);
            call.enqueue(new Callback<Announce.Annonce>() {
                @Override
                public void onResponse(@NonNull Call<Announce.Annonce> call, @NonNull Response<Announce.Annonce> response) {
                    if (response.isSuccessful()) {

                        Announce.Annonce annonce = response.body();
                        List<Announce.Annonce.Illustration> arrayList = null;
                        if (annonce != null) {
                            arrayList = annonce.illustrations;
                        }
                        if (arrayList != null) {
                            images.addAll(arrayList);
                        }

                    } else {
                        Toast.makeText(ActivityAnnonceEditPhotos.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                    }

                    countPictures();
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(@NonNull Call<Announce.Annonce> call, @NonNull Throwable t) {
                    Toast.makeText(ActivityAnnonceEditPhotos.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
        /*String url = Constants.HOST_URL + "illustration/read/" + id_annonce;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                images.clear();
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    Announce.Annonce.Illustration illustration = new Illustration();
                    illustration.setId(data.getString("id_illustration"));
                    illustration.setNom(data.getString("nom"));
                    images.add(illustration);
                }
                adapter.notifyDataSetChanged();
                countPictures();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
            }

        }, error -> Toast.makeText(this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show());
        requestQueue.add(request);*/
    }

    @Override
    public void uploadStart() {
    }

    private MultipartBody.Part prepareFilePart(String part, Uri uri) {
        File file = getFile(this, uri);
        assert file != null;
        FileCompressingUtil fileCompressingUtil = new FileCompressingUtil();
        File compressedFile = fileCompressingUtil.saveBitmapToFile(file);
        ProgressRequestBody requestFile = new ProgressRequestBody(compressedFile, this);
        return MultipartBody.Part.createFormData(part, file.getName(), requestFile);
    }

    private RequestBody createPart(String id_ann) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, id_ann);
    }

    private void uploadPictures() {
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setTitle(R.string.chargement_en_cours);
        progressDoalog.show();

        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < uri_images.size(); i++) {
            parts.add(prepareFilePart("image" + i, uri_images.get(i)));
        }
        RequestBody id_ann = createPart(id_annonce);
        RequestBody size = createPart(parts.size() + "");
        Call<ResponseBody> call = illustrationInterface.uploadPhotos(id_ann, size, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d("ActivityEdit", response.toString());
                if (response.isSuccessful()) {
                    progressDoalog.dismiss();
                    Toast.makeText(ActivityAnnonceEditPhotos.this, R.string.succes, Toast.LENGTH_SHORT).show();
                    getAllPictures();
                } else {
                    Toast.makeText(ActivityAnnonceEditPhotos.this, R.string.une_erreur_sest_produite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(ActivityAnnonceEditPhotos.this, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
