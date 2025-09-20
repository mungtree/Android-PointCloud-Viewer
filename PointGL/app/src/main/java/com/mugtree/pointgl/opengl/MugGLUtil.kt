package com.mugtree.pointgl.opengl

import android.opengl.GLES20
import android.util.Log
import java.nio.CharBuffer
import java.nio.IntBuffer
import kotlin.math.log

object MugGLUtil {
    val TAG = "MugGL"
    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val successBuf = IntBuffer.allocate(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, successBuf)
        val isSuccess = (successBuf.get() == GLES20.GL_TRUE)
        if (!isSuccess) {
            throw Exception("Failed to Compile Shader $type - $shaderCode")
        }

        return shader
    }

    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vShader)
            GLES20.glAttachShader(it, fShader)
            GLES20.glLinkProgram(it)
        }
        if (!LinkSuccessful(program)) {
            val log = GLES20.glGetShaderInfoLog(program)
            Log.e(TAG, "Failed to compile shader $log")
        }
        return program
    }

    fun LinkSuccessful(program: Int): Boolean {
        val status = IntBuffer.allocate(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status)
        val result = status.get()
        return result == GLES20.GL_TRUE
    }

    fun GetShaderLog(program: Int) {
        val logLengthBuf = IntBuffer.allocate(1)
        GLES20.glGetShaderiv(program, GLES20.GL_INFO_LOG_LENGTH, logLengthBuf)
        val logLength = logLengthBuf.get()
        if (logLength > 0) {
            val logBuf = CharBuffer.allocate(logLength)
        }


    }

}