package dk.cachet.carp.webservices.datastream.service.impl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Decompresses a GZIP-compressed byte array into a String.
 *
 * @param data The GZIP-compressed byte array to decompress.
 * @return The decompressed string.
 * @throws IOException If an I/O error occurs during decompression.
 */
@Throws(IOException::class)
fun decompressGzip(data: ByteArray): String {
    return try {
        val byteArrayInputStream = ByteArrayInputStream(data)
        val gzipInputStream = GZIPInputStream(byteArrayInputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()

        gzipInputStream.copyTo(byteArrayOutputStream)
        gzipInputStream.close()

        byteArrayOutputStream.toString(Charsets.UTF_8.name())
    } catch (e: IOException) {
        throw IOException("Failed to decompress GZIP data", e)
    }
}

/**
 * Compresses a string into a GZIP-compressed byte array.
 *
 * @param data The string to compress.
 * @return The GZIP-compressed byte array.
 * @throws IOException If an I/O error occurs during compression.
 */
@Throws(IOException::class)
fun compressData(data: String): ByteArray {
    return try {
        val bytes = data.toByteArray(Charsets.UTF_8)
        val byteArrayOutputStream = ByteArrayOutputStream()

        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(bytes)
        }

        byteArrayOutputStream.toByteArray()
    } catch (e: IOException) {
        throw IOException("Failed to compress data", e)
    }
}
