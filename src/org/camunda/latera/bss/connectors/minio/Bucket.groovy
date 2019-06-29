package org.camunda.latera.bss.connectors.minio

import org.camunda.latera.bss.utils.JSON
import io.minio.errors.MinioException

trait Bucket {
  List getBuckets() {
    List result = []
    try {
      result = client.listBuckets()
    } catch (MinioException e) {
      logger.error(e)
    }
    return result
  }

  Boolean isBucketExists(CharSequence name = defaultBucketName) {
    Boolean result = false
    try {
      logger.info("Checking bucket with name ${name} exists")
      result = client.bucketExists(name)
      logger.info("  Bucket ${result ? '' : 'does not '}exists")
    } catch (MinioException e) {
      logger.error(e)
    }
    return result
  }

  Map getBucketPolicy(CharSequence name = defaultBucketName) {
    LinkedHashMap result = null
    try {
      result = JSON.from(client.getBucketPolicy(name))
    } catch (Exception e) {
      logger.error(e)
    }
    return result
  }

  Boolean createBucket(CharSequence name = defaultBucketName) {
    try {
      logger.info("Creating bucket with name ${name}")
      client.makeBucket(name)
      logger.info("  Bucket was created successfully!")
      return true
    } catch (Exception e) {
      logger.error("   Error while creating bucket")
      logger.error(e)
      return false
    }
  }

  Boolean putBucket(CharSequence name = defaultBucketName) {
    if (!isBucketExists) {
      return createBucket(name)
    }
    return true
  }

  Boolean ensureBucketExists(CharSequence name = defaultBucketName) {
    return putBucket(name)
  }

  Boolean updateBucketPolicy(CharSequence name = defaultBucketName, Map policy) {
    try {
      logger.info("Updating bucket ${name} policy with data ${policy}")
      client.setBucketPolicy(JSON.to(policy))
      logger.info("  Bucket policy was updated successfully!")
      return true
    } catch (Exception e) {
      logger.error("   Error while updating bucket policy ${policy}")
      logger.error(e)
      return false
    }
  }

  Boolean deleteBucket(CharSequence name = defaultBucketName) {
    try {
      if (isBucketExists) {
        logger.info("Deleting bucket name ${name}")
        client.removeBucket(name)
        logger.info("  Bucket policy was deleted successfully!")
        return true
      }
      logger.info("Bucket with name ${name} does not exists")
      return true
    } catch (Exception e) {
      logger.error("   Error while deleting bucket")
      logger.error(e)
      return false
    }
  }
}