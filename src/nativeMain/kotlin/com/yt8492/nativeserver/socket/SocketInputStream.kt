package com.yt8492.nativeserver.socket

import com.yt8492.nativeserver.stream.InputStream
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.errno
import platform.posix.recv

class SocketInputStream(
    private val socketDescriptor: Int,
) : InputStream {
    override fun read(buffer: ByteArray, length: Int): Int {
        if (length <= 0) {
            return 0
        }
        return buffer.usePinned { pined ->
            val len = recv(
                socketDescriptor,
                pined.addressOf(0),
                length.toULong(),
                0,
            ).toInt()
            if (len < 0) {
                throw SocketException("errno: $errno")
            }
            len
        }
    }
}
