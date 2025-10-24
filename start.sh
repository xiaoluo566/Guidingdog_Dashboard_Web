#!/bin/bash

# 导盲犬仪表板启动脚本（Linux/Mac）

echo ""
echo "===================================="
echo "  导盲犬仪表板 - 启动脚本"
echo "===================================="
echo ""

# 检查 Java 是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未检测到 Java 环境，请先安装 JDK"
    exit 1
fi

echo "[✓] Java 已找到"

# 设置应用参数
APP_JAR="target/Guidingdog_dashboard_web-0.0.1-SNAPSHOT.jar"
PORT=8088
ROSBRIDGE_URL="ws://localhost:9090"

# 检查 JAR 文件是否存在
if [ ! -f "$APP_JAR" ]; then
    echo ""
    echo "错误: JAR 文件不存在: $APP_JAR"
    echo "请先运行编译命令: mvn clean package"
    exit 1
fi

echo "[✓] JAR 文件已找到"
echo ""
echo "启动参数:"
echo "  - 应用端口: $PORT"
echo "  - ROS Bridge URL: $ROSBRIDGE_URL"
echo ""
echo "===================================="
echo "  应用启动中..."
echo "===================================="
echo ""
echo "访问地址: http://localhost:$PORT"
echo ""

# 启动应用
java -jar "$APP_JAR" \
    --server.port=$PORT \
    --guidingdog.upstream.rosbridge=$ROSBRIDGE_URL
@echo off
REM 导盲犬仪表板启动脚本

echo.
echo ====================================
echo   导盲犬仪表板 - 启动脚本
echo ====================================
echo.

REM 检查 Java 是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到 Java 环境，请先安装 JDK
    pause
    exit /b 1
)

echo [✓] Java 已找到

REM 设置应用参数
set APP_JAR=target\Guidingdog_dashboard_web-0.0.1-SNAPSHOT.jar
set PORT=8088
set ROSBRIDGE_URL=ws://localhost:9090

REM 检查 JAR 文件是否存在
if not exist %APP_JAR% (
    echo.
    echo 错误: JAR 文件不存在: %APP_JAR%
    echo 请先运行编译命令: mvnw.cmd clean package
    pause
    exit /b 1
)

echo [✓] JAR 文件已找到
echo.
echo 启动参数:
echo   - 应用端口: %PORT%
echo   - ROS Bridge URL: %ROSBRIDGE_URL%
echo.
echo ====================================
echo   应用启动中...
echo ====================================
echo.
echo 访问地址: http://localhost:%PORT%
echo.

REM 启动应用
java -jar %APP_JAR% ^
    --server.port=%PORT% ^
    --guidingdog.upstream.rosbridge=%ROSBRIDGE_URL%

pause

