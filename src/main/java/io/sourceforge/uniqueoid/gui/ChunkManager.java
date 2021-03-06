package io.sourceforge.uniqueoid.gui;

import io.sourceforge.uniqueoid.logic.FindDuplicatesTask;
import io.sourceforge.uniqueoid.logic.FindTaskSettings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Parent;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created by MontolioV on 30.08.17.
 */
public class ChunkManager {
    private final GUI GUI;
    private Results results;
    private Set<AbstractGUIChunk> chunks = new HashSet<>();
    private MenuBarChunk menuBarChunk;
    private SetupChunk setupChunk;
    private RuntimeStatusChunk runtimeChunk;
    private DeleterChunk deleterChunk;
    private AboutChunk aboutChunk;
    private SettingsChunk settingsChunk;

    public ChunkManager(GUI gui) {
        GUI = gui;
        makeChunks();
        showMenuBar();
        showSetupNode();
        updateChunksStates(GuiStates.NO_RESULTS);
    }

    //Make chunks
    protected void makeChunks() {
        menuBarChunk = new MenuBarChunk(this, GUI.getPrimaryStage());
        runtimeChunk = new RuntimeStatusChunk(this);
        setupChunk = new SetupChunk(GUI.getPrimaryStage(), this);
        settingsChunk = new SettingsChunk(this);

        setupChunk.setStartButtonHandler(runtimeChunk.getTaskButtonHandler(() -> setupChunk.getStartTask()));

        chunks.add(menuBarChunk);
        chunks.add(runtimeChunk);
        chunks.add(setupChunk);
        chunks.add(settingsChunk);
    }
    protected void makeDeleterChunk() {
        deleterChunk = new DeleterChunk(this);
        deleterChunk.updateChunk();
        chunks.add(deleterChunk);

        setupChunk.setAddToResultsButtonHandler(runtimeChunk.getTaskButtonHandler(() -> results.addToPreviousResultTask()));
    }
    protected void makeAboutChunk() {
        aboutChunk = new AboutChunk(this);
        chunks.add(aboutChunk);
    }

    //Show/hide in GUI
    protected void showMenuBar() {
        GUI.setTopNode(menuBarChunk.getAsNode());
    }
    protected void showSetupNode() {
        GUI.setCenterNode(setupChunk.getAsNode());
    }
    protected void showRuntimeStatusNode() {
        GUI.setCenterNode(runtimeChunk.getAsNode());
    }
    protected void showDeletionNode() {
        GUI.setCenterNode(deleterChunk.getAsNode());
    }
    protected void showException(Exception ex) {
        ex.printStackTrace();
        GUI.showException(ex);
    }
    protected void resizeAlertManually(Alert alert) {
        GUI.resizeAlertManually(alert);
    }
    protected void showInNewStage(Parent parent) {
        GUI.showInNewStage(parent);
    }
    protected void showSettingsNode() {
        GUI.setLeftNode(settingsChunk.getAsNode());
    }
    protected void hideSettingsNode() {
        GUI.setLeftNode(null);
    }

    //Update all chunks
    protected void updateChunksLocales() {
        chunks.forEach(LocaleDependent::updateLocaleContent);
    }
    protected void updateChunksStates(GuiStates newState) {
        chunks.forEach(chunk -> chunk.changeState(newState));
    }

    //Results chunk features
    protected void setResults(Map<String, Set<File>> processedFilesMap) {
        results = new Results(this, processedFilesMap);
    }
    protected void saveResults(File file) {
        results.saveToFile(file);
    }
    protected void loadResults(File file) {
        results = new Results(this, file);
        updateChunksStates(GuiStates.HAS_RESULTS);

        DeleterChunk oldDeleterChunk = deleterChunk;
        makeDeleterChunk();

        if (oldDeleterChunk != null) {
            chunks.remove(oldDeleterChunk);
        }

        showDeletionNode();
    }
    /**
     * Remove files that already don't exist.
     */
    protected void updateResults() {
        results.update();
    }
    protected int getDuplicatesAmount() {
        if (results == null) return 0;
        return results.countDuplicates();
    }
    protected Set<File> getDuplicateFilesCopy(String checksumKey) {
        return results.getDuplicateFilesCopy(checksumKey);
    }
    protected Set<String> getDuplicatesChecksumSet() {
        return results.getDuplicateChecksumSet();
    }
    protected void makeChecksumSetByFileName(String fileNameStarts) {
        results.makeDuplicateSetByFileName(fileNameStarts);
    }
    protected void chooseOneAmongDuplicates(String checksum, File fileThatRemains) {
        results.chooseOneAmongDuplicates(checksum, fileThatRemains);
    }
    protected int[] massChooseByParent(String patternToFind) {
        return results.massChooseByParent(patternToFind);
    }
    protected int[] massChooseByRoot(String patternToFind) {
        return results.massChooseByRoot(patternToFind);
    }
    protected void removeSelectionsByChecksum(String checksum) {
        results.unselectByChecksum(checksum);
    }
    protected void removeSelectionsAll() {
        results.unselectAll();
        if (deleterChunk != null) deleterChunk.updateChunk();
    }
    protected Set<File> getFilesThatRemains() {
        return results.getFilesThatRemains();
    }
    protected Set<File> getFilesToDelete() {
        return results.getFilesToDelete();
    }
    protected boolean isFileChosen(File file) {
        return results.isFileChosen(file);
    }
    protected boolean isChoiceMadeOnChecksum(String checksum) {
        return results.isChoiceMadeOnChecksum(checksum);
    }
    protected void ignoreDuplicate(String checksum, File duplicate) {
        results.ignoreDuplicate(checksum, duplicate);
    }
    protected void ignoreDuplicatesFromDirectory(String directoryString) {
        results.ignoreDuplicatesFromDirectory(directoryString);
    }
    protected void ignoreDuplicatesFromRoot(String rootString) {
        results.ignoreDuplicatesFromRoot(rootString);
    }
    protected int[] getStatistics(String checksum) {
        return results.getStatistics(checksum);
    }


    //Setup chunk features
    protected FindDuplicatesTask getAddToResultsTask(Map<String, Set<File>> previousResult) {
        return setupChunk.getAddToResultsTask(previousResult);
    }

    //Deleter chunk features
    protected void removeSelectionsCurrent() {
        deleterChunk.unselectCurrent();
        deleterChunk.updateChunk();
    }
    protected void updateDeleterChunk() {
        if (deleterChunk != null) {
            deleterChunk.updateChunk();
        }
    }
    protected void ignoreSelectedDuplicate() {
        deleterChunk.ignoreSelectedDuplicate();
    }
    protected void ignoreDuplicatesByParent() {
        deleterChunk.ignoreDuplicatesByParent();
    }
    protected void ignoreDuplicatesByRoot() {
        deleterChunk.ignoreDuplicatesByRoot();
    }
    protected void removeFilter() {
        deleterChunk.removeFilter();
    }

    //Settings chunk features
    protected FindTaskSettings getFindTaskSettings() {
        return settingsChunk.getFindTaskSettings();
    }

    //Memory saving
    protected void cleanOldResults() {
        results = null;
        deleterChunk = null;
    }
    protected void terminateAboutChunk() {
        aboutChunk.shutDownAnimation();
        aboutChunk = null;
    }

    //Bindings
    protected BooleanProperty[] getPropertiesToBindToTemplateTF() {
        return menuBarChunk.getPropertiesToBindToTemplateTF();
    }
}
