package com.mugtree.pointgl.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mugtree.pointgl.pcl.MugPointCloudData
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointGLRenderer : IPointGLRenderer {
    companion object {
        val TAG = PointGLRenderer::class.java.simpleName
    }

    var pointCloudData: MugPointCloudData? = null

    override var angleX = 0f
    override var angleY = 0f

    // region GL
    private var shaderProgram = -1
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    // endregion

    override fun setPointCloud(pcd: MugPointCloudData) {
        this.pointCloudData = pcd
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        shaderProgram = MugGLUtil.createProgram(vertexShaderCode, fragmentShaderCode)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height

        // Update projection matrix using aspect ratio
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 20f)

        // ???
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f, // eye
            0f, 0f, 0f, // center
            0f, 1f, 0f  // up
        )
        // Apply rotation to model
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)

        // MVP = Projection * View * Model
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // updateMVPMatrix()
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val pcd = pointCloudData ?: return
        if (shaderProgram < 0) return
        // updateMVPMatrix()
        // Apply rotation to model
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)

        // MVP = Projection * View * Model
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)


        GLES20.glUseProgram(shaderProgram)
        pcd.vertices.rewind()
        pcd.colors.rewind()
        val posHandle = GLES20.glGetAttribLocation(shaderProgram, "aPos")
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT,
            false, 0, pcd.vertices)
        val colHandle = GLES20.glGetAttribLocation(shaderProgram, "aCol")
        GLES20.glEnableVertexAttribArray(colHandle)
        GLES20.glVertexAttribPointer(colHandle, 3, GLES20.GL_UNSIGNED_BYTE,
            true, 0, pcd.colors)

        val mvpHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pcd.nVerts)
        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(colHandle)
    }

    fun updateMVPMatrix() {
        // Apply rotation to model
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)
        // MVP = Projection * View * Model
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
    }


    val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec3 aPos;
            attribute vec3 aCol;
            varying vec3 vColor;
            
            void main() {
                gl_Position = uMVPMatrix * vec4(aPos, 1.0);
                gl_PointSize = 5.0;
                vColor = aCol;
            }
        """.trimIndent()
    val fragmentShaderCode = """
            precision mediump float;
            varying vec3 vColor;
            void main() {
                gl_FragColor = vec4(vColor, 1.0);
            }
        """.trimIndent()
}