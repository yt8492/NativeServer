package com.yt8492.nativeserver.socket

import platform.posix.close

class Socket(
    private val host: String,
    private val port: Int,
    private val socketDescriptor: Int,
) {
    val inputStream by lazy {
        SocketInputStream(socketDescriptor)
    }

    val outputStream by lazy {
        SocketOutputStream(socketDescriptor)
    }

    fun close() {
        close(socketDescriptor)
    }
}
