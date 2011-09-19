package info.opencards.oimputils;

import com.sun.star.beans.*;
import com.sun.star.container.XNameAccess;
import com.sun.star.document.XDocumentInfo;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.document.XStandaloneDocumentInfo;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XStringSubstitution;
import info.opencards.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Some utility-methods which ease the hacking of OOo
 *
 * @author Holger Brandl
 */
public class OpenOfficeUtils {

    public static final String FORCED_UI_LANGUAGE = "forced.uilang";

    public static final String OC_SERIALIZATION_PREFIX = "opencards_";


    /**
     * Maps the Locale of OpenOpen (obtained via the ConfigurationProvider) into a java-Locale.
     */
    public static java.util.Locale getLocale(XComponentContext xContext) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();

            // create the provider
            String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
            XMultiServiceFactory xProvider = ImpressHelper.cast(XMultiServiceFactory.class, serviceManager.createInstanceWithContext(sProviderService, xContext));

            String sReadOnlyView = "com.sun.star.configuration.ConfigurationUpdateAccess";
            com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
            aPathArgument.Name = "nodepath";
            aPathArgument.Value = "org.openoffice.Setup/L10N";

            Object[] aArguments = new Object[1];
            aArguments[0] = aPathArgument;

            XInterface xViewRoot = (XInterface) xProvider.createInstanceWithArguments(sReadOnlyView, aArguments);
            XNameAccess xProperties = ImpressHelper.cast(XNameAccess.class, xViewRoot);

            String language = (String) xProperties.getByName("ooLocale");

            String forcedUILangName = Utils.getPrefs().get(FORCED_UI_LANGUAGE, null);
            if (forcedUILangName != null)
                language = forcedUILangName;

