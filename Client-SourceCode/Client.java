package ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static ftp.Choice.*;

/**
 * The client application.
 */
public class Client {

  private static int portNumber;
  private static final Scanner SCANNER = new Scanner(System.in);

  public static void main(String[] args) throws IOException {
    portNumber = Client.parsePortNumber(args);
    if (portNumber == -1) {
      System.err.println("Invalid port number!");
      return;
    }

    Command command = Client.getCommand();

    while (command.getChoice() != QUIT) {
      // Continue prompting user until valid choice.
      while (command.getChoice() == INVALID) {
        System.err.println("Invalid command!");
        command = Client.getCommand();
      }

      // Perform the specified action.
      switch (command.getChoice()) {
        case GET:
          if (command.getArgument().isPresent()) {
            Client.get(command.getArgument().get());
          } else {
            System.err.println("Missing file name!");
          }
          break;
        case PUT:
          if (command.getArgument().isPresent()) {
            Client.put(command.getArgument().get());
          } else {
            System.err.println("Missing file name!");
          }
          break;
        case LS:
          Client.ls();
          break;
      }

      // Continue interactions with server if necessary.
      command = Client.getCommand();
    }
  }

  /**
   * Gets the port number.
   *
   * @param args Program arguments.
   * @return Port number if valid, otherwise -1.
   * @throws NumberFormatException If invalid argument.
   */
  private static int parsePortNumber(String... args) throws NumberFormatException {
    if (args.length == 1) {
      int portNumber = Integer.parseInt(args[0]);
      if (1 <= portNumber && portNumber <= 65535) {
        return portNumber;
      }
    }

    return -1;
  }

  /**
   * Prompts the user for an action: get, put, ls, quit.
   * Get and put both require a <fileName> parameter.
   *
   * @return The typed command.
   */
  private static Command getCommand() {
    System.out.print("ftp> ");
    String input = SCANNER.nextLine();

    // Parse file name argument if it exists.
    String arg = "";
    if (input.split(" ").length > 1) {
      arg = input.split(" ")[1];
    }

    for (Choice choice : Choice.values()) {
      if (arg.isEmpty() && choice.name().equalsIgnoreCase(input)) {
        return new Command(choice);
      } else if (!arg.isEmpty() && choice.name().equalsIgnoreCase(input.split(" ")[0])) {
        return new Command(choice, arg);
      }
    }

    return new Command(INVALID);
  }

  /**
   * Downloads a file from the server.
   *
   * @param fileName Name of the file.
   * @throws IOException File exceptions.
   */
  private static void get(String fileName) throws IOException {
    // Create the file on client.
    File file = new File(fileName);
    if (!file.exists()) {
      file.createNewFile();
    }

    Socket socket = new Socket("localhost", portNumber);

    // Header info.
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
    outputStream.writeInt(GET.index);
    outputStream.writeUTF(fileName);
    outputStream.writeLong(0L); // unused file size

    FileOutputStream fileOutputStream = new FileOutputStream(file);
    DataInputStream inputStream = new DataInputStream(socket.getInputStream());

    int ch;
    long bytesReceived = 0L;
    long fileSize = inputStream.readLong();

    // Read data from stream.
    while (bytesReceived < fileSize) {
      while ((ch = inputStream.read()) != -1) {
        fileOutputStream.write((char) ch);
        ++bytesReceived;
      }
    }

    System.out.println("Retrieved " + fileName + " from server. (" + fileSize + " bytes)");
    inputStream.close();
    socket.close();
  }

  /**
   * Uploads a file to the server.
   *
   * @param fileName Name of the file.
   * @throws IOException File exceptions.
   */
  private static void put(String fileName) throws IOException {
    // File does not exist.
    File file = new File(fileName);
    if (!file.exists()) {
      System.err.println("File " + fileName + " was not found!");
      return;
    }

    Socket socket = new Socket("localhost", portNumber);
    FileInputStream inputStream = new FileInputStream(file);
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

    int ch;
    long bytesSent = 0L;
    long fileSize = inputStream.getChannel().size();

    // Header info.
    outputStream.writeInt(PUT.index);
    outputStream.writeUTF(fileName);
    outputStream.writeLong(fileSize);

    // Send data through stream.
    while (bytesSent < fileSize) {
      while ((ch = inputStream.read()) != -1) {
        outputStream.write((char) ch);
        ++bytesSent;
      }
    }

    System.out.println("Uploaded " + fileName + " to server. (" + fileSize + " bytes)");
    outputStream.close();
    inputStream.close();
    socket.close();
  }

  /**
   * Displays a list of files stored in the server.
   *
   * @throws IOException File exceptions.
   */
  private static void ls() throws IOException {
    Socket socket = new Socket("localhost", portNumber);

    // Header info.
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
    outputStream.writeInt(LS.index);
    outputStream.writeUTF(""); // unused file name
    outputStream.writeLong(0L); // unused file size

    DataInputStream inputStream = new DataInputStream(socket.getInputStream());

    String fileName;
    int namesRead = 0;
    int listSize = inputStream.readInt();

    // Read file names from stream.
    while (namesRead < listSize) {
      fileName = inputStream.readUTF();
      ++namesRead;
      System.out.println("  " + namesRead + ". " + fileName);
    }
  }
}