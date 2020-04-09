package bf.fasobizness.bafatech.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileCompressingUtil {

    public FileCompressingUtil() {
    }

    public File saveBitmapToFile(File file) {

        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 200;        // x............

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            File outPutFile = File.createTempFile("abc", "image");
            FileOutputStream outputStream = new FileOutputStream(outPutFile);
            // y.......
            if (selectedBitmap != null) {
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            }

            return outPutFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

