package com.mugtree.pointgl.pcl

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class MugPointCloud {
    companion object {
        val TAG = MugPointCloud::class.java.simpleName
    }
    var mugPointCloudData: MugPointCloudData? = null

    fun loadPLY(context: Context, filePath: String) {
        val inputStream = context.assets.open(filePath)
        loadPLY(context, inputStream)
    }

    fun loadPLY(context: Context, inputStream: InputStream) {
        try {
            mugPointCloudData = PLYReader().readPLYFile(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        } finally {
            inputStream.close()
        }
         Log.i(TAG, "Loaded Vertices ${(mugPointCloudData?.nVerts ?: 0)}")
    }

}
