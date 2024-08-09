package dk.cachet.carp.webservices.data.service.impl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun decompressGzip(data: ByteArray): String {
    val byteArrayInputStream = ByteArrayInputStream(data)
    val gzipInputStream = GZIPInputStream(byteArrayInputStream)
    val byteArrayOutputStream = ByteArrayOutputStream()

    gzipInputStream.copyTo(byteArrayOutputStream)
    gzipInputStream.close()
    return byteArrayOutputStream.toString(Charsets.UTF_8)
}

fun compressData(data: String): ByteArray {
    val bytes = data.toByteArray(Charsets.UTF_8)
    val byteArrayOutputStream = ByteArrayOutputStream()
    GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
        gzipOutputStream.write(bytes)
    }
    return byteArrayOutputStream.toByteArray()
}
