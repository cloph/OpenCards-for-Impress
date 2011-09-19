package info.opencards.core.categories;

import com.thoughtworks.xstream.XStream;
import info.opencards.Utils;
import info.opencards.core.CardFile;
import info.opencards.core.CardFileCache;
import info.opencards.util.globset.AdvancedSettings;

import javax.swing.*;
import java.io.*;
import java.util.*;


/**
 * Some static utitilty methods which is the handling of categories
 *
 * @author Holger Brandl
 */
public class CategoryUtils {

    public static final String TREE_PROP = "serialCatTree";
    public static final String INCLUDE_CHILDS = "includeChilds";


    public static List<Category> recursiveCatCollect(Category rootCategory) {
        HashSet<Category> allCats = new HashSet<Category>();

        for (Category category : rootCategory.getChildCategories()) {
            allCats.add(category);
            allCats.addAll(recursiveCatCollect(category));
        }

        allCats.add(rootCategory);
        return new ArrayList<Category>(allCats);
    }


    public static Set<CardFile> recursiveCardFileCollect(Category rootCategory) {
        List<Category> categories = recursiveCatCollect(rootCategory);

        Set<CardFile> cardFiles = new HashSet<CardFile>();

        for (Category category : categories) {
            cardFiles.addAll(new HashSet<CardFile>(category.getCardSets()));
        }

        return cardFiles;
    }


    public static void serializeCategoryModel(Category rootCategory) {
        File confTreeFile = getConfTreeFile();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(confTreeFile));
            new XStream().toXML(rootCategory, writer);
            writer.close();
        } catch (Throwable e) {
            throw new RuntimeException("can not serialize categorytree to " + confTreeFile + ": " + e);
        }

//        String setCatTree = new XStream().toXML(rootCategory);
//        Utils.getPrefs().put(TREE_PROP, setCatTree);
    }


    public static File getConfTreeFile() {
        // use a custom location if defined
        return new File(Utils.getPrefs().get(AdvancedSettings.CUSTOM_CATTREE_LOCATION, AdvancedSettings.getDefaultCatTreeLocation().getAbsolutePath()));
    }


    public static Category deserializeCategoryModel(JDialog awtParent) {
//        String serCatTree = Utils.getPrefs().get(TREE_PROP, null);

        Category rootCat = null;
        File confTreeFile = getConfTreeFile();

        if (confTreeFile.isFile()) {
            try {
//                rootCat = (Category) new XStream().fromXML(serCatTree);
                rootCat = (Category) new XStream().fromXML(new BufferedReader(new FileReader(confTreeFile)));
            } catch (Throwable e) {
                throw new RuntimeException("category-tree deserialization from " + confTreeFile + " failed: " + e);
            }
        }

        if (rootCat != null) {
            // iterate over all categorized files and remove files which are not existent and add remaining files to cache
            for (CardFile cardFile : CategoryUtils.recursiveCardFileCollect(rootCat)) {
                if (!cardFile.getFileLocation().isFile()) {
                    String msg = Utils.getRB().getString("CategoryUtils.fileNotFound").replaceAll("<<filename>>", "'" + cardFile.getFileLocation().getAbsolutePath() + "' ");
                    JOptionPane.showMessageDialog(awtParent, msg, Utils.getRB().getString("CategoryUtils.fileNotFound.title"), JOptionPane.ERROR_MESSAGE);

                    for (Category category : cardFile.belongsTo()) {
                        category.unregisterCardSet(cardFile);
                    }
                } else {
                    CardFileCache.register(cardFile);
                }
            }
        }

//        try{
////         now fix the category-model bug that was present in OC until v 0.16.3
//        for (CardFile cardFile : recursiveCardFileCollect(rootCat)) {
//            // condense and prune all non-existent categories
//            List<Category> fileCats = (List<Category>) cardFile.belongsTo();
//
//            for (int i = 0; i < fileCats.size(); i++) {
//                Category category = fileCats.get(i);
//                Collections.sort(fileCats, new Comparator<Category>() {
//                    public int compare(Category o1, Category o2) {
//                        return o1.getChildCategories().size() > o2.getChildCategories().size() ? -1 : 1;
//                    }
//                });
//
//                boolean isRooted = CategoryUtils.isRooted(rootCat, category);
////                boolean hasMultipleInstances = fileCats.subList(0, i).contains(category);
//                boolean hasMultipleInstances = false;
//
//                if(!isRooted || hasMultipleInstances) {
//                    fileCats.remove(i);
//                    i--;
//                }
//            }
//        }
//        }catch(Throwable t){
//            System.err.println("test");
//        }


        return rootCat;
    }


    private static boolean isRooted(Category rootCat, Category category) {
        if (category.equals(rootCat))
            return true;

        while (category != null) {
            if (category.equals(rootCat))
                return true;

            category = category.getParent();
        }

        return false;
    }


    public static Collection<CardFile> extractSelectedFiles(Collection<Category> selectedCategories) {
        HashSet<CardFile> currentFiles = new HashSet<CardFile>();

        boolean includeChildFiles = Utils.getPrefs().getBoolean(INCLUDE_CHILDS, false);
        if (includeChildFiles) {
            for (Category selectedCategory : selectedCategories) {
                Set<CardFile> recursiveCollectCards = recursiveCardFileCollect(selectedCategory);
                for (CardFile recCollectCardFile : recursiveCollectCards) {
                    if (!currentFiles.contains(recCollectCardFile))
                        currentFiles.add(recCollectCardFile);
                }
            }
        } else {
            for (Category selectedCategory : selectedCategories) {
                List<CardFile> catFiles = selectedCategory.getCardSets();
                for (CardFile catFile : catFiles) {
                    if (!currentFiles.contains(catFile))
                        currentFiles.add(catFile);
                }
            }
        }

        return currentFiles;
    }
}
