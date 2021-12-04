package com.yt8492.nativeserver

import com.yt8492.nativeserver.socket.ServerSocket

const val MAX_BUF_SIZE = 1024

fun main() {
    val serverSocket = ServerSocket(8080)
    val buffer = ByteArray(MAX_BUF_SIZE)
    while (true) {
        val socket = serverSocket.accept()
        while (true) {
            val length = socket.inputStream.read(buffer, buffer.size)
            if (length == 0) {
                break
            }
            socket.outputStream.write(buffer, length)
        }
        socket.close()
    }
}
