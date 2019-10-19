package org.camunda.latera.bss.connectors.minio

import static org.camunda.latera.bss.utils.IO.getStream
import static org.camunda.latera.bss.utils.IO.getBytes
import static org.camunda.latera.bss.utils.Base64Converter.to as toBase64
import static org.camunda.latera.bss.utils.Base64Converter.from as fromBase64
import static org.camunda.latera.bss.utils.StringUtil.isString
import io.minio.errors.MinioException

trait File {
  List getFiles(CharSequence bucketName = defaultBucketName, CharSequence prefix = '') {
    List result = []
    try {
      List files = client.listObjects(bucketName ?: defaultBucketName, prefix)
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

  def getFilesRaw(CharSequence bucketName = defaultBucketName, CharSequence prefix = '') {
    try {
      return client.listObjects(bucketName ?: defaultBucketName, prefix)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  InputStream getFile(CharSequence bucketName, CharSequence fileName) {
    try {
      return client.getObject(bucketName ?: defaultBucketName, fileName)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  InputStream getFile(CharSequence fileName) {
    return getFile(defaultBucketName, fileName)
  }

  byte[] getFileContent(CharSequence bucketName, CharSequence fileName) {
    try {
      def stream = getFile(bucketName, fileName)
      if (stream) {
        return getBytes(stream)
      } else {
        return null
      }
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  byte[] getFileContent(CharSequence fileName) {
    return getFileContent(defaultBucketName, fileName)
  }

  String getFileBase64Content(CharSequence bucketName, CharSequence fileName) {
    try {
      def content = getFileContent(bucketName, fileName)
      if (content) {
        return toBase64(content)
      } else {
        return null
      }
    } catch (Exception e) {
      logger.error(e)
      return null
    }
  }

  String getFileBase64Content(CharSequence fileName) {
    return getFileBase64Content(defaultBucketName, fileName)
  }

  Boolean isFileExists(CharSequence bucketName, CharSequence fileName) {
    Boolean result = false
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

  Boolean isFileExists(CharSequence fileName) {
    return isFileExists(defaultBucketName, fileName)
  }

  Map getFileMetadata(CharSequence bucketName, CharSequence fileName) {
    try {
      return getFiles(bucketName ?: defaultBucketName, fileName)?.getAt(0)
    } catch (MinioException e) {
      logger.error(e)
      return null
    }
  }

  Map getFileMetadata(CharSequence fileName) {
    return getFileMetadata(defaultBucketName, fileName)
  }

  Boolean createFile(CharSequence bucketName, CharSequence fileName, def data) {
    // data can be byte[], String (with base464) or InputStream
    try {
      logger.info("Creating file with name ${fileName} in bucket ${bucketName}")
      if (isString(data)) {
        data = fromBase64(data)
      }
      if (data instanceof byte[]) {
        data = getStream(data)
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

  Boolean createFile(CharSequence fileName, def data) {
    return createFile(defaultBucketName, fileName, data)
  }

  Boolean putFile(CharSequence bucketName, CharSequence fileName, def data) {
    return createFile(bucketName, fileName, data)
  }

  Boolean putFile(CharSequence fileName, def data) {
    return putFile(defaultBucketName, fileName, data)
  }

  Boolean copyFile(CharSequence srcBucketName, CharSequence srcFileName, CharSequence destBucketName, CharSequence destFileName) {
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

  Boolean copyFile(Map input) {
    LinkedHashMap params = [
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

  Boolean moveFile(CharSequence srcBucketName, CharSequence srcFileName, CharSequence destBucketName, CharSequence destFileName) {
    if (srcBucketName == destBucketName && srcFileName == destFileName) {
      logger.error("Trying to move file ${srcFileName} from bucket ${srcBucketName} to itself, nothing to do")
      return true
    }
    Boolean result = copyFile(srcBucketName, srcFileName, destBucketName, destFileName)
    if (result) {
      result = deleteFile(srcBucketName, srcFileName)
    }
    return result
  }

  Boolean moveFile(Map input) {
    LinkedHashMap params = [
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

  Boolean deleteFile(CharSequence bucketName, CharSequence fileName) {
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

  Boolean deleteFile(CharSequence fileName) {
    return deleteFile(defaultBucketName, fileName)
  }

  Boolean deleteFiles(CharSequence bucketName, List fileNames) {
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
    return deleteFile(defaultBucketName, fileNames)
  }
}