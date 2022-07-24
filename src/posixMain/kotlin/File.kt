package ktfio

import kotlinx.cinterop.*
import platform.posix.*

actual class File actual constructor(
    private val pathname: String
) {

    internal val modeRead = "r"
    private val modeAppend = "a"
    private val modeRewrite = "w"

    actual fun getParent(): String? {
        return if (exists()) getAbsolutePath().substringBeforeLast(filePathSeparator) else null
    }

    actual fun getParentFile(): File? {
        return getParent()?.run(::File)
    }

    actual fun getName(): String {
        return if (filePathSeparator in pathname) {
            pathname.split(filePathSeparator).last(String::isNotBlank)
        } else {
            pathname
        }
    }

    actual fun getAbsolutePath(): String {
        return if (!pathname.startsWith(filePathSeparator)) {
            memScoped {
                getcwd(allocArray(FILENAME_MAX), FILENAME_MAX.convert())
                    ?.toKString() + filePathSeparator + pathname
            }
        } else pathname
    }

    actual fun lastModified(): Long = modified(this)

    actual fun mkdirs(): Boolean {
        if (exists()) return false

        if (getParentFile()?.exists() == false) {
            getParentFile()?.mkdirs()
        }

        mkdir(pathname, (S_IRWXU or S_IRWXG or S_IRWXO).convert())
            .ensureUnixCallResult("mkdir") { ret -> ret == 0 }

        return true
    }

    actual fun createNewFile(): Boolean {
        if (exists()) {
            return true
        }

        fopen(pathname, modeRewrite).let { fd ->
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }

        return exists()
    }

    actual fun renameTo(file: File): Boolean {
        return rename(getAbsolutePath(), file.getAbsolutePath()) == 0
    }

    actual fun isFile(): Boolean {
        if (!exists()) {
            return false
        }

        return memScoped {
            val result = alloc<stat>()

            stat(pathname, result.ptr)
                .ensureUnixCallResult("stat") { ret -> ret == 0 }

            return@memScoped result.checkFileIs(S_IFREG)
        }
    }

    actual fun isDirectory(): Boolean {
        if (!exists()) {
            return false
        }

        return memScoped {
            val result = alloc<stat>()

            stat(pathname, result.ptr)
                .ensureUnixCallResult("stat") { ret -> ret == 0 }

            return@memScoped result.checkFileIs(S_IFDIR)
        }
    }

    actual fun list(): Array<String> = memScoped {
        val dir = opendir(pathname)
            ?: return emptyArray()

        val result = ArrayList<String>()

        do {
            val record = readdir(dir)

            record?.pointed?.let { entity: dirent ->
                result.add(entity.d_name.toKString())
            }
        } while (record != NULL)

        closedir(dir).ensureUnixCallResult("closedir") { ret -> ret == 0 }

        return result.filter { name -> name !in arrayOf(".", "..") }
            .toTypedArray()
    }

    actual fun listFiles(): Array<File> {
        val thisPath = getAbsolutePath().let { path ->
            if (!path.endsWith(filePathSeparator)) {
                path + filePathSeparator
            } else path
        }
        return list()
            .map { name -> File(thisPath + name) }
            .toTypedArray()
    }

    actual fun delete(): Boolean {
        if (isDirectory()) {
            return rmdir(pathname) == 0 // do not throw errors here
        }

        return unlink(pathname) == 0
    }

    actual fun exists(): Boolean {
        return access(pathname, F_OK) != -1
    }

    actual fun canRead(): Boolean {
        return access(getAbsolutePath(), R_OK) != -1
    }

    actual fun canWrite(): Boolean {
        return access(getAbsolutePath(), W_OK) != -1
    }

    internal fun writeBytes(bytes: ByteArray, mode: Int, size: ULong = ULong.MAX_VALUE, elemSize: ULong = 1U) {
        val fd = fopen(getAbsolutePath(), if (mode and O_APPEND == O_APPEND) modeAppend else modeRewrite)
        try {
            memScoped {
                bytes.usePinned { pinnedBytes ->
                    val bytesSize: ULong = if (size != ULong.MAX_VALUE) size else pinnedBytes.get().size.convert()
                    fwrite(pinnedBytes.addressOf(0), elemSize, bytesSize, fd)
                        .ensureUnixCallResult("fwrite") { ret -> ret == bytesSize }
                }
            }
        } finally {
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }
    }

    actual override fun equals(other: Any?): Boolean {
        return when (other) {
            is File -> other.pathname == pathname
            else -> false
        }
    }

    actual override fun hashCode(): Int {
        var hash = 17
        hash = hash * 31 + pathname.hashCode()
        hash = hash * 31 + File::class.hashCode()
        return hash
    }

    override fun toString(): String {
        return "File {\n" +
            "path=${getAbsolutePath()}\n" +
            "name=${getName()}\n" +
            "exists=${exists()}\n" +
            "canRead=${canRead()}\n" +
            "canWrite=${canWrite()}\n" +
            "isFile=${isFile()}\n" +
            "isDirectory=${isDirectory()}\n" +
            "lastModified=${lastModified()}\n" +
            (if (isDirectory()) "files=[${listFiles().joinToString()}]" else "") +
            "}"
    }
}

