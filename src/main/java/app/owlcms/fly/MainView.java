package app.owlcms.fly;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.page.WebStorage.Storage;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a
 * notification.
 */
@Route("")
public class MainView extends VerticalLayout {

	private static final String TOKEN_KEY = Main.class.getPackage() + ".accessToken";
	private long lastClick = 0;
	final private Logger logger = LoggerFactory.getLogger(MainView.class);
	private String oldToken;
	private ExecArea execArea;
	private FlyCtlCommands tokenConsumer;
	private VerticalLayout intro;
	private VerticalLayout tokenMissing;
	private PasswordField accessTokenField;
	private VerticalLayout apps;
	Button clearToken;
	Button listApplications;

	public MainView() {
		this.setSpacing(false);
		this.setHeightFull();
		H2 title = new H2("owlcms - fly.io cloud");

		execArea = new ExecArea();
		execArea.setVisible(false);
		execArea.setSizeFull();
		tokenConsumer = new FlyCtlCommands(execArea);

		intro = buildIntro();
		tokenMissing = buildTokenMissingExplanation();
		accessTokenField = buildAccessTokenField();
		apps = buildAppsPlaceholder();

		clearToken = new Button("Clear Token");
		listApplications = new Button("List Applications",
				e -> doListApplications(tokenMissing, accessTokenField, apps, clearToken));
		listApplications.setEnabled(false);
		listApplications.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		listApplications.addClickShortcut(Key.ENTER);

		accessTokenField.addValueChangeListener(
				v -> doTokenSet(clearToken, listApplications, v));
		clearToken.addClickListener(
				e -> doTokenClear(tokenMissing, accessTokenField, apps, clearToken, listApplications));

		WebStorage.getItem(Storage.LOCAL_STORAGE, TOKEN_KEY,
				value -> doTokenFromStorage(tokenMissing, accessTokenField, clearToken, listApplications, value));

		HorizontalLayout buttons = new HorizontalLayout(listApplications, clearToken);
		add(title, intro, tokenMissing, accessTokenField, buttons, apps, execArea);
	}

	private PasswordField buildAccessTokenField() {
		PasswordField accessTokenField = new PasswordField("Access Token");
		accessTokenField.setValueChangeMode(ValueChangeMode.EAGER);
		accessTokenField.setWidth("45em");
		accessTokenField.addClassName("bordered");
		return accessTokenField;
	}

	private VerticalLayout buildAppsPlaceholder() {
		VerticalLayout apps = new VerticalLayout();
		apps.setMargin(false);
		apps.setPadding(false);
		return apps;
	}

	private ConfirmDialog buildDeletionDialog(App app, Runnable callback) {
		ConfirmDialog deletionDialog = new ConfirmDialog();
		deletionDialog.setHeader("Deletion Confirmation Required");
		deletionDialog.setText(new Html(
				"<div>This will remove the application and make the name available again.</div>"));

		deletionDialog.setConfirmText("Delete");
		deletionDialog.setConfirmButtonTheme("error primary");
		deletionDialog.setCancelButtonTheme("primary");
		deletionDialog.setCancelable(true);
		deletionDialog.setCancelText("Cancel");
		deletionDialog.addConfirmListener(e -> {
			execArea.append("Deleting " + app.name, UI.getCurrent());
			logger.info("Deleting {}", app.name);
			tokenConsumer.appDestroy(app);
			if (app.appType == AppType.OWLCMS) {
				App dbApp = tokenConsumer.getApps().get(AppType.DB);
				execArea.append("Deleting associated database " + dbApp.name, UI.getCurrent());
				logger.info("Deleting associated database{}", dbApp.name);
			}
			callback.run();
		});
		deletionDialog.addCancelListener(e -> {
			deletionDialog.close();
		});
		return deletionDialog;
	}

