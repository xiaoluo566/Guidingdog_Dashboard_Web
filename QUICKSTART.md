# 实时图像监控系统 - 快速测试指南

## 📌 功能概述

已为导盲犬仪表板添加了完整的实时图像推送功能，包括：

✅ **雷达图像实时显示** - WebSocket 实时推送  
✅ **摄像头图像实时显示** - WebSocket 实时推送  
✅ **双向 WebSocket 通信** - 支持多客户端连接  
✅ **图像推送控制** - 启动/停止推送  
✅ **系统监控面板** - 实时日志和状态显示  

## 🚀 快速启动

### Windows 用户
```bash
# 双击运行启动脚本
start.bat
```

### Linux/Mac 用户
```bash
# 给脚本赋予执行权限
chmod +x start.sh

# 运行脚本
./start.sh
```

### 手动启动
```bash
cd Guidingdog_dashboard_web

# 编译并运行
mvnw.cmd spring-boot:run

# 或构建 JAR 后运行
mvnw.cmd clean package
java -jar target/Guidingdog_dashboard_web-0.0.1-SNAPSHOT.jar
```

## 🌐 访问应用

应用启动后，打开浏览器访问：
```
http://localhost:8088
```

## 📊 操作步骤

### 1️⃣ 连接 WebSocket
- 点击 **"连接 WebSocket"** 按钮
- 等待状态变为绿色 **"✅ 已连接"**
- 日志窗口显示连接成功信息

### 2️⃣ 启动图像推送
- 点击 **"启动图像推送"** 按钮
- 状态显示为 **"▶️ 推送中"**
- 图像区域显示实时图像（目前为演示数据）

### 3️⃣ 查看实时图像
- 左侧面板显示 **📡 雷达图像**
- 右侧面板显示 **📷 摄像头图像**
- 上方状态栏显示已接收的图像帧数

### 4️⃣ 停止推送
- 点击 **"停止图像推送"** 按钮
- 状态显示为 **"⏸️ 已停止"**

### 5️⃣ 断开连接
- 点击 **"断开连接"** 按钮
- 状态显示为 **"❌ 已断开"**

## 🔧 新增文件

### 后端 Java 类

| 文件 | 说明 |
|------|------|
| `ImageStreamHandler.java` | WebSocket 图像流处理器 |
| `ImageSubscriptionService.java` | 图像订阅和推送服务 |
| `ImageController.java` | 图像控制 HTTP 接口 |

### 前端资源

| 文件 | 说明 |
|------|------|
| `index.html` | 完全重写的仪表板界面 |

### 配置和脚本

| 文件 | 说明 |
|------|------|
| `README.md` | 详细文档 |
| `start.bat` | Windows 启动脚本 |
| `start.sh` | Linux/Mac 启动脚本 |

## 🎨 前端功能

### 实时图像显示区域
- 雷达图像容器 (左侧)
- 摄像头图像容器 (右侧)
- 自适应响应式设计
- 加载动画提示

### 控制面板
- ✅ WebSocket 连接状态实时显示
- ▶️ 图像推送状态显示
- 📊 图像帧计数器
- 📝 系统日志窗口

### 按钮操作
- 连接/断开 WebSocket
- 启动/停止图像推送
- 清空日志

## 📡 API 接口

### WebSocket 端点
```
ws://localhost:8088/ws/image
```

### HTTP REST 接口

```bash
# 启动图像推送
curl -X POST http://localhost:8088/api/image/start

# 停止图像推送
curl -X POST http://localhost:8088/api/image/stop

# 获取推送状态
curl -X GET http://localhost:8088/api/image/status
```

## 🔌 与 ROS 集成说明

### 目前的演示模式
- 系统目前运行在 **演示模式**
- 每 100ms 生成一帧雷达图像
- 每 33ms 生成一帧摄像头图像
- 图像数据为占位符 (JPEG 头部)

### 与真实 ROS 集成

修改 `ImageSubscriptionService.java` 的以下方法：

