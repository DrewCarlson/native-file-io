package ktfio

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URLConnection
import java.nio.charset.Charset
import kotlin.io.appendBytes as kAppendBytes
import kotlin.io.readBytes as kReadBytes
import kotlin.io.writeBytes as kWriteBytes

actual typealias File = java.io.File

actual val filePathSeparator by lazy { File.separatorChar }

actual val File.mimeType: String
    get() = URLConnection.guessContentTypeFromName(name)

actual fun File.readBytes() = kReadBytes()

actual fun File.readText() = readText(Charset.defaultCharset())

actual fun File.readUTF8Lines(): Sequence<String> {
    return sequence {
        BufferedReader(InputStreamReader(FileInputStream(this@readUTF8Lines), Charsets.UTF_8))
            .use { reader -> while (reader.ready()) yield(reader.readLine()) }
    }
}

actual fun File.writeBytes(bytes: ByteArray) = kWriteBytes(bytes)

actual fun File.appendBytes(bytes: ByteArray) = kAppendBytes(bytes)

actual fun File.appendText(text: String) = appendText(text, Charset.defaultCharset())

actual fun File.writeText(text: String) = writeText(text, Charset.defaultCharset())
