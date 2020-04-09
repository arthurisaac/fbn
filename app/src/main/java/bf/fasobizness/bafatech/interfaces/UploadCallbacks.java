package bf.fasobizness.bafatech.interfaces;

public interface UploadCallbacks {
    void onProgressUpdate(int percentage, String path);

    void onError();

    void onFinish();

    void uploadStart();
}
