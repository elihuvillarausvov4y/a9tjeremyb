package org.code4everything.wetool.plugin.dbops;

import cn.hutool.core.io.FileUtil;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.wetool.plugin.dbops.controller.MainController;
import org.code4everything.wetool.plugin.test.WetoolTester;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class DbOpsTest {

    public static void main(String[] args) {
        MainController.scriptJsonFile = FileUtil.file(FileUtils.currentWorkDir("ql-script.json"));
        WetoolTester.runTest(args);
    }
}
