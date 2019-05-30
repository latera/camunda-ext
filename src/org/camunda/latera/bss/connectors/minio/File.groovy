package org.camunda.latera.bss.connectors.minio

import java.io.InputStream
import org.camunda.latera.bss.utils.JSON
import org.camunda.latera.bss.utils.IO
import org.camunda.latera.bss.utils.StringUtil
import org.camunda.latera.bss.utils.Base64Converter
import io.minio.errors.MinioException

trait File {
  List getFiles(String bucketName = defaultBucketName, String prefix = '') {
    def result = []
    try {
      def files = client.listObjects(bucketName ?: defaultBucketName, prefix)
      for (def file : files) {
        def item = file.get()
        result += [
          name         : item.objectName(),
          size         : item.objectSize(),
          isDir        : item.isDir(),
          lastModified : item.lastModified(),
          owner        : item.owner(),
          storageClass : item.storageClass()
        ]
      }
      return result
    } catch (MinioException e) {
      logger.error(e)
    }
    return result
  }

  def getFilesRaw(String bucketName = defaultBucketName, String prefix = '') {
    try {
      return client.listObjects(bucketName ?: defaultBucketName, prefix)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  InputStream getFile(String bucketName, String fileName) {
    try {
      return client.getObject(bucketName ?: defaultBucketName, fileName)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  InputStream getFile(String fileName) {
    return getFile(defaultBucketName, fileName)
  }

  byte[] getFileContent(String bucketName, String fileName) {
    try {
      def stream = getFile(bucketName, fileName)
      if (stream) {
        return IO.getBytes(stream)
      } else {
        return null
      }
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  byte[] getFileContent(String fileName) {
    return getFileContent(defaultBucketName, fileName)
  }

  String getFileBase64Content(String bucketName, String fileName) {
    try {
      def content = getFileContent(bucketName, fileName)
      if (content) {
        return Base64Converter.to(getFileContent(bucketName, fileName))
      } else {
        return null
      }
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  String getFileBase64Content(String fileName) {
    return getFileBase64Content(defaultBucketName, fileName)
  }

  Boolean isFileExists(String bucketName, String fileName) {
    def result = false
    try {
      logger.info("Checking file with name ${fileName} exists in bucket ${bucketName}")
      if (client.statObject(name).length() > 0) {
        result = true
      } else {
        result = false
      }
    } catch (MinioException e) {
      logger.error(e)
    }
    logger.info("  File ${result ? '' : 'does not '}exists")
    return result
  }

  Boolean isFileExists(String fileName) {
    return isFileExists(defaultBucketName, fileName)
  }

  LinkedHashMap getFileMetadata(String bucketName, String fileName) {
    try {
      return getFiles(bucketName ?: defaultBucketName, filename)?.getAt(0)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  LinkedHashMap getFileMetadata(String fileName) {
    return getFileMetadata(defaultBucketName, fileName)
  }

  Boolean createFile(String bucketName, String fileName, def data) {
    // data can be byte[], String (with base464) or InputStream
    try {
      logger.info("Creating file with name ${fileName} in bucket ${bucketName}")
      if (StringUtil.isString(data)) {
        data = Base64Converter.from(data)
      }
      if (data instanceof byte[]) {
        data = IO.getStream(data)
      }
      client.putObject(bucketName, fileName, data)
      logger.info("  File was created successfully!")
      return true
    } catch (MinioException e) {
      logger.error("   Error while creating file")
      logger.error(e)
      return false
    }
  }

  Boolean createFile(String fileName, def data) {
    return createFile(defaultBucketName, fileName, data)
  }

  Boolean putFile(String bucketName, String fileName, def data) {
    return createFile(bucketName, fileName, data)
  }

  Boolean putFile(String fileName, def data) {
    return putFile(defaultBucketName, fileName, data)
  }

  Boolean copyFile(String srcBucketName, String srcFileName, String destBucketName, String destFileName) {
    try {
      if (srcBucketName == destBucketName && srcFileName == destFileName) {
        logger.error("Cannot copy file ${srcFileName} from bucket ${srcBucketName} to itself!")
        return false
      }
      logger.info("Copyng file with name ${srcFileName} from bucket ${srcBucketName} to bucket ${destBucketName} with new name ${destFileName}")
      copyObject(srcBucketName, srcFileName, destBucketName, destFileName)
      logger.info("  File was copied successfully!")
      return true
    } catch (MinioException e) {
      logger.error("   Error while copying file")
      logger.error(e)
      return false
    }

    return copyObject(srcBucketName, srcFileName, destBucketName, destFileName)
  }

  Boolean copyFile(LinkedHashMap input) {
    def params = [
      srcBucketName  : defaultBucketName,
      srcFileName    : null,
      destBucketName : null,
      destFileName   : null
    ] + input
    if (!params.destBucketName) {
      params.destBucketName = params.srcBucketName
    } else if (!params.destFileName) {
      params.destFileName = params.srcFileName
    }
    return copyFile(params.srcBucketName, params.srcFileName, params.destBucketName, params.destFileName)
  }

  Boolean moveFile(String srcBucketName, String srcFileName, String destBucketName, String destFileName) {
    if (srcBucketName == destBucketName && srcFileName == destFileName) {
      logger.error("Trying to move file ${srcFileName} from bucket ${srcBucketName} to itself, nothing to do")
      return true
    }
    def result = copyFile(srcBucketName, srcFileName, destBucketName, destFileName)
    if (result) {
      result = deleteFile(srcBucketName, srcFileName)
    }
    return result
  }

  Boolean moveFile(LinkedHashMap input) {
    def params = [
      srcBucketName  : defaultBucketName,
      srcFileName    : null,
      destBucketName : null,
      destFileName   : null
    ] + input
    if (!params.destBucketName) {
      params.destBucketName = params.srcBucketName
    } else if (!params.destFileName) {
      params.destFileName = params.srcFileName
    }
    return moveFile(params.srcBucketName, params.srcFileName, params.destBucketName, params.destFileName)
  }

  Boolean deleteFile(String bucketName, String fileName) {
    try {
      logger.info("Deleting file with name ${fileName} from bucket ${bucketName}")
      client.removeFile(bucketName, fileName)
      logger.info("  File was deleted successfully!")
      return true
    } catch (MinioException e) {
      logger.error("   Error while deleting file")
      logger.error(e)
      return false
    }
  }

  Boolean deleteFile(String fileName) {
    return deleteFile(defaultBucketName, fileName)
  }

  Boolean deleteFiles(String bucketName, List fileNames) {
    try {
      logger.info("Deleting files ${fileNames} from bucket ${bucketName}")
      client.removeFiles(bucketName, fileNames)
      logger.info("  Files were deleted successfully!")
      return true
    } catch (MinioException e) {
      logger.error("   Error while deleting files")
      logger.error(e)
      return false
    }
  }

  Boolean deleteFile(List fileNames) {
    return deleteFile(defaultBucketName, fileName)
  }
}