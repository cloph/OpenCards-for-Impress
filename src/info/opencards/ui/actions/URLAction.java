package info.opencards.ui.actions;

import info.opencards.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class URLAction extends AbstractAction {


    private String url;


    public URLAction(String name, Icon icon, String url) {
        super(name, icon);
        this.url = url;
    }


    public URLAction(String name, String url) {
        super(name);

        this.url = url;
    }


    public void actionPerformed(ActionEvent e) {
        // commented out to make compatible with java5
//        if (Desktop.isDesktopSupported()) {
//            try {
//                Desktop.getDesktop().browse(new URL(url).toURI());
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            } catch (URISyntaxException e1) {
//                e1.printStackTrace();
//            }
//        } else {
//            JOptionPane.showConfirmDialog(null, "Can not open browser. To make a donation please visit\n" + url);
//        }

        try {
            BrowserControl.displayURL(url);
        } catch (Exception e1) {
            JOptionPane.showConfirmDialog(null, "Can not open browser. To make a donation please visit\n" + url);
        }
    }


}


class BrowserControl {

    /**
     * Display a file in the system browser.  If you want to display a file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://" or "file://").
     */
    public static void displayURL(final String url) {
        boolean windows = Utils.isWindowsPlatform();
        String cmd = null;
        try {
            if (windows) {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Process p = Runtime.getRuntime().exec(cmd);
            } else {
                // Under Unix, Netscape has to be running for the "-remote"
                // command to work.  So, we try sending the command and
                // check for an exit value.  If the exit command is 0,
                // it worked, otherwise we need to start the browser.
                // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
                new Thread() {
                    public void run() {
                        super.run();
                        try {
                            String cmd = UNIX_PATH + " " + url;
                            Process p = Runtime.getRuntime().exec(cmd);

                            // wait for exit code -- if it's 0, command worked,
                            // otherwise we need to start the browser up.
                            int exitCode = p.waitFor();
                            if (exitCode != 0) {
                                // Command failed, start up the browser
                                // cmd = 'netscape http://www.javaworld.com'
                                cmd = UNIX_PATH + " " + url;
                                p = Runtime.getRuntime().exec(cmd);
                            }
                        } catch (InterruptedException x) {
                            System.err.println("Error bringing up browser, cmd=''");
                            System.err.println("Caught: " + x);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
        catch (IOException x) {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }


    /**
     * Simple example.
     */
    public static void main(String[] args) {
        displayURL("http://www.javaworld.com");
    }


    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";
    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
    // The default browser under unix.
    private static final String UNIX_PATH = "firefox";
    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";
}