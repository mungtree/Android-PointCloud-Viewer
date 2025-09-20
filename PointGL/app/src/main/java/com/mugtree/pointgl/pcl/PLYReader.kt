package com.mugtree.pointgl.pcl

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.charset.Charset
import kotlin.math.ceil
import kotlin.math.min

enum class PLYDataPackType {
    ASCII,
    BINARY_LITTLE_ENDIAN,
    UNKNOWN
}

class PLYReader {
    companion object {
        var MAX_VERTICES = 1000000
        var READ_BUFFER_SIZE = 128
    }
    var MAX_HEADER_LINES = 128
    val VERTEX_COLORED_SIZE = (3 * 4) + (3 * 1)
    val VERTEX_NO_COLOR = (3 * 4)

    var numVertices = 0
    var hasColor = false
    var dataFormatType = PLYDataPackType.UNKNOWN
    var headerBytes = 0

    var mugPCD: MugPointCloudData? = null

    fun readPLYFile(inputStream: InputStream): MugPointCloudData? {
        val bis = BufferedInputStream(inputStream)
        readHeader(bis)

        when (dataFormatType) {
            PLYDataPackType.ASCII -> { throw UnsupportedOperationException("ASCII Not Supported") }
            PLYDataPackType.BINARY_LITTLE_ENDIAN -> {
                readBinaryLittleEndianData(bis)
            }
            PLYDataPackType.UNKNOWN -> { throw UnsupportedOperationException("Unknown Data Type") }
        }
        return mugPCD
    }

    /**
     * Modifies
     * numVertices
     * hasColor
     */
    private fun readHeader(bis: BufferedInputStream) {
        var i = 0
        while (i++ < MAX_HEADER_LINES) {
            val line = bis.readLine() ?: break
            headerBytes += line.toByteArray().size
            if (line.startsWith("format")) {
                if (line.contains("binary_little_endian")) {
                    dataFormatType = PLYDataPackType.BINARY_LITTLE_ENDIAN
                } else if (line.contains("ascii")) {
                    dataFormatType = PLYDataPackType.ASCII
                }
            }
            if (line.startsWith("element vertex")) {
                numVertices = line.split(" ")[2].toInt()
            }
            else if (line.startsWith("property uchar") && line.contains("red")) {
                hasColor = true
            } else if (line.startsWith("end_header")) break
        }
    }

    private fun readBinaryLittleEndianData(bis: BufferedInputStream) {
        val vertexStride = if (hasColor) VERTEX_COLORED_SIZE else VERTEX_NO_COLOR
        val mSkip: Int = (numVertices / MAX_VERTICES).coerceAtLeast(1)
        val nVerts = ceil((numVertices.toDouble() / mSkip.toDouble())).toInt()


        val verts = ByteBuffer.allocateDirect(3 * 4 * nVerts).order(ByteOrder.nativeOrder()).asFloatBuffer()
        val colors: ByteBuffer

        if (hasColor) {
            colors = ByteBuffer.allocateDirect(3 * nVerts).order(ByteOrder.nativeOrder())
        } else {
            colors = ByteBuffer.allocateDirect(3).order(ByteOrder.nativeOrder())
        }
        val tmpBuff = ByteArray(READ_BUFFER_SIZE * vertexStride)
        var vertsRead = 0

        // Read Chunk
        while (bis.available() > 0) {
            val bytesGot = min(tmpBuff.size, bis.available())
            bis.read(tmpBuff, 0, bytesGot)
            val buffer = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN)
            var bytesParsed = 0

            // Parse vertices
            var skipCounter = 0
            while (vertsRead < nVerts && bytesParsed < bytesGot) {
                skipCounter++
                if (buffer.position() + vertexStride > buffer.limit()) {
                    break
                }
                if (skipCounter < mSkip) {
                    buffer.position(buffer.position() + vertexStride)
                    continue
                }
                verts.put(3 * vertsRead, buffer.getFloat())
                verts.put(3 * vertsRead + 1, buffer.getFloat())
                verts.put(3 * vertsRead + 2, buffer.getFloat())
                if (hasColor) {
                    colors.put(3 * vertsRead, buffer.get())
                    colors.put(3 * vertsRead + 1, buffer.get())
                    colors.put(3 * vertsRead + 2, buffer.get())
                }
                vertsRead++
                bytesParsed += vertexStride
            }
        }
        verts.position(0)
        colors.position(0)
        mugPCD = MugPointCloudData(verts, colors, nVerts)
    }
}


fun BufferedInputStream.readLine(charset: Charset = Charsets.UTF_8): String? {
    val lineBuffer = mutableListOf<Byte>()
    while (true) {
        val nextByte = this.read()
        if (nextByte == -1) {
            // End of stream
            return if (lineBuffer.isEmpty()) null else lineBuffer.toByteArray().toString(charset)
        }
        if (nextByte == '\n'.code) {
            // End of line
            break
        }
        if (nextByte == '\r'.code) {
            // Handle \r\n
            val peek = this.read()
            break
        }
        lineBuffer.add(nextByte.toByte())
    }
    return lineBuffer.toByteArray().toString(charset)
}
