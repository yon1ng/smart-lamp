# Smart Lamp
Keelight智能灯泡 & 乐逗智能灯泡

# 项目介绍
在 src 目录下是控制智能灯泡的 Java 代码，这是标准的 Maven 项目，可以将其打包成 Docker 镜像，然后运行在 NAS 或软路由中。
而 arduino 目录下是智能灯泡与天猫精灵对接的 Arduino 代码。只需进行轻微修改，就可以使其兼容其他平台，例如：百度小度、Apple HomeKit 等。
# 使用说明
1. 修改代码中的设备密钥、wifi ssid、wifi 密码
2. 编译上传至 esp8266 或 arduino 板子上
3. 连接智能灯泡并打开智能语音助手，即可进行控制

# 注意事项
- 本代码需要在 arduino 环境下进行编译上传
- 请勿随意修改其他关键代码内容
- 如有任何问题，请在 GitHub Issues 中提交
