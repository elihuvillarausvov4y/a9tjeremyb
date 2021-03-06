package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.function.VoidFunction;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.config.WeStatus;
import org.code4everything.wetool.plugin.support.constant.AppConsts;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.handler.BaseKeyboardEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseNoMessageEventHandler;
import org.code4everything.wetool.plugin.support.event.message.KeyboardListenerEventMessage;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.func.FunctionCenter;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pantao
 * @since 2019/7/4
 **/
@Slf4j
@UtilityClass
public class FxUtils {

    private static final int DOUBLE_CLICK = 2;

    private static final Map<String, Menu> MENU_MAP = new ConcurrentHashMap<>(4);

    private static final Set<Integer> PRESSING_KEY_CODE = new ConcurrentHashSet<>();

    private static final AtomicBoolean KEY_EVENT_SUBSCRIBED = new AtomicBoolean(false);

    private static final List<Pair<List<Integer>, Runnable>> SHORTCUT_ACTION = new ArrayList<>();

    private static final List<Pair<List<Integer>, Runnable>> GLOBAL_SHORTCUT_ACTION = new ArrayList<>();

    private static Method searchActionMethod;

    private static Method unregisterActionMethod;

    private static Robot robot;

    static {
        try {
            Class<?> clazz = Class.forName("org.code4everything.wetool.controller.MainController");
            searchActionMethod = ReflectUtil.getMethod(clazz, "registerAction", String.class, EventHandler.class);
            unregisterActionMethod = ReflectUtil.getMethod(clazz, "unregisterAction", String.class);
        } catch (ClassNotFoundException e) {
            log.error(ExceptionUtil.stacktraceToString(e));
        }
    }

    public static Set<Integer> getPressingKeyCodes() {
        return Collections.unmodifiableSet(PRESSING_KEY_CODE);
    }

