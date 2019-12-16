package ftp;

import java.util.Optional;

/**
 * A command typed in the terminal.
 */
public class Command {

  /** The action to perform. */
  private final Choice choice;
  /** An optional string parameter; used for file names. */
  private String argument;

  public Command(Choice choice) {
    this(choice, null);
  }

  public Command(Choice choice, String argument) {
    this.choice = choice;
    this.argument = argument;
  }

  public Choice getChoice() {
    return this.choice;
  }

  public Optional<String> getArgument() {
    return Optional.ofNullable(this.argument);
  }
}
