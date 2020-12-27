package gitlet;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {
    static final File HEAD = Utils.join(".gitlet", "HEAD");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        // FILL THIS IN
        if (args.length == 0) {
           Utils.exitWithError("No argument provided. Exit the program");
        }

        switch (args[0]) {
            case "init":
                validateNumArgs("init", args, 1);
                if (Utils.join(".gitlet").exists()) {
                    Utils.exitWithError("A Gitlet version-control system " +
                            "already exists in the current directory.");
                }
                Command.init();
                break;


            case "add":
                validateNumArgs("add", args, 2);
                if (Utils.join(args[1]).exists() == false) {
                    Utils.exitWithError("File does not exist.");
                }
                Command.add(args[1]);
                break;

            case "commit":
                if (args.length != 2) {
                    Utils.exitWithError("Please enter a commit message.");
                }
                Command.commit(args[1]);
                break;

            default:
                Utils.exitWithError(String.format("Unknown command: %s", args[0]));
        }
        return;
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @author su2020 lab09
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(

                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }

}
