package org.code4everything.wetool.plugin.support.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.event.handler.BaseClipboardChangedEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseKeyboardEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseMouseCornerEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseMouseEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseNoMessageEventHandler;
import org.code4everything.wetool.plugin.support.event.handler.BaseQuickStartClickedEventHandler;
import org.code4everything.wetool.plugin.support.util.WeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @author pantao
 * @since 2020/10/25
 */
@Slf4j
@UtilityClass
public class EventCenter {

    /**
     * 注意这个事件没有 EventMessage 对象，订阅事件时可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_SECONDS_TIMER = "wetool_timer_seconds";

    /**
     * 注意这个事件没有 EventMessage 对象，订阅事件时可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_100_MS_TIMER = "wetool_timer_100_ms";

    /**
     * 点击快启菜单，订阅事件时可使用 {@link BaseQuickStartClickedEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_QUICK_START_CLICKED = "wetool_quick_start_clicked";

    /**
     * 清楚所有缓存事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_CLEAR_FXML_CACHE = "wetool_clear_fxml_cache";

    /**
     * WeTool重启事件，该事件包括推出事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_WETOOL_RESTART = "wetool_restart";

    /**
     * WeTool退出事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_WETOOL_EXIT = "wetool_exit";

    /**
     * WeTool界面显示事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_WETOOL_SHOW = "wetool_show";

    /**
     * WeTool界面隐藏事件，订阅事件可使用 {@link BaseNoMessageEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_WETOOL_HIDDEN = "wetool_hidden";

    /**
     * 剪贴板变化事件（仅针对文本内容），订阅事件可使用 {@link BaseClipboardChangedEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_CLIPBOARD_CHANGED = "event_clipboard_changed";

    /**
     * 触发角事件，订阅事件可使用 {@link BaseMouseCornerEventHandler}
     *
     * @since 1.2.0
     */
    public static final String EVENT_MOUSE_CORNER_TRIGGER = "event_mouse_corner_trigger";

    /**
     * 键盘按下事件，订阅事件可使用 {@link BaseKeyboardEventHandler}
     *
     * @since 1.3.0
     */
    public static final String EVENT_KEYBOARD_PRESSED = "event_keyboard_pressed";

    /**
     * 键盘弹起事件，订阅事件可使用 {@link BaseKeyboardEventHandler}
     *
     * @since 1.3.0
     */
    public static final String EVENT_KEYBOARD_RELEASED = "event_keyboard_released";

    /**
     * 鼠标移动事件，订阅事件可使用 {@link BaseMouseEventHandler}
     *
     * @since 1.3.0
     */
    public static final String EVENT_MOUSE_MOTION = "event_mouse_motion";

    /**
     * 鼠标按下事件，订阅事件可使用 {@link BaseMouseEventHandler}
     *
     * @since 1.3.0
     */
    public static final String EVENT_MOUSE_PRESSED = "event_mouse_pressed";

    /**
     * 鼠标释放事件，订阅事件可使用 {@link BaseMouseEventHandler}
     *
     * @since 1.3.0
     */
    public static final String EVENT_MOUSE_RELEASED = "event_mouse_released";

    /**
     * 所有插件加载完毕，订阅事件可使用 {@link BaseNoMessageEventHandler}
     */
    public static final String EVENT_ALL_PLUGIN_LOADED = "event_all_plugin_loaded";

    private static final Map<String, EventMode> EVENT_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<EventHandler>> HANDLER_MAP = new ConcurrentHashMap<>();

    private static final Semaphore EVENT_SEMAPHORE = new Semaphore(16);

