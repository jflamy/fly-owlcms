package app.owlcms.fly;

import java.security.SecureRandom;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting message in a
 * notification.
 */
@Route("")
// @PreserveOnRefresh
public class MainView extends VerticalLayout {

	private long lastClick = 0;
	final private Logger logger = LoggerFactory.getLogger(MainView.class);
	// private String oldToken;
	private ExecArea execArea;
	private FlyCtlCommands tokenConsumer;
	private VerticalLayout intro;
	private VerticalLayout apps;
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
		H2 title = new H2("owlcms Cloud Applications - " + tokenConsumer.getUserName());
		Button logoutButton = new Button("Logout", (e) -> {
			Notification.show("Logging out", -1, Position.TOP_END);
			UI.getCurrent().close();
		});

		intro = buildIntro();
		apps = buildAppsPlaceholder();
		add(new HorizontalLayout(title, logoutButton), intro, apps, execArea);
		UI ui = UI.getCurrent();
		ui.push();
		logger.warn("==================================");
		doListApplications(apps, ui);
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

		Html propagandaInfo = new Html("""
		        <div>
		        	<h3>How does this work and why is it free?</h3>
		        	<div>
		        		Fly.io is a cloud provider that has promotional billing for small users. They
		        		also do not bill if the actual monthly usage is under 5$.
		        		Running owlcms, its database, and the public results server costs less than
		        		the minimum billable amount, so it is free.
		        	</div>
		        	<div>
		        		This application is a "remote control" application for fly.io. Instead of you
		        		having to understand how to create and manage
		        		an application on fly.io, this site runs the commands for you.
		        	</div>
		        	<div>
		        		The commercial relationship is directly between you and fly.io. All we do is
		        		"type the commands" on your behalf. We do not
		        		get a percentage of your non-existent fees, and do not get any other
		        		advantage.
		        	</div>
		        </div>
		        """);

		Html antiPropagandaInfo = new Html("""
		        <div>
		        	<h3>When to use cloud owlcms? (or not)</h3>
		        	<div>
		        		If you are hosting a major competition, we recommend that you run it locally
		        		on a laptop, and that you use your own Wifi router
		        		instead of relying on the facilities' router. In this way you can run the
		        		competition even if there is an Internet failure.
		        		Running on-site also allows using IWF-Compliant MQTT devices that you can
		        		build
		        		(<a style="text-decoration:underline"
		        		href="https://github.com/jflamy/owlcms-firmata/blob/main/README.md">see this
		        		page</a>)
		        		or buy from suppliers like <a style="text-decoration:underline"
		        		href="https://blue-owl.nemikor.com">blue-owl</a>.
		        		You would, however, still configure a public results site.
		        	</div>
		        </div>
		        """);

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
		mapContainerContainer.setClassName("map");

		getLoginOverlay();
		Button login = new Button("Login", e -> {
			view.add(loginOverlay);
			loginOverlay.setOpened(true);
		});
		login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout topRow = new HorizontalLayout(new H2("owlcms Cloud Application Dashboard "), login);

		Div page = new Div(owlcmsInfo, publicResultsInfo, mapContainerContainer, propagandaInfo, antiPropagandaInfo);
		page.setClassName("page");
		owlcmsInfo.setClassName("info");
		publicResultsInfo.setClassName("info");
		propagandaInfo.setClassName("info");
		antiPropagandaInfo.setClassName("info");

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