	private VerticalLayout buildIntro() {
		Div p1 = new Div(
				"This page creates and manages your owlcms applications");
		VerticalLayout intro = new VerticalLayout(p1);
		intro.setSpacing(false);
		intro.setPadding(false);
		intro.setMargin(false);
		intro.getStyle().set("margin-top", "1em");
		return intro;
	}

	private VerticalLayout buildTokenMissingExplanation() {
		Anchor a = new Anchor("https://fly.io/user/personal_access_tokens",
				"Click on this link to go to the fly.io account settings page.",
				AnchorTarget.BLANK);
		ListItem step1 = new ListItem(a);
		ListItem step2 = new ListItem(
				"Login to your account. Sign up if you don't have one. You will need to provide a credit card number, but you will NOT be charged.");
		ListItem step3 = new ListItem(
				"The token creation is at the top right of the account settings. Follow the steps in the image to the right.");
		ListItem step3a = new ListItem("Type a name for your token - for example 'owlcms token' or anything you want.");
		ListItem step3b = new ListItem("Click on the create button.");
		ListItem step3c = new ListItem("Click on the 'Copy' icon next to the token to copy it to the clipboard.");
		step3.add(new OrderedList(step3a, step3b, step3c));
		ListItem step4 = new ListItem("Paste the token into the text field below.");
		ListItem step5 = new ListItem(
				"Your internet browser will remember the token until you clear it, so you don't have to repeat the steps.");
		OrderedList steps = new OrderedList(step1, step2, step3, step4, step5);
		steps.setWidth("50%");

		Image howTo = new Image("img/token.png", "How to get a Token");
		Div p2 = new Div(
				"A fly.io access token is required to issue commands on your behalf. The token is only kept in your browser, and you can clear it at any time.");
		Emphasis summary = new Emphasis("Click here to get a fly.io token.");
		summary.getStyle().set("color", "red");
		Details details = new Details(summary, new HorizontalLayout(steps, howTo));
		VerticalLayout tokenMissing = new VerticalLayout(p2, details);
		tokenMissing.setSpacing(false);
		tokenMissing.setMargin(false);
		tokenMissing.getStyle().set("margin-top", "1em");
		tokenMissing.setPadding(false);
		return tokenMissing;
	}

	private void doListApplications(VerticalLayout tokenMissing, PasswordField accessTokenField, VerticalLayout apps,
			Button clearToken) {
		long timeMillis = System.currentTimeMillis();
		if (timeMillis - lastClick < 100) {
			lastClick = timeMillis;
			return;
		}
		lastClick = timeMillis;
		String newToken = accessTokenField.getValue();
		if (newToken != null) {
			tokenMissing.setVisible(false);
			clearToken.setEnabled(true);
			tokenConsumer.setToken(newToken);
		} else {
			tokenMissing.setVisible(true);
			clearToken.setEnabled(false);
			tokenConsumer.setToken(null);
		}

		if (newToken != null && (oldToken == null || !newToken.contentEquals(oldToken))) {
			WebStorage.setItem(Storage.LOCAL_STORAGE, TOKEN_KEY, newToken);
		}
		apps.removeAll();

		UI ui = UI.getCurrent();
		execArea.clear(ui);
		execArea.setVisible(true);

		execArea.append("Listing Applications. Please wait.", ui);
		ui.push();

		new Thread(() -> {
			ui.access(() -> {
				showApps(tokenConsumer.getApps(), apps);
				execArea.clear(ui);
				execArea.setVisible(false);
			});
		}).start();
	}

	private void doTokenClear(VerticalLayout tokenMissing, PasswordField accessTokenField, VerticalLayout apps,
			Button clearToken, Button listApplications) {
		WebStorage.removeItem(Storage.LOCAL_STORAGE, TOKEN_KEY);
		accessTokenField.setValue("");
		listApplications.setEnabled(false);
		tokenMissing.setVisible(true);
		clearToken.setEnabled(false);
		apps.removeAll();
	}

