package bf.fasobizness.bafatech.interfaces;


/**
 * For implementing by real message model
 */
public interface IMessage {

    /**
     * Returns message identifier
     *
     * @return the message id
     */
    String getMessage_id();

    /**
     * Returns message text
     *
     * @return the message text
     */
    String getMessage();


    /**
     * Returns message creation date
     *
     * @return the message creation date
     */
    String getCreated_at();

    /**
     * Returns message creation date
     *
     * @return the message senderId
     */
    String getSender();


    String getType();
}

