package com.esp.arapp.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.esp.arapp.SampleApplication.SampleAppRenderer;
import com.esp.arapp.SampleApplication.SampleAppRendererControl;
import com.esp.arapp.SampleApplication.SampleApplicationSession;
import com.esp.arapp.SampleApplication.utils.CubeShaders;
import com.esp.arapp.SampleApplication.utils.SampleUtils;
import com.esp.arapp.SampleApplication.utils.Teapot;
import com.esp.arapp.SampleApplication.utils.Texture;
import com.esp.arapp.activities.VuforiaARActivity;
import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VuforiaARRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {

    private SampleApplicationSession vuforiaAppSession;
    private SampleAppRenderer mSampleAppRenderer;

    private boolean mIsActive = false;

    private Vector<Texture> mTextures;
    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    static final float kObjectScale = 3.f;

    private Teapot mTeapot;

    private VuforiaARActivity mActivity;

    public VuforiaARRenderer(SampleApplicationSession vuforiaAppSession, VuforiaARActivity mActivity) {
        this.vuforiaAppSession = vuforiaAppSession;
        this.mActivity = mActivity;

        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 10f, 5000f);

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        vuforiaAppSession.onSurfaceCreated();
        mSampleAppRenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mActivity.updateRendering();
        vuforiaAppSession.onSurfaceChanged(width, height);
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
        mTeapot = new Teapot();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mIsActive) {
            mSampleAppRenderer.render();
        }
    }

    @Override
    public void renderFrame(State state, float[] projectionMatrix) {
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        mActivity.refFreeFrame.render();
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            float[] modelViewProjection = new float[16];
            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);
            GLES20.glUseProgram(shaderProgramID);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mTeapot.getVertices());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(0).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mTeapot.getIndices());
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            SampleUtils.checkGLError("UserDefinedTargets renderFrame");
        }
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
    }

    public void setActive(boolean active) {
        mIsActive = active;
        if (mIsActive) {
            mSampleAppRenderer.configureVideoBackground();
        }
    }

    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;
    }
}
