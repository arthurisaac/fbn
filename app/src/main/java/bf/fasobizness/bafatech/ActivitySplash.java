package bf.fasobizness.bafatech;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);

        int SPLASH_TIME_OUT = 4000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(ActivitySplash.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);

        ObjectAnimator flip = ObjectAnimator.ofFloat(logo, "rotationY", 100f, 0f);
        flip.setDuration(1000);
        flip.start();
    }

    /*private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            test();
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

    private void test() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File file = new File(Environment.getExternalStorageDirectory(), "fasobizness");
            if (file.mkdirs()) {
                Log.d("ActivitySplash", "Directory created");
            } else {
                Log.d("ActivitySplash", "Directory not created");
            }
        }
    }*/
}