	private void doTokenFromStorage(VerticalLayout tokenMissing, PasswordField accessTokenField, Button clearToken,
			Button listApplications, String value) {
		if (value != null && !value.isBlank()) {
			accessTokenField.setValue(value);
			listApplications.setEnabled(true);
			clearToken.setEnabled(true);
			tokenMissing.setVisible(false);
		} else {
			listApplications.setEnabled(false);
			clearToken.setEnabled(false);
			tokenMissing.setVisible(true);
		}
		oldToken = value;
	}

	private void doTokenSet(Button clearToken, Button listApplications,
			ComponentValueChangeEvent<PasswordField, String> v) {
		if (v.getValue() != null && !v.getValue().isBlank()) {
			listApplications.setEnabled(true);
			clearToken.setEnabled(true);
		} else {
			listApplications.setEnabled(false);
			clearToken.setEnabled(false);
		}
	}

	private HorizontalLayout showApplication(App app) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setAlignItems(Alignment.CENTER);
		NativeLabel label = new NativeLabel(app.appType.toString());
		label.setWidth("15em");
		hl.add(label);

		if (app.created) {
			Anchor a = new Anchor("https://" + app.name + ".fly.dev", app.name, AnchorTarget.BLANK);
			a.getStyle().set("text-decoration", "underline");
			a.setWidth("15em");
			hl.add(a);
			Button updateButton = new Button("Update",
					e -> tokenConsumer.appDeploy(app));
			hl.add(updateButton);

			Button restartButton = new Button("Restart",
					e -> tokenConsumer.appRestart(app));
			hl.add(restartButton);

			ConfirmDialog deletionDialog = buildDeletionDialog(app,
					() -> doListApplications(tokenMissing, accessTokenField, apps, clearToken));
			Button deleteButton = new Button("Delete");
			deleteButton.addClickListener(event -> {
				deletionDialog.open();
			});
			hl.add(deleteButton);

		} else {
			TextField tf = new TextField("");
			tf.setValue(app.name);
			tf.setPlaceholder("enter application name");
			tf.setWidth("15em");
			hl.add(tf);
			tf.setRequired(true);
			tf.setRequiredIndicatorVisible(true);

			Button creationButton = new Button("Create",
					e -> {
						if (tf.getValue() == null || tf.getValue().isBlank()) {
							tf.setErrorMessage("You must provide a value");
							tf.setInvalid(true);
							// Button closeButton = new Button("Close");
							// Notification not = new Notification(
							// new Text("You must provide a value"),
							// closeButton);
							// closeButton.addClickListener((e2) -> not.close());
							// not.addThemeVariants(NotificationVariant.LUMO_ERROR);
							// not.setPosition(Position.MIDDLE);
							// not.open();
							// UI.getCurrent().push();
						} else {
							String siteName = tf.getValue() + ".fly.net";
							boolean ok = tokenConsumer.checkHostname(tf.getValue());
							if (!ok) {
								tf.setErrorMessage(siteName + " is already taken.");
								tf.setInvalid(true);
							} else {
								// this is what we want
								tf.setInvalid(false);
								app.name = tf.getValue();
								logger.info("creating {}", siteName);
								tokenConsumer.appCreate(app);
								doListApplications(tokenMissing, accessTokenField, apps, clearToken);
							}
						}
					});
			hl.add(creationButton);
		}

		return hl;

	}

	private void showApps(Map<AppType, App> appMap, VerticalLayout apps) {
		App owlcmsApp = appMap.get(AppType.OWLCMS);
		App publicApp = appMap.get(AppType.PUBLICRESULTS);

		apps.getStyle().set("margin-top", "1em");
		if (owlcmsApp != null) {
			HorizontalLayout showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		} else {
			owlcmsApp = new App("", AppType.OWLCMS);
			HorizontalLayout showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		}
		if (publicApp != null) {
			HorizontalLayout showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		} else {
			publicApp = new App("", AppType.PUBLICRESULTS);
			HorizontalLayout showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		}
	}
}
