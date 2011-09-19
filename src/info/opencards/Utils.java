package info.opencards;

import com.jgoodies.looks.Options;
import info.opencards.ui.actions.HelpAction;
import info.opencards.util.HackAroundSDN6519088bugPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Some global static utility methods which are used here, there and everywhere in OpenCards codebase.
 *
 * @author Holger Brandl
 */
public class Utils {


    private static Preferences prefs;

    // Used to identify the windows platform.
    public static final String WIN_ID = "Windows";

    public static final String PROP_STARTUP_COUNTER = "general.startup.counter";

    public static File configDir;


    public static File getConfigDir() {
        return configDir == null ? new File(System.getProperty("user.home")) : configDir;
    }


    public static Random r;


    /**
     * Returns a random generator, which is also globally accessible in order to allow a determinstic behavior for
     * testing purposes.
     */
    public synchronized static Random getRandGen() {
        if (r == null)
            r = new Random(System.currentTimeMillis());

        return r;
    }


    /**
     * Make java to use the much better jgoodies L&F.
     */
    public static void configureUI() {
//        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
//        Options.setDefaultIconSize(new Dimension(18, 18));

        if (!isWindowsPlatform()) // don't change the l&f on linux
            return;

        String lafName = isWindowsPlatform()
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        try {
            LookAndFeel laf = (LookAndFeel) getOCClass().getClassLoader().loadClass(lafName).getConstructors()[0].newInstance();
            UIManager.setLookAndFeel(laf);
        } catch (Throwable e) {
            System.err.println("Can't set look & feel:" + e);
        }
    }


    public synchronized static Preferences getPrefs() {
        if (prefs == null) {
            if (isWindowsPlatform()) {
                prefs = Preferences.userNodeForPackage(Utils.class);
            } else {
                // we're on linux now
                File prefsFile = new File(System.getProperties().getProperty("user.home") + File.separatorChar + ".opencards");
                prefs = new HackAroundSDN6519088bugPreferences(prefsFile);
            }
        }

        return prefs;
    }

//    public static void flushPropsOnShutDown() {
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                super.run();
//                try {
//                    getPrefs().flush();
//                    JOptionPane.showMessageDialog(null, "test");
//                    System.err.println("flushing OpenCards preferences......");
//                } catch (BackingStoreException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//    }


    public static void flushPrefs() {
        try {
            getPrefs().flush();
        } catch (BackingStoreException e) {
//            JOptionPane.showMessageDialog(null, "" + e.toString());
            e.printStackTrace();
        }
    }


    public static void resetAllSetttings() {
        try {
            getPrefs().clear();
            getPrefs().flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

//        prefs = null;
    }


    /**
     * Returns the global tranlation bundle of OpenCards.
     */
    public static ResourceBundle getRB() {
        return ResourceBundle.getBundle("resources.translation");
    }


    public static void log(String msg) {
        log(Level.INFO, msg);
    }


    public static void log(Level level, String msg) {
        Logger.getLogger("opencards").log(level, msg);
    }


    public static JDialog getOwnerDialog(Container awtOwner) {
        while (awtOwner != null && !(awtOwner instanceof JDialog)) {
            awtOwner = awtOwner.getParent();
        }

        return (JDialog) awtOwner;
    }


    public static String convert2OOurl(File file) {
        try {
            return file.toURI().toURL().toString().replace("file:/", "file:///");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    private static DummyClass dummyObject = new DummyClass();


    /**
     * This method is a workaround for the new class-path policy introduced in OO.o 2.3. In order to make calls of
     * Class.getResource to work properly on the extension installation directory it is necessary that the class object
     * being used is an instance of a class which is located in the extension-jar-archive.
     */
    public static Class getOCClass() {
        return dummyObject.getClass();
    }


    public static void main(String[] args) {
        try {
            for (String s : getPrefs().keys()) {
                System.out.println("pref is : " + s + " and its value is " + getPrefs().get(s, "foobar"));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        resetAllSetttings();

        // set pref for testing
        getPrefs().putDouble("lalelu", 56);
    }


    /**
     * Try to determine whether this application is running under Windows or some other platform by examing the
     * "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
    }


    public static boolean isLinux() {
        return !isWindowsPlatform();
    }


    public static File createTempCopy(File file) {
        // create a random file-name
        try {
            assert file.isFile() : "File to be copied is not a regular one";
            File tempFile = File.createTempFile(file.getName().replace(".odp", ""), ".odp");

            copyFile(file, tempFile);

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static File copyFile(File inFile, File outFile) {
        try {
            assert inFile.isFile();
            assert outFile.getParentFile().isDirectory();

            InputStream is = new FileInputStream(inFile);
            OutputStream os = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }

            is.close();
            os.close();

            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Collects all files list which match ALL given filters.
     *
     * @param directory the base directory for the search
     * @return The list of all odp-files found in one of the subdirectories of <code>directory</code>.
     */
    public static java.util.List<File> findOdpFiles(File directory) {
        assert directory.isDirectory();

        java.util.List<File> allFiles = new ArrayList<File>();

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                allFiles.addAll(findOdpFiles(file));
            } else {
                if (file.isFile() && file.getName().endsWith(".odp"))
                    allFiles.add(file);
            }
        }

        Collections.sort(allFiles);
        return allFiles;
    }


    /**
     * closes a dialog when escape is pressed either by disposing (<code>dispose == true</code> )it or by making it
     * invisible.
     */
    public static void closeOnEsc(final JDialog dialog, final boolean doDispose) {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (doDispose)
                    dialog.dispose();
                else
                    dialog.setVisible(false);
            }
        };

        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }


    public static void actionOnEsc(final JDialog dialog, final Action action) {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(null);
            }
        };

        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }


    /**
     * @param dialog      The dialog for which f1 as help-trigger should be registered
     * @param helpSection (Optional) The help-section which will be attached to the base-help url
     */
    public static void helpOnF1(final JDialog dialog, String helpSection) {
        KeyStroke fOne = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false);

        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(fOne, "F_ONE");
        dialog.getRootPane().getActionMap().put("F_ONE", new HelpAction(helpSection != null ? helpSection : "help"));
    }


    public static void sleep(final int timeMs) {
        new Thread() {

            public void run() {
                try {
                    sleep(timeMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }


    public static boolean isAllFalse(Collection<Boolean> booleans) {
        for (Boolean aBoolean : booleans) {
            if (aBoolean)
                return false;
        }

        return true;
    }
}


class DummyClass {

}

