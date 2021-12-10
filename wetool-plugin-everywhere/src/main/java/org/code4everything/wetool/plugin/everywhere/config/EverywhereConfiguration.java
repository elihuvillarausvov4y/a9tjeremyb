package org.code4everything.wetool.plugin.everywhere.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.boot.base.bean.BaseBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2019/11/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EverywhereConfiguration implements BaseBean {

    private static Formatted formatted = new Formatted();

    /**
     * 需要创建内容索引的文件名（正则匹配）
     */
    private Set<String> includeFilenames;

    /**
     * 不创建内容索引的文件名（正则匹配）
     */
    private Set<String> excludeFilenames;

    private Boolean ignoreHiddenFile;

    /**
     * 超过设置大小的文件不创建内容索引，最大100MB(100,000,000)，格式：1,000,000，单位：B
     */
    private String sizeLimit;

    public static Formatted getFormatted() {
        return formatted;
    }

    public void setIncludeFilenames(Set<String> includeFilenames) {
        this.includeFilenames = includeFilenames;
        formatted.setIncludeFilenames(toPatterns(includeFilenames));
    }

    public void setExcludeFilenames(Set<String> excludeFilenames) {
        this.excludeFilenames = excludeFilenames;
        formatted.setExcludeFilenames(toPatterns(excludeFilenames));
    }

    public void setIgnoreHiddenFile(Boolean ignoreHiddenFile) {
        this.ignoreHiddenFile = ignoreHiddenFile;
        formatted.setIgnoreHiddenFile(ObjectUtil.defaultIfNull(ignoreHiddenFile, true));
    }

    public void setSizeLimit(String sizeLimit) {
        this.sizeLimit = sizeLimit;
        String size = sizeLimit.replaceAll("[,_\\s]", "");
        if (NumberUtil.isNumber(size)) {
            formatted.setSizeLimit(NumberUtil.parseInt(size));
        }
    }

    private List<Pattern> toPatterns(Set<String> patterns) {
        if (CollUtil.isEmpty(patterns)) {
            return Collections.emptyList();
        }
        List<Pattern> patternList = new ArrayList<>(patterns.size());
        patterns.forEach(p -> {
            try {
                patternList.add(Pattern.compile(p));
            } catch (Exception e) {
                // ignore
            }
        });
        return patternList;
    }

    @Data
    public static class Formatted {

        private List<Pattern> includeFilenames = Collections.emptyList();

        private List<Pattern> excludeFilenames = Collections.emptyList();

        private boolean ignoreHiddenFile = true;

        private int sizeLimit = 100_000_000;
    }
}
