package com.unduplicator;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>Created by MontolioV on 18.08.17.
 */
public class ResourcesProvider {
    private static ResourcesProvider ourInstance = new ResourcesProvider();
    private Locale currentLocal;
    private Map<String, ResourceBundle> bundles = new HashMap<>();
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public static ResourcesProvider getInstance() {
        return ourInstance;
    }

    private ResourcesProvider() {
        currentLocal = getSavedLocale();
        setBundlesLocale(currentLocal);
    }

    protected Locale getSavedLocale() {
        try (ObjectInputStream ois = new ObjectInputStream(
                                     new FileInputStream(System.getProperty("user.dir") + "/locale.ser")))
        {
            return (Locale) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Locale("en", "EN");
        }
    }

    protected boolean saveLocale() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                                      new FileOutputStream(System.getProperty("user.dir") + "/locale.ser")))
        {
            oos.writeObject(currentLocal);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setBundlesLocale(Locale locale) {
        currentLocal = locale;

        readWriteLock.writeLock().lock();

        ResourceBundle exceptionsBundle = ResourceBundle.getBundle("com.resources.Exception_Bundle", locale);
        ResourceBundle guiBundle = ResourceBundle.getBundle("com.resources.GUI_Bundle", locale);
        ResourceBundle messagesBundle = ResourceBundle.getBundle("com.resources.Messages_Bundle", locale);
        bundles.put("exceptions", exceptionsBundle);
        bundles.put("gui", guiBundle);
        bundles.put("messages", messagesBundle);

        readWriteLock.writeLock().unlock();

        saveLocale();
    }

    public String getStrFromBundle(String bundleName, String stringKey) {
        readWriteLock.readLock().lock();
        String result = bundles.get(bundleName).getString(stringKey);
        readWriteLock.readLock().unlock();

        return result;
    }

    String getStrFromExceptionBundle(String stringKey) {
        return getStrFromBundle("exceptions", stringKey);
    }

    String getStrFromGUIBundle(String stringKey) {
        return getStrFromBundle("gui", stringKey);
    }

    String getStrFromMessagesBundle(String stringKey) {
        return getStrFromBundle("messages", stringKey);
    }
}