package cs.ink.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by cwh on 15-12-24.
 */
public class DeviceUtils {
	public static int[] getScreenSize(Activity context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

		return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
	}

}
