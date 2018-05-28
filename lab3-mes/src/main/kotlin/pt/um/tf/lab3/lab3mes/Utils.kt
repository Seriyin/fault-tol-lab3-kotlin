package pt.um.tf.lab3.lab3mes

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

/**
 * Returns a Base64 Encoding of the SHA-256 hash of the provided data string.
 */
fun getSHA256(data : String) : String {
    val sb = StringBuilder();
    try {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(data.toByteArray(StandardCharsets.UTF_8))
        val byteData = md.digest()
        sb.append(Base64.getEncoder().encodeToString(byteData))
    } catch(e : Exception) {
        e.printStackTrace()
    }
    return sb.toString()
}