    public static boolean onAllPluginLoaded(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_ALL_PLUGIN_LOADED, eventHandler);
    }

    public static boolean onMouseReleased(BaseMouseEventHandler eventHandler) {
        return subscribeEvent(EVENT_MOUSE_RELEASED, eventHandler);
    }

    public static boolean onMousePressed(BaseMouseEventHandler eventHandler) {
        return subscribeEvent(EVENT_MOUSE_PRESSED, eventHandler);
    }

    public static boolean onMouseMotion(BaseMouseEventHandler eventHandler) {
        return subscribeEvent(EVENT_MOUSE_MOTION, eventHandler);
    }

    public static boolean onKeyReleased(BaseKeyboardEventHandler eventHandler) {
        return subscribeEvent(EVENT_KEYBOARD_RELEASED, eventHandler);
    }

    public static boolean onKeyPressed(BaseKeyboardEventHandler eventHandler) {
        return subscribeEvent(EVENT_KEYBOARD_PRESSED, eventHandler);
    }

    public static boolean onSysCornerTrigger(BaseMouseCornerEventHandler eventHandler) {
        return subscribeEvent(EVENT_MOUSE_CORNER_TRIGGER, eventHandler);
    }

    public static boolean onClipboardChanged(BaseClipboardChangedEventHandler eventHandler) {
        return subscribeEvent(EVENT_CLIPBOARD_CHANGED, eventHandler);
    }

    public static boolean onWetoolHidden(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_WETOOL_HIDDEN, eventHandler);
    }

    public static boolean onWetoolShow(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_WETOOL_SHOW, eventHandler);
    }

    public static boolean onWetoolExit(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_WETOOL_EXIT, eventHandler);
    }

    public static boolean onWetoolRestart(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_WETOOL_RESTART, eventHandler);
    }

    public static boolean onFxmlCacheClear(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_CLEAR_FXML_CACHE, eventHandler);
    }

    public static boolean onQuickStartClicked(BaseQuickStartClickedEventHandler eventHandler) {
        return subscribeEvent(EVENT_QUICK_START_CLICKED, eventHandler);
    }

    public static boolean on100MsTimer(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_100_MS_TIMER, eventHandler);
    }

    public static boolean onSecondTimer(BaseNoMessageEventHandler eventHandler) {
        return subscribeEvent(EVENT_SECONDS_TIMER, eventHandler);
    }

    /**
     * 获取所有注册时事件KEY
     *
     * @return 事件KEY列表
     */
    public static Set<String> listEventKeys() {
        return Collections.unmodifiableSet(EVENT_MAP.keySet());
    }

    /**
     * 注册一个事件，你在发布一个事件之前需要先注册他
     *
     * @param eventKey 事件KEY，不可重复
     * @param eventMode 事件订阅模式，单订阅或多订阅者
     *
     * @return 是否注册成功
     */
    public static Optional<EventPublisher> registerEvent(String eventKey, EventMode eventMode) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventMode);

        if (EVENT_MAP.containsKey(eventKey)) {
            log.warn("event '{}' is already registered!", eventKey);
            return Optional.empty();
        }

        EVENT_MAP.put(eventKey, eventMode);
        log.info("event '{}' register success!", eventKey);
        return Optional.of(new EventPublisher(eventKey));
    }

    /**
     * 发布一个事件，发布事件之前你需要先注册事件
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生的时间
     *
     * @return 事件是否发布成功
     */
    public static boolean publishEvent(String eventKey, Date eventTime) {
        return publishEvent(eventKey, eventTime, null);
    }

    /**
     * 发布一个事件，发布事件之前你需要先注册事件
     *
     * @param eventKey 事件KEY
     * @param eventTime 事件发生的时间
     * @param eventMessage 自定义事件消息，可不传
     *
     * @return 事件是否发布成功
     */
    public static boolean publishEvent(String eventKey, Date eventTime, EventMessage eventMessage) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventTime);

        if (!EVENT_MAP.containsKey(eventKey)) {
            log.debug("event '{}' not register!", eventKey);
            return false;
        }

        List<EventHandler> list = HANDLER_MAP.get(eventKey);
        if (CollUtil.isEmpty(list)) {
            log.debug("event '{}' published, but no subscriber execution", eventKey);
            return true;
        }

        WeUtils.execute(() -> {
            if (list.size() == 1) {
                list.get(0).handleEvent(eventKey, eventTime, eventMessage);
            } else {
                list.forEach(e -> WeUtils.execute(() -> {
                    try {
                        // 限制同时执行事件的线程
                        EVENT_SEMAPHORE.acquire();
                        e.handleEvent(eventKey, eventTime, eventMessage);
                    } catch (Exception exception) {
                        log.error(ExceptionUtil.stacktraceToString(exception, Integer.MAX_VALUE));
                    } finally {
                        EVENT_SEMAPHORE.release();
                    }
                }));
            }
        });

        return true;
    }

    /**
     * 发布传过来的事件
     *
     * @since 1.3.0
     */
    @SuppressWarnings("unchecked")
    public static boolean publishEventFromRemote(JSONObject eventJson) {
        String eventKey = eventJson.getString("eventKey");
        if (!EVENT_MAP.containsKey(eventKey)) {
            log.error("event '{}' not register yet!", eventKey);
            return false;
        }

        if (EVENT_CLIPBOARD_CHANGED.equals(eventKey)) {
            JSONObject jsonObject = eventJson.getJSONObject("eventMessage");
            String clip = null;
            if (Objects.nonNull(jsonObject)) {
                clip = jsonObject.getString("clipboardText");
            }
            if (StrUtil.isBlank(clip)) {
                log.error("event '{}' no clipboard text", eventKey);
                return false;
            }

            try {
                ClipboardUtil.setStr(clip);
            } catch (Exception e) {
                log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            }
            return true;
        }

        EventMessage eventMessage = null;
        String messageString = eventJson.getString("eventMessage");

        if (StrUtil.isNotEmpty(messageString)) {
            String messageClass = eventJson.getString("messageClass");
            if (StrUtil.isBlank(messageClass)) {
                log.error("event '{}' missing event message class", eventKey);
                return false;
            }
            try {
                Class<? extends EventMessage> messageClazz = (Class<? extends EventMessage>) Class.forName(messageClass);
                eventMessage = JSON.parseObject(messageString, messageClazz);
            } catch (Exception e) {
                log.error("event '{}' msg class cast to event message error", eventKey);
                return false;
            }
        }

        Date eventTime = eventJson.getDate("eventTime");
        if (Objects.isNull(eventTime)) {
            eventTime = DateUtil.date();
        }

        log.debug("push event from remote: " + eventJson.toJSONString());
        return publishEvent(eventKey, eventTime, eventMessage);
    }

    /**
     * 订阅事件
     *
     * @param eventKey 事件KEY
     * @param eventHandler 事件处理器
     *
     * @return 是否订阅成功
     */
    public static boolean subscribeEvent(String eventKey, EventHandler eventHandler) {
        Objects.requireNonNull(eventKey);
        Objects.requireNonNull(eventHandler);

        EventMode eventMode = EVENT_MAP.get(eventKey);
        if (Objects.isNull(eventMode)) {
            log.debug("event '{}' is not register yet!", eventKey);
        }

        if (eventMode == EventMode.SINGLE_SUB) {
            List<EventHandler> list = HANDLER_MAP.get(eventKey);
            if (CollUtil.isNotEmpty(list)) {
                log.warn("event '{}' only allow single subscriber!", eventKey);
                return false;
            }
        }

        HANDLER_MAP.computeIfAbsent(eventKey, s -> new ArrayList<>()).add(eventHandler);
        return true;
    }
}
