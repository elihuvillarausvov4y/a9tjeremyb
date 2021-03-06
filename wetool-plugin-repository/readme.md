## 插件提交

1. Fork 本仓库

2. 在本文件夹下，新建专属自己某个插件的目录，建议格式`{author}-{plugin}`，例如：`ease-ftp`，方便其他用户阅览

3. 在新建的目录中自定义插件的一些信息（包括名称、版本、历史更新、下载地址等），注意：插件的`Jar`最好不要上传到仓库中来，建议上传到其他地方，以下载链接的方式提供

4. 提交变更至`Master`分支

5. 新建Pull Request，等待合并

## 运行插件

1. [安装WeTool](https://gitee.com/code4everything/wetool)

2. 将插件`Jar`放置到WeTool工作目录的`plugins`目录下（没有的可以新建）

3. 启动WeTool（或重启）

## 插件列表

|名称|作者|说明文档|下载地址|
|---|---|---|---|
|开发工具集合（含Redis客户端）|EASE|[文档](ease-devtool/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-devtool-1.6.0.zip)|
|文件检索工具|EASE|[文档](ease-everywhere/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.6.0.jar)|
|FTP客户端|EASE|[文档](ease-ftp-client/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-ftpclient-1.6.0.jar)|
|FTP服务器|EASE|[文档](ease-ftp-server/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-ftpserver-1.6.0.jar)|
|七牛对象存储管理|EASE|[文档](ease-qiniu/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-qiniu-1.6.0.jar)|
|JavaQL脚本小程序|EASE|[文档](ease-dbops/readme.md)|[最新版](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-dbops-1.6.0.jar)|
|倒计时|EASE|[文档](https://gitee.com/easepan/countdown-wetool-plugin)|暂无（需自行打包）|
|命令助记器|EASE|[文档](https://gitee.com/easepan/cmdassist-wetool-plugin)|暂无（需自行打包）|

### 第三方工具封装

|名称|原作者|项目地址|插件下载地址|
|---|---|---|---|
|蜗牛下载工具|ACGIST|[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)|[最新插件下载](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-thirdparty-downloader-1.6.0.jar)|
