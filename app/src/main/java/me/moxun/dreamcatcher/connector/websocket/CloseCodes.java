package me.moxun.dreamcatcher.connector.websocket;

/**
 * Close codes as defined by RFC6455.
 */
public interface CloseCodes {
  int NORMAL_CLOSURE = 1000;
  int PROTOCOL_ERROR = 1002;
  int CLOSED_ABNORMALLY = 1006;
  int UNEXPECTED_CONDITION = 1011;
}
