package app.owlcms.fly.commands;

public class NoPermissionException extends RuntimeException {

    NoPermissionException(String s) {
        super(s);
    }

}
