package bf.fasobizness.bafatech.interfaces;

import androidx.annotation.Nullable;

public interface MessageContentType extends IMessage {

    interface Image extends IMessage {
        @Nullable
        String getImageUrl();
    }

}
