package me.moxun.dreamcatcher.connector.server.http;

public interface HttpStatus {
  int HTTP_SWITCHING_PROTOCOLS = 101;
  int HTTP_OK = 200;
  int HTTP_NOT_FOUND = 404;
  int HTTP_INTERNAL_SERVER_ERROR = 500;
  int HTTP_NOT_IMPLEMENTED = 501;
}
