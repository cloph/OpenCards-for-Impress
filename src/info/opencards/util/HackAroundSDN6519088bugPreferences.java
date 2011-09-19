package info.opencards.util;

import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


/**
 * This ugly and hacky implmenation provides a workaround the inability of the folks behind java to fix the
 * showstopper-bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6519088 for Java6
 *
 * @author Holger Brandl
 */
public class HackAroundSDN6519088bugPreferences extends Preferences {

    Map<String, Object> prefs = new HashMap<String, Object>();
    private File prefsFile;


    public HackAroundSDN6519088bugPreferences(File prefsFile) {
        this.prefsFile = prefsFile;

        if (prefsFile.isFile()) {
            try {
                prefs = (Map<String, Object>) new XStream().fromXML(new FileReader(prefsFile));
            } catch (Throwable e) {
//                e.printStackTrace();

                prefs = new HashMap<String, Object>();
                prefsFile.delete();

                try {
                    prefsFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public void put(String key, String value) {
        prefs.put(key, value);
    }


    public String get(String key, String def) {
        if (prefs.containsKey(key))
            return (String) prefs.get(key);
        else
            return def;
    }


    public void remove(String key) {
        prefs.remove(key);
    }


    public void clear() throws BackingStoreException {
        prefs.clear();
    }


    public void putInt(String key, int value) {
        prefs.put(key, value);
    }


    public int getInt(String key, int def) {
        if (prefs.containsKey(key))
            return (Integer) prefs.get(key);
        else
            return def;
    }


    public void putLong(String key, long value) {
        prefs.put(key, value);
    }


    public long getLong(String key, long def) {
        if (prefs.containsKey(key))
            return (Long) prefs.get(key);
        else
            return def;
    }


    public void putBoolean(String key, boolean value) {
        prefs.put(key, value);
    }


    public boolean getBoolean(String key, boolean def) {
        if (prefs.containsKey(key))
            return (Boolean) prefs.get(key);
        else
            return def;
    }


    public void putFloat(String key, float value) {
        prefs.put(key, value);
    }


    public float getFloat(String key, float def) {
        if (prefs.containsKey(key))
            return (Float) prefs.get(key);
        else
            return def;
    }


    public void putDouble(String key, double value) {
        prefs.put(key, value);
    }


    public double getDouble(String key, double def) {
        if (prefs.containsKey(key))
            return (Double) prefs.get(key);
        else
            return def;
    }


    public void putByteArray(String key, byte[] value) {
        throw new RuntimeException("not implemented");
    }


    public byte[] getByteArray(String key, byte[] def) {
        throw new RuntimeException("not implemented");
    }


    public String[] keys() throws BackingStoreException {
        return prefs.keySet().toArray(new String[prefs.keySet().size()]);
    }


    public String[] childrenNames() throws BackingStoreException {
        throw new RuntimeException("not implemented");
    }


    public Preferences parent() {
        throw new RuntimeException("not implemented");
    }


    public Preferences node(String pathName) {
        throw new RuntimeException("not implemented");
    }


    public boolean nodeExists(String pathName) throws BackingStoreException {
        throw new RuntimeException("not implemented");
    }


    public void removeNode() throws BackingStoreException {
        prefs.clear();
    }


    public String name() {
        throw new RuntimeException("not implemented");
    }


    public String absolutePath() {
        throw new RuntimeException("not implemented");
    }


    public boolean isUserNode() {
        throw new RuntimeException("not implemented");
    }


    public String toString() {
        return prefs.toString();
    }


    public void flush() throws BackingStoreException {
        try {
            new XStream().toXML(prefs, new FileWriter(prefsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sync() throws BackingStoreException {
        throw new RuntimeException("not implemented");
    }


    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new RuntimeException("not implemented");
    }


    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new RuntimeException("not implemented");
    }


    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new RuntimeException("not implemented");
    }


    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new RuntimeException("not implemented");
    }


    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new RuntimeException("not implemented");
    }


    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new RuntimeException("not implemented");
    }
}
