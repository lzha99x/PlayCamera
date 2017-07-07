package org.ecs.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

public class CamParaUtil {
	private static final String TAG = "CamParaUtil";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CamParaUtil myCamPara = null;
	private CamParaUtil(){

	}
	public static CamParaUtil getInstance(){
		if(myCamPara == null){
			myCamPara = new CamParaUtil();
			return myCamPara;
		}
		else{
			return myCamPara;
		}
	}
/*******************************************************************************************/
    private Point getDefaultDisplaySize(Activity activity, Point size) {
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }
    public  Size getOptimalPreviewSize(Activity currentActivity, List<Camera.Size> sizes, double targetRatio) {
        int optimalPickIndex = getOptimalPreviewSizeIndex(currentActivity, sizes, targetRatio);
        if (optimalPickIndex == -1) {
            return null;
        } else {
            return sizes.get(optimalPickIndex);
        }
    }

    private int getOptimalPreviewSizeIndex(Activity currentActivity, List<Camera.Size> sizes, double targetRatio) {
        final double aspectRatioTolerance = 0.02;

        return getOptimalPreviewSizeIndex(currentActivity, sizes, targetRatio, aspectRatioTolerance);
    }

    private int getOptimalPreviewSizeIndex(
            Activity currentActivity,List<Camera.Size> previewSizes,
            double targetRatio, Double aspectRatioTolerance) {
        if (previewSizes == null) {
            return -1;
        }

        if (aspectRatioTolerance == null) {
            return getOptimalPreviewSizeIndex(currentActivity, previewSizes, targetRatio);
        }

        int optimalSizeIndex = -1;
        double minDiff = Double.MAX_VALUE;
        Point point = getDefaultDisplaySize(currentActivity,new Point());
        int targetHeight = Math.min(point.x, point.y);
        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectRatioTolerance) {
                continue;
            }

            double heightDiff = Math.abs(size.height - targetHeight);
            if (heightDiff < minDiff) {
                optimalSizeIndex = i;
                minDiff = heightDiff;
            } else if (heightDiff == minDiff) {
                if (size.height < targetHeight) {
                    optimalSizeIndex = i;
                    minDiff = heightDiff;
                }
            }
        }
        // 没有匹配到使用Height差最小的
        if (optimalSizeIndex == -1) {
            minDiff = Double.MAX_VALUE;
            for (int i = 0; i < previewSizes.size(); i++) {
                Size size = previewSizes.get(i);
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSizeIndex = i;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSizeIndex;
    }
    /*******************************************************************************************/
	public  Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth){
		Collections.sort(list, sizeComparator);

		int i = 0;
		for(Size s:list){
			if((s.width >= minWidth) && equalRate(s, th)){
				Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if(i == list.size()){
			i = 0; //如果没找到，就选最小的size
		}
		return list.get(i);
	}
	public Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){
		Collections.sort(list, sizeComparator);

		int i = 0;
		for(Size s:list){
			if((s.width >= minWidth) && equalRate(s, th)){
				Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if(i == list.size()){
			i = 0; //如果没找到，就选最小的size
		}
		return list.get(i);
	}

	public boolean equalRate(Size s, float rate){
		float r = (float)(s.width)/(float)(s.height);
		if(Math.abs(r - rate) <= 0.03)
		{
			return true;
		}
		else{
			return false;
		}
	}

	public  class CameraSizeComparator implements Comparator<Camera.Size>{
		public int compare(Size lhs, Size rhs) {
			// TODO Auto-generated method stub
			if(lhs.width == rhs.width){
				return 0;
			}
			else if(lhs.width > rhs.width){
				return 1;
			}
			else{
				return -1;
			}
		}

	}

	/**
	 * @param params
	 */
	public  void printSupportPreviewSize(Camera.Parameters params){
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for(int i=0; i< previewSizes.size(); i++){
			Size size = previewSizes.get(i);
			Log.i(TAG, "previewSizes:width = "+size.width+" height = "+size.height);
		}
	
	}

	/**
	 * @param params
	 */
	public  void printSupportPictureSize(Camera.Parameters params){
		List<Size> pictureSizes = params.getSupportedPictureSizes();
		for(int i=0; i< pictureSizes.size(); i++){
			Size size = pictureSizes.get(i);
			Log.i(TAG, "pictureSizes:width = "+ size.width
					+" height = " + size.height);
		}
	}
	/**
	 * @param params
	 */
	public void printSupportFocusMode(Camera.Parameters params){
		List<String> focusModes = params.getSupportedFocusModes();
		for(String mode : focusModes){
			Log.i(TAG, "focusModes--" + mode);
		}
	}
}
