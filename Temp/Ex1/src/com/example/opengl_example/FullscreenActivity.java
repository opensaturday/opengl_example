package com.example.opengl_example;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import min3d.core.Object3dContainer;
import min3d.parser.IParser;
import min3d.parser.ParseObjectData;
import min3d.parser.Parser;

import com.example.opengl_example.parser.OBJParser;
import com.example.opengl_example.parser.TDModel;
import com.example.opengl_example.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGLConfig;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.opengl.GLES20;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	
	GLSurfaceView mGLSurfaceView;
	Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_fullscreen);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(new Renderer());
        //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        setContentView(mGLSurfaceView);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	  protected void onResume() {
          super.onResume();
          mGLSurfaceView.onResume();
     }
 
     protected void onPause() {
          super.onPause();
          mGLSurfaceView.onPause();
     }
	class Renderer implements GLSurfaceView.Renderer {
		//private Object3dContainer objModel;  //min3D용
		private OBJParser parser;
		private TDModel model;
		private int LOAD_MODE_FILE = 0;
		private int LOAD_MODE_RESOURCE = 1;
		
        public Renderer() {
        	x=y=z=0;
        	angle=1f;        	
        	parser=new OBJParser(mContext);
    		model=parser.parseOBJ("/sdcard/camaro_obj",LOAD_MODE_RESOURCE);    		   
        	loadBuffer();
        	
        	ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
    		byteBuf.order(ByteOrder.nativeOrder());
    		lightAmbientBuffer = byteBuf.asFloatBuffer();
    		lightAmbientBuffer.put(lightAmbient);
    		lightAmbientBuffer.position(0);
    		
    		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
    		byteBuf.order(ByteOrder.nativeOrder());
    		lightDiffuseBuffer = byteBuf.asFloatBuffer();
    		lightDiffuseBuffer.put(lightDiffuse);
    		lightDiffuseBuffer.position(0);
    		
    		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
    		byteBuf.order(ByteOrder.nativeOrder());
    		lightPositionBuffer = byteBuf.asFloatBuffer();
    		lightPositionBuffer.put(lightPosition);
    		lightPositionBuffer.position(0); 
        } 

        public void onSurfaceChanged(GL10 gl, int width, int height) {

        	gl.glViewport(0, 0, width, height);
     
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
     
            float ratio = (float) width / height;
            // 프로젝션 설정
            GLU.gluPerspective(gl, 45.0f, ratio, 1, 100f);
				
        }

        
        public void onDrawFrame(GL10 gl) {
        	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        	 
            // 카메라의 위치, 카메라가 보는 방향, 법선벡터가 나가는 방향을 지정한다(객체의 위치)
            GLU.gluLookAt(gl, 0.0f, 32.0f, 32.0f, 0, 0, 0, 0f, 1f, 0f);
     
            //오브젝트를 그릴땐 모델뷰 모드로 한다.
            gl.glMatrixMode(GL10.GL_MODELVIEW);
     
            gl.glLoadIdentity();
            DrawMap(gl);
     
            gl.glLoadIdentity();
            DrawAxis(gl);
     
            gl.glLoadIdentity();
            gl.glTranslatef(x, y, z);
            gl.glScalef(3,3,3);
            gl.glRotatef(angle++, 0, 1, 0);
            gl.glRotatef(270, 1, 0, 0);
            gl.glEnable(GL10.GL_LIGHTING);
           
            //gl.glEnable(GL10.GL_TEXTURE_2D);
            //GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0); 
            model.draw(gl);	
            //gl.glDisable(GL10.GL_TEXTURE_2D);
            
            gl.glLoadIdentity();
        }

        Bitmap bmp;
        private float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
    	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    	private float[] lightPosition = {0.0f, -3.0f, 2.0f, 1.0f};
    	private FloatBuffer lightAmbientBuffer;
    	private FloatBuffer lightDiffuseBuffer;
    	private FloatBuffer lightPositionBuffer;
    	
		@Override
		public void onSurfaceCreated(GL10 gl,
				javax.microedition.khronos.egl.EGLConfig config) {
			// TODO Auto-generated method stub
			/*
			gl.glDisable(GL10.GL_DITHER);
	        gl.glClearColor(1, 1, 1, 1);
	 
	        gl.glEnable(GL10.GL_DEPTH_TEST);
	        gl.glDepthFunc(GL10.GL_LEQUAL);
	 
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	 
	        gl.glFrontFace(GL10.GL_CW);
	        */
	        
	        
	        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	
			gl.glEnable(GL10.GL_LIGHT0);
										
			
			gl.glShadeModel(GL10.GL_SMOOTH); 			
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	
			gl.glClearDepthf(1.0f); 					
			gl.glEnable(GL10.GL_DEPTH_TEST); 			
			gl.glDepthFunc(GL10.GL_LEQUAL); 		
		
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
			
	        
	        /*
	        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_LINEAR);
	         */
            bmp = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.camaro);
		}
		
		private FloatBuffer mfbuf, mfMapBuf;

		private int map = 15;
		private float angle, radian, x, y, z, dx, dz;

	    private float vtxAxis[] = { 0, 0, 0, 4.0f, 0, 0, 0, 0, 0, 0, 4.0f, 0, 0, 0,
	            0, 0, 0, 4.0f };
	 
	    private float vtxMap[] = { -map, 0, 0, map, 0, 0, 0, 0, -map, 0, 0, map };
		
		
		private void DrawAxis(GL10 gl) {
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mfbuf);
	 
	        gl.glNormal3f(0, 0, 1);
	        // X축(빨강)
	        gl.glColor4f(1, 0, 0, 1);
	        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
	 
	        // Y축(초록)
	        gl.glColor4f(0, 1, 0, 1);
	        gl.glDrawArrays(GL10.GL_LINES, 2, 2);
	 
	        // Z축(파랑)
	        gl.glColor4f(0, 0, 1, 1);
	        gl.glDrawArrays(GL10.GL_LINES, 4, 2);
	 
	        gl.glColor4f(1, 1, 1, 1);
	    }
		private void DrawMap(GL10 gl) {
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mfMapBuf);
	 
	        gl.glColor4f(.5f, .5f, .5f, 1); // 회색
	        gl.glNormal3f(0, 0, 1);
	 
	        for (int i = -map; i <= map; i++) {
	            gl.glLoadIdentity();
	            gl.glTranslatef(0, 0, (float) i);
	            gl.glDrawArrays(GL10.GL_LINES, 0, 2);
	        }
	        for (int i = -map; i <= map; i++) {
	            gl.glLoadIdentity();
	            gl.glTranslatef((float) i, 0, 0);
	            gl.glDrawArrays(GL10.GL_LINES, 2, 2);
	        }
	        gl.glColor4f(1, 1, 1, 1); // 흰색
	    }
		private void loadBuffer() {
	        mfbuf = loadFBuf(vtxAxis);
	        mfMapBuf = loadFBuf(vtxMap);
	    }		
		private FloatBuffer loadFBuf(float fb[]) {
	        FloatBuffer FB;	 
	        ByteBuffer bb = ByteBuffer.allocateDirect(fb.length * 4);
	        bb.order(ByteOrder.nativeOrder());
	        FB = bb.asFloatBuffer();
	        FB.put(fb);
	        FB.position(0);	 
	        return FB;
	    }

   }
	
	
	
	

}
