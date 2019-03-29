package org.erachain.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@SuppressWarnings("serial")
public class LoggerTextArea extends JTextArea {

    private Handler handler;
    private Logger logger;

    public LoggerTextArea(Logger logger) {
        super();

        //CREATE HANDLER
        this.handler = new TextComponentHandler(this);
        this.logger = logger;

        //DISABLE INPUT
        this.setLineWrap(true);
        this.setEditable(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        for (Handler hh : this.logger.getHandlers()) {
            if (hh == this.handler) {
                return;
            }
        }

        this.logger.addHandler(this.handler);
    }


    @Override
    public void removeNotify() {
        super.removeNotify();
        this.logger.removeHandler(this.handler);
    }
}