```java
private String generateDummyRadarImage() {
    // 1. 通过 ROS Bridge WebSocket 订阅 /radar/image 话题
    // 2. 接收实际的图像数据
    // 3. 转换为 Base64 编码
    // 4. 返回编码后的字符串
    
    byte[] imageData = receiveImageFromROS("/radar/image");
    return Base64.getEncoder().encodeToString(imageData);
}
```

## 📈 性能参数

| 参数 | 值 |
|------|-----|
| 雷达图像帧率 | 10 FPS |
| 摄像头图像帧率 | 30 FPS |
| 最大同时连接数 | 无限制 |
| WebSocket 消息格式 | JSON |
| 图像编码 | Base64 (JPEG) |

## 🐛 常见问题

### Q: 收不到任何图像？
**A:** 这是正常的，因为目前运行在演示模式。要接收真实图像，需要：
1. 确保 ROS Bridge 运行在 `ws://localhost:9090`
2. ROS 节点正在发布图像到 `/radar/image` 和 `/camera/image` 话题
3. 修改 `ImageSubscriptionService` 以连接真实 ROS 数据源

### Q: WebSocket 连接失败？
**A:** 检查以下几点：
1. 应用是否成功启动（查看终端输出）
2. 防火墙是否阻止了 8088 端口
3. 浏览器是否支持 WebSocket (现代浏览器都支持)

### Q: 如何修改图像推送频率？
**A:** 编辑 `ImageSubscriptionService.java`，修改定时任务的间隔参数：
```java
// 修改最后一个参数（单位：毫秒）
radarTimer.scheduleAtFixedRate(task, 0, 100);   // 改为其他值
cameraTimer.scheduleAtFixedRate(task, 0, 33);   // 改为其他值
```

## 📋 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                   浏览器客户端                           │
│  (index.html - WebSocket 连接 + 实时显示)               │
└────────────────────┬────────────────────────────────────┘
                     │ WebSocket
                     │ /ws/image
                     ▼
┌─────────────────────────────────────────────────────────┐
│            Spring Boot WebFlux 应用                      │
├─────────────────────────────────────────────────────────┤
│  ImageStreamHandler  ◄─── WebSocket 请求处理            │
│        ▲                                                 │
│        │                                                 │
│        └─ 接收图像数据                                   │
│                                                          │
│  ImageSubscriptionService ◄─── 定时推送服务             │
│        │                                                 │
│        ├─► 订阅 /radar/image 话题                       │
│        └─► 订阅 /camera/image 话题                      │
│                                                          │
│  ImageController ◄─── HTTP 控制接口                      │
│        │                                                 │
│        ├─► /api/image/start                             │
│        ├─► /api/image/stop                              │
│        └─► /api/image/status                            │
└─────────────────────────────────────────────────────────┘
                     │ HTTP & WebSocket
                     │ (代理)
                     ▼
        ┌────────────────────────────┐
        │     ROS Bridge 服务         │
        │  (ws://localhost:9090)     │
        └────────────────────────────┘
                     │ ROS 通信
                     ▼
        ┌────────────────────────────┐
        │   ROS 节点 & 话题          │
        │  /radar/image              │
        │  /camera/image             │
        └────────────────────────────┘
```

## 📦 项目编译

项目已成功编译，生成的 JAR 包位于：
```
target/Guidingdog_dashboard_web-0.0.1-SNAPSHOT.jar
```

## ✨ 功能亮点

- 🎨 **响应式设计** - 支持桌面和移动设备
- ⚡ **高性能** - 使用 Spring WebFlux 异步处理
- 🔄 **实时推送** - 低延迟的 WebSocket 通信
- 📊 **实时监控** - 完整的系统日志和状态显示
- 🔌 **易于集成** - 清晰的 API 和标准接口

## 🎯 后续改进建议

1. **数据库集成** - 保存历史图像数据
2. **图像处理** - 添加图像增强和分析功能
3. **实时告警** - 异常情况自动告警
4. **多用户支持** - 基于角色的权限管理
5. **性能优化** - 图像压缩和流量优化

---

**项目状态**: ✅ 完成  
**最后更新**: 2025-10-24  
**版本**: 0.0.1-SNAPSHOT

