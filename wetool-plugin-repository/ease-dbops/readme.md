## Ease DB Ops

dbops，之前中文名为数据库小应用，现在正式更名为 `JavaQL脚本小程序`，我们可以将一系列的脚本封装成一个小应用，程序会将这个小应用可视化为一个按钮，只需点击一下即可执行脚本。

为什么叫之前数据库小应用？因为原本这款插件是为了将一系列SQL操作封装成小应用而设计的，而考虑到执行这一系列的SQL可能会用到参数的注入，变量的定义，绑定方法，那这一系列操作肯定会绕到脚本的执行上去，自然就不仅限于SQL了，只要符合语法的脚本都是能够被执行的，这也是现在改名的原因。

最新版下载地址：[wetool-plugin-dbops-1.6.0.jar](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.6.0.jar)

在wetool搜索框中查看全局变量：`dbops global name`。

> 如需配置数据库，[请点击此处查看](https://gitee.com/code4everything/wetool) 。本插件使用的脚本解析库 [QLExpress](https://github.com/alibaba/QLExpress), 语法基本和java一致，几乎没有学习成本，了解语法点击链接即可。

### 运行截图

![image](images/dbops.png)

### 自动导包

脚本自动导入以下类

```java
import java.lang.*;
import java.util.*;

import org.code4everything.wetool.plugin.support.util.*;
import org.code4everything.wetool.plugin.support.factory.*;
import org.code4everything.wetool.plugin.support.http.*;
import org.code4everything.wetool.plugin.support.event.*;
import org.code4everything.wetool.plugin.support.druid.*;
import org.code4everything.wetool.plugin.support.cache.*;
import org.code4everything.wetool.plugin.support.func.*;

import com.alibaba.fastjson.*;

import cn.hutool.core.util.*;
import cn.hutool.core.collection.*;
import cn.hutool.core.date.*;
import cn.hutool.core.io.*;
import cn.hutool.core.lang.*;
import cn.hutool.core.map.*;
```

### 内置方法

|方法|返回类型|说明|
|---|---|---|
|dialog(Object object)|void|弹窗|
|list(Object... params)|List\<Object\>|将数组转成List|
|input(String tip)|String|输入框弹窗，参数为提示内容|
|log(String msg, Object... params)|void|打印日志，参数与slf4j的log.info保持一致|
|format(String str, Object... params)|String|格式化字符串，与hutool的StrUtil.format保持一致|
|query(String sql, List params)|List\<Map\<String, Object\>\>|执行SQL查询语句，需连接数据库|
|update(String sql, List params)|long|执行SQL更新语句，需连接数据库，返回影响的行数|
|processes(String name)|List\<OSProcess\>|模糊匹配进程名称|
|run(String... cmds)|String|执行命令，返回执行结果|
|put(String key, Object value)|void|设置局部变量，可传递到子脚本中|
|global(String key, Object value)|void|声明全局变量，自动注入到脚本中|
|exec(String varKey)|Object|执行脚本，参数为全局变量名称，脚本内容取全局变量对应的值，返回执行结果|
|http0(String api, String varKey)|boolean|暴露http服务，api格式：get/api/hello，varKey为回调脚本（子脚本）的名称|
|http1(int port, String api, String varKey)|boolean|暴露http服务|
|pushThisEvent2Remote(String postApi)|void|推送当前事件到远程|
|join(String delimiter, String... params)|String|拼接字符串|
|append(Object param)|StringBuilder|追加字符串|
|chooseSaveFile()|File|保存文件的弹窗|
|chooseMultiFile()|List\<File\>|多选文件的弹窗|
|chooseFile()|File|单选文件的弹窗|
|chooseFolder()|File|选择文件夹的弹窗|
|save(File file, String content)|boolean|保存文件|
|random(List\<Object\>)|Object|从列表中随机取一个元素|
|choice(String tip, List\<String\>)|String|下拉框弹窗|
|get(String urlString)|String|http get请求，返回响应内容|
|post(String urlString, String body)|String|http post请求，返回响应数据|
|evalJson(Object rootObject, String path|Object|按json路径获取值|
|getGlobal(String key)|Object|获取全局变量|
|request(String method, String url)|cn.hutool.http.HttpRequest|创建一个HttpRequest|
|setClipboard(String str)|void|设置字符串到剪贴板|
|getClipboard()|String|获取剪贴板字符串|

> 补充说明：子（回调）脚本会注册到全局变量中，`varKey` 可以直接是脚本代码，不一定是子（回调）脚本的Key

### 内置变量

|变量名|类型|说明|
|---|---|---|
|now|Date|触发脚本执行的时间|
|dbName|String|脚本当前执行的dbName|
|currDir|String|当前工作目录|
|lineSep|String|文件换行符|
|fileSep|String|文件目录分隔符|
|userHome|String|用户目录|

### 事件触发

目前脚本支持触发方式：手动执行，定时任务，以及事件订阅。

**定时任务**

支持cron表达式，如：每10秒钟执行一次 `*/10 * * * * *` ，定时任务触发时会注入 `cron` 和 `date` 两个变量，cron即当前执行的表达式，date表示当前执行的时间。
  
**事件订阅**
  
如果需要用到事件的消息作为参数，可能需要查看源代码，[查看事件](../../wetool-plugin-support/readme.md)，[源码目录](../../wetool-plugin-support/src/main/java/org/code4everything/wetool/plugin/support/event/message)。

事件发布的消息会随一个被定义为 `eventMessage` 的变量一起注入到脚本中，同时还会注入 `eventKey` `eventTime` 属性。


### 远程事件的推送与发布

假设我们在两台机器上（A和B）都启动了WeTool，需要对事件进行汇总，A作为主节点（假设ip为`192.168.1.130`），即事件接收方，B作为副节点即远程事件的推送方。

现在我们需要在A端暴露一个HTTP服务来接收远端传过来的事件，并发布该事件：

```java
http0("post/api/event/remote", "http_remote_event_sub_script");
```

然后添加名称为`http_remote_event_sub_script`的子脚本，脚本内容如下（body由http回调自动带入）：

```java
EventCenter.publishEventFromRemote(body);
```

最后我们需要在B端将事件推送出来，添加一个脚本，订阅要远程推送的事件，脚本内容如下：

```java
pushThisEvent2Remote("http://192.168.1.130:8189/api/event/remote");
```

### 脚本引用

支持引用QL脚本文件和HTTP内容，在脚本内容框直接填入使用QL语法编写的脚本文件或HTTP全路径即可，格式如：`http://share.qiniu.easepan.xyz/text/script/dbops_hello.ql` ，`file:/home/user/java/script/test.txt`，`file:c:\project\test.ql`。

### 例子1：存储剪贴板历史并添加查询按钮

首先，我们需要创建用于存储剪贴板内容的数据表

```sql
create table clipboard_history (
    id bigint auto_increment not null primary key,
    created_date datetime(6) not null,
    clip_string text not null
) comment '剪贴板历史';

create index clipboard_history_date_index on clipboard_history (created_date desc);
```

监听剪贴板事件，并保存至数据库

![image](images/save_clipboard.png)

```java
import cn.hutool.core.util.StrUtil;

sql="insert into clipboard_history(created_date, clip_string) values(now(6),?)";
clip=eventMessage.getClipboardText();

if(StrUtil.isNotBlank(clip)){
update(sql,list(clip));
}
```

最后新增一个查询最近一个月剪贴板内容的小应用

![image](images/search_clipboard.png)

```java
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

sql="select * from clipboard_history where unix_timestamp(created_date)>? and clip_string like ?";
start=DateUtil.offsetMonth(DateUtil.date(), -1).getTime()/1000;
key=input("搜索剪贴板");

if(StrUtil.isBlank(key)){
dialog("搜索内容不能为空");
return null;
}

result=query(sql,list(start,"%"+key+"%"));
dialog(result);
```

> 这里注意有个小坑：使用return结束执行时一定要有返回值，否则语法会报错。

![image](images/search_clipboard_input.png)

![image](images/search_clipboard_result.png)

### 例子2：保存键盘敲击记录

创建表结构

```sql
create table keyboard_history (
    id bigint auto_increment primary key,
    created_date datetime(6) not null,
    key_text varchar(32) not null,
    key_code int not null,
    raw_code int not null
) comment '键盘敲击历史记录';

create index keyboard_history_key_code_index on keyboard_history (key_code);
create index keyboard_history_key_text_index on keyboard_history (key_text);
```

添加存储脚本

![image](images/key_board_history.png)

```java
sql="insert into keyboard_history(created_date, key_text, key_code, raw_code) values(now(6),?,?,?)";

keyText=eventMessage.toKeyText();
keyCode=eventMessage.getKeyEvent().getKeyCode();
rawCode=eventMessage.getKeyEvent().getRawCode();

update(sql,list(keyText,keyCode,rawCode));
```

大功告成，任何键盘敲击行为都会记录到数据库，我们可以根据这些数据来分析我们每天的活动行为。

### 例子3：鼠标点击记录

创建数据表结构

```sql
create table mouse_click_history (
    id bigint auto_increment not null primary key,
    created_date datetime(6) not null,
    button int not null,
    click_count int not null,
    pos_x int not null,
    pos_y int not null
) comment '鼠标点击历史记录';
```

添加存储脚本

![image](images/mouse_click_histor.png)

```java
sql="insert into mouse_click_history(created_date, button, click_count, pos_x, pos_y) values(now(6),?,?,?,?)";

button=eventMessage.getMouseEvent().getButton();
clickCount=eventMessage.getMouseEvent().getClickCount();
posX=eventMessage.getMouseEvent().getX();
posY=eventMessage.getMouseEvent().getY();

update(sql,list(button,clickCount,posX,posY));
```

### 例子4：新增Windows虚拟桌面触发角

![image](images/windows_virtual_desktop.png)

```java
if(eventMessage.getType().name()=="LEFT_TOP"){
FxUtils.multiDesktopOnWindows();
}
```

### 更新历史

#### [v1.6.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.6.0.jar)

- 支持引用脚本文件
- 支持运行时启动本插件
- 新增内置方法

#### [v1.5.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.5.0.jar)

- 支持CRON表达式
- 支持引用脚本文件
- 新增内置方法

#### [v1.5.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.5.0.jar)

- 注册脚本执行方法到WeTool方法调用中心
- 修复了一些已知问题
- 新增全局变量

#### [v1.3.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.3.0.jar)

- 支持导入导出
- 修复了一些已知问题
- 支持子脚本
- 支持暴露http服务

#### [v1.2.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.2.0.jar)

- 支持订阅事件
- 支持QLExpress脚本
- 支持SQL执行