            // here we only locales are listed to which OpenCards was translated
            if (language.startsWith("en") || language.length() == 0) {
                return java.util.Locale.ENGLISH;
            } else if (language.startsWith("de")) {
                return java.util.Locale.GERMAN;
            } else if (language.startsWith("fr")) {
                return java.util.Locale.FRENCH;
            } else if (language.startsWith("es")) {
                return new Locale("es", "Spain");
            } else if (language.startsWith("it")) {
                return new Locale("it", "Italian");
            } else if (language.startsWith("el")) {
                return new Locale("el", "Greek");
            } else if (language.startsWith("pt")) {
                return new Locale("pt", "Portuguese");
            } else if (language.startsWith("bg")) {
                return new Locale("bg", "Bulgarian");
            } else {
                return null;
            }
        } catch (com.sun.star.uno.Exception e) {
            throw new RuntimeException("Can not determine the used OpenOffice-Locale");
        }
    }


    /**
     * Determines the used version of OpenOffice
     */
    public static String getOOoVersion(XComponentContext xContext) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();

            // create the provider
            String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
            XMultiServiceFactory xProvider = ImpressHelper.cast(XMultiServiceFactory.class, serviceManager.createInstanceWithContext(sProviderService, xContext));

            String sReadOnlyView = "com.sun.star.configuration.ConfigurationUpdateAccess";
            com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
            aPathArgument.Name = "nodepath";
            aPathArgument.Value = "org.openoffice.Setup/Product";

            Object[] aArguments = new Object[1];
            aArguments[0] = aPathArgument;

            XInterface xViewRoot = (XInterface) xProvider.createInstanceWithArguments(sReadOnlyView, aArguments);
            XNameAccess xProperties = ImpressHelper.cast(XNameAccess.class, xViewRoot);

            return (String) xProperties.getByName("ooSetupVersion");

        } catch (com.sun.star.uno.Exception e) {
            throw new RuntimeException("Can not determine the used OpenOffice-version");
        }
    }


    public static void main(String[] args) {
        Utils.getPrefs().put(FORCED_UI_LANGUAGE, "en");
//        Utils.getPrefs().remove(FORCED_UI_LANGUAGE);
    }


    public static boolean writeDocumentProperty(XComponent xComponent, String propName, Object propValue) {
        try {
            XDocumentInfoSupplier xdis = ImpressHelper.cast(XDocumentInfoSupplier.class, xComponent);
            XDocumentInfo xdi = xdis.getDocumentInfo();

            writeProperty(xdi, propName, propValue);

            return true;

//            int fieldIndex = Utils.getPrefs().getInt(AdvancedSettings.SAVE_LOCATION, AdvancedSettings.SAVE_LOCATION_DEFAULT);
//            xdi.setUserFieldName((short) fieldIndex, Constants.ODF_PROP_FIELD);
//            xdi.setUserFieldValue((short) fieldIndex, propValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Object readDocumentProperty(XComponent xComponent, String propName) {
        try {
            XDocumentInfoSupplier xdis = ImpressHelper.cast(XDocumentInfoSupplier.class, xComponent);
            XDocumentInfo xdi = xdis.getDocumentInfo();

            XPropertySet xPropSet = ImpressHelper.cast(XPropertySet.class, xdi);
            return readProperty(xPropSet, OC_SERIALIZATION_PREFIX + propName);

//            int fieldIndex = Utils.getPrefs().getInt(AdvancedSettings.SAVE_LOCATION, AdvancedSettings.SAVE_LOCATION_DEFAULT);
//            return xdi.getUserFieldValue((short) fieldIndex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Object readClosedFileDocumentProperty(XComponentContext xContext, String propName, File file) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();
            XInterface xDocInterface = (XInterface) serviceManager.createInstanceWithContext("com.sun.star.document.StandaloneDocumentInfo", xContext);
            XStandaloneDocumentInfo xDocInfo = ImpressHelper.cast(XStandaloneDocumentInfo.class, xDocInterface);

            String fileURL = file.toURI().toURL().toString();
            xDocInfo.loadFromURL(fileURL);

            XPropertySet xPropSet = ImpressHelper.cast(XPropertySet.class, xDocInfo);
            return readProperty(xPropSet, OC_SERIALIZATION_PREFIX + propName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void writeClosedFileDocumentProperty(XComponentContext xContext, String propName, Object propValue, File file) {
        try {
            XMultiComponentFactory serviceManager = xContext.getServiceManager();
            XInterface xDocInterface = (XInterface) serviceManager.createInstanceWithContext("com.sun.star.document.StandaloneDocumentInfo", xContext);
            XStandaloneDocumentInfo xDocInfo = ImpressHelper.cast(XStandaloneDocumentInfo.class, xDocInterface);

            String fileURL = file.toURI().toURL().toString();
            xDocInfo.loadFromURL(fileURL);

            writeProperty(xDocInfo, propName, propValue);

            xDocInfo.storeIntoURL(fileURL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Object readProperty(XPropertySet xPropSet, String propName) throws UnknownPropertyException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {
        if (!xPropSet.getPropertySetInfo().hasPropertyByName(propName))
            return null;

        Object propValue = xPropSet.getPropertyValue(propName);

        Type type = AnyConverter.getType(propValue);
        if (type.getTypeName().equals("string")) {
            return AnyConverter.toString(propValue);

        } else if (type.getTypeName().equals("int")) {
            return AnyConverter.toInt(propValue);

        } else if (type.getTypeName().equals("double")) {
            return AnyConverter.toDouble(propValue);

        } else if (type.getTypeName().equals("long")) {
            return AnyConverter.toLong(propValue);

        } else if (type.getTypeName().equals("boolean")) {
            return AnyConverter.toBoolean(propValue);

        } else if (type.getTypeName().contains("void")) {
            return null;

        } else {
            throw new RuntimeException("can not convert ANY");
        }
    }


    public static void writeProperty(XDocumentInfo xDocInfo, String propName, Object propValue) throws PropertyExistException, IllegalTypeException, com.sun.star.lang.IllegalArgumentException {
        try {
            propName = OC_SERIALIZATION_PREFIX + propName;

            XPropertySet xPropSet = ImpressHelper.cast(XPropertySet.class, xDocInfo);

            if (xPropSet.getPropertySetInfo().hasPropertyByName(propName)) {
                xPropSet.setPropertyValue(propName, propValue);
            } else {
                XPropertyContainer xPropContainer = ImpressHelper.cast(XPropertyContainer.class, xDocInfo);
                xPropContainer.addProperty(propName, (short) (PropertyAttribute.REMOVABLE & PropertyAttribute.OPTIONAL), propValue);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void removeProperty(XComponent xDrawComponent, String propName) throws Exception {

        propName = OpenOfficeUtils.OC_SERIALIZATION_PREFIX + propName;

        XDocumentInfoSupplier xdis = ImpressHelper.cast(XDocumentInfoSupplier.class, xDrawComponent);
        XDocumentInfo xdi = xdis.getDocumentInfo();
        XPropertyContainer xPropContainer = ImpressHelper.cast(XPropertyContainer.class, xdi);

        XPropertySet xPropSet = ImpressHelper.cast(XPropertySet.class, xdi);

        if (xPropSet.getPropertySetInfo().hasPropertyByName(propName)) {
            xPropContainer.removeProperty(propName);
        }
    }


    /**
     * Removes all custom OpenCards properties from an Impress-document.
     */
    public static void removeAllOCProperties(XComponent xDrawComponent) throws Exception {
        XDocumentInfoSupplier xdis = ImpressHelper.cast(XDocumentInfoSupplier.class, xDrawComponent);
        XDocumentInfo xdi = xdis.getDocumentInfo();
        XPropertyContainer xPropContainer = ImpressHelper.cast(XPropertyContainer.class, xdi);

        XPropertySet xPropSet = ImpressHelper.cast(XPropertySet.class, xdi);
        List<String> removeCandidates = new ArrayList<String>();
        for (Property property : xPropSet.getPropertySetInfo().getProperties()) {
            if (property.Name.startsWith(OpenOfficeUtils.OC_SERIALIZATION_PREFIX))
                removeCandidates.add(property.Name);
        }

        for (String removeCandidate : removeCandidates) {
            xPropContainer.removeProperty(removeCandidate);
        }
    }


    public static XComponent getAWTHandle(XComponentContext xCompContext) {
        throw new RuntimeException("not implemented yet");

//        XDesktop xDesktop = OOoDocumentUtils.getXDesktop(xCompContext);
//        XFrame currentFrame = xDesktop.getCurrentFrame();
//        currentFrame.getComponentWindow()
//        return xDesktop.getCurrentComponent();
    }


    public static File getOOUserDirectory(XComponentContext xContext) {
        try {
            XMultiComponentFactory xMCF = xContext.getServiceManager();
            Object pathObject = xMCF.createInstanceWithContext("com.sun.star.util.PathSubstitution", xContext);
            XStringSubstitution stringSubstitution = ImpressHelper.cast(XStringSubstitution.class, pathObject);
            String pathName = stringSubstitution.getSubstituteVariableValue("$(user)");

            if (Utils.isWindowsPlatform())
                pathName = pathName.replaceAll("file:///", "").replaceAll("%20", " ");
            else {
                pathName = pathName.replaceAll("file:", "").replaceAll("%20", " ");
            }

            return new File(pathName + File.separatorChar + "config");
        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        return new File(System.getProperty("user.home"));
    }
}
