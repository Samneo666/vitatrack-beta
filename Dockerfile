# 使用 Maven 官方映像檔先將專案打包
FROM maven:3.9.6-eclipse-temurin-11 AS builder
WORKDIR /app
COPY pom.xml .
# 第一次先下載依賴
RUN mvn dependency:go-offline

# 複製程式碼並打包 (產生 war 檔)
COPY src ./src
RUN mvn clean package -DskipTests

# 使用官方 Tomcat 映像檔來執行
FROM tomcat:9.0-jdk11
# 移除原本 Tomcat 預設的首頁
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# 把打包好的 war 檔放進 webapps，並將名稱改成 ROOT.war (這樣就會綁定在根目錄 / )
COPY --from=builder /app/target/vitatrack.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
# 支援 Railway 動態注入的 $PORT 環境變數（預設 8080）
CMD sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml && catalina.sh run
