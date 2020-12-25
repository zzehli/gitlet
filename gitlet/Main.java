package gitlet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        // FILL THIS IN
        if (args.length == 0) {
           exitWithError("No argument provided. Exit the program");
        }

        switch (args[0]) {
            case "init":
                validateNumArgs("init", args, 1);
                Command.init();
                //System.out.println(Utils.sha1("fish", "kids", "heree"));
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Command.add(args[1]);
            default:
                exitWithError(String.format("Unknown command: %s", args[0]));
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

    /**
     * Prints out MESSAGE and exits with error code -1.
     * Note:
     *     The functionality for erroring/exit codes is different within Gitlet
     *     so DO NOT use this as a reference.
     *     Refer to the spec for more information.
     *
     * @ref su2020 lab09
     * @param message message to print
     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }


}
