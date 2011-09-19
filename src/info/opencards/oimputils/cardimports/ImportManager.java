package info.opencards.oimputils.cardimports;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyle;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import info.opencards.Utils;
import info.opencards.oimputils.DrawPageHelper;
import info.opencards.oimputils.ImpressHelper;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.oimputils.OOoDocumentUtils;
import info.opencards.ui.actions.URLAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class ImportManager {

    public ImportManager(final Frame owner, final XComponent xComponent) {

        new Thread() {

            public void run() {
                super.run();

                final String DEFAULT_DIR = "import.defdir";
                File defDir = new File(Utils.getPrefs().get(DEFAULT_DIR, System.getProperty("user.home")));
                JFileChooser importChooser = new JFileChooser(defDir);

                ImpSeparatorPanel sepPanel = new ImpSeparatorPanel(importChooser);

                importChooser.setDialogTitle(Utils.getRB().getString("cardimport.filechoose.title"));
                importChooser.setMultiSelectionEnabled(false);

                FileFilter csvFilter = new FileFilter() {

                    public boolean accept(File f) {
                        String fileName = f.getName();
                        return (fileName.endsWith(".csv") || fileName.endsWith(".txt")) || f.isDirectory();
                    }


                    public String getDescription() {
                        return "Text with tab separated Q/A (*.csv, *.txt)";
                    }
                };

                importChooser.setFileFilter(csvFilter);
                int status = importChooser.showOpenDialog(null);
                if (status != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File selectedFile = importChooser.getSelectedFile();
                FileFilter selectedFilter = importChooser.getFileFilter();

                Utils.getPrefs().put(DEFAULT_DIR, selectedFile.getParentFile().getAbsolutePath());

                boolean addedFlashcards = false;

                if (selectedFilter == csvFilter) {
                    Map<String, String> title2contents = readCsvFile(selectedFile, sepPanel.getCurSeparator());

                    addedFlashcards = insertCardsIntoPresentation(title2contents, xComponent);

//                } else(selectedFilter == ...){
//                    // ... import from wiki, db, other flashcard-files, whatever!! :-)
                }

                // show the FAQ if nothing was imported. But do this only once to avoid users to become annoyed
                String SHOWN_IMPORT_HELP_BEFORE = "hasShownImportHelp";
                if (!addedFlashcards && !Utils.getPrefs().getBoolean(SHOWN_IMPORT_HELP_BEFORE, false)) {
                    Utils.getPrefs().putBoolean(SHOWN_IMPORT_HELP_BEFORE, true);
                    new URLAction("nocardsimported", "http://www.opencards.info/index.php/faq#import").actionPerformed(null);
                }

            }
        }.start();
    }


    public static Map<String, String> readCsvFile(File selectedFile, String separatorString) {
        try {

            Charset encoding = new SmartEncodingInputStream(new FileInputStream(selectedFile)).getEncoding();

            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), encoding.displayName()));

            Map<String, String> title2contents = new LinkedHashMap<String, String>();

            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] splitLine = line.split(separatorString);

                if (splitLine.length < 2)
                    continue;

                String title = splitLine[0].trim();
                String content = splitLine[1].trim();

                if (title.length() > 0 && content.length() > 0) {

                    // fix initial and final " if space separator has been used
                    if (separatorString.equals(ImpSeparatorPanel.SPACE_SEPARATOR)) {
                        title = title.replace("\"", "");
                        content = content.replace("\"", "");
                    }

                    // make sure that the card set does not already contains the key
                    while (title2contents.containsKey(title))
                        title = title + " ";

                    title2contents.put(title, content);
                }
            }

            fileReader.close();
            return title2contents;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean insertCardsIntoPresentation(Map<String, String> title2contents, XComponent xComponent) {

        try {

            boolean addedFlashcards = false;

            for (String title : title2contents.keySet()) {
                String content = title2contents.get(title);
                title = title.trim(); // strip optional whitespace (previously attached to make map keys unique

                // create a new slide using the first part as title and the second one as content
                int numSlides = ImpressHelper.numSlides(xComponent);
                XDrawPage insertPage = ImpressHelper.insertNewDrawPageByIndex(xComponent, numSlides - 1);
                XPropertySet xPropSet = cast(XPropertySet.class, insertPage);

                // the most important thing to do: use an auto-layout (autolayout) to format title and content
                xPropSet.setPropertyValue("Layout", 0);

                XText xText = cast(XText.class, DrawPageHelper.getTitleShape(insertPage));
                xText.setString(title);

                setContent(insertPage, content);
                addedFlashcards = true;
            }
            return addedFlashcards;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public static void setContent(XDrawPage xDrawPage, String slideContent) {
        try {
            XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);

            // try to find a shape which has is a subtitle
            for (int j = 0; j < xShapes.getCount(); j++) {
                XShape xShape = cast(XShape.class, xShapes.getByIndex(j));
                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

                Object style = xPropSet.getPropertyValue("Style");
                XStyle usedXStyle = cast(XStyle.class, style);

                if (usedXStyle.getName().equals("subtitle")) {
                    XText xText = cast(XText.class, xShape);
                    xText.setString(slideContent);
                }
            }
        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws BootstrapException {
        XComponent xComponent = OOoDocumentUtils.openDocument(Bootstrap.bootstrap(), "private:factory/simpress");

        new ImportManager(new Frame(), xComponent);
    }
}