		loginOverlay = new LoginOverlay();
		loginOverlay.setI18n(i18n);

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
				successful = tokenConsumer.doLogin(e.getUsername(), e.getPassword());
			} catch (NoLockException e1) {
				successful = false;
				Button closeButton = new Button("Close");
				Notification not = new Notification(
				        new Text(
				                """
				                        "Technical problem: Could not acquire the access token. Please report to owlcms-bugs@jflamy.dev
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
				view.removeAll();
				showApplicationView();
			} else {
				loginOverlay.setError(true);
			}
		});
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
		deletionDialog.setText(new Html("""
		        <div>
		        This will remove the application and make the name available again.
		        </div>
		        """));

		deletionDialog.setConfirmText("Delete");
		deletionDialog.setConfirmButtonTheme("error primary");
		deletionDialog.setCancelButtonTheme("primary");
		deletionDialog.setCancelable(true);
		deletionDialog.setCancelText("Cancel");
		deletionDialog.addConfirmListener(e -> {
			execArea.append("Deleting " + app.name, UI.getCurrent());
			tokenConsumer.appDestroy(app, callback);
			if (app.appType == AppType.OWLCMS) {
				App dbApp = tokenConsumer.getApps().get(AppType.DB);
				execArea.append("Deleting associated database " + dbApp.name, UI.getCurrent());
			}
		});
		deletionDialog.addCancelListener(e -> {
			deletionDialog.close();
		});
		return deletionDialog;
	}

	private VerticalLayout buildIntro() {
		Html p1 = new Html("""
		        <div>
		        This page creates and manages your owlcms applications
		        </div>
		        """);
		VerticalLayout intro = new VerticalLayout(p1);
		intro.setSpacing(false);
		intro.setPadding(false);
		intro.setMargin(false);
		intro.getStyle().set("margin-top", "1em");
		return intro;
	}

	private void doListApplications(VerticalLayout apps, UI ui) {
		long timeMillis = System.currentTimeMillis();
		if (timeMillis - lastClick < 100) {
			lastClick = timeMillis;
			return;
		}
		lastClick = timeMillis;

		new Thread(() -> {
			ui.access(() -> {
				apps.removeAll();
				execArea.clear(ui);
				execArea.setVisible(true);
				execArea.append("Retrieving your application configurations. Please wait.", ui);
				ui.push();
				showApps(tokenConsumer.getApps(), apps);
				execArea.clear(ui);
				execArea.setVisible(false);
			});
		}).start();
	}

	private Div showApplication(App app) {
		Div publicResultsSection = new Div();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setPadding(false);
		publicResultsSection.add(hl);

		hl.setAlignItems(Alignment.CENTER);
		NativeLabel label = new NativeLabel(app.appType.toString());
		label.setWidth("15em");
		hl.add(label);
		UI ui = UI.getCurrent();

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
			        () -> doListApplications(apps, ui));
			Button deleteButton = new Button("Delete");
			deleteButton.addClickListener(event -> {
				deletionDialog.open();
			});
			hl.add(deleteButton);

			if (app.appType == AppType.PUBLICRESULTS) {
				HorizontalLayout hl1 = new HorizontalLayout();
				hl1.setMargin(false);
				NativeLabel label1 = new NativeLabel("Shared Key");
				label1.setWidth("15em");
				Html sharedKeyExplanation1 = new Html("""
				        <div style="margin-bottom:0; width: 40em">
				        	<div>
				        		<em>YOU NEED TO SET THE SHARED KEY <b>ONCE</b> once both owlcms and publicresults are present.</em>
				        	</div>
				        </div>
				        """);
				hl1.add(label1, sharedKeyExplanation1);
				
				HorizontalLayout hl2 = new HorizontalLayout();
				NativeLabel label2 = new NativeLabel("");
				label2.setWidth("15em");
				TextField sharedKeyField = new TextField();
				sharedKeyField.setTitle("Shared string between owlcms and public results");
				sharedKeyField.setPlaceholder("enter a shared string");
				sharedKeyField.setWidth("15em");
				sharedKeyField.setValue(generateRandomString(20));
				Button sharedKeyButton = new Button("Set Shared Key and restart apps",
				        e -> {
					        if (sharedKeyField.getValue() == null || sharedKeyField.getValue().isBlank()) {
						        sharedKeyField.setErrorMessage("The shared key cannot be empty");
						        sharedKeyField.setInvalid(true);
					        } else {
						        tokenConsumer.doSetSharedKey(sharedKeyField.getValue());
					        }
				        });
				hl2.add(label2, sharedKeyField, sharedKeyButton);

				HorizontalLayout hl3 = new HorizontalLayout();
				hl3.setMargin(false);
				NativeLabel label3 = new NativeLabel("");
				label3.setWidth("15em");
				Html sharedKeyExplanation3 = new Html("""
				        <ul style="margin-bottom:0; width: 40em">
				        	<li>
					        	The shared key is a string that is exchanged between owlcms and publicresults so they can trust one another.
				        	</li>
				        	<li>
				        		If your owlcms is running locally at the competition site, you will need to set this
				        		value as the shared key on the laptop using the owlcms user interface, in the Preparation - Settings - Connexions section.
				        	</li>
				        	<li>
				        		<em>Setting the shared key is only needed once, but you can change the shared key at any time if you wish.</em>
				        	</li>
				        </ul>
				        """);
				hl3.add(label3,sharedKeyExplanation3);

				hl1.getStyle().set("margin-top", "1em");
				publicResultsSection.add(hl1,hl2,hl3);
			}
		} else {
			TextField nameField = new TextField("");
			nameField.setValue(app.name);
			nameField.setPlaceholder("enter application name");
			nameField.setWidth("15em");
			hl.add(nameField);
			nameField.setRequired(true);
			nameField.setRequiredIndicatorVisible(true);

			Button creationButton = new Button("Create",
			        e -> {
				        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
					        nameField.setErrorMessage("You must provide a value");
					        nameField.setInvalid(true);
				        } else {
					        String siteName = nameField.getValue() + ".fly.net";
					        boolean ok = tokenConsumer.createApp(nameField.getValue());
					        if (!ok) {
						        nameField.setErrorMessage(siteName + " is already taken.");
						        nameField.setInvalid(true);
					        } else {
						        // this is what we want
						        nameField.setInvalid(false);
						        app.name = nameField.getValue();
						        tokenConsumer.appCreate(app, () -> doListApplications(apps, ui));
					        }
				        }
			        });
			hl.add(creationButton);
			publicResultsSection.add(hl);
		}
		return publicResultsSection;
	}

	private void showApps(Map<AppType, App> appMap, VerticalLayout apps) {
		App owlcmsApp = appMap.get(AppType.OWLCMS);
		App publicApp = appMap.get(AppType.PUBLICRESULTS);

		apps.getStyle().set("margin-top", "1em");
		if (owlcmsApp != null) {
			Div showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		} else {
			owlcmsApp = new App("", AppType.OWLCMS);
			Div showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		}
		if (publicApp != null) {
			Div showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		} else {
			publicApp = new App("", AppType.PUBLICRESULTS);
			Div showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		}
	}

	// Define printable characters
	private static final String PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";

	public String generateRandomString(int length) {
		SecureRandom random = new SecureRandom();
		StringBuilder stringBuilder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(PRINTABLE_CHARACTERS.length());
			char randomChar = PRINTABLE_CHARACTERS.charAt(randomIndex);
			stringBuilder.append(randomChar);
		}

		return stringBuilder.toString();
	}
}
