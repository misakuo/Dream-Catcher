package me.moxun.dreamcatcher.connector.inspector;

public class MismatchedResponseException extends MessageHandlingException {
  public long mRequestId;

  public MismatchedResponseException(long requestId) {
    super("Response for request id " + requestId + ", but no such request is pending");
    mRequestId = requestId;
  }

  public long getRequestId() {
    return mRequestId;
  }
}
