#!/bin/zsh

# ===============================================
# 🚀 JSP/Servlet Auto Deploy Script for Tomcat 10
# ===============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_NAME="sentiment-mvc"
TOMCAT_HOME="$HOME/sentiment-tomcat"   # Bạn nhớ thay theo Tomcat riêng của bạn
WAR_FILE="${SCRIPT_DIR}/target/${APP_NAME}.war"
DEFAULT_JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

# ===============================================
# ☕ Java setup (JDK 17 required)
# ===============================================
echo ""
echo "======================================"
echo "☕ Checking JAVA_HOME (using JDK 17)"
echo "======================================"

export JAVA_HOME="${DEFAULT_JAVA_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "Using $(java -version 2>&1 | head -n 1)"

# ===============================================
# 🚀 Build WAR
# ===============================================
echo ""
echo "======================================"
echo "🚀 Building WAR package"
echo "======================================"

mvn -DskipTests clean package || {
  echo "❌ Build failed!"
  exit 1
}

# ===============================================
# 🧹 Clean old deployment
# ===============================================
echo ""
echo "======================================"
echo "🧹 Cleaning old WAR in Tomcat..."
echo "======================================"

rm -rf "${TOMCAT_HOME}/webapps/${APP_NAME}" "${TOMCAT_HOME}/webapps/${APP_NAME}.war"

# ===============================================
# 📦 Copy new WAR
# ===============================================
echo ""
echo "======================================"
echo "📦 Deploying new WAR to Tomcat..."
echo "======================================"

cp "${WAR_FILE}" "${TOMCAT_HOME}/webapps/"

# ===============================================
# 🔁 Restart YOUR Tomcat instance (NOT brew one)
# ===============================================
echo ""
echo "======================================"
echo "🔁 Restarting standalone Tomcat..."
echo "======================================"

# Stop Tomcat riêng
"${TOMCAT_HOME}/bin/shutdown.sh" >/dev/null 2>&1 || true
sleep 1

# Start lại Tomcat riêng
"${TOMCAT_HOME}/bin/startup.sh"

# ===============================================
# 🎉 Output
# ===============================================
echo ""
echo "======================================"
echo "🎉 Deployment Completed!"
echo "🌐 App URL: http://localhost:8080/${APP_NAME}/"
echo "======================================"