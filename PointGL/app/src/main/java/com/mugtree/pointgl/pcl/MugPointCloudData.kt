package com.mugtree.pointgl.pcl

import java.nio.ByteBuffer
import java.nio.FloatBuffer



data class MugPointCloudData(
    val vertices: FloatBuffer,
    val colors: ByteBuffer,
    val nVerts: Int
)