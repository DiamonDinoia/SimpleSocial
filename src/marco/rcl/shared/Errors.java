package marco.rcl.shared;

/**
 * Created by Marco on 29/05/16.
 */
public interface Errors {

    int CommandNotFound = -1;
    int noErrors = 0;
    int UserAlreadyRegistered = 1;
    int UsernameNotValid = 2;
    int PasswordNotValid = 3;
    int UserNotRegistered = 4;
    int TokenNotValid = 5;
    int UserNotLogged = 6;
    int AddressNotValid = 7;
    int RequestNotValid = 8;
    int ConfirmNotValid = 9;
    int IgnoreNotValid = 10;
    int UserNotValid = 11;
    int UserOffline = 12;
    int ContentNotValid = 13;
}
