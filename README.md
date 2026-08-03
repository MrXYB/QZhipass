## Qzhipass-Backend

<div align="center">

Qzhipass is an Enterprise AI Orchestration Platform

### **Codacy 代码质量报告：**

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/596b79d10d7f4f549f024d62493ee939)](https://app.codacy.com/gh/NeonAngelThreads/QZhipass/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

</div>

 > ![NOTE]
>
> **System Requirements:**
>
> - Java 21
> - Redis 7.0.15
> - MySQL 8.0.46
> - Nginx 1.24.0

### Build
```bash
./mvnw clean package -DskipTests
```
### Run
```bash
java -jar qintelipass-0.0.1-SNAPSHOT.jar
```
### Run with JVM Options
```bash
java -DDATABASE_PASSWORD=xxx -DDATABASE_URL=xxx -DDATABASE_USERNAME=xxx -DREDIS_HOST=xxx -DREDIS_PASSWORD=xxx -jar target/qintelipass-0.0.1-SNAPSHOT.jar
```

### Run with Environment Variables
```bash
set DATABASE_PASSWORD=xxx
set DATABASE_URL=xxx
set DATABASE_USERNAME=xxx
set REDIS_PASSWORD=xxx

java -jar target/qintelipass-0.0.1-SNAPSHOT.jar
```


## Encountered Bug?
Please feel confident to issue questions! Detailed descriptions of bugs is appreciated.
