package org.code4everything.wetool.plugin.support.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.code4everything.wetool.plugin.support.event.EventMessage;

/**
 * @author pantao
 * @since 2020/11/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MouseCornerEventMessage implements EventMessage {

    private int x;

    private int y;

    private LocationTypeEnum type;

    public static MouseCornerEventMessage of(LocationTypeEnum type, int x, int y) {
        return new MouseCornerEventMessage(x, y, type);
    }

    public enum LocationTypeEnum {

        /**
         * 左上角
         */
        LEFT_TOP,

        /**
         * 坐下角
         */
        LEFT_BOTTOM,

        /**
         * 右上角
         */
        RIGHT_TOP,

        /**
         * 右下角
         */
        RIGHT_BOTTOM,

        /**
         * 左侧
         */
        LEFT_SIDE,

        /**
         * 右侧
         */
        RIGHT_SIDE,

        /**
         * 顶部
         */
        TOP_LINE,

        /**
         * 底部
         */
        BOTTOM_LINE,

        /**
         * 非边缘情况
         */
        NONE;
    }
}
