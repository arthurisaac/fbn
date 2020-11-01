package bf.fasobizness.bafatech.interfaces;

public interface UploadCallbacks {
    void onProgressUpdate(int percentage);

    void onError();

    void onFinish();

    void uploadStart();
}