    /**
     * ??????windows????????????
     */
    public static void multiDesktopOnWindows() {
        if (Objects.isNull(robot)) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                log.error(ExceptionUtil.stacktraceToString(e));
            }
        }

        if (Objects.isNull(robot)) {
            return;
        }

        robot.keyPress(java.awt.event.KeyEvent.VK_WINDOWS);
        robot.keyPress(java.awt.event.KeyEvent.VK_TAB);
        robot.keyRelease(java.awt.event.KeyEvent.VK_WINDOWS);
        robot.keyRelease(java.awt.event.KeyEvent.VK_TAB);
    }

    /**
     * ??????????????????????????????????????????hutool*, env*
     *
     * @param name ??????
     * @param eventHandler ???????????????
     *
     * @since 1.5.0
     */
    public static void registerAction(String name, EventHandler<ActionEvent> eventHandler) {
        if (Objects.isNull(searchActionMethod)) {
            log.error("method not found");
            return;
        }
        ReflectUtil.invokeStatic(searchActionMethod, name, eventHandler);
    }

    /**
     * ?????????????????????
     *
     * @since 1.6.0
     */
    public static void executeAction(String action) {
        FunctionCenter.callFunc("execute-wetool-action", List.of(action));
    }

    /**
     * ??????????????????????????????
     *
     * @param packageName ????????????
     * @param appName ?????????
     *
     * @since 1.6.0
     */
    public static void addLogNameMapping(String packageName, String appName) {
        FunctionCenter.callFunc("add-package-to-app-name-mapping", List.of(packageName, appName));
    }

    /**
     * ??????????????????
     *
     * @param name ??????
     *
     * @since 1.5.0
     */
    public static void unregisterAction(String name) {
        if (Objects.isNull(unregisterActionMethod)) {
            log.error("method not found");
            return;
        }
        ReflectUtil.invokeStatic(unregisterActionMethod, name);
    }

    /**
     * ?????????????????????
     *
     * @since 1.0.2
     */
    public static void clearText(TextInputControl... tics) {
        if (ArrayUtil.isNotEmpty(tics)) {
            for (TextInputControl tic : tics) {
                tic.clear();
            }
        }
    }

    /**
     * ???????????????
     *
     * @since 1.3.0
     */
    public static synchronized void registerShortcuts(List<Integer> shortcutKeyCodes, Runnable runnable) {
        Objects.requireNonNull(runnable);
        checkShortcuts(shortcutKeyCodes);
        SHORTCUT_ACTION.add(new Pair<>(shortcutKeyCodes, runnable));
    }

    /**
     * ?????????????????????
     *
     * @since 1.3.0
     */
    public static synchronized void registerGlobalShortcuts(List<Integer> shortcutKeyCodes, Runnable runnable) {
        if (BooleanUtil.isTrue(WeUtils.getConfig().getDisableKeyboardMouseListener())) {
            log.warn("register global shortcuts failed, because jnative hook is disabled");
            return;
        }

        Objects.requireNonNull(runnable);
        checkShortcuts(shortcutKeyCodes);
        String errMsg = "global cannot register shortcut only use key 'escape'";
        Preconditions.checkArgument(shortcutKeyCodes.size() > 1 || !shortcutKeyCodes.contains(NativeKeyEvent.VC_ESCAPE), errMsg);
        GLOBAL_SHORTCUT_ACTION.add(new Pair<>(shortcutKeyCodes, runnable));
    }

    private static void checkShortcuts(List<Integer> shortcutKeyCodes) {
        Preconditions.checkArgument(CollUtil.isNotEmpty(shortcutKeyCodes), "shortcuts must not be empty");
        shortcutKeyCodes.forEach(Objects::requireNonNull);
        String errMsg = "key 'escape' cannot combined with any other shortcuts";
        Preconditions.checkArgument(!shortcutKeyCodes.contains(NativeKeyEvent.VC_ESCAPE) || shortcutKeyCodes.size() == 1, errMsg);
    }

    /**
     * ????????????
     *
     * @since 1.3.0
     */
    public static void listenKeyEvent() {
        if (KEY_EVENT_SUBSCRIBED.get()) {
            return;
        }
        KEY_EVENT_SUBSCRIBED.set(true);
        EventCenter.onKeyPressed(new BaseKeyboardEventHandler() {
            @Override
            public void handleEvent0(String eventKey, Date eventTime, KeyboardListenerEventMessage eventMessage) {
                int keyCode = eventMessage.getKeyEvent().getKeyCode();
                if (keyCode == NativeKeyEvent.VC_ESCAPE) {
                    PRESSING_KEY_CODE.clear();
                }
                PRESSING_KEY_CODE.add(keyCode);

                GLOBAL_SHORTCUT_ACTION.forEach(FxUtils::handleShortcuts);

                Stage stage = getStage();
                if (stage.isShowing() && stage.isFocused()) {
                    SHORTCUT_ACTION.forEach(FxUtils::handleShortcuts);
                }
            }
        });
        EventCenter.onKeyReleased(new BaseKeyboardEventHandler() {
            @Override
            public void handleEvent0(String eventKey, Date eventTime, KeyboardListenerEventMessage eventMessage) {
                PRESSING_KEY_CODE.remove(eventMessage.getKeyEvent().getKeyCode());
            }
        });

        BaseNoMessageEventHandler eventHandler = new BaseNoMessageEventHandler() {
            @Override
            public void handleEvent0(String eventKey, Date eventTime) {
                PRESSING_KEY_CODE.clear();
            }
        };
        EventCenter.onWetoolShow(eventHandler);
        EventCenter.onWetoolHidden(eventHandler);
    }

    private static void handleShortcuts(Pair<List<Integer>, Runnable> pair) {
        if (pair.getKey().size() == PRESSING_KEY_CODE.size() && CollUtil.containsAll(PRESSING_KEY_CODE, pair.getKey())) {
            // ???????????????
            Platform.runLater(pair.getValue());
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param label ????????????
     *
     * @return ??????
     *
     * @since 1.0.1
     */
    public static Menu makePluginMenu(String label) {
        Menu menu = MENU_MAP.get(label);
        if (Objects.isNull(menu)) {
            menu = new Menu(label);
            getPluginMenu().getItems().add(menu);
            MENU_MAP.put(label, menu);
        }
        return menu;
    }

    /**
     * ??????????????????
     *
     * @since 1.0.1
     */
    public static Menu getPluginMenu() {
        return BeanFactory.get(AppConsts.BeanKey.PLUGIN_MENU);
    }

    /**
     * ????????????
     *
     * @param label ?????????
     * @param handler ???????????????
     *
     * @return ??????
     *
     * @since 1.0.1
     */
    public static MenuItem createMenuItem(String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(handler);
        return item;
    }

    /**
     * ????????????
     *
     * @param label ?????????
     * @param handler ???????????????
     *
     * @return ??????
     *
     * @since 1.1.2
     */
    public static MenuItem createBarMenuItem(String label, EventHandler<ActionEvent> handler) {
        return createMenuItem(label, handler);
    }

    /**
     * ????????????
     *
     * @param label ?????????
     * @param listener ?????????
     *
     * @return ??????
     *
     * @since 1.0.1
     */
    public static java.awt.MenuItem createMenuItem(String label, ActionListener listener) {
        java.awt.MenuItem item = new java.awt.MenuItem(label);
        item.addActionListener(listener);
        return item;
    }

    /**
     * ????????????
     *
     * @param label ?????????
     * @param listener ?????????
     *
     * @return ??????
     *
     * @since 1.1.2
     */
    public static java.awt.MenuItem createTrayMenuItem(String label, ActionListener listener) {
        return createMenuItem(label, listener);
    }

    /**
     * ??????????????????????????????????????????
     */
    public static BaseViewController getSelectedTabController() {
        Tab tab = getTabPane().getSelectionModel().getSelectedItem();
        return Objects.isNull(tab) ? null : BeanFactory.getView(tab.getId() + tab.getText());
    }

    /**
     * ???????????????
     *
     * @param tabContent ????????????
     * @param tabId ?????????tabId????????????????????????????????????
     * @param tabName ?????????tabName?????????????????????
     */
    public static void openTab(Node tabContent, String tabId, String tabName) {
        openTab(tabContent, tabId, tabName, null);
    }

    /**
     * ???????????????
     *
     * @param tabContent ????????????
     * @param tabId ?????????tabId????????????????????????????????????
     * @param tabName ?????????tabName?????????????????????
     * @param callable tab?????????????????????????????????????????????????????????????????????????????????????????????Null
     */
    public static void openTab(Node tabContent, String tabId, String tabName, Callable<Tab> callable) {
        // ????????????
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
                // ??????????????????
                tabPane.getSelectionModel().select(i);
                return;
            }
        }

        if (ObjectUtil.isNotNull(callable)) {
            callable.call(tab);
        }
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    /**
     * ??????????????? {@link TabPane}
     */
    public static TabPane getTabPane() {
        return BeanFactory.get(TabPane.class);
    }

    /**
     * ????????????????????? {@link Stage}
     */
    public static Stage getStage() {
        return BeanFactory.get(Stage.class);
    }

    /**
     * ???????????????
     *
     * @since 1.3.0
     */
    public static void showStage() {
        Platform.runLater(() -> getStage().show());
    }

    /**
     * ????????????????????????
     *
     * @since 1.3.0
     */
    public static void toggleStage() {
        if (getStage().isShowing()) {
            hideStage();
        } else {
            showStage();
        }
    }

    /**
     * ???????????????
     *
     * @since 1.3.0
     */
    public static void hideStage() {
        Platform.runLater(() -> {
            if (BooleanUtil.isTrue(BeanFactory.get("isTraySuccess"))) {
                FxUtils.getStage().hide();
            } else {
                getStage().setIconified(true);
            }
        });
    }

    /**
     * ??????????????????????????????
     *
     * @param callable ?????????????????????????????????Callable??????
     */
    public static void saveFile(Callable<File> callable) {
        File file = getFileChooser().showSaveDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param callable ?????????????????????????????????Callable??????
     */
    public static void chooseFiles(Callable<List<File>> callable) {
        List<File> files = getFileChooser().showOpenMultipleDialog(getStage());
        handleFileListCallable(files, callable);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param callable ?????????????????????????????????Callable??????
     */
    public static void chooseFile(Callable<File> callable) {
        File file = getFileChooser().showOpenDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * ?????????????????????????????????
     *
     * @param callable ?????????????????????????????????Callable??????
     */
    public static void chooseFolder(Callable<File> callable) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        File file = chooser.showDialog(getStage());
        handleFileCallable(file, callable);
    }

    /**
     * ????????????????????????????????????
     */
    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_LINK_ERROR, e);
        }
    }

    /**
     * ?????????????????????????????????
     */
    public static void openFile(String file) {
        openFile(FileUtil.file(file));
    }

    /**
     * ?????????????????????????????????
     */
    public static void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            FxDialogs.showException(AppConsts.Tip.OPEN_FILE_ERROR, e);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param control ???????????????
     * @param event ????????????
     */
    public static void dropFileContent(TextInputControl control, DragEvent event) {
        dropFiles(event, files -> control.setText(FileUtil.readUtf8String(files.get(0))));
    }

    /**
     * ????????????????????????????????????
     *
     * @param event ????????????
     * @param eventCallableMap ?????????????????????Object?????????????????????Callable????????????????????????
     */
    public static void dropFiles(DragEvent event, Map<Object, Callable<List<File>>> eventCallableMap) {
        handleFileListCallable(event.getDragboard().getFiles(), eventCallableMap.get(event.getSource()));
    }

    /**
     * ?????????????????????????????????
     *
     * @param event ????????????
     * @param callable Callable?????????????????????????????????
     */
    public static void dropFiles(DragEvent event, Callable<List<File>> callable) {
        handleFileListCallable(event.getDragboard().getFiles(), callable);
    }

    /**
     * ??????????????????
     */
    public static void acceptCopyMode(DragEvent event) {
        event.acceptTransferModes(TransferMode.COPY);
    }

    /**
     * ??????????????????????????????
     */
    public static void enterDo(KeyEvent event, VoidFunction function) {
        if (event.getCode() == KeyCode.ENTER) {
            function.call();
        }
    }

    /**
     * ??????????????????????????????
     */
    public static void doubleClicked(MouseEvent event, VoidFunction function) {
        if (event.getClickCount() == DOUBLE_CLICK) {
            function.call();
        }
    }

    /**
     * ????????????
     *
     * @param url ?????????classpath????????????????????????url????????????
     *
     * @since 1.0.2
     */
    public static Pane loadFxml(String url) {
        return loadFxml(url, true);
    }

    /**
     * ????????????
     *
     * @param url ?????????classpath????????????????????????url????????????
     * @param cache ????????????
     *
     * @since 1.0.2
     */
    public static Pane loadFxml(String url, boolean cache) {
        return loadFxml(FxUtils.class, url, cache);
    }

    /**
     * ?????????????????????java9?????????????????????
     *
     * @param url ?????????classpath????????????????????????url????????????
     * @param cache ????????????
     *
     * @since 1.1.0
     */
    public static synchronized Pane loadFxml(Class<?> cls, String url, boolean cache) {
        Pane pane = BeanFactory.get(url);
        if (ObjectUtil.isNull(pane)) {
            URL realUrl = cls.getResource(url);
            try {
                Thread.currentThread().setContextClassLoader(cls.getClassLoader());
                FXMLLoader.setDefaultClassLoader(cls.getClassLoader());
                pane = FXMLLoader.load(realUrl);
                if (cache) {
                    BeanFactory.register(url, pane);
                }
                return pane;
            } catch (Exception e) {
                FxDialogs.showException(AppConsts.Tip.FXML_ERROR, e);
                return null;
            }
        }
        return pane;
    }

    /**
     * ??????????????????
     */
    public static void restart() {
        restart(null);
    }

    /**
     * ??????????????????
     *
     * @since 1.3.0
     */
    public static void restart(String jarName) {
        BeanFactory.get(WeStatus.class).setState(WeStatus.State.TERMINATING);
        EventCenter.publishEvent(EventCenter.EVENT_WETOOL_RESTART, DateUtil.date());
        String batchFile = WeUtils.getConfig().getRestartBatch();
        if (StrUtil.isEmpty(batchFile)) {
            if (StrUtil.isEmpty(jarName)) {
                jarName = getWetoolJarName();
            }
            restartHelper("java -jar ./" + jarName);
        } else {
            restartHelper(FileUtils.currentWorkDir(batchFile));
        }
        WeUtils.exitSystem();
    }

    /**
     * ?????????????????????jar??????
     *
     * @since 1.3.0
     */
    public static String getWetoolJarName() {
        // ??????????????????????????????
        String jarPath = System.getProperty("java.class.path");
        // ????????????????????????
        int idx = Math.max(jarPath.lastIndexOf('/'), jarPath.lastIndexOf('\\')) + 1;
        return jarPath.substring(idx);
    }

    private static void restartHelper(String cmd) {
        log.info("restart use cmd: " + cmd);
        WeUtils.execute(() -> RuntimeUtil.execForStr(cmd));
    }

    public static FileChooser getFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppConsts.Title.APP_TITLE);
        chooser.setInitialDirectory(new File(WeUtils.getConfig().getFileChooserInitDir()));
        return chooser;
    }

    public static void handleFileListCallable(List<File> files, Callable<List<File>> callable) {
        if (CollUtil.isEmpty(files)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(files.get(0).getParent());

        if (Objects.isNull(callable)) {
            return;
        }
        callable.call(files);
    }

    public static void handleFileCallable(File file, Callable<File> callable) {
        if (Objects.isNull(file)) {
            return;
        }
        WeUtils.getConfig().setFileChooserInitDir(file.getParent());

        if (Objects.isNull(callable)) {
            return;
        }
        callable.call(file);
    }
}
