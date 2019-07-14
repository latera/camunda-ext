package org.camunda.latera.bss.connectors

import io.minio.MinioClient
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.delegate.DelegateExecution

import org.camunda.latera.bss.connectors.minio.Bucket
import org.camunda.latera.bss.connectors.minio.File

class Minio implements Bucket, File {
  String url
  String defaultBucketName
  private String accessKey
  private String secretKey
  MinioClient client
  SimpleLogger logger

  Minio(DelegateExecution execution) {
    this.logger    =  new SimpleLogger(execution)
    def ENV        =  System.getenv()

    this.url       =  ENV['MINIO_URL']        ?: execution.getVariable("minioUrl")     ?: 'http://minio:9000'
    this.accessKey =  ENV['MINIO_ACCESS_KEY'] ?: execution.getVariable("minioAccessKey")
    this.secretKey =  ENV['MINIO_SECRET_KEY'] ?: execution.getVariable("minioSecretKey")
    this.defaultBucketName = ENV['MINIO_BUCKET_NAME'] ?: execution.getVariable("minioBucketName")
    this.client    =  new MinioClient(url, accessKey, secretKey)
  }
}