package org.vaadin.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting message in a
 * notification.
 */
@Route("")
public class MainView extends VerticalLayout {

	Logger logger = LoggerFactory.getLogger(MainView.class);

	public MainView() {
		// Use TextField for standard text input
		TextField textField = new TextField("Access Token");
		textField.addClassName("bordered");
		// Button click listeners can be defined as lambda expressions
		UseToken tokenConsumer = new UseToken();
		logger.warn("doit");
		Button button = new Button("List Applications", e -> {
			for (String str : tokenConsumer.getApps("")) {
				if (str.endsWith("-db"))	{
					continue;
				}
				HorizontalLayout hl = new HorizontalLayout();
				hl.setAlignItems(Alignment.CENTER);
				NativeLabel label = new NativeLabel(str);
				hl.add(label);
				hl.add(new Button("Update"));
				hl.add(new Button("Restart"));
				add(hl);
			}

		});

		// Theme variants give you predefined extra styles for components.
		// Example: Primary button is more prominent look.
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		// You can specify keyboard shortcuts for buttons.
		// Example: Pressing enter in this view clicks the Button.
		button.addClickShortcut(Key.ENTER);

		// // Use custom CSS classes to apply styling. This is defined in
		// // styles.css.
		// addClassName("centered-content");

		add(textField, button);
	}
}
