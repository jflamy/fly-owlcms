package app.owlcms.fly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.html.Paragraph;
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
	Logger logger = LoggerFactory.getLogger(MainView.class);
	String oldToken;
	long lastClick = 0;

	public MainView() {
		this.setSpacing(false);
		H2 title = new H2("owlcms - fly.io cloud");

		Div p1 = new Div(
				"This page creates and manages your owlcms applications");
		VerticalLayout intro = new VerticalLayout(p1);
		intro.setSpacing(false);
		intro.setPadding(false);
		intro.setMargin(false);
		intro.getStyle().set("margin-top", "1em");

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
				"A fly.io access token is required to issue commands on your behalf.");
		Emphasis summary = new Emphasis("Click here to get a fly.io token.");
		summary.getStyle().set("color", "red");
		Details details = new Details(summary, new HorizontalLayout(steps, howTo));
		VerticalLayout tokenMissing = new VerticalLayout(p2, details);
		tokenMissing.setSpacing(false);
		tokenMissing.setMargin(false);
		tokenMissing.getStyle().set("margin-top", "1em");
		tokenMissing.setPadding(false);

		PasswordField accessTokenField = new PasswordField("Access Token");
		accessTokenField.setValueChangeMode(ValueChangeMode.EAGER);
		accessTokenField.setWidth("50ch");
		accessTokenField.addClassName("bordered");

		VerticalLayout apps = new VerticalLayout();
		apps.setMargin(false);
		apps.setPadding(false);

		UseToken tokenConsumer = new UseToken();
		Button clearToken = new Button("Clear Token");

		Button listApplications = new Button("List Applications", e -> {
			long timeMillis = System.currentTimeMillis();
			if (timeMillis - lastClick < 100) {
				return;
			}
			lastClick = timeMillis;
			UI.getCurrent().access(() -> {
				String newToken = accessTokenField.getValue();
				if (newToken != null) {
					tokenMissing.setVisible(false);
					System.err.println("enabling clearToken 2");
					clearToken.setEnabled(true);
				} else {
					tokenMissing.setVisible(true);
					System.err.println("disabling clearToken 2");
					clearToken.setEnabled(false);
				}
				if (newToken != null && (oldToken == null || !newToken.contentEquals(oldToken))) {
					WebStorage.setItem(Storage.LOCAL_STORAGE, TOKEN_KEY, newToken);
				}
				apps.removeAll();
				tokenConsumer.getApps(newToken).values().stream().sorted().forEach(app -> {
					if (app.appType != AppType.DB) {
						HorizontalLayout hl = showApplication(app);
						apps.add(hl);
					}
				});
			});
		});
		listApplications.setEnabled(false);
		accessTokenField.addValueChangeListener(v -> {
			if (v.getValue() != null && !v.getValue().isBlank()) {
				listApplications.setEnabled(true);
				clearToken.setEnabled(true);
				System.err.println("enabling 3");
			} else {
				listApplications.setEnabled(false);
				clearToken.setEnabled(false);
				System.err.println("disabling 3");
			}
		});

		clearToken.addClickListener(e -> {
			WebStorage.removeItem(Storage.LOCAL_STORAGE, TOKEN_KEY);
			accessTokenField.setValue("");
			listApplications.setEnabled(false);
			tokenMissing.setVisible(true);
			System.err.println("disabling clearToken 0");
			clearToken.setEnabled(false);
			apps.removeAll();
		});

		WebStorage.getItem(Storage.LOCAL_STORAGE, TOKEN_KEY,
				(value -> {
					if (value != null && !value.isBlank()) {
						accessTokenField.setValue(value);
						listApplications.setEnabled(true);
						System.err.println("enabling clearToken 1");
						clearToken.setEnabled(true);
						tokenMissing.setVisible(false);
					} else {
						listApplications.setEnabled(false);
						System.err.println("disabling clearToken 2");
						clearToken.setEnabled(false);
						tokenMissing.setVisible(true);
					}
					oldToken = value;
				}));

		listApplications.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		listApplications.addClickShortcut(Key.ENTER);

		HorizontalLayout buttons = new HorizontalLayout(listApplications, clearToken);

		add(title, intro, tokenMissing, accessTokenField, buttons, apps);
	}

	private HorizontalLayout showApplication(App app) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setAlignItems(Alignment.CENTER);
		NativeLabel label = new NativeLabel(app.name);
		hl.add(label);
		hl.add(new Button("Update"));
		hl.add(new Button("Restart"));
		return hl;
	}
}
