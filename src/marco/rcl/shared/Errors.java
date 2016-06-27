package marco.rcl.shared;


/**
 * Created by Marco on 29/05/16.
 */
public enum Errors {
    CommandNotFound, noErrors, UserAlreadyRegistered, UsernameNotValid, PasswordNotValid, UserNotRegistered,
    TokenNotValid, UserNotLogged, AddressNotValid, RequestNotValid, ConfirmNotValid, IgnoreNotValid, UserNotValid,
    UserOffline, ContentNotValid;

    public static String getError (Errors error){
        switch (error){
            case noErrors:
                return "Command correctly executed";
            case UserAlreadyRegistered:
                return "User Already Registered";
            case UsernameNotValid:
                return "Username Not Valid";
            case PasswordNotValid:
                return "Password Not Valid";
            case UserNotRegistered:
                return "User Not Registered";
            case TokenNotValid:
                return "Token Not Valid";
            case UserNotLogged:
                return "User Not Logged";
            case AddressNotValid:
                return "Address Not Valid";
            case RequestNotValid:
                return "Request Not Valid";
            case ConfirmNotValid:
                return "Confirm Not Valid";
            case IgnoreNotValid:
                return "IgnoreN ot Valid";
            case UserNotValid:
                return "User Not Valid";
            case UserOffline:
                return "User Offline";
            case ContentNotValid:
                return "Content Not Valid";
            default:
                return "Command Not Found";
        }
    }

/*
    public static String printErrors (Errors error){
        switch (error){
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
*/
}
