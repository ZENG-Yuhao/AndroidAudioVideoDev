package com.zengyuhao.demo.androidaudiovideodev.ui.demo05


import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zengyuhao.demo.androidaudiovideodev.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLESFragment : Fragment() {
    private lateinit var mGLView: MyGLSurfaceView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_opengl_es, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mGLView = view.findViewById(R.id.glView)
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
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
        }
    }

    /**
     * MyGLRenderer
     */
    class MyGLRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the background frame color
            GLES20.glClearColor(0f, 0f, 0f, 1f)

        }

        override fun onDrawFrame(gl: GL10?) {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }
    }
}
