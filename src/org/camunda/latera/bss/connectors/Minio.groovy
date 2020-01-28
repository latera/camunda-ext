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
    this.logger = new SimpleLogger(execution)
    def ENV     = System.getenv()

    this.url               = execution.getVariable('minioUrl')        ?: ENV['MINIO_URL'] ?: 'http://minio:9000'
    this.accessKey         = execution.getVariable('minioAccessKey')  ?: ENV['MINIO_ACCESS_KEY']
    this.secretKey         = execution.getVariable('minioSecretKey')  ?: ENV['MINIO_SECRET_KEY']
    this.defaultBucketName = execution.getVariable('minioBucketName') ?: ENV['MINIO_BUCKET_NAME']

    this.client = new MinioClient(url, accessKey, secretKey)
  }
}