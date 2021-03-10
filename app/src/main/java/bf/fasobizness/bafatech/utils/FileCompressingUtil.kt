package bf.fasobizness.bafatech.utils

import android.R.attr
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class FileCompressingUtil {
    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveBitmapToFile(file: File?): File? {
        return try {

            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 200 // x............

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)

            /*val ei: ExifInterface = ExifInterface(inputStream)
            val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)
            var rotatedBitmap: Bitmap? = null
            rotatedBitmap = when (attr.orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> selectedBitmap?.let { rotateImage(it, 90) }
                ExifInterface.ORIENTATION_ROTATE_180 -> selectedBitmap?.let { rotateImage(it, 180) }
                ExifInterface.ORIENTATION_ROTATE_270 -> selectedBitmap?.let { rotateImage(it, 270) }
                ExifInterface.ORIENTATION_NORMAL -> selectedBitmap
                else -> selectedBitmap
            }*/
            inputStream.close()

            // here i override the original image file
            val outPutFile = File.createTempFile("abc", "image")
            val outputStream = FileOutputStream(outPutFile)
            // y.......
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outPutFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}