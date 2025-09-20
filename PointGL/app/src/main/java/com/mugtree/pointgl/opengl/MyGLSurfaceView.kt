package com.mugtree.pointgl.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.mugtree.pointgl.pcl.MugPointCloudData

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: IPointGLRenderer = PointGLRenderer()
//    private val renderer: IPointGLRenderer = MyGLRenderer()

    private var previousX = 0f
    private var previousY = 0f

    // Sensitivity factor
    private val TOUCH_SCALE = 0.5f

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }

    fun setPointCloud(cloud: MugPointCloudData) {
        renderer.setPointCloud(cloud)
        requestRender()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                renderer.angleX += dy * TOUCH_SCALE
                renderer.angleY += dx * TOUCH_SCALE

                requestRender() // trigger redraw
            }
        }

        previousX = x
        previousY = y
        return true
    }

}