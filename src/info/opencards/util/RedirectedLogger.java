package info.opencards.util;

import info.opencards.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.logging.Level;


/**
 * Redirects the logger output either to file to a logger-JFrame  or to both.
 */
public class RedirectedLogger {

    private final PrintStream aPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));

    private boolean log2File;
    private File logFile;

    private boolean showUI;
    private TextArea aTextArea;


    public RedirectedLogger(boolean showUI, File logFile) {
        this.log2File = logFile != null;
        this.logFile = logFile;
        this.showUI = showUI;

        System.setOut(aPrintStream);
        System.setErr(aPrintStream);

        if (showUI) {
            final JFrame loggerUI = new JFrame("OpenCards log-viewer");
            loggerUI.getContentPane().setLayout(new BorderLayout());
            aTextArea = new TextArea();
            loggerUI.getContentPane().add(BorderLayout.CENTER, aTextArea);
            loggerUI.setBounds(LayoutRestorer.getInstance().getBounds(loggerUI.getTitle(), loggerUI, new Rectangle(300, 300, 300, 300)));

            loggerUI.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    loggerUI.dispose();
                }
            });

            loggerUI.setSize(100, 100);
            loggerUI.setVisible(true);
        }
    }


    private void appendLogMsg(String aString) throws IOException {
        if (showUI)
            aTextArea.append(aString);

        if (log2File) {
            FileWriter aWriter = new FileWriter(logFile, true);
            aWriter.write(aString);
            aWriter.close();
        }
    }


    class FilteredStream extends FilterOutputStream {

        public FilteredStream(OutputStream aStream) {
            super(aStream);
        }


        public void write(byte b[]) throws IOException {
            String aString = new String(b);
            appendLogMsg(aString);
        }


        public void write(byte b[], int off, int len) throws IOException {
            String aString = new String(b, off, len);
            appendLogMsg(aString);
        }
    }


    // a small test app
    public static void main(String s[]) throws InterruptedException {
        try {
            // force an exception for demonstration purpose
            Class.forName("unknown").newInstance();
        }
        catch (Throwable t) {
            // for applet, always RedirectedFrame(false)
            RedirectedLogger r = new RedirectedLogger(true, null);
            t.printStackTrace();
        }

        Thread.sleep(100);

        System.out.println("this is a test.");
        System.out.println("this is a test4.");
        Utils.log(Level.FINE, "test12");
    }
}
