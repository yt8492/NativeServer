package com.yt8492.nativeserver.socket

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.errno
import platform.posix.recv

class SocketInputStream(
    private val socketDescriptor: Int,
) {
    fun read(buffer: ByteArray, length: Int): Int {
        if (length <= 0) {
            return 0
        }
        return memScoped {
            val buf = allocArray<ByteVar>(buffer.size)
            val len = recv(socketDescriptor, buf, length.toULong(), 0).toInt()
            if (len < 0) {
                throw SocketException("errno: $errno")
            }
            repeat(len) {
                buffer[it] = buf[it]
            }
            len
        }
    }
}
