package io.powerrangers.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException
import java.util.UUID

@Service
class S3Service(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String,
) {

    @Throws(IOException::class)
    fun upload(file: MultipartFile): String {
        val fileName = "${UUID.randomUUID()}-${file.originalFilename}"

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.contentType)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))

        return "https://$bucket.s3.$region.amazonaws.com/$fileName"
    }

    @Throws(IOException::class)
    fun delete(imagePath: String?) {
        if (imagePath.isNullOrBlank()) return

        val key = extractKeyFromUrl(imagePath)

        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    private fun extractKeyFromUrl(imagePath: String): String {
        val baseUrl = "https://$bucket.s3.$region.amazonaws.com/"
        return imagePath.replace(baseUrl, "")
    }
}
