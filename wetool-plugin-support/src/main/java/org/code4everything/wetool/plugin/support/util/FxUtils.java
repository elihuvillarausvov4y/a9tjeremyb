package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.function.VoidFunction;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author pantao
 * @since 2019/7/4
 **/
@Slf4j
@UtilityClass
public class FxUtils {

    private static final int DOUBLE_CLICK = 2;

    /**
     * 获取选中的选项卡的视图控制器
     */
    public static BaseViewController getSelectedTabController() {
        Tab tab = getTabPane().getSelectionModel().getSelectedItem();
        return Objects.isNull(tab) ? null : BeanFactory.getView(tab.getId() + tab.getText());
    }

    /**
     * 插件请调用下面的 {@link #openTab(Node, String, String)} 方法，而不是调用此方法
     */
    public static void openTab(Node tabContent, String tabName) {
        openTab(tabContent, AppConsts.Title.APP_TITLE, tabName);
    }

    /**
     * 打开选项卡
     *
     * @param tabContent 视图内容
     * @param tabId 自定义tabId，防止与其他插件名称冲突
     * @param tabName 自定义tabName，即选项卡标题
     */
    public static void openTab(Node tabContent, String tabId, String tabName) {
        // 校验参数
        Preconditions.checkNotNull(tabContent, "tab content node must not null");
        Preconditions.checkArgument(StrUtil.isNotEmpty(tabId), "tab id must not empty, please set a custom unique id");
        Preconditions.checkArgument(StrUtil.isNotEmpty(tabName), "tab name must not be empty");

        Tab tab = new Tab(tabName, tabContent);
        tab.setId(tabId);
        tab.setClosable(true);

        TabPane tabPane = getTabPane();
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab t = tabPane.getTabs().get(i);
            if (Objects.equals(t.getId(), tab.getId()) && Objects.equals(t.getText(), tab.getText())) {
                // 选项卡已打开
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    /**
     * 获取当前的 {@link TabPane}
     */
    public static TabPane getTabPane() {
        return BeanFactory.get(TabPane.class);
    }

    /**
     * 获取当前运行的 {@link Stage}
     */
    public static Stage getStage() {
        return BeanFactory.get(Stage.class);
    }

    /**
     * 弹出保存文件的对话框
     *
     * @param callable 用户选择文件后，会调用Callable接口
     */
    public static void saveFile(Callable<File> callable) {
        File file = getFileChooser().showSaveDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * 弹出选择文件的对话框（多选）
     *
     * @param callable 用户选择文件后，会调用Callable接口
     */
    public static void chooseFiles(Callable<List<File>> callable) {
        List<File> files = getFileChooser().showOpenMultipleDialog(getStage());
        handleFileListCallable(files, callable);
    }

    /**
     * 弹出选择文件的对话框（单选）
     *
     * @param callable 用户选择文件后，会调用Callable接口
     */
    public static void chooseFile(Callable<File> callable) {
        File file = getFileChooser().showOpenDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * 弹出选择文件夹的对话框
     *
     * @param callable 用户选择文件后，会调用Callable接口
     */
    public static void chooseFolder(Callable<File> callable) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        File file = chooser.showDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * 用系统默认浏览器打开链接
     */
    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_LINK_ERROR, e);
        }
    }

    /**
     * 用系统默认软件打开文件
     */
    public static void openFile(String file) {
        openFile(FileUtil.file(file));
    }

    /**
     * 用系统默认软件打开文件
     */
    public static void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_FILE_ERROR, e);
        }
    }

    /**
     * 重启本工具库
     */
    public static void restart() {
        // 获取当前程序运行路径
        final String jarPath = System.getProperty("java.class.path");
        // 文件名的截取索引
        final int idx = Math.max(jarPath.lastIndexOf('/'), jarPath.lastIndexOf('\\')) + 1;
        ThreadUtil.execute(() -> RuntimeUtil.execForStr("java -jar ./" + jarPath.substring(idx)));
        WeUtils.exitSystem();
    }

    /**
     * 将用户拖曳的文件的内容赋值给文本输入框
     *
     * @param control 文本输入框
     * @param event 拖曳事件
     */
    public static void dropFileContent(TextInputControl control, DragEvent event) {
        dropFiles(event, files -> control.setText(FileUtil.readUtf8String(files.get(0))));
    }

    /**
     * 设置拖曳事件的批处理功能
     *
     * @param event 拖曳事件
     * @param eventCallableMap 事件处理集合，Object为事件源对象，Callable为用户拖曳的回调
     */
    public static void dropFiles(DragEvent event, Map<Object, Callable<List<File>>> eventCallableMap) {
        handleFileListCallable(event.getDragboard().getFiles(), eventCallableMap.get(event.getSource()));
    }

    /**
     * 设置拖曳事件的处理功能
     *
     * @param event 拖曳事件
     * @param callable Callable为用户拖曳文件后的回调
     */
    public static void dropFiles(DragEvent event, Callable<List<File>> callable) {
        handleFileListCallable(event.getDragboard().getFiles(), callable);
    }

    /**
     * 设置拖曳模式
     */
    public static void acceptCopyMode(DragEvent event) {
        event.acceptTransferModes(TransferMode.COPY);
    }

    /**
     * 键盘事件为回车时调用
     */
    public static void enterDo(KeyEvent event, VoidFunction function) {
        if (event.getCode() == KeyCode.ENTER) {
            function.call();
        }
    }

    /**
     * 鼠标事件为双击时调用
     */
    public static void doubleClicked(MouseEvent event, VoidFunction function) {
        if (event.getClickCount() == DOUBLE_CLICK) {
            function.call();
        }
    }

    /**
     * 插件请调用下面的 {@link #loadFxml(WePluginSupportable, String)} 方法，而不是此方法
     */
    public static Pane loadFxml(String url) {
        return loadFxml(FxUtils.class.getResource(url), FxUtils.class.getClassLoader());
    }

    /**
     * 加载视图
     *
     * @param supportable 实现了 {@link WePluginSupportable} 的类
     * @param url 视图在classpath中路径
     *
     * @since 1.0.0
     */
    public static Pane loadFxml(WePluginSupportable supportable, String url) {
        Class clazz = supportable.getClass();
        return loadFxml(clazz.getResource(url), clazz.getClassLoader());
    }

    private static Pane loadFxml(URL url, ClassLoader loader) {
        String nodeKey = url.toString();
        if (BeanFactory.isRegistered(nodeKey)) {
            // 从缓存中取出视图
            return BeanFactory.get(nodeKey);
        }
        FXMLLoader.setDefaultClassLoader(loader);
        try {
            return FXMLLoader.load(url);
        } catch (Exception e) {
            FxDialogs.showException(AppConsts.Tip.FXML_ERROR, e);
            return null;
        }
    }

    private static FileChooser getFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        return chooser;
    }

    private static void handleFileListCallable(List<File> files, Callable<List<File>> callable) {
        if (CollUtil.isEmpty(files) || Objects.isNull(callable)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(files.get(0).getParent());
        callable.call(files);
    }

    private static void handleFileCallable(File file, Callable<File> callable) {
        if (Objects.isNull(file) || Objects.isNull(callable)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(file.getParent());
        callable.call(file);
    }
}
