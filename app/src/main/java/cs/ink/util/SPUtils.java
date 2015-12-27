package cs.ink.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cwh on 15-12-27.
 */
public class SPUtils {
	public static void putString(String key, String value, Context context) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static String getString(String key, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		return sharedPreferences.getString(key, "");
	}

	public static void putInt(String key, int value, Context context) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt(key, value);
		editor.apply();
	}

	public static int getInt(String key, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		return sharedPreferences.getInt(key, -1);
	}

	public static void putLong(String key, long value, Context context) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putLong(key, value);
		editor.apply();
	}

	public static long getLong(String key, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		return sharedPreferences.getLong(key, -1);
	}

	public static void putFloat(String key, float value, Context context) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putFloat(key, value);
		editor.apply();
	}

	public static float getFloat(String key, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		return sharedPreferences.getFloat(key, -1);
	}

	public static void putBoolean(String key, boolean value, Context context) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public static boolean getBoolean(String key, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("CommonSp",
				Activity.MODE_PRIVATE);
		return sharedPreferences.getBoolean(key, false);
	}
}
