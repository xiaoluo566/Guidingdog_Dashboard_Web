# 导盲犬仪表板 - 实时图像监控系统

## 📋 项目概述

这是一个基于 Spring Boot WebFlux 的实时图像监控系统，用于显示导盲犬的雷达图像和摄像头图像。系统通过 WebSocket 实现实时数据推送，支持双向通信。

## 🎯 核心功能

1. **实时雷达图像显示** - 从 ROS `/radar/image` 话题接收并显示
2. **实时摄像头图像显示** - 从 ROS `/camera/image` 话题接收并显示
3. **WebSocket 实时推送** - 双向通信，支持多客户端连接
4. **图像控制面板** - 启动/停止图像推送
5. **系统监控日志** - 实时显示系统状态和事件

## 🏗️ 项目结构

```
src/main/java/com/javaweb_study/guidingdog_dashboard_web/
├── GuidingdogDashboardWebApplication.java    # 应用入口
├── http/
│   ├── HealthController.java                  # 健康检查接口
│   ├── ImageController.java                   # 图像控制接口 (NEW)
│   └── VideoProxyController.java              # 视频代理接口
├── service/
│   └── ImageSubscriptionService.java          # 图像订阅服务 (NEW)
└── ws/
    ├── RosbridgeProxyHandler.java             # ROS Bridge 代理处理器 (UPDATED)
    ├── ImageStreamHandler.java                # 图像流处理器 (NEW)
    └── WebSocketConfig.java                   # WebSocket 配置 (UPDATED)

src/main/resources/
├── static/
│   └── index.html                             # 前端仪表板 (UPDATED)
├── application.yml                            # 应用配置
└── application.properties
```

## 🚀 快速开始

### 1. 启动应用
```bash
cd Guidingdog_dashboard_web
mvnw.cmd spring-boot:run
```

或者构建 JAR 包运行：
```bash
mvnw.cmd clean package
java -jar target/Guidingdog_dashboard_web-0.0.1-SNAPSHOT.jar
```

### 2. 访问仪表板
打开浏览器，访问：
```
http://localhost:8088
```

### 3. 使用步骤

1. **连接 WebSocket**
   - 点击 "连接 WebSocket" 按钮
   - 等待连接成功（状态会变为绿色）

2. **启动图像推送**
   - 点击 "启动图像推送" 按钮
   - 系统开始订阅 ROS 中的图像话题
   - 图像会实时显示在左右两个面板中

3. **停止图像推送**
   - 点击 "停止图像推送" 按钮
   - 系统停止订阅，释放资源

4. **断开连接**
   - 点击 "断开连接" 按钮
   - WebSocket 连接将关闭

## 📡 API 接口

### WebSocket 端点

#### 图像推送 WebSocket
```
ws://localhost:8088/ws/image
```

接收的消息格式：
```json
{
    "type": "radar",           // 或 "camera"
    "data": "base64_image_data",
    "timestamp": 1629801234567
}
```

#### ROS Bridge 代理 WebSocket
```
ws://localhost:8088/ws/ros
```

### HTTP 接口

#### 启动图像推送
```
POST /api/image/start

响应：
{
    "status": "success",
    "message": "Image streaming started"
}
```

#### 停止图像推送
```
POST /api/image/stop

响应：
{
    "status": "success",
    "message": "Image streaming stopped"
}
```

#### 获取图像推送状态
```
GET /api/image/status

响应：
{
    "status": "active",
    "message": "Image streaming service is running"
}
```

#### 健康检查
```
GET /actuator/health

响应：
{
    "status": "UP"
}
```

## ⚙️ 配置文件

编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 8088                    # 应用端口

guidingdog:
  upstream:
    rosbridge: ws://localhost:9090    # ROS Bridge 服务地址
    video: http://localhost:8080      # 视频代理服务地址

spring:
  webflux:
    base-path: /

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## 🔌 与 ROS 的集成

### 订阅的话题

1. **雷达图像话题**
   - 话题名: `/radar/image`
   - 消息类型: `sensor_msgs/Image`
   - 推送频率: 10 FPS (每 100ms 一次)

2. **摄像头图像话题**
   - 话题名: `/camera/image`
   - 消息类型: `sensor_msgs/Image`
   - 推送频率: 30 FPS (每 33ms 一次)

### 通过 ROS Bridge 发布图像

在 ROS 节点中，您可以发布图像数据：

```python
import rospy
from sensor_msgs.msg import Image
from cv_bridge import CvBridge

def publish_radar_image():
    pub = rospy.Publisher('/radar/image', Image, queue_size=10)
    bridge = CvBridge()
    
    # 假设 cv_image 是您的图像
    msg = bridge.cv2_to_imgmsg(cv_image, encoding="bgr8")
    pub.publish(msg)

if __name__ == '__main__':
    rospy.init_node('radar_image_publisher')
    rate = rospy.Rate(10)  # 10 Hz
    while not rospy.is_shutdown():
        publish_radar_image()
        rate.sleep()
```

## 📊 核心类说明

### ImageStreamHandler
处理图像推送的 WebSocket 连接。
- `handle()` - 处理客户端连接
- `broadcastImage()` - 广播图像数据给所有客户端
- `broadcastRadarImage()` - 广播雷达图像
- `broadcastCameraImage()` - 广播摄像头图像

### ImageSubscriptionService
管理图像订阅和推送。
- `subscribeToRadarImage()` - 订阅雷达图像
- `subscribeToCameraImage()` - 订阅摄像头图像
- `unsubscribeFromImages()` - 取消订阅
- `publishRadarImage()` - 手动推送雷达图像
- `publishCameraImage()` - 手动推送摄像头图像

### ImageController
HTTP 接口控制器。
- `startImageStreaming()` - 启动图像推送
- `stopImageStreaming()` - 停止图像推送
- `getImageStreamStatus()` - 获取状态

## 🛠️ 高级定制

### 修改图像推送频率

编辑 `ImageSubscriptionService.java`：

```java
// 修改雷达图像频率（单位：毫秒）
radarTimer.scheduleAtFixedRate(task, 0, 100);  // 改为其他值

// 修改摄像头图像频率（单位：毫秒）
cameraTimer.scheduleAtFixedRate(task, 0, 33);  // 改为其他值
```

### 集成真实的 ROS 图像源

修改 `ImageSubscriptionService.java` 中的以下方法：

```java
private String generateDummyRadarImage() {
    // 替换为实际的 ROS 订阅逻辑
    // 从 ROS Bridge 接收图像数据
    // 转换为 Base64 编码
}
```

## 🐛 故障排除

### 连接 WebSocket 时超时
- 检查应用是否正常运行
- 检查防火墙设置
- 确认端口 8088 是否被占用

### 收不到图像数据
- 确保 ROS Bridge 服务运行在 `ws://localhost:9090`
- 检查 `/radar/image` 和 `/camera/image` 话题是否有数据发布
- 查看浏览器控制台的错误信息

### 图像显示不出来
- 检查 Base64 数据是否正确编码
- 确保图像数据格式为 JPEG
- 查看浏览器网络选项卡，验证 WebSocket 消息

## 📚 依赖项

- Spring Boot 3.5.7
- Spring WebFlux
- ReactorNettyWebSocketClient
- Java 25

## 📝 许可证

MIT License

## 👨‍💻 开发者

JavaWeb Study Project

---

**最后更新**: 2025-10-24

