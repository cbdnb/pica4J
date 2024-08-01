package de.dnb.basics.mvc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.dnb.basics.utils.OutputUtils;
import de.dnb.basics.utils.TimeUtils;

public class Controller {

    View view;

    Model model;

    private static Controller rc;

    private Controller() {
        this.model = new Model();
        this.view = new View(this.model);
        this.model.addObserver(this.view);
        this.view.addButtonListener(new ButtonListener());
        this.view.addMenuListener(new InfoListener());
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final String s = Controller.this.view.getInput();
            Controller.this.model.setContent(s);
        }

    }

    class InfoListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            OutputUtils.show("Heute ist " + TimeUtils.getToday());
        }
    }

    public static void main(final String[] args) {
        rc = new Controller();
    }

}
