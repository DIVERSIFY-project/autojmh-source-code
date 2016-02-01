<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>auto-generated-benchmarks</artifactId>
    <version>1.0</version>

    <dependencies>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-core</artifactId>
            <version>1.11-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>1.11-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>

    <#list dependencies as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#if dep.version??><version>${dep.version}</version></#if>
        </dependency>
    </#list>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
        </dependency>

        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.8.11.1</version>
        </dependency>

    </dependencies>

</project>