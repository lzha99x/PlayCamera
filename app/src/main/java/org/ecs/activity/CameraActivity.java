package org.ecs.activity;

import org.ecs.camera.CameraInterface;
import org.ecs.camera.preview.CameraSurfaceView;
import org.ecs.mode.GoogleFaceDetect;
import org.ecs.playcamera.R;
import org.ecs.ui.FaceView;
import org.ecs.util.DisplayUtil;
import org.ecs.util.EventUtil;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

public class CameraActivity extends Activity{
	private static final String TAG = "CameraActivity";
	private static final int TASKS_DELAY_MSEC = 2500;
    private final boolean LOGV = true;
	CameraSurfaceView surfaceView = null;
	ImageButton shutterBtn;
	ImageButton switchBtn;
	FaceView faceView;
	float previewRate = -1f;
	private MainHandler mMainHandler = null;
	GoogleFaceDetect googleFaceDetect = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (LOGV) Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		mMainHandler = new MainHandler();
		googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);


		shutterBtn.setOnClickListener(new BtnListeners());
		switchBtn.setOnClickListener(new BtnListeners());
		//mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(LOGV) Log.d(TAG, "onResume: ");
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(LOGV) Log.d(TAG, "onPause: ");
        mMainHandler.removeCallbacksAndMessages(null);
    }


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		switchBtn = (ImageButton)findViewById(R.id.btn_switch);
		faceView = (FaceView)findViewById(R.id.face_view);
	}
	private void initViewParams(){
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this);
        surfaceView.setMainActivity(this);
		surfaceView.setLayoutParams(params);


	}

	private class BtnListeners implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.btn_shutter:
				takePicture();
				break;
			case R.id.btn_switch:
                switchBtn.setVisibility(View.INVISIBLE);
				switchCamera();
                mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_SWITCH_BUTTON,TASKS_DELAY_MSEC);
				break;
			default:break;
			}
		}

	}
	private  class MainHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what){
			case EventUtil.UPDATE_FACE_RECT:
                Log.d(TAG, "handleMessage: UPDATE_FACE_RECT");
                Face[] faces = (Face[]) msg.obj;
				faceView.setFaces(faces);
                //stopGoogleFaceDetect();
                //mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_TAKE_PICTURE, TASKS_DELAY_MSEC);
				break;
			case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
				startGoogleFaceDetect();
				break;
			case EventUtil.CAMERA_TAKE_PICTURE:
                takePicture();
                break;
            case  EventUtil.CAMERA_SWITCH_BUTTON:
                switchBtn.setVisibility(View.VISIBLE);
                break;
			}
			super.handleMessage(msg);
		}

	}

	private void takePicture(){
        mMainHandler.removeMessages(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
		CameraInterface.getInstance().doTakePicture();
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, TASKS_DELAY_MSEC);
	}
	private void switchCamera(){
		stopGoogleFaceDetect();
		int newId = (CameraInterface.getInstance().getCameraId() + 1)%2;
		CameraInterface.getInstance().doStopCamera();
		CameraInterface.getInstance().doOpenCamera(null, newId);
		CameraInterface.getInstance().doStartPreview(this,surfaceView.getSurfaceHolder(), previewRate);
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, TASKS_DELAY_MSEC);
//		startGoogleFaceDetect();

	}
	private void startGoogleFaceDetect(){
		Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
        if (LOGV) Log.d(TAG, "startGoogleFaceDetect: params.getMaxNumDetectedFaces() = " + params.getMaxNumDetectedFaces());
        if(params.getMaxNumDetectedFaces() > 0){
			if(faceView != null){
				faceView.clearFaces();
				faceView.setVisibility(View.VISIBLE);
			}
			CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(googleFaceDetect);
		    try {
                CameraInterface.getInstance().getCameraDevice().startFaceDetection();
            } catch(RuntimeException e) {
                Log.d(TAG, "RuntimeException " + e);
                //stopGoogleFaceDetect();
            }
            //CameraInterface.getInstance().getCameraDevice().startFaceDetection();
		}
	}
	private void stopGoogleFaceDetect(){
        //mMainHandler.removeCallbacksAndMessages(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
        mMainHandler.removeMessages(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
		Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
		if(params.getMaxNumDetectedFaces() > 0){
			CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(null);
			CameraInterface.getInstance().getCameraDevice().stopFaceDetection();
			faceView.clearFaces();
		}
	}

}
