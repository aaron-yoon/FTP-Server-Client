package ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The server application.
 */
public class Server {

  private static final File SERVER_DIRECTORY = new File("server");
  private static int portNumber;

  public static void main(String[] args) throws IOException {
    portNumber = Server.parsePortNumber(args);
    if (portNumber == -1) {
      System.err.println("Invalid port number!");
      return;
    }

    // Create location for server files.
    if (!SERVER_DIRECTORY.exists()) {
      SERVER_DIRECTORY.mkdir();
    }

    ServerSocket serverSocket = new ServerSocket(portNumber);

    // Server remains listening.
    while (true) {
      Socket socket = serverSocket.accept();
      DataInputStream inputStream = new DataInputStream(socket.getInputStream());

      // Parse header info.
      Choice choice = Choice.values()[inputStream.readInt()];
      String fileName = inputStream.readUTF();
      long fileSize = inputStream.readLong();

      switch (choice) {
        case GET:
          Server.get(socket, fileName);
          break;
        case PUT:
          Server.put(inputStream, fileName, fileSize);
          break;
        case LS:
          Server.ls(socket);
          break;
      }

      // Close connection and listen for another one.
      inputStream.close();
      socket.close();
    }
  }

  /**
   * Gets the port number.
   *
   * @param args Program arguments.
   * @return Port number if valid, otherwise -1.
   */
  private static int parsePortNumber(String... args) {
    if (args.length == 1) {
      try {
        int portNumber = Integer.parseInt(args[0]);
        if (1 <= portNumber && portNumber <= 65535) {
          return portNumber;
        }
      } catch (NumberFormatException exception) {
        return -1;
      }
    }

    return -1;
  }

  /**
   * Retrieves a file from this server and sends it to the requesting client.
   *
   * @param socket The socket connection.
   * @param fileName Name of the file.
   * @throws IOException File exceptions.
   */
  private static void get(Socket socket, String fileName) throws IOException {
    File file = new File(SERVER_DIRECTORY, fileName);
    if (!file.exists()) {
      System.err.println("[FAILURE] File " + fileName + " not found!");
      return;
    }

    FileInputStream inputStream = new FileInputStream(file);
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

    int ch;
    long bytesSent = 0L;
    long fileSize = inputStream.getChannel().size();

    // Header info.
    outputStream.writeLong(fileSize);

    // Send data through stream.
    while (bytesSent < fileSize) {
      while ((ch = inputStream.read()) != -1) {
        outputStream.write((char) ch);
        ++bytesSent;
      }
    }

    System.out.println("[SUCCESS] Retrieved " + fileName + ".");
    outputStream.close();
    inputStream.close();
  }

  /**
   * Uploads a file to this server.
   *
   * @param inputStream Stream containing file data.
   * @param fileName Name of the file.
   * @param fileSize Size of the file.
   * @throws IOException File exceptions.
   */
  private static void put(DataInputStream inputStream, String fileName, long fileSize) throws IOException {
    // Create any missing directories.
    File file = new File(SERVER_DIRECTORY, fileName);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }

    FileOutputStream outputStream = new FileOutputStream(file);

    // Read data from stream.
    int ch;
    while (outputStream.getChannel().size() < fileSize) {
      while ((ch = inputStream.read()) != -1) {
        outputStream.write((char) ch);
      }
    }

    System.out.println("[SUCCESS] Uploaded " + fileName + ".");
    outputStream.close();
  }

  /**
   * Retrieves a list of the stored file names.
   *
   * @param socket The socket connection.
   * @throws IOException File exceptions.
   */
  private static void ls(Socket socket) throws IOException {
    List<String> files = new ArrayList<>();
    for (File file : SERVER_DIRECTORY.listFiles()) {
      if (file.isFile()) {
        files.add(file.getName());
      }
    }

    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

    // Header info; total number of files.
    outputStream.writeInt(files.size());

    int sentFiles = 0;
    Iterator<String> iter = files.iterator();

    // Send file names through stream.
    while (sentFiles < files.size()) {
      while (iter.hasNext()) {
        outputStream.writeUTF(iter.next());
        ++sentFiles;
      }
    }

    System.out.println("[SUCCESS] Retrieved list of file names.");
    outputStream.close();
  }
}