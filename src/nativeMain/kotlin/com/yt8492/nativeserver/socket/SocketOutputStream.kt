package com.yt8492.nativeserver.socket

import com.yt8492.nativeserver.stream.OutputStream
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.errno
import platform.posix.send

class SocketOutputStream(
    private val socketDescriptor: Int,
) : OutputStream {
    override fun write(buffer: ByteArray, length: Int): Int {
        if (length <= 0) {
            return 0
        }
        buffer.usePinned { pinned ->
            val len = send(
                socketDescriptor,
                pinned.addressOf(0),
                length.toULong(),
                0,
            ).toInt()
            if (len < 0) {
                throw SocketException("errno: $errno")
            }
            return len
        }
    }
}
