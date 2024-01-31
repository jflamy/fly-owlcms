package app.owlcms.fly.ui;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;

import app.owlcms.fly.commands.FlyCtlCommands;
import app.owlcms.fly.commands.NoLockException;
import ch.qos.logback.classic.Logger;

public class Login extends LoginOverlay {

	Logger logger = (Logger) LoggerFactory.getLogger(Login.class);
	private Runnable callback;
	private FlyCtlCommands tokenConsumer;

	Login(Runnable callbackArg, FlyCtlCommands tokenConsumerArg) {
		this.setCallback(callbackArg);
		this.setTokenConsumer(tokenConsumerArg);
		LoginOverlay loginOverlay = this;
		LoginI18n i18n = LoginI18n.createDefault();
		LoginI18n.Form i18nForm = i18n.getForm();
		i18nForm.setTitle("Log in to fly.io");
		i18n.setForm(i18nForm);

		LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
		i18nErrorMessage.setMessage(
		        "Check that you are using your fly.io credentials.  If you have forgotten your password, go to the fly.io site to recover it.");
		i18n.setErrorMessage(i18nErrorMessage);

		loginOverlay.setI18n(i18n);
		IntegerField code = new IntegerField("2FA Authenticator Code (leave empty unless you have enabled 2FA)");
		code.getElement().setAttribute("name", "code");
		loginOverlay.getCustomFormArea().add(code);

		Button loginCancelbutton = new Button("Cancel", e -> loginOverlay.close());
		loginCancelbutton.setWidthFull();
		loginOverlay.setForgotPasswordButtonVisible(false);
		loginOverlay.setTitle("owlcms Cloud Application Management");
		loginOverlay.setDescription("""
		        This application issues fly.io commands on your behalf.
		        Your login information is used to get the permission to do so.
		        The application does NOT keep your login information.
		        """);

		Button signup = new Button("Sign Up to fly.io", e -> {
			UI.getCurrent().getPage().open("https://fly.io/app/sign-up");
		});
		signup.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_PRIMARY);
		signup.setWidthFull();

		loginOverlay.getFooter().add(signup, loginCancelbutton);

		loginOverlay.addLoginListener(e -> {
			logger.info("user login {} ", e.getUsername());
			boolean successful;
			try {
				successful = tokenConsumer.doLogin(e.getUsername(), e.getPassword(), code.getValue());
			} catch (NoLockException e1) {
				successful = false;
				Button closeButton = new Button("Close");
				Notification not = new Notification(
				        new Text("""
		                        Technical problem: Could not acquire the access token. Please report to owlcms-bugs@jflamy.dev
		                        """),
				        closeButton);
				closeButton.addClickListener((e2) -> not.close());
				not.addThemeVariants(NotificationVariant.LUMO_ERROR);
				not.setPosition(Position.MIDDLE);
				not.open();
			}
			if (successful) {
				loginOverlay.setError(false);
				loginOverlay.setOpened(false);
				callback.run();
			} else {
				loginOverlay.setError(true);
			}
		});
	}

    public Runnable getCallback() {
		return callback;
	}

	public FlyCtlCommands getTokenConsumer() {
		return tokenConsumer;
	}

	public void setTokenConsumer(FlyCtlCommands tokenConsumer) {
		this.tokenConsumer = tokenConsumer;
	}

	public Login(FlyCtlCommands tokenConsumer) {
		this(null, tokenConsumer);
    }

    public void setCallback(Runnable callback) {
		this.callback = callback;

    }

}
