package io.sourceforge.uniqueoid.gui;

import io.sourceforge.uniqueoid.GlobalFiles;
import io.sourceforge.uniqueoid.ResourcesProvider;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
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
    private MenuItem settingsMI = new MenuItem();
    private MenuItem exitMI = new MenuItem();

    private Menu editMenu = new Menu();
    private MenuItem removeFilterMI = new MenuItem();
    private Menu deletionEditMenu = new Menu();
    private MenuItem unselectCurrentMI = new MenuItem();
    private MenuItem unselectAllMI = new MenuItem();
    private Menu resultsEditMenu = new Menu();
    private MenuItem removeFileFromResultMI = new MenuItem();
    private MenuItem removeDirectoryFromResultMI = new MenuItem();
    private MenuItem removeRootFromResultMI = new MenuItem();

    private Menu panelsMenu = new Menu();
    private MenuItem setupPanelMI = new MenuItem();
    private MenuItem runtimePanelMI = new MenuItem();
    private MenuItem deletionPanelMI = new MenuItem();

    private Menu languageMenu = new Menu();
    private Map<Locale, MenuItem> languageMIMap = new HashMap<>();

    private Menu helpMenu = new Menu();
    private MenuItem licenseMI = new MenuItem();
    private MenuItem getStartedMI = new MenuItem();
    private MenuItem aboutMI = new MenuItem();

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
        settingsMI.setText(resProvider.getStrFromGUIBundle("settingsMI"));
        exitMI.setText(resProvider.getStrFromGUIBundle("exitMI"));

        editMenu.setText(resProvider.getStrFromGUIBundle("editMenu"));
        deletionEditMenu.setText(resProvider.getStrFromGUIBundle("deletionEditMenu"));
        unselectCurrentMI.setText(resProvider.getStrFromGUIBundle("unselectCurrentMI"));
        unselectAllMI.setText(resProvider.getStrFromGUIBundle("unselectAllMI"));
        resultsEditMenu.setText(resProvider.getStrFromGUIBundle("resultsEditMenu"));
        removeFileFromResultMI.setText(resProvider.getStrFromGUIBundle("removeFileFromResultMI"));
        removeDirectoryFromResultMI.setText(resProvider.getStrFromGUIBundle("removeDirectoryFromResultMI"));
        removeRootFromResultMI.setText(resProvider.getStrFromGUIBundle("removeRootFromResultMI"));
        removeFilterMI.setText(resProvider.getStrFromGUIBundle("removeFilterMI"));

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
        licenseMI.setText(resProvider.getStrFromGUIBundle("licenseMI"));
        getStartedMI.setText(resProvider.getStrFromGUIBundle("getStartedMI"));
        aboutMI.setText(resProvider.getStrFromGUIBundle("aboutMI"));
    }

    @Override
    public boolean changeState(GuiStates newState) {
        if (super.changeState(newState)) {
            switch (newState) {
                case NO_RESULTS:
                    setupPanelMI.setDisable(false);
                    saveMI.setDisable(true);
                    deletionPanelMI.setDisable(true);
                    editMenu.setDisable(true);
                    break;
                case RUNNING:
                    setupPanelMI.setDisable(true);
                    saveMI.setDisable(true);
                    deletionPanelMI.setDisable(true);
                    editMenu.setDisable(true);
                    break;
                case HAS_RESULTS:
                    setupPanelMI.setDisable(false);
                    saveMI.setDisable(false);
                    deletionPanelMI.setDisable(false);
                    editMenu.setDisable(false);
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
            fileChooser.setInitialDirectory(GlobalFiles.getInstance().getAppDir());
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
        settingsMI.setOnAction(event -> chunkManager.showSettingsNode());
        exitMI.setOnAction(event -> Platform.exit());

        saveMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        loadMI.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        settingsMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

        fileMenu.getItems().addAll(
                saveMI,
                loadMI,
                new SeparatorMenuItem(),
                settingsMI,
                new SeparatorMenuItem(),
                exitMI);

        return fileMenu;
    }

    private Menu makeEditMenu() {
        unselectCurrentMI.setOnAction(event -> chunkManager.removeSelectionsCurrent());
        unselectCurrentMI.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN));
        unselectAllMI.setOnAction(event -> chunkManager.removeSelectionsAll());
        unselectAllMI.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN));
        deletionEditMenu.getItems().addAll(
                unselectCurrentMI,
                unselectAllMI);

        removeFileFromResultMI.setOnAction(event -> chunkManager.ignoreSelectedDuplicate());
        removeFileFromResultMI.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        removeDirectoryFromResultMI.setOnAction(event -> chunkManager.ignoreDuplicatesByParent());
        removeDirectoryFromResultMI.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
        removeRootFromResultMI.setOnAction(event -> chunkManager.ignoreDuplicatesByRoot());
        removeRootFromResultMI.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        resultsEditMenu.getItems().addAll(
                removeFileFromResultMI,
                removeDirectoryFromResultMI,
                removeRootFromResultMI);

        removeFilterMI.setOnAction(event -> chunkManager.removeFilter());
        removeFilterMI.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));

        editMenu.getItems().addAll(
                deletionEditMenu,
                resultsEditMenu,
                removeFilterMI);
        return editMenu;
    }

    private Menu makePanelsMenu() {
        setupPanelMI.setOnAction(event -> chunkManager.showSetupNode());
        runtimePanelMI.setOnAction(event -> chunkManager.showRuntimeStatusNode());
        deletionPanelMI.setOnAction(event -> chunkManager.showDeletionNode());

        setupPanelMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN));
        runtimePanelMI.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN));
        deletionPanelMI.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN));

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
        licenseMI.setOnAction(event -> {
            try (BufferedReader br = new BufferedReader(
                                     new InputStreamReader(
                                     getClass().getClassLoader().getResourceAsStream("LICENSE"))))
            {
                StringJoiner sj = new StringJoiner("\n");
                String tmpS;
                while ((tmpS = br.readLine()) != null) {
                    sj.add(tmpS);
                }

                TextArea ta = new TextArea();
                ta.setWrapText(true);
                ta.setEditable(false);
                ta.setPrefHeight(500);
                ta.setText(sj.toString());
                chunkManager.showInNewStage(ta);
            } catch (IOException e) {
                chunkManager.showException(e);
            }
        });
        getStartedMI.setOnAction(event -> {
            TextArea ta = new TextArea(resProvider.getStrFromMessagesBundle("getStarted"));
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefHeight(500);
            chunkManager.showInNewStage(ta);
        });
        aboutMI.setOnAction(event -> {
            chunkManager.makeAboutChunk();
        });

        helpMenu.getItems().addAll(
                getStartedMI,
                licenseMI,
                aboutMI);
        return helpMenu;
    }

    protected BooleanProperty[] getPropertiesToBindToTemplateTF() {
        return new BooleanProperty[]{removeDirectoryFromResultMI.disableProperty(),
                                     removeRootFromResultMI.disableProperty()};
    }
}
