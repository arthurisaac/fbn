package bf.fasobizness.bafatech.helper;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import bf.fasobizness.bafatech.interfaces.UploadCallbacks;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048 * 2;
    private final File mFile;
    private final UploadCallbacks mListener;

    public ProgressRequestBody(final File file, final UploadCallbacks listener) {
        mFile = file;
        mListener = listener;
        mListener.uploadStart();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public long contentLength() {
        return mFile.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (FileInputStream in = new FileInputStream(mFile)) {
            long uploaded = 0;
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
                handler.post(new ProgressUpdater(uploaded, fileLength));
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private final long mUploaded;
        private final long mTotal;

        private ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            try {

                int progress = (int) (100 * mUploaded / mTotal);

                if (progress == 100)
                    mListener.onFinish();
                else
                    mListener.onProgressUpdate(progress, mFile.getPath());
            } catch (ArithmeticException e) {
                mListener.onError();
                e.printStackTrace();
            }
        }
    }
}
