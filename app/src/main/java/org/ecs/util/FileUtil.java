package org.ecs.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static   String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";
    private static ImageFileName sImageFileName = new ImageFileName("yyyyMMdd_HHmmss");

	/**初始化保存路径
	 * @return
	 */
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
        //sImageFileName = new ImageFileName("yyyyMMdd_HHmmss");
		return storagePath;
	}

	/**初始化保存路径
	 * @param b
	 */
	public static void saveBitmap(Bitmap b){

		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + createJpegName(dataTake) +".jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(TAG, "saveBitmap成功");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "saveBitmap:失败");
			e.printStackTrace();
		}

	}

	private static String createJpegName(long dateTaken) {
        synchronized (sImageFileName) {
            return sImageFileName.generateName(dateTaken);
        }
	}

	private static class ImageFileName {
		private final SimpleDateFormat mFormat;

        private long mLastDate;

        private int mSameSecondCount;
        ImageFileName(String format) {
			mFormat = new SimpleDateFormat(format, Locale.getDefault());
		}

		String generateName(long dateTaken) {
			Date date = new Date(dateTaken);
			String result = mFormat.format(date);

            if (dateTaken / 1000 == mLastDate / 1000) {
                mSameSecondCount++;
                result += "_" + mSameSecondCount;
            } else {
                mLastDate = dateTaken;
                mSameSecondCount = 0;
            }
			return result;
		}
	}
}
