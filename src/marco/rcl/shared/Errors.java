package marco.rcl.shared;


/**
 * this enum contains all possibles errors that can occur between client and server
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

}