internal expect fun modified(file: File): Long

internal expect fun stat.checkFileIs(flag: Int): Boolean

internal expect fun mkdir(path: String, mode: UInt): Int

internal expect fun opendir(path: String): CPointer<out CPointed>?

internal expect fun readdir(dir: CPointer<out CPointed>): CPointer<dirent>?

internal expect fun closedir(dir: CPointer<out CPointed>): Int

@SharedImmutable
actual val filePathSeparator by lazy { if (Platform.osFamily == OsFamily.WINDOWS) '\\' else '/' }

// todo determine mimeType on file extension; see jdk mappings
actual val File.mimeType: String
    get() = ""

actual fun File.readBytes(): ByteArray {
    val fd = fopen(getAbsolutePath(), modeRead)
    try {
        memScoped {
            fseek(fd, 0, SEEK_END)
            val size = ftell(fd).convert<Int>()
            fseek(fd, 0, SEEK_SET)

            return ByteArray(size + 1).also { buffer ->
                fread(buffer.refTo(0), 1UL, size.convert(), fd)
                    .ensureUnixCallResult("fread") { ret -> ret > 0U }
            }
        }
    } finally {
        fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
    }
}

actual fun File.readUTF8Lines(): Sequence<String> = sequence {
    val handle = fopen(getAbsolutePath(), "r") ?: return@sequence
    try {
        memScoped {
            val overflow = StringBuilder()
            val lineCstr = allocArray<ByteVar>(LINE_BUFFER_SIZE)
            while (true) {
                fgets(lineCstr, LINE_BUFFER_SIZE, handle) ?: break // read the line or end loop if null/EOF
                val lineKstr = lineCstr.toKStringFromUtf8().trimEnd('\n')
                if (lineCstr[LINE_BUFFER_SIZE - 1] == 0.toByte() && lineCstr[lineKstr.length] != '\n'.code.toByte()) {
                    overflow.append(lineKstr) // Read incomplete line, overflow is required
                } else if (overflow.isBlank()) {
                    yield(lineKstr) // Whole line was read to buffer and no overflow to append
                } else {
                    // Remaining line loaded, join with overflow and yield a complete line
                    overflow.append(lineKstr)
                    yield(overflow.toString())
                    overflow.clear()
                }
            }
        }
    } finally {
        fclose(handle)
    }
}

actual fun File.writeBytes(bytes: ByteArray) {
    // no need to use pinning or memscope, cause it's inside the method already does
    writeBytes(bytes, O_RDWR, bytes.size.convert(), Byte.SIZE_BYTES.convert())
}

actual fun File.appendBytes(bytes: ByteArray) {
    writeBytes(bytes, O_APPEND, bytes.size.convert(), Byte.SIZE_BYTES.convert())
}

actual fun File.readText(): String {
    return readBytes().toKString()
}

actual fun File.appendText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_APPEND, strlen(text))
}

actual fun File.writeText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_CREAT, strlen(text))
}
