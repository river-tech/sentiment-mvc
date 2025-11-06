#!/bin/zsh

# ===============================================
# 🚀 JSP/Servlet Auto Deploy Script for Tomcat 10
# ===============================================

APP_NAME="sentiment-mvc"
TOMCAT_HOME="/opt/homebrew/opt/tomcat@10/libexec"
WAR_FILE="target/${APP_NAME}.war"

echo ""
echo "======================================"
echo "🚀 Building WAR for ${APP_NAME}"
echo "======================================"
mvn -DskipTests clean package

# kiểm tra build thành công
if [ $? -ne 0 ]; then
  echo "❌ Build thất bại. Kiểm tra lỗi Maven!"
  exit 1
fi

echo ""
echo "======================================"
echo "🧹 Cleaning old deployment..."
echo "======================================"
rm -rf "${TOMCAT_HOME}/webapps/${APP_NAME}" "${TOMCAT_HOME}/webapps/${APP_NAME}.war"

echo ""
echo "======================================"
echo "📦 Copying new WAR to Tomcat..."
echo "======================================"
cp "${WAR_FILE}" "${TOMCAT_HOME}/webapps/"

if [ $? -ne 0 ]; then
  echo "❌ Không thể copy WAR file. Kiểm tra quyền truy cập Tomcat folder."
  exit 1
fi

echo ""
echo "======================================"
echo "🔁 Restarting Tomcat service..."
echo "======================================"
brew services restart tomcat@10

echo ""
echo "======================================"
echo "✅ Deployment hoàn tất!"
echo "Truy cập: http://localhost:8080/${APP_NAME}/"
echo "======================================"