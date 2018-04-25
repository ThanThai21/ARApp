/*
 * Copyright 2016 Maxst, Inc. All Rights Reserved.
 */

package com.esp.arapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.InputStream;

public abstract class ShaderUtil {

	private static String TAG = ShaderUtil.class.getName();

	public static int createProgram(String vertexSrc, String fragmentSrc) {
		int vertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexSrc);
		int fragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);

		int shaderProgramId = GLES20.glCreateProgram();
		GLES20.glAttachShader(shaderProgramId, vertexShader);
		GLES20.glAttachShader(shaderProgramId, fragmentShader);
		GLES20.glLinkProgram(shaderProgramId);

		return shaderProgramId;
	}

	public static int createProgram(String vertexSrc, String fragmentSrc, String... attributes) {
		int vertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexSrc);
		int fragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);
		int programHandle = GLES20.glCreateProgram();
		if (programHandle != 0) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShader);

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShader);

			// Bind attributes
			if (attributes != null) {
				final int size = attributes.length;
				for (int i = 0; i < size; i++) {
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}
			}

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error creating program.");
		}

		return programHandle;
	}

	public static int loadShader(int type, String shaderSrc) {
		int shader;
		shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderSrc);
		GLES20.glCompileShader(shader);
		return shader;
	}

	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			// Log.e(TAG, glOperation + ": glError " + error);
			// throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	public static int loadTexture(final InputStream is) {
		Log.v("GLUtil", "Loading texture '" + is + "' from stream...");

		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);
		checkGlError("glGenTextures");

		if (textureHandle[0] != 0) {
			Log.i("GLUtil", "Handler: " + textureHandle[0]);

			final BitmapFactory.Options options = new BitmapFactory.Options();
			// By default, Android applies pre-scaling to bitmaps depending on the resolution of your device and which
			// resource folder you placed the image in. We don’t want Android to scale our bitmap at all, so to be sure,
			// we set inScaled to false.
			options.inScaled = false;

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			if (bitmap == null) {
				throw new RuntimeException("couldnt load bitmap");
			}

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			checkGlError("glBindTexture");
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			checkGlError("texImage2D");
			bitmap.recycle();
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle,
										   final String[] attributes) {
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			if (attributes != null) {
				final int size = attributes.length;
				for (int i = 0; i < size; i++) {
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}
			}

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error creating program.");
		}

		return programHandle;
	}
}
