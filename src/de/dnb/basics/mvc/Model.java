package de.dnb.basics.mvc;

import java.util.Observable;

public class Model extends Observable {

    private String content;

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
        setChanged();
        notifyObservers();
    }

    public Model() {

    }

}
