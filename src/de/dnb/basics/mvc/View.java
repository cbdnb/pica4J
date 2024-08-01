package de.dnb.basics.mvc;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

public class View implements Observer {

    private GUI gui;
    private Model model;

    public View(final Model model) {
        this.model = model;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    View.this.gui = new GUI();
                    View.this.gui.setVisible(true);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public final void addButtonListener(final ActionListener al) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    View.this.gui.btnPress.addActionListener(al);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public final void addMenuListener(final ActionListener al) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    View.this.gui.mntmInfo.addActionListener(al);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public final void update(final Observable obs, final Object message) {
        setOutput(this.model.getContent());
    }

    public final Component getGui() {
        return this.gui;
    }

    public String getInput() {
        return this.gui.textFieldInput.getText();
    }

    public void setOutput(final String output) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    View.this.gui.textFieldOutput.setText(output);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
