### 此插件用于上传jar包到阿里云oss和aws s3
> 用于部署多个服务器, 避免多个服务器重复打包

在Maven项目pom中引入插件

````
<build>
    <plugins>
        <plugin>
            <groupId>org.max.maven.plugins</groupId>
            <artifactId>oss-s3-maven-plugin</artifactId>
            <version>1.0</version>
            <configuration>
                <!-- OSS 配置, 系统环境变量-->
                <ossEndpoint>${env.OSS_ENDPOINT}</ossEndpoint>
                <ossAccessKeyId>${env.OSS_ACCESS_KEY_ID}</ossAccessKeyId>
                <ossAccessKeySecret>${env.OSS_ACCESS_KEY_SECRET}</ossAccessKeySecret>
                <ossBucketName>${env.OSS_BUCKET_NAME}</ossBucketName>
                <!-- OSS 上传文件名 -->
                <ossObjectName>${project.artifactId}-${project.version}.jar</ossObjectName>
                <!-- 是否上传到 OSS -->
                <updateToOSS>true</updateToOSS>

                <!-- s3 配置, 系统环境变量-->
                <s3Region>${env.S3_REGION}</s3Region>
                <s3AccessKeyId>${env.S3_ACCESS_KEY_ID}</s3AccessKeyId>
                <s3AccessKeySecret>${env.S3_ACCESS_KEY_SECRET}</s3AccessKeySecret>
                <s3BucketName>${env.S3_BUCKET_NAME}</s3BucketName>
                <!-- S3 上传文件名 -->
                <s3ObjectName>${project.artifactId}-${project.version}.jar</s3ObjectName>
                <!-- 是否上传到 S3 -->
                <updateToS3>true</updateToS3>
            </configuration>
            <executions>
                <execution>
                    <phase>install</phase>
                    <goals>
                        <goal>upload-to-oss-and-s3</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
````
> 自行配置系统环境变量, 或者直接替换 ${evn.xxx} 为具体值

在项目根目录中执行maven打包命令

    mvn clean install -DskipTests