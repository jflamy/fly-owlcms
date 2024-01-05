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
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
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
	// private String oldToken;
	private ExecArea execArea;
	private FlyCtlCommands tokenConsumer;
	private VerticalLayout intro;
	private VerticalLayout tokenMissing;
	private PasswordField accessTokenField;
	private VerticalLayout apps;
	private Button clearToken;
	private Button listApplications;
	private LoginOverlay loginOverlay;
	private MainView view;

	public MainView() {
		execArea = new ExecArea();
		execArea.setVisible(false);
		execArea.setSizeFull();
		tokenConsumer = new FlyCtlCommands(execArea);

		if (tokenConsumer.getToken() == null) {
			this.removeAll();
			showLandingPage();
		} else {
			this.removeAll();
			showApplicationView();
		}
	}

	private void showApplicationView() {
		this.setSpacing(false);
		this.setHeightFull();
		H2 title = new H2("owlcms - fly.io cloud");

		intro = buildIntro();
		tokenMissing = buildTokenMissingExplanation();
		accessTokenField = buildAccessTokenField();
		apps = buildAppsPlaceholder();

		clearToken = new Button("Clear Token");
		listApplications = new Button("List Applications",
				e -> doListApplications(tokenMissing, accessTokenField, apps, clearToken));
		listApplications.setEnabled(false);
		listApplications.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		// listApplications.addClickShortcut(Key.ENTER);

		accessTokenField.addValueChangeListener(
				v -> doTokenSet(clearToken, listApplications, v));
		clearToken.addClickListener(
				e -> doTokenClear(tokenMissing, accessTokenField, apps, clearToken, listApplications));

		// WebStorage.getItem(Storage.LOCAL_STORAGE, TOKEN_KEY,
		// 		value -> doTokenFromStorage(tokenMissing, accessTokenField, clearToken, listApplications, value));

		// HorizontalLayout buttons = new HorizontalLayout(listApplications,
		// clearToken);
		add(title, intro,
				// tokenMissing, accessTokenField, buttons,
				apps, execArea);
		doListApplications(tokenMissing, accessTokenField, apps, clearToken);
	}

	private void showLandingPage() {
		view = this;
		view.setSizeFull();

		Html publicResultsInfo = new Html(
				"""
						<div>
							<h3>Publish live results to the Internet, for free</h3>
							<h5 style="margin-top: 0.2em; margin-bottom: 0.2em">From either on-site or cloud owlcms</h4>
							<ul>
								<li>
									Use this site to create a free cloud server that will make your competition results available LIVE, with no delays.
									To anyone in the world with internet access, on a phone, tablet, or laptop.
								</li>
								<li>
									This also works if you run your competitions locally on a Windows, Mac or Linux laptop and have Internet access at your site.
									Your results will be sent to the cloud results server.
								</li>
							</ul>
						</div>
						""");

		Html owlcmsInfo = new Html(
				"""
						<div>
							<h3>Run competitions in the cloud, for free</h3>
							<ul>
								<li>
									If you have good Internet at your club, you can run competitions without having to	install anything.
									You can run a competition on a free cloud server.
								</li>
								<li>
									Use the <b>Login</b> button to create and manage your applications on the fly.io cloud.
								</li>
							</ol>
						</div>
						""");

		// Html propagandaInfo = new Html(
		// """
		// <div>
		// <h3>How does this work and why is it free?</h3>
		// <div>
		// Fly.io is a cloud provider that has promotional billing for small users. They
		// also do not bill if the actual monthly usage is under 5$.
		// Running owlcms, its database, and the public results server costs less than
		// the minimum billable amount, so it is free.
		// </div>
		// <div>
		// This application is a "remote control" application for fly.io. Instead of you
		// having to understand how to create and manage
		// an application on fly.io, this site runs the commands for you.
		// </div>
		// <div>
		// The commercial relationship is directly between you and fly.io. All we do is
		// "type the commands" on your behalf. We do not
		// get a percentage of your non-existent fees, and do not get any other
		// advantage.
		// </div>
		// </div>
		// """);

		// Html antiPropagandaInfo = new Html(
		// """
		// <div>
		// <h3>When to use cloud owlcms? (or not)</h3>
		// <div>
		// If you are hosting a major competition, we recommend that you run it locally
		// on a laptop, and that you use your own Wifi router
		// instead of relying on the facilities' router. In this way you can run the
		// competition even if there is an Internet failure.
		// Running on-site also allows using IWF-Compliant MQTT devices that you can
		// build
		// (<a style="text-decoration:underline"
		// href="https://github.com/jflamy/owlcms-firmata/blob/main/README.md">see this
		// page</a>)
		// or buy from suppliers like <a style="text-decoration:underline"
		// href="https://blue-owl.nemikor.com">blue-owl</a>.
		// You would, however, still configure a public results site.
		// </div>
		// </div>
		// """);

		Div mapContainer = new Div();
		mapContainer.setWidth("1030px");
		mapContainer.setHeight("695px");
		mapContainer.getStyle().set("overflow", "hidden");
		IFrame map = new IFrame(
				"https://www.google.com/maps/d/embed?mid=1cFqfyfoF_RSoM56GewSPDWbuoHihsw4&ehbc=2E312F&z=2");
		map.setWidth("1000px");
		map.setHeight("733px");
		map.getStyle().set("position", "relative");
		map.getStyle().set("top", "-65px");
		map.getStyle().set("left", "-5px");
		mapContainer.add(map);
		Div mapDescription = new Div("Users of the latest owlcms version, since Jan 01 2023");
		mapDescription.getStyle().set("margin-top", "-1em");
		Div mapContainerContainer = new Div();
		mapContainerContainer.add(mapContainer);
		mapContainerContainer.add(mapDescription);

		getLoginOverlay();
		Button login = new Button("Login", e -> {
			view.add(loginOverlay);
			loginOverlay.setOpened(true);
		});
		login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout topRow = new HorizontalLayout(new H2("owlcms Cloud Application Dashboard "), login);

		VerticalLayout blocks1 = new VerticalLayout(owlcmsInfo, publicResultsInfo);
		// VerticalLayout blocks2 = new VerticalLayout(propagandaInfo,
		// antiPropagandaInfo);
		FlexLayout page = new FlexLayout(blocks1, mapContainerContainer);
		page.setFlexDirection(FlexDirection.ROW);
		// page.setFlexShrink(1.0, blocks);
		page.setFlexGrow(0.45, blocks1);
		page.setFlexWrap(FlexWrap.WRAP);
		page.setSizeFull();
		page.setFlexBasis("40%", blocks1);

		view.add(topRow, page);
	}

	private void getLoginOverlay() {

		LoginI18n i18n = LoginI18n.createDefault();
		LoginI18n.Form i18nForm = i18n.getForm();
		i18nForm.setTitle("Log in to fly.io");
		i18n.setForm(i18nForm);

		LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
		i18nErrorMessage.setMessage(
				"Check that you are using your fly.io credentials.  If you have forgotten your password, go to the fly.io site to recover it.");
		i18n.setErrorMessage(i18nErrorMessage);

		// i18n.setAdditionalInformation("Jos tarvitset lisätietoja käyttäjälle.");

		loginOverlay = new LoginOverlay();
		loginOverlay.setI18n(i18n);

		Button loginCancelbutton = new Button("Cancel", e -> loginOverlay.close());
		loginCancelbutton.setWidthFull();
		loginOverlay.setForgotPasswordButtonVisible(false);
		loginOverlay.setTitle("owlcms Cloud Application Management");
		loginOverlay.setDescription(
				"""
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
				successful = tokenConsumer.doLogin(e.getUsername(), e.getPassword());
			} catch (NoLockException e1) {
				successful = false;
				Button closeButton = new Button("Close");
				Notification not = new Notification(
						new Text(
								"Technical problem: Could not acquire the access token. Please report to owlcms-bugs@jflamy.dev"),
						closeButton);
				closeButton.addClickListener((e2) -> not.close());
				not.addThemeVariants(NotificationVariant.LUMO_ERROR);
				not.setPosition(Position.MIDDLE);
				not.open();
			}
			if (successful) {
				loginOverlay.setError(false);
				loginOverlay.setOpened(false);
				view.removeAll();
				showApplicationView();
			} else {
				loginOverlay.setError(true);
			}
		});
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
		// String newToken = accessTokenField.getValue();
		// if (newToken != null) {
		// 	tokenMissing.setVisible(false);
		// 	clearToken.setEnabled(true);
		// 	tokenConsumer.setToken(newToken);
		// } else {
		// 	tokenMissing.setVisible(true);
		// 	clearToken.setEnabled(false);
		// 	tokenConsumer.setToken(null);
		// }

		// if (newToken != null && (oldToken == null || !newToken.contentEquals(oldToken))) {
		// 	WebStorage.setItem(Storage.LOCAL_STORAGE, TOKEN_KEY, newToken);
		// }
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

	// private void doTokenFromStorage(VerticalLayout tokenMissing, PasswordField accessTokenField, Button clearToken,
	// 		Button listApplications, String value) {
	// 	if (value != null && !value.isBlank()) {
	// 		accessTokenField.setValue(value);
	// 		listApplications.setEnabled(true);
	// 		clearToken.setEnabled(true);
	// 		tokenMissing.setVisible(false);
	// 	} else {
	// 		listApplications.setEnabled(false);
	// 		clearToken.setEnabled(false);
	// 		tokenMissing.setVisible(true);
	// 	}
	// 	oldToken = value;
	// }

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
