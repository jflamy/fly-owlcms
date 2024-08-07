package app.owlcms.fly.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Pre;

public class ExecArea extends Pre {

    Logger logger = LoggerFactory.getLogger(ExecArea.class);

    ExecArea() {
        this.getStyle().set("overflow", "scroll");
        this.getStyle().set("padding-left","1em");
        this.getElement().setProperty("scrollTop", Integer.MAX_VALUE);
        this.setId("execArea");
    }

    public void clear(UI ui) {
        ui.access(() -> {
            this.setText("");
            ui.push();
        });
    }

    public void appendLine(String line, UI ui, String prompt) {
        ui.access(() -> {
            logger.info(prompt+" "+line);
            String curValue = getText();
            String newValue = curValue + System.lineSeparator() + line;
            setText(newValue);
            this.getElement().executeJs("var objDiv = document.getElementById('execArea');objDiv.scrollTop = objDiv.scrollHeight;objDiv.scrollIntoView(false)");
            ui.push();
        });
    }

    public void append(String string, UI ui) {
        appendLine(string, ui, ">>>");
    }
    public void appendError(String string, UI ui) {
        appendLine(string, ui, "***");
    }
}
