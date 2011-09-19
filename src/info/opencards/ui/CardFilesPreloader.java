package info.opencards.ui;

import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.CardFileSerializer;
import info.opencards.core.categories.Category;
import info.opencards.ui.catui.CategoryTreeSelectionListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class CardFilesPreloader implements CategoryTreeSelectionListener {

    private Container awtOwner;
    private CardFileSerializer serializer;


    public CardFilesPreloader(Container awtOwner, CardFileSerializer serializer) {
        this.awtOwner = awtOwner;
        this.serializer = serializer;
    }


    public void categorySelectionChanged(final java.util.List<CardFile> selectedFiles, Set<Category> selCategories) {
        int nonEmptyFiles = 0;
        for (CardFile curFile : selectedFiles) {
            if (!curFile.isDeserialized()) {
                nonEmptyFiles++;
            }
        }

        if (nonEmptyFiles == 0)
            return;

        final JProgressBar bar = new JProgressBar(1, 100);
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setFont(bar.getFont().deriveFont(bar.getFont().getStyle() | Font.BOLD));

//        final JButton cancelLoading = new JButton();
        final JDialog awtOwner = Utils.getOwnerDialog(this.awtOwner);
        final JDialog dialog = new JDialog(awtOwner);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(bar, BorderLayout.CENTER);
//        dialog.getContentPane().add(cancelLoading, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        if (awtOwner == null)
            dialog.setAlwaysOnTop(true);

        dialog.setTitle(Utils.getRB().getString("CardFilesPreloader.loadingLearnStates") + "...");

        Dimension dim = new Dimension(300, 50);
        dialog.setPreferredSize(dim);
        dialog.setSize(dim);
        dialog.validate();

        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        new Thread() {

            public void run() {

                // collect new cardfiles in order to test the card-hash after loading
                List<CardFile> nonValidatedHashFiles = new ArrayList<CardFile>();

                for (CardFile curFile : selectedFiles) {
                    if (curFile.getFileLocation() == null)
                        continue;

                    bar.setString(curFile.getFileLocation().getName() + "");
                    if (curFile.getSerializer() == null) {
                        curFile.setSerializer(serializer);
                        nonValidatedHashFiles.add(curFile);
                    }

                    try {
                        curFile.getFlashCards();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.log(e.toString());

                        // remove file because it is invalid and inform the user
                        String msg = "Failed to load '" + curFile + "' " + Utils.getRB().getString("");
                        JOptionPane.showConfirmDialog(dialog, msg, "Error", JOptionPane.ERROR_MESSAGE);
                        Collection<Category> curFileCats = new ArrayList<Category>(curFile.belongsTo());
                        for (Category curFileCat : curFileCats) {
                            curFileCat.unregisterCardSet(curFile);
                        }
                    }
                }

                dialog.dispose();


                // ensure the correct hashcodes
                for (CardFile nonValidatedHashFile : nonValidatedHashFiles) {
                    FileOwnerValidator.ensureFileOwnership(nonValidatedHashFile, awtOwner);
                }
            }
        }.start();

        dialog.setVisible(true);
    }

}
