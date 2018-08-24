package com.zengyuhao.demo.androidaudiovideodev.ui.demo05

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import com.zengyuhao.demo.androidaudiovideodev.ui.demo05.shape.Square
import com.zengyuhao.demo.androidaudiovideodev.ui.demo05.shape.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLESActivity : AppCompatActivity() {
    private lateinit var mGLView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLView = MyGLSurfaceView(this)
        setContentView(mGLView)
    }

    /**
     * MyGLSurfaceView
     */
    class MyGLSurfaceView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {
        private val mRenderer: MyGLRenderer

        init {
            setEGLContextClientVersion(2)
            mRenderer = MyGLRenderer()
            setRenderer(mRenderer)
            // Render the view only when there is a change in the drawing data
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        }
    }

    /**
     * MyGLRenderer
     */
    class MyGLRenderer : GLSurfaceView.Renderer {
        private lateinit var mTriangle: Triangle
        private lateinit var mSquare: Square
        private val mMVPMatrix = FloatArray(16)
        private val mProjectionMatrix = FloatArray(16)
        private val mViewMatrix = FloatArray(16)

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the background frame color
            GLES20.glClearColor(0f, 0f, 0f, 1f)

            // initialize a triangle
            mTriangle = Triangle()
            // initialize a square
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)

            val ratio: Float = width.toFloat() / height.toFloat()
            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        }

        override fun onDrawFrame(gl: GL10?) {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            val eye = object {
                val x = 0f
                val y = 0f
                val z = -3f
            }
            val center = object {
                val x = 0f
                val y = 0f
                val z = 0f
            }
            val upVec = object {
                val x = 0f
                val y = 1f
                val z = 0f
            }
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, eye.x, eye.y, eye.z, center.x, center.y, center.z, upVec.x, upVec.y, upVec.z)

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

            // Draw shape
            mTriangle.draw(mMVPMatrix)
        }
    }
}
