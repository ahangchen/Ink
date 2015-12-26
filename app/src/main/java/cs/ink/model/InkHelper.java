package cs.ink.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;

import cs.ink.R;
import cs.ink.util.FileUtils;
import cs.ink.util.concurrent.Task;
import cs.ink.util.concurrent.TaskCallback;
import cs.ink.util.concurrent.ThreadUtils;
import cs.ink.util.img.BitmapUtils;
import cs.ink.util.img.ImageProcessUtils;

/**
 * Created by cwh on 15-12-25.
 */
public class InkHelper {
	public static void saveImg(final Bitmap bitmap,final Activity activity) {
		final String savePath = FileUtils.getSdcardDir() + "/" + new Date().getTime() + ".bmp";
		if (bitmap == null) {
			Toast.makeText(activity, R.string.no_img, Toast.LENGTH_LONG).show();
			return;
		}
		TaskCallback saveCallback = new TaskCallback();
		saveCallback.setOnSuccess(new Runnable() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(activity,
								String.format(activity.getString(R.string.save_complete), savePath),
								Toast.LENGTH_SHORT
						).show();
					}
				});
			}
		});

		saveCallback.setOnError(new Runnable() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(activity, activity.getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		Task save = new Task() {
			@Override
			public boolean run() {
				boolean result = BitmapUtils.save(bitmap, savePath);
				Log.d("ahang", "savePath:" + savePath);
				return result;
			}
		};
		save.setCallback(saveCallback);
		ThreadUtils.runInBackGround(save);
	}


}
