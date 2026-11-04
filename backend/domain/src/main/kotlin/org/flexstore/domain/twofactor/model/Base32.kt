package org.flexstore.domain.twofactor.model

private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
private const val GROUP_BITS = 5
private const val BYTE_BITS = 8
private const val BYTE_MASK = 0xff
private const val GROUP_MASK = 0x1f

// RFC 4648 Base32, without padding: the authenticator apps read and type the secret in this alphabet
object Base32 {

    fun encode(bytes: ByteArray): String {
        val encoded = StringBuilder()
        var buffer = 0
        var bitsInBuffer = 0
        for (byte in bytes) {
            buffer = (buffer shl BYTE_BITS) or (byte.toInt() and BYTE_MASK)
            bitsInBuffer += BYTE_BITS
            while (bitsInBuffer >= GROUP_BITS) {
                bitsInBuffer -= GROUP_BITS
                encoded.append(ALPHABET[(buffer shr bitsInBuffer) and GROUP_MASK])
            }
        }
        if (bitsInBuffer > 0) {
            encoded.append(ALPHABET[(buffer shl (GROUP_BITS - bitsInBuffer)) and GROUP_MASK])
        }
        return encoded.toString()
    }

    fun decode(base32: String): ByteArray {
        val cleaned = base32.trim().uppercase().trimEnd('=')
        val decoded = ArrayList<Byte>()
        var buffer = 0
        var bitsInBuffer = 0
        for (letter in cleaned) {
            buffer = (buffer shl GROUP_BITS) or ALPHABET.indexOf(letter)
            bitsInBuffer += GROUP_BITS
            if (bitsInBuffer >= BYTE_BITS) {
                bitsInBuffer -= BYTE_BITS
                decoded.add(((buffer shr bitsInBuffer) and BYTE_MASK).toByte())
            }
        }
        return decoded.toByteArray()
    }
}
