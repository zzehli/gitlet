package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        if (args.length == 0) {
           Utils.exitWithError("Please enter a command.");
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

            case "rm":
                validateNumArgs("rm", args, 2);
                Command.rm(args[1]);
                break;

            case "log":
                validateNumArgs("log", args, 1);
                Command.log();
                break;

            case "status":
                validateNumArgs("status", args, 1);
                Command.status();
                break;

            case "branch":
                validateNumArgs("branch", args, 2);
                Command.branch(args[1]);
                break;

            case "checkout":
                if (args.length == 4) {
                    Command.checkoutPastFile(args[1], args[3]);
                }
                else if (args.length == 3) {
                    Command.checkoutHeadFile(args[2]);
                }
                else if (args.length == 2) {
                    Command.checkoutBranch(args[1]);
                }
                else {
                    Utils.exitWithError("Not valid input.");
                }
                break;

            case "merge" :
                validateNumArgs("merge", args, 2);
                Command.merge(args[1]);
                break;

            case "rebase":
                validateNumArgs("rebase", args, 2);
                //Command.rebase(args[1]);
                break;

            default:
                Utils.exitWithError("No command with that name exists.");

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
