package com.unduplicator.gui;

import com.unduplicator.ResourcesProvider;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>Created by MontolioV on 11.09.17.
 */
public class MenuBarChunk extends AbstractGUIChunk {
    private ResourcesProvider resProvider = ResourcesProvider.getInstance();
    private ChunkManager chunkManager;
    private Stage mainStage;

    private Menu fileMenu = new Menu();
    private MenuItem saveMI = new MenuItem();
    private MenuItem loadMI = new MenuItem();
    private MenuItem exitMI = new MenuItem();

    private Menu editMenu = new Menu();
    private Menu deletionEditMenu = new Menu();
    private MenuItem unselectCurrentMI = new MenuItem();
    private MenuItem unselectAllMI = new MenuItem();

    private Menu panelsMenu = new Menu();
    private MenuItem setupPanelMI = new MenuItem();
    private MenuItem runtimePanelMI = new MenuItem();
    private MenuItem deletionPanelMI = new MenuItem();

    private Menu languageMenu = new Menu();
    private Map<Locale, MenuItem> languageMIMap = new HashMap<>();

    private Menu helpMenu = new Menu();


    public MenuBarChunk(ChunkManager chunkManager, Stage mainStage) {
        this.chunkManager = chunkManager;
        this.mainStage = mainStage;
        setSelfNode(makeMenuBar());
        updateLocaleContent();
    }

    /**
     * Updates gui local dependent elements to current local settings.
     */
    @Override
    public void updateLocaleContent() {
        fileMenu.setText(resProvider.getStrFromGUIBundle("fileMenu"));
        saveMI.setText(resProvider.getStrFromGUIBundle("saveMI"));
        loadMI.setText(resProvider.getStrFromGUIBundle("loadMI"));
        exitMI.setText(resProvider.getStrFromGUIBundle("exitMI"));

        editMenu.setText(resProvider.getStrFromGUIBundle("editMenu"));
        deletionEditMenu.setText(resProvider.getStrFromGUIBundle("deletionEditMenu"));
        unselectCurrentMI.setText(resProvider.getStrFromGUIBundle("unselectCurrentMI"));
        unselectAllMI.setText(resProvider.getStrFromGUIBundle("unselectAllMI"));

        panelsMenu.setText(resProvider.getStrFromGUIBundle("panelsMenu"));
        setupPanelMI.setText((resProvider.getStrFromGUIBundle("setupNode")));
        runtimePanelMI.setText(resProvider.getStrFromGUIBundle("runtimeNode"));
        deletionPanelMI.setText(resProvider.getStrFromGUIBundle("deletionNode"));

        languageMenu.setText(resProvider.getStrFromGUIBundle("languageMenu"));
        for (Locale locale : resProvider.getSupportedLocales()) {
            MenuItem tmpMI = languageMIMap.get(locale);
            tmpMI.setText(locale.getDisplayLanguage(resProvider.getCurrentLocal()));
        }

        helpMenu.setText(resProvider.getStrFromGUIBundle("helpMenu"));
    }

    @Override
    public boolean changeState(GuiStates newState) {
        if (super.changeState(newState)) {
            switch (newState) {
                case NO_RESULTS:
                    saveMI.setDisable(true);
                    deletionPanelMI.setDisable(true);
                    deletionEditMenu.setDisable(true);
                    break;
                case RUNNING:
                    saveMI.setDisable(true);
                    deletionPanelMI.setDisable(true);
                    deletionEditMenu.setDisable(true);
                    break;
                case HAS_RESULTS:
                    saveMI.setDisable(false);
                    deletionPanelMI.setDisable(false);
                    deletionEditMenu.setDisable(false);
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    private MenuBar makeMenuBar() {
        MenuBar result = new MenuBar();

        result.getMenus().add(makeFileMenu());
        result.getMenus().add(makeEditMenu());
        result.getMenus().add(makePanelsMenu());
        result.getMenus().add(makeLanguageMenu());
        result.getMenus().add(makeHelpMenu());

        return result;
    }

    private Menu makeFileMenu() {
        Supplier<FileChooser> fileChooserSupplier = () -> {
            FileChooser fileChooser = new FileChooser();
            ExtensionFilter filter = new ExtensionFilter(resProvider.getStrFromGUIBundle("fileFilter"),
                                                         "*.ser");
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setSelectedExtensionFilter(filter);
            fileChooser.setInitialFileName("*.ser");
            return fileChooser;
        };

        saveMI.setOnAction(event -> {
            File file = fileChooserSupplier.get().showSaveDialog(mainStage);
            if (file != null) {
                chunkManager.saveResults(file);
            }
        });
        loadMI.setOnAction(event -> {
            File file = fileChooserSupplier.get().showOpenDialog(mainStage);
            if (file != null) {
                chunkManager.loadResults(file);
            }
        });
        exitMI.setOnAction(event -> Platform.exit());

        fileMenu.getItems().addAll(
                saveMI,
                loadMI,
                new SeparatorMenuItem(),
                exitMI);

        return fileMenu;
    }

    private Menu makeEditMenu() {
        unselectCurrentMI.setOnAction(event -> chunkManager.removeSelectionsCurrent());
        unselectAllMI.setOnAction(event -> chunkManager.removeSelectionsAll());

        deletionEditMenu.getItems().addAll(unselectCurrentMI, unselectAllMI);
        editMenu.getItems().addAll(deletionEditMenu);
        return editMenu;
    }

    private Menu makePanelsMenu() {
        setupPanelMI.setOnAction(event -> chunkManager.showSetupNode());
        runtimePanelMI.setOnAction(event -> chunkManager.showRuntimeStatusNode());
        deletionPanelMI.setOnAction(event -> chunkManager.showDeletionNode());

        panelsMenu.getItems().addAll(setupPanelMI, runtimePanelMI, deletionPanelMI);
        return panelsMenu;
    }

    private Menu makeLanguageMenu() {
        for (Locale locale : resProvider.getSupportedLocales()) {
            MenuItem localeMI = new MenuItem();
            localeMI.setOnAction(event -> {
                resProvider.setBundlesLocale(locale);
                chunkManager.updateChunksLocales();
            });
            languageMIMap.put(locale, localeMI);
            languageMenu.getItems().add(localeMI);
        }
        return languageMenu;
    }

    private Menu makeHelpMenu() {

        return helpMenu;
    }

}