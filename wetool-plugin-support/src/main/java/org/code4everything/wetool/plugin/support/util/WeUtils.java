package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.boot.base.constant.IntegerConsts;
import org.code4everything.boot.config.BootConfig;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.exception.ToDialogException;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pantao
 * @since 2018/3/31
 */
@Slf4j
@UtilityClass
public class WeUtils {

    private static final String TIME_VARIABLE = "%(TIME|time)%";

    private static final String DATE_VARIABLE = "%(DATE|date)%";

    private static final String PREFIX = "wetool-pool-";

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final Thread.UncaughtExceptionHandler exceptionHandler = (t, e) -> {
            ToDialogException dialogCaused = getDialogCaused(e);
            if (Objects.isNull(dialogCaused)) {
                log.error(ExceptionUtil.stacktraceToString(e), Integer.MAX_VALUE);
            } else {
                FxDialogs.showDialog(dialogCaused);
            }
        };

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, PREFIX + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        }
    };

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(16, 32, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(512), THREAD_FACTORY);

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return THREAD_POOL_EXECUTOR;
    }

    private static int compressLen = 0;

    public static ToDialogException getDialogCaused(Throwable ex) {
        return getDialogCaused(ex, 0);
    }

    private static ToDialogException getDialogCaused(Throwable ex, int recursionCount) {
        if (recursionCount > IntegerConsts.SIXTEEN) {
            // ??????????????????
            return null;
        }
        if (Objects.isNull(ex)) {
            return null;
        }
        if (ex instanceof ToDialogException) {
            return (ToDialogException) ex;
        }
        return getDialogCaused(ex.getCause(), recursionCount + 1);
    }

    /**
     * ??????????????????id
     *
     * @since 1.6.0
     */
    public static int getCurrentPid() {
        return NumberUtil.parseInt(StrUtil.split(ManagementFactory.getRuntimeMXBean().getName(), "@")[0]);
    }

    /**
     * ????????????
     *
     * @since 1.3.0
     */
    public static void execute(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /**
     * ????????????
     *
     * @since 1.3.0
     */
    public static <V> Future<V> executeAsync(Callable<V> callable) {
        return THREAD_POOL_EXECUTOR.submit(callable);
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     *
     * @since 1.2.0
     */
    public static File getPluginFolder() {
        String pluginDir = FileUtils.currentWorkDir("plugins");
        return FileUtil.mkdir(pluginDir);
    }

    /**
     * ??????????????????
     *
     * @param filename ???????????????
     *
     * @return ????????????
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String filename) {
        return parsePathByOs(FileUtils.currentWorkDir(), filename);
    }

    /**
     * ??????????????????
     *
     * @param parentDir ????????????
     * @param filename ???????????????
     *
     * @return ????????????
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String parentDir, String filename) {
        int idx = filename.lastIndexOf('.');
        String name = filename.substring(0, idx);
        String ext = filename.substring(idx);
        return parsePathByOs(parentDir, name + "-win" + ext, name + "-mac" + ext, name + "-lin" + ext, filename);
    }

    /**
     * ??????????????????
     *
     * @param parentDir ????????????
     * @param winFile Windows??????
     * @param macFile Mac??????
     * @param linFile Linux??????
     * @param defaultFile ????????????
     *
     * @return ????????????
     *
     * @since 1.0.1
     */
    public static String parsePathByOs(String parentDir, String winFile, String macFile, String linFile, String defaultFile) {
        OsInfo osInfo = SystemUtil.getOsInfo();
        parentDir = StrUtil.addSuffixIfNot(parentDir, File.separator);
        // Windows????????????
        String winPath = parentDir + winFile;
        // Mac????????????
        String macPath = parentDir + macFile;
        // Linux????????????
        String linPath = parentDir + linFile;
        // ??????????????????
        String defPath = parentDir + defaultFile;

        // ?????????????????????????????????
        String path = null;
        if (osInfo.isWindows() && FileUtil.exist(winPath)) {
            path = winPath;
        } else if (osInfo.isMac() && FileUtil.exist(macPath)) {
            path = macPath;
        } else if (osInfo.isLinux() && FileUtil.exist(linPath)) {
            path = linPath;
        } else if (FileUtil.exist(defPath)) {
            path = defPath;
        }
        return path;
    }

    /**
     * ?????????????????????
     */
    public static WeConfig getConfig() {
        return BeanFactory.get(WeConfig.class);
    }

    /**
     * ????????????????????????
     *
     * @param currVer ????????????
     * @param reqVer ?????????????????????
     */
    public static boolean isRequiredVersion(String currVer, String reqVer) {
        String[] currArr = currVer.split("\\.");
        String[] reqArr = reqVer.split("\\.");
        int len = Math.max(currArr.length, reqArr.length);
        for (int i = 0; i < len; i++) {
            int curr = i < currArr.length ? Integer.parseInt(currArr[i]) : 0;
            int req = i < reqArr.length ? Integer.parseInt(reqArr[i]) : 0;
            if (curr > req) {
                return true;
            }
            if (curr < req) {
                return false;
            }
        }
        return true;
    }

    /**
     * ???????????????
     */
    public static String compressString(String string) {
        string = string.trim();
        if (string.length() > getCompressLen()) {
            string = string.substring(0, getCompressLen()) + "......";
        }
        return string.replaceAll("(\\s{2,}|\r\n|\r|\n)", " ");
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param src ???????????????
     * @param adds ??????????????????
     */
    public static void addFiles(List<File> src, List<File> adds) {
        if (CollUtil.isEmpty(adds)) {
            return;
        }
        WeConfig config = getConfig();
        for (File file : adds) {
            if (!config.getFilterPattern().matcher(file.getName()).matches()) {
                // ???????????????
                log.info("filter file: {}", file.getAbsolutePath());
                continue;
            }
            if (file.isFile() && !src.contains(file)) {
                src.add(file);
            } else if (file.isDirectory()) {
                addFiles(src, CollUtil.newArrayList(file.listFiles()));
            }
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    public static String parseFolder(File file) {
        return file.isDirectory() ? file.getAbsolutePath() : file.getParent();
    }

    /**
     * ????????????%(TIME|time)%???"%(DATE|date)%"
     *
     * @param str ????????????????????????
     */
    public static String replaceVariable(String str) {
        str = StrUtil.nullToEmpty(str);
        if (StrUtil.isNotEmpty(str)) {
            Date date = new Date();
            str = str.replaceAll(DATE_VARIABLE, DateUtil.formatDate(date));
            str = str.replaceAll(TIME_VARIABLE, DateUtil.formatTime(date));
        }
        return str;
    }

    /**
     * ??????????????????
     *
     * @param num ?????????
     * @param minVal ?????????
     */
    public static int parseInt(String num, int minVal) {
        int n = 0;
        if (NumberUtil.isNumber(num)) {
            n = NumberUtil.parseInt(num);
        }
        return Math.max(n, minVal);
    }

    /**
     * ????????????
     */
    public static void exitSystem() {
        System.exit(IntegerConsts.ZERO);
    }

    /**
     * ????????????
     */
    public static void printDebug(String msg, Object... objects) {
        if (BootConfig.isDebug()) {
            String message = StrUtil.format(msg, objects);
            Console.log(message);
            log.warn(message);
        }
    }

    private static int getCompressLen() {
        if (compressLen < 1) {
            Integer val = getConfig().getLogCompressLen();
            compressLen = Objects.isNull(val) || val < 1 ? 64 : val;
        }
        return compressLen;
    }
}
