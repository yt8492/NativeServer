package com.yt8492.nativeserver.http.response

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.socket.SocketOutputStream
import com.yt8492.nativeserver.stream.OutputStream

class Response(
    val statusLine: StatusLine,
    val headers: Headers,
    val body: ByteArray,
) {
    fun writeTo(
        outputStream: SocketOutputStream,
    ) {
        writeStatusLine(outputStream)
        writeHeaders(outputStream)
        writeBody(outputStream)
    }

    private fun writeStatusLine(
        outputStream: SocketOutputStream,
    ) {
        outputStream.write(statusLine.httpVersion)
        outputStream.write(" ")
        outputStream.write(statusLine.statusCode.toString())
        outputStream.write(" ")
        outputStream.write(statusLine.reasonPhrase)
        outputStream.write("\r\n")
    }

    private fun writeHeaders(
        outputStream: SocketOutputStream,
    ) {
        headers.forEach { header ->
            outputStream.write("${header.name}:${header.value}\r\n")
        }
        outputStream.write("\r\n")
    }

    private fun writeBody(
        outputStream: SocketOutputStream,
    ) {
        outputStream.write(body, body.size)
    }
}

fun OutputStream.write(text: String): Int {
    val byteArray = text.encodeToByteArray()
    return write(byteArray, byteArray.size)
}
