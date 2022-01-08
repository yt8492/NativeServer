package com.yt8492.nativeserver.http.request

import com.yt8492.nativeserver.http.Header
import com.yt8492.nativeserver.socket.SocketInputStream
import kotlinx.cinterop.toKString

class Request(
    val requestLine: RequestLine,
    val headers: List<Header>,
    val body: ByteArray,
) {
    companion object {
        private const val MAX_BUF_SIZE = 1024 * 64

        fun from(inputStream: SocketInputStream): Request {
            val requestLine = readRequestLine(inputStream)
            val headers = readHeaders(inputStream)
            val body = readBody(inputStream)
            return Request(
                requestLine = requestLine,
                headers = headers,
                body = body,
            )
        }

        private fun readRequestLine(
            inputStream: SocketInputStream,
        ): RequestLine {
            val rawMethod = readUntilSpace(inputStream)
            val method = Method.from(rawMethod)
            val uri = readUntilSpace(inputStream)
            val httpVersion = readUntilCRLF(inputStream)

            return RequestLine(
                method = method,
                uri = uri,
                httpVersion = httpVersion,
            )
        }

        private fun readHeaders(
            inputStream: SocketInputStream,
        ): List<Header> {
            val headers = mutableListOf<Header>()
            var line = readUntilCRLF(inputStream)
            while (line != "") {
                val (name, value) = line.split(":").map {
                    it.trim()
                }
                val header = Header(name, value)
                headers.add(header)
                line = readUntilCRLF(inputStream)
            }
            return headers
        }

        private fun readBody(
            inputStream: SocketInputStream,
        ): ByteArray {
            return readAllBytes(inputStream)
        }

        private fun readUntilSpace(
            inputStream: SocketInputStream,
        ): String {
            val stringBuffer = StringBuilder()
            while (true) {
                val buf = ByteArray(1)
                inputStream.read(buf, buf.size)
                val temp = buf.toKString()
                if (temp == " ") {
                    break
                }
                stringBuffer.append(temp)
            }
            return stringBuffer.toString()
        }

        private fun readUntilCRLF(
            inputStream: SocketInputStream,
        ): String {
            val stringBuffer = StringBuilder()
            while (true) {
                val buf = ByteArray(1)
                inputStream.read(buf, buf.size)
                val temp = buf.toKString()
                if (temp == "\r") {
                    inputStream.read(buf, buf.size)
                    break
                }
                if (temp == "\n") {
                    break
                }
                stringBuffer.append(buf.toKString())
            }
            return stringBuffer.toString()
        }

        private fun readAllBytes(
            inputStream: SocketInputStream,
        ): ByteArray {
            val buf = ByteArray(MAX_BUF_SIZE)
            var body = ByteArray(0)
            var len = inputStream.read(buf, buf.size)
            while (len != 0) {
                body += if (len == MAX_BUF_SIZE) {
                    buf
                } else {
                    buf.copyOf(len)
                }
                len = inputStream.read(buf, buf.size)
            }
            return body
        }
    }
}
