package org.erachain.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class TextComponentHandler extends Handler {
    private static final Logger LOGGER = Logger.getLogger(LoggerTextArea.class.getName());
    private final JTextArea text;

    TextComponentHandler(JTextArea text) {
        this.text = text;
        this.setFormatter(new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            synchronized (this.text) {
                this.text.append(this.getFormatter().format(record));

                //ONLY KEEP LAST 1000 LANES TO PREVENT MEMORY ISSUES
                int rows = this.text.getLineCount();
                if (rows > 10000) {
                    try {
                        int end = this.text.getLineEndOffset(rows - 10000 - 1);
                        this.text.replaceRange("", 0, end);
                    } catch (BadLocationException e) {
                        LOGGER.info(e.getMessage());
                    }

                }

            }
        }
    }

    @Override
    public void flush() {/**/}

    @Override
    public void close() throws SecurityException {/**/}
}
