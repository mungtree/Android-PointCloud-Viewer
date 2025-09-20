package com.mugtree.pointgl.opengl

import android.opengl.GLSurfaceView
import com.mugtree.pointgl.pcl.MugPointCloudData

interface IPointGLRenderer : GLSurfaceView.Renderer {
    var angleX: Float
    var angleY: Float

    fun setPointCloud(pcd: MugPointCloudData)
}