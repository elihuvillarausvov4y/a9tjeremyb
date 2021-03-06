package org.code4everything.wetool.plugin.everywhere.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.StrUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.code4everything.wetool.plugin.everywhere.config.EverywhereConfiguration;
import org.code4everything.wetool.plugin.everywhere.constant.CommonConsts;
import org.code4everything.wetool.plugin.everywhere.model.FileInfo;
import org.code4everything.wetool.plugin.everywhere.util.LuceneUtils;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/26
 */
public class MainController implements BaseViewController {

    @FXML
    public CheckBox folderCheck;

    @FXML
    public CheckBox fileCheck;

    @FXML
    public CheckBox contentCheck;

    @FXML
    public TextField searchText;

    @FXML
    public TableView<FileInfo> fileTable;

    @FXML
    public TableColumn<FileInfo, String> nameColumn;

    @FXML
    public TableColumn<FileInfo, String> pathColumn;

    @FXML
    public TableColumn<FileInfo, String> sizeColumn;

    @FXML
    public TableColumn<FileInfo, String> timeColumn;

    @FXML
    public TextField filterText;

    @FXML
    private void initialize() {
        BeanFactory.registerView(CommonConsts.APP_ID, CommonConsts.APP_NAME, this);
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        LuceneUtils.setSearchNotification(list -> {
            if (CollUtil.isEmpty(list)) {
                FxDialogs.showInformation("??????????????????????????????", null);
            } else {
                fileTable.getItems().addAll(list);
            }
        });

        // ??????????????????????????????
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("modified"));
    }

    public void openConfigFile() {
        String path = EverywhereConfiguration.getPath();
        if (!FileUtil.exist(path)) {
            final EverywhereConfiguration conf = new EverywhereConfiguration();
            conf.setExcludePatterns(Collections.emptySet());
            conf.setIgnoreHiddenFile(true);
            conf.setIncludePatterns(Collections.emptySet());
            conf.setSizeLimit("10,000,000");
            conf.setIndexContent(false);
            conf.setIndexExpire(24 * 60L);
            FileUtil.writeUtf8String(conf.toJsonString(true), path);
        }
        FxUtils.openFile(path);
    }

    public void reloadConfig() {
        // ??????????????????
        EverywhereConfiguration.loadConfiguration();
        LuceneUtils.indexAsync(true);
    }

    public void findEverywhere() {
        final String word = searchText.getText();
        if (StrUtil.isEmpty(word)) {
            return;
        }

        boolean folder = folderCheck.isSelected();
        boolean file = fileCheck.isSelected();
        boolean content = contentCheck.isSelected();

        if (!folder && !file && !content) {
            return;
        }

        fileTable.getItems().clear();

        Pattern filterPattern = null;
        if (StrUtil.isNotBlank(filterText.getText())) {
            try {
                filterPattern = Pattern.compile(filterText.getText());
            } catch (Exception e) {
                // ignore
            }
        }
        LuceneUtils.searchAsync(word, folder, file, content, filterPattern);
    }

    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            findEverywhere();
        }
    }

    public void openFile() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(f -> FxUtils.openFile(f.getPath()));
    }

    public void openFolder() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(f -> FxUtils.openFile(WeUtils.parseFolder(FileUtil.file(f.getPath()))));
    }

    public void deleteFile() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<FileInfo> deleted = new ArrayList<>(list.size());
        list.forEach(f -> {
            if (FileUtil.del(f.getPath())) {
                deleted.add(f);
            }
        });
        fileTable.getItems().removeAll(deleted);
    }

    public void copyFilePath() {
        final ObservableList<FileInfo> list = fileTable.getSelectionModel().getSelectedItems();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        ClipboardUtil.setStr(list.get(0).getPath());
    }

    public void tableClicked(MouseEvent event) {
        FxUtils.doubleClicked(event, this::openFile);
    }
}
