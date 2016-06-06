package marco.rcl.shared;


/**
 * Created by Marco on 29/05/16.
 */
public class Errors {

    final static public int CommandNotFound = -1;
    final static public int noErrors = 0;
    final static public int UserAlreadyRegistered = 1;
    final static public int UsernameNotValid = 2;
    final static public int PasswordNotValid = 3;
    final static public int UserNotRegistered = 4;
    final static public int TokenNotValid = 5;
    final static public int UserNotLogged = 6;
    final static public int AddressNotValid = 7;
    final static public int RequestNotValid = 8;
    final static public int ConfirmNotValid = 9;
    final static public int IgnoreNotValid = 10;
    final static public int UserNotValid = 11;
    final static public int UserOffline = 12;
    final static public int ContentNotValid = 13;

    public static void printErrors (int code){
        switch (code){
            case noErrors:
                System.out.println("Command correctly executed");
                break;
            case UserAlreadyRegistered:
                System.out.println("UserAlreadyRegistered");
                break;
            case UsernameNotValid:
                System.out.println("UsernameNotValid");
                break;
            case PasswordNotValid:
                System.out.println("PasswordNotValid");
                break;
            case UserNotRegistered:
                System.out.println("UserNotRegistered");
                break;
            case TokenNotValid:
                System.out.println("TokenNotValid");
                break;
            case UserNotLogged:
                System.out.println("UserNotLogged");
                break;
            case AddressNotValid:
                System.out.println("AddressNotValid");
                break;
            case RequestNotValid:
                System.out.println("RequestNotValid");
                break;
            case ConfirmNotValid:
                System.out.println("ConfirmNotValid");
                break;
            case IgnoreNotValid:
                System.out.println("IgnoreNotValid");
                break;
            case UserNotValid:
                System.out.println("UserNotValid");
                break;
            case UserOffline:
                System.out.println("UserOffline");
                break;
            case ContentNotValid:
                System.out.println("ContentNotValid");
                break;
            default:
                System.out.println("CommandNotFound");
        }
    }
}
