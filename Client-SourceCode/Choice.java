package ftp;

/**
 * Possible ways to interact with server.
 */
public enum Choice {
  INVALID(0),
  GET(1),
  PUT(2),
  LS(3),
  QUIT(4);

  public final int index;

  Choice(int index) {
    this.index = index;
  }
}
