package app.owlcms.fly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.ScrollerVariant;

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
    };

    public void append(String line, UI ui) {
        ui.access(() -> {
            logger.warn("appending {}", line);
            String curValue = getText();
            String newValue = curValue + System.lineSeparator() + line;
            setText(newValue);
            logger.warn("new value {}", newValue);
            this.getElement().executeJs("var objDiv = document.getElementById('execArea');objDiv.scrollTop = objDiv.scrollHeight;objDiv.scrollIntoView(false)");
            ui.push();
        });
    }
}
