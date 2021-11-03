package org.code4everything.wetool.plugin.devtool.redis.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.code4everything.wetool.plugin.devtool.redis.jedis.JedisUtils;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pantao
 * @since 2019/11/13
 */
public class ValueController {

    private final String lineSep = "\n";

    @FXML
    public TextField expireText;

    @FXML
    public TextField keyText;

    @FXML
    public TextArea valueText;

    @FXML
    public ToggleGroup typeGroup;

    @FXML
    public Label serverLabel;

    @FXML
    public RadioButton typeHashRadio;

    @FXML
    public RadioButton typeSortedSetRadio;

    @FXML
    public RadioButton typeSetRadio;

    @FXML
    public RadioButton typeListRadio;

    @FXML
    public RadioButton typeStringRadio;

    private JedisUtils.KeyExplorer keyExplorer;

    private String key;

    @FXML
    private void initialize() {
        keyExplorer = JedisUtils.getKeyExplorer();
        serverLabel.setText(StrUtil.format("服务器：{}，数据库：db{}", keyExplorer.getAlias(), keyExplorer.getDb()));
        keyText.setText(keyExplorer.getKey());
        key = keyText.getText();
        refresh();
    }

    public void refresh() {
        Jedis jedis = JedisUtils.getJedis(keyExplorer);
        if (StrUtil.isEmpty(key) || !jedis.exists(key)) {
            return;
        }

        expireText.setText(String.valueOf(jedis.ttl(key)));
        StringBuilder sb = new StringBuilder();
        switch (keyExplorer.getType()) {
            case "hash":
                Map<String, String> map = jedis.hgetAll(key);
                map.forEach((k, v) -> sb.append(k).append(": ").append(v).append(lineSep));
                typeGroup.selectToggle(typeHashRadio);
                break;
            case "zset":
                Set<Tuple> tuples = jedis.zrangeWithScores(key, 0, -1);
                tuples.forEach(t -> sb.append(t.getElement()).append(": ").append(t.getScore()).append(lineSep));
                typeGroup.selectToggle(typeSortedSetRadio);
                break;
            case "set":
                Set<String> set = jedis.smembers(key);
                set.forEach(m -> sb.append(m).append(lineSep));
                typeGroup.selectToggle(typeSetRadio);
                break;
            case "list":
                List<String> list = jedis.lrange(key, 0, -1);
                list.forEach(e -> sb.append(e).append(lineSep));
                typeGroup.selectToggle(typeListRadio);
                break;
            default:
                sb.append(jedis.get(key));
                typeGroup.selectToggle(typeStringRadio);
                break;
        }
        valueText.setText(sb.toString());
    }

    public void update() {
        if (StrUtil.isEmpty(keyText.getText())) {
            FxDialogs.showInformation("Key不能为空！", null);
            return;
        }

        if (StrUtil.isEmpty(valueText.getText())) {
            FxDialogs.showInformation("Value不能为空！", null);
            return;
        }

        int expire = -1;
        if (NumberUtil.isNumber(expireText.getText())) {
            expire = NumberUtil.parseInt(expireText.getText());
        } else {
            try {
                DateTime dateTime = DateUtil.parseDateTime(expireText.getText());
                expire = (int) (dateTime.getTime() / 1000);
                expireText.setText(String.valueOf(expire));
            } catch (Exception e) {
                // ignore
            }
        }

        Jedis jedis = JedisUtils.getJedis(keyExplorer);
        deleteKey();
        key = keyText.getText();

        updateHash(jedis);
        updateSortedSet(jedis);
        updateSet(jedis);
        updateList(jedis);
        updateString(jedis);

        if (expire > 0) {
            jedis.expire(key, expire);
        }
        FxDialogs.showInformation("保存成功", null);
    }

    private void updateString(Jedis jedis) {
        if (typeStringRadio.isSelected()) {
            jedis.set(key, valueText.getText());
        }
    }

    private void updateList(Jedis jedis) {
        if (typeListRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), lineSep);
            jedis.rpush(key, list.toArray(new String[0]));
        }
    }

    private void updateSet(Jedis jedis) {
        if (typeSetRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), lineSep);
            jedis.sadd(key, list.toArray(new String[0]));
        }
    }

    private void updateSortedSet(Jedis jedis) {
        if (typeSortedSetRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), lineSep);
            Map<String, Double> map = new HashMap<>(list.size(), 1);
            for (String kv : list) {
                if (!kv.contains(":")) {
                    FxDialogs.showInformation("格式不正确！", null);
                    return;
                }
                List<String> kvs = StrUtil.splitTrim(kv, ":", 2);
                try {
                    double score = Double.parseDouble(kvs.get(1));
                    map.put(kvs.get(0), score);
                } catch (Exception e) {
                    FxDialogs.showInformation("格式不正确！", null);
                    return;
                }
                jedis.zadd(key, map);
            }
        }
    }

    private void updateHash(Jedis jedis) {
        if (typeHashRadio.isSelected()) {
            List<String> list = StrUtil.splitTrim(valueText.getText(), lineSep);
            Map<String, String> map = new HashMap<>(list.size(), 1);
            for (String kv : list) {
                if (!kv.contains(":")) {
                    FxDialogs.showInformation("格式不正确！", null);
                    return;
                }
                List<String> kvs = StrUtil.splitTrim(kv, ":", 2);
                map.put(kvs.get(0), kvs.get(1));
            }
            jedis.hmset(key, map);
        }
    }

    public void delete() {
        deleteKey();
        FxDialogs.showInformation("删除成功", null);
    }

    private void deleteKey() {
        if (StrUtil.isEmpty(key)) {
            return;
        }
        JedisUtils.getJedis(keyExplorer).del(key);
    }
}
