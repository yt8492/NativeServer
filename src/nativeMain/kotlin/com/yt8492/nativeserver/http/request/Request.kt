package com.yt8492.nativeserver.http.request

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.socket.SocketInputStream
import kotlinx.cinterop.toKString

class Request(
    val requestLine: RequestLine,
    val headers: Headers,
    val body: ByteArray,
) {
    companion object {
        private const val MAX_BUF_SIZE = 1024 * 64

        fun from(inputStream: SocketInputStream): Request {
            val requestLine = readRequestLine(inputStream)
            val headers = readHeaders(inputStream)
            val contentLength = headers["Content-Length"]?.toIntOrNull()
            val transferEncoding = headers["Transfer-Encoding"]
            val body = when {
                contentLength != null -> {
                    readBody(inputStream, contentLength)
                }
                transferEncoding != null -> {
                    // TODO: ちゃんと対応する
                    readBody(inputStream)
                }
                else -> {
                    ByteArray(0)
                }
            }
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
        ): Headers {
            val headers = Headers()
            var line = readUntilCRLF(inputStream)
            while (line != "") {
                val (name, value) = line.split(":").map {
                    it.trim()
                }
                headers.add(name, value)
                line = readUntilCRLF(inputStream)
            }
            return headers
        }

        private fun readBody(
            inputStream: SocketInputStream,
            contentLength: Int,
        ): ByteArray {
            val body = ByteArray(contentLength)
            inputStream.read(body, contentLength)
            return body
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
                if (len == MAX_BUF_SIZE) {
                    body += buf
                } else {
                    body += buf.copyOf(len)
                    break
                }
                len = inputStream.read(buf, buf.size)
            }
            return body
        }
    }
}
