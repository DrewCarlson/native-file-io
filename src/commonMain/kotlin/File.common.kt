package ktfio

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val pathSeparator by lazy { filePathSeparator.toString() }
private fun PathString(vararg parts: String): String = parts.joinToString(pathSeparator)

fun File(parentPathname: String, childName: String, vararg childParts: String) =
    File(PathString(parentPathname, childName, *childParts))

internal const val LINE_BUFFER_SIZE = 1024

expect class File(pathname: String) {
    fun getParent(): String?
    fun getParentFile(): File?

    fun getName(): String

    fun lastModified(): Long
    fun mkdirs(): Boolean
    fun createNewFile(): Boolean
    fun renameTo(file: File): Boolean

    fun isFile(): Boolean
    fun isDirectory(): Boolean

    fun getAbsolutePath(): String

    fun exists(): Boolean
    fun canRead(): Boolean
    fun canWrite(): Boolean

    fun list(): Array<String>
    fun listFiles(): Array<File>

    fun delete(): Boolean
}

/**
 * Create a [File] referring to the [child] file
 * within this [File], assuming it is a directory.
 *
 * NOTE: It is the user's responsibility to validate
 * the receiver is a valid directory.
 */
fun File.nestedFile(child: String): File {
    return File(getAbsolutePath(), child)
}

/**
 * Create a [File] referring to the [sibling] file
 * inside this file's [File.getParent] or null if
 * there is no parent folder.
 */
fun File.siblingFile(sibling: String): File? {
    return getParent()?.let { File(it, sibling) }
}

expect val filePathSeparator: Char

val File.nameWithoutExtension: String
    get() = getName().substringBeforeLast(".")

expect val File.mimeType: String

expect fun File.readBytes(): ByteArray

expect fun File.readText(): String

/**
 * Read each line of UTF8 text from the file.
 *
 * @return A [Sequence] of line strings with line endings trimmed.
 */
expect fun File.readUTF8Lines(): Sequence<String>

expect fun File.appendText(text: String)

expect fun File.writeText(text: String)

fun File.deleteRecursively(): Boolean = walkBottomUp()
    .fold(initial = true) { res, it ->
        (it.delete() || !it.exists()) && res
    }

fun File.getParentFileUnsafe(): File {
    return getParentFile()
        ?: getAbsolutePath()
            .substringBeforeLast(filePathSeparator)
            .run(::File)
}

fun File.validate() = run {
    print("Validating $nameWithoutExtension file...")

    if (!exists()) {
        println(); throw FileNotFoundException(getAbsolutePath(), "No such file or directory!")
    } else if (!canRead()) {
        println(); throw IllegalFileAccess(getAbsolutePath(), "Read access not granted!")
    } else if (!canWrite()) {
        println(); throw IllegalFileAccess(getAbsolutePath(), "Write access not granted!")
    }

    println(" OK!")
}
