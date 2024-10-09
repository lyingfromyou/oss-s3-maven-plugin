package org.max.maven.plugins;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Mojo(name = "upload-to-oss-and-s3")
public class OssAndS3UploadMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "updateToOSS", defaultValue = "false", required = false)
    private Boolean updateToOSS = false;

    @Parameter(property = "ossEndpoint", required = false)
    private String ossEndpoint;

    @Parameter(property = "ossAccessKeyId", required = false)
    private String ossAccessKeyId;

    @Parameter(property = "ossAccessKeySecret", required = false)
    private String ossAccessKeySecret;

    @Parameter(property = "ossBucketName", required = false)
    private String ossBucketName;

    @Parameter(property = "ossObjectName", required = false)
    private String ossObjectName;


    @Parameter(property = "updateToS3", defaultValue = "false", required = false)
    private Boolean updateToS3 = false;

    @Parameter(property = "s3Region", required = false)
    private String s3Region;

    @Parameter(property = "s3AccessKeyId", required = false)
    private String s3AccessKeyId;

    @Parameter(property = "s3AccessKeySecret", required = false)
    private String s3AccessKeySecret;

    @Parameter(property = "s3BucketName", required = false)
    private String s3BucketName;

    @Parameter(property = "s3ObjectName", required = false)
    private String s3ObjectName;

    @Parameter(property = "jarFileName", defaultValue = "target/${project.artifactId}.jar")
    private String jarFileName;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if ((null == updateToOSS || !updateToOSS) && (null == updateToS3 || !updateToS3)) {
                getLog().info("updateToOSS and updateToS3 is false");
                return;
            }

            // 获取项目路径
            String projectPath = project.getBasedir().getAbsolutePath();

            // 构建文件路径
            File jarFile = new File(projectPath, jarFileName);

            // 检查文件是否存在
            if (!jarFile.exists()) {
                throw new MojoExecutionException("Jar file not found: " + jarFile.getAbsolutePath());
            }

            if (null != updateToOSS && updateToOSS) {
                // 上传到OSS
                uploadToOss(jarFile);
            }
            if (null != updateToS3 && updateToS3) {
                // 上传到S3
                uploadToS3(jarFile);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error during upload to OSS and S3", e);
        }
    }

    private void uploadToOss(File jarFile) throws Exception {
        // check 参数
        if (null == ossEndpoint || null == ossAccessKeyId || null == ossAccessKeySecret || null == ossBucketName || null == ossObjectName) {
            throw new MojoExecutionException("ossEndpoint or ossAccessKeyId or ossAccessKeySecret or ossBucketName or ossObjectName is null");
        }

        // 构建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
        try {
            // 上传文件到OSS
            ossClient.putObject(ossBucketName, ossObjectName, jarFile);
            getLog().info("File uploaded to OSS successfully: " + ossObjectName);
        } catch (Exception e) {
            getLog().error("Error uploading to OSS: " + e.getMessage(), e);
        } finally {
            // 关闭OSSClient
            if (null != ossClient) {
                ossClient.shutdown();
            }
        }
    }

    private void uploadToS3(File jarFile) {
        // 构建S3客户端
        S3Client s3Client = S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(s3AccessKeyId, s3AccessKeySecret)
                        )
                )
                .build();

        try {
            // 上传文件到S3
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3ObjectName)
                    .build();
            s3Client.putObject(putOb, RequestBody.fromFile(jarFile));
            getLog().info("File uploaded to S3 successfully: " + s3ObjectName);
        } catch (Exception e) {
            getLog().error("Error uploading to S3: " + e.getMessage(), e);
        } finally {
            if (null != s3Client) {
                s3Client.close();
            }
        }
    }
}