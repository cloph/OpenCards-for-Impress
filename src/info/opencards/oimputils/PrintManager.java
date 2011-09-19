/*
 * Created by JFormDesigner on Fri Dec 14 01:44:01 CET 2007
 */

package info.opencards.oimputils;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.Item;
import info.opencards.core.ReversePolicy;
import info.opencards.core.backend.BackendFactory;
import info.opencards.core.backend.CardFileBackend;
import static info.opencards.oimputils.ImpressHelper.cast;
import info.opencards.util.LayoutRestorer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ResourceBundle;


/**
 * @author Holger Brandl
 */
public class PrintManager extends JDialog {

    private XComponent xComponent;
    private CardFileBackend backend;

    public static final String TEMP_PRINT_SLIDE = ImpressProxy.OC_TMP_SLIDE + "tempPrintSlide";


    public PrintManager(Frame owner, XComponent xComponent, CardFileBackend backend) {
        super(owner);
        this.xComponent = xComponent;
        this.backend = backend;

        initComponents();
        setBounds(LayoutRestorer.getInstance().getBounds("global.printui.location", this, getBounds()));

        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                cleanUpTempSlides();
            }
        });
    }


    private void prepareButtonActionPerformed() {
        // in order to avoid total corruption the current presentation cleanup first before doing print-preparation
        cleanUpTempSlides();

        // get the card-set of this presentation (necessary to dermine the correct reversing policy)
        CardFile cardFile = new CardFile(null);
        cardFile.setSerializer(backend.getSerializer());
        cardFile.synchronize();

        ImpressProxy impressProxy = (ImpressProxy) backend.getPresProxy();

        for (int i = 0; i < cardFile.getFlashCards().getLTMItems().size(); i++) {
            Item item = cardFile.getFlashCards().getLTMItems().get(i);

            ReversePolicy revPolicy = item.getFlashCard().getRevPolicy();
            impressProxy.showReversePoliciedCardFront(i * 2, revPolicy, TEMP_PRINT_SLIDE + " " + (i + 1));
        }

        if (true) // we stop here because of missing OOo-API (cf. http://www.openoffice.org/issues/show_bug.cgi?id=84611 )
            return;

        int numSlidesPerPage = (Integer) numPagesSpinner.getValue();

        // to do: ensure that there are no non-OC-registered slides which might disturb the reordering

        XDrawPagesSupplier xDrawPagesSupplier = cast(XDrawPagesSupplier.class, xComponent);

        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();

        //  fill the missing gap at the end if necessary (only necessary for the last page to be printed)
        while (xDrawPages.getCount() % (numSlidesPerPage * 2) != 0) {
            XDrawPage fillSlide = xDrawPages.insertNewByIndex(xDrawPages.getCount() - 1);
            ImpressHelper.cast(XNamed.class, fillSlide).setName(TEMP_PRINT_SLIDE + " " + (xDrawPages.getCount()));
        }

        // now resort the slides ato make fronts and backs to appear on the opposite sides of coming print-out
        for (int i = 0; i < xDrawPages.getCount(); i++) {
            XDrawPage xDrawPage = getDrawPage(xDrawPages, i);

            if (ImpressHelper.cast(XNamed.class, xDrawPage).getName().startsWith(TEMP_PRINT_SLIDE))
                continue;

            // relocate the slide if it is a tempSlide
            int newPosition = (i % (numSlidesPerPage * 2)) + (numSlidesPerPage * 2);

            // unfortuately there is no OOo-api for slide relocations (cf. bug http://www.openoffice.org/issues/show_bug.cgi?id=84611 )
            //xDrawPages.setPosition(xDrawPage, newPosition);
        }
    }


    private void cleanupCloseButtonActionPerformed() {
        try {
//            cleanUpTempSlides(); --> becomes called automatically as soon as the window is disposed

            dispose();
        } catch (Throwable t) {
            System.err.println("caught: " + t.toString());
        }
    }


    private void cleanUpTempSlides() {
        Object documentSupplier = cast(XDrawPagesSupplier.class, xComponent);

        if (!(documentSupplier instanceof XDrawPagesSupplier))
            return;

        XDrawPagesSupplier xDrawPagesSupplier = (XDrawPagesSupplier) documentSupplier;
        XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();


        for (int i = 0; i < xDrawPages.getCount(); i++) {
            XDrawPage xDrawPage = getDrawPage(xDrawPages, i);

            // this just removed not-removed temporary slides
            if (cast(XNamed.class, xDrawPage).getName().startsWith(TEMP_PRINT_SLIDE)) {
                xDrawPages.remove(xDrawPage);
                i--;
            }
        }
    }


    private void numPagesSpinnerStateChanged() {
        cleanUpTempSlides();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("resources.translation");
        JLabel numSlidesPerPageLabel = new JLabel();
        numPagesSpinner = new JSpinner();
        JButton prepareButton = new JButton();
        JButton cleanupCloseButton = new JButton();

        //======== this ========
        setResizable(false);
        setTitle(bundle.getString("PrintManager.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout) contentPane.getLayout()).columnWidths = new int[]{0, 80, 0};
        ((GridBagLayout) contentPane.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
        ((GridBagLayout) contentPane.getLayout()).columnWeights = new double[]{1.0, 0.0, 1.0E-4};
        ((GridBagLayout) contentPane.getLayout()).rowWeights = new double[]{0.0, 1.0, 1.0, 1.0E-4};

        //---- numSlidesPerPageLabel ----
        numSlidesPerPageLabel.setText(bundle.getString("PrintManager.numSlidesPerPageLabel.text"));
        contentPane.add(numSlidesPerPageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        //---- numPagesSpinner ----
        numPagesSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 2));
        numPagesSpinner.setEnabled(false);
        numPagesSpinner.setToolTipText(bundle.getString("PrintManager.numPagesSpinner.toolTipText"));
        numPagesSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                numPagesSpinnerStateChanged();
            }
        });
        contentPane.add(numPagesSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        //---- prepareButton ----
        prepareButton.setText(bundle.getString("PrintManager.prepareButton.text"));
        prepareButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prepareButtonActionPerformed();
            }
        });
        contentPane.add(prepareButton, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        //---- cleanupCloseButton ----
        cleanupCloseButton.setText(bundle.getString("PrintManager.cleanupCloseButton.text"));
        cleanupCloseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cleanupCloseButtonActionPerformed();
            }
        });
        contentPane.add(cleanupCloseButton, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        setSize(255, 105);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JSpinner numPagesSpinner;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static void main(String[] args) throws BootstrapException {
        XComponentContext xContext = Bootstrap.bootstrap();
        String fileURL = Utils.convert2OOurl(Utils.createTempCopy(new File("testdata/lala.odp")));
        XComponent xComponent = OOoDocumentUtils.openDocument(xContext, fileURL);

        CardFileBackend backend = BackendFactory.getImpressBackend(new Frame(), xComponent, xContext);
        new PrintManager(null, xComponent, backend).setVisible(true);
    }


    public static XDrawPage getDrawPage(XDrawPages xDrawPages, int i) {
        try {
            return cast(XDrawPage.class, xDrawPages.getByIndex(i));
        } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
            throw new RuntimeException("Can not extract slide from XDrawPages");
        } catch (WrappedTargetException e) {
            throw new RuntimeException("Can not extract slide from XDrawPages");
        }
    }
}
