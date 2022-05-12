package org.code4everything.wetool.plugin.support.config;

import lombok.*;
import org.code4everything.boot.base.bean.BaseBean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/7/3
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeTab implements BaseBean, Serializable {

    private static final long serialVersionUID = -7095575648923571810L;

    /**
     * 初始化加载的选项卡
     */
    private Set<String> loads;

    /**
     * 支持加载的选项卡，可不配置此属性，展示的配置文件只是为了说明目前支持的选项卡
     */
    private Set<String> supports;

    @Generated
    public Set<String> getLoads() {
        return Objects.isNull(loads) ? new HashSet<>() : loads;
    }
}
