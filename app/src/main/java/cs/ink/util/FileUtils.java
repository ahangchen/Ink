package cs.ink.util;

/**
 * Created by cwh on 15-12-24.
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;

import cs.ink.util.img.BitmapUtils;

public class FileUtils {
	public static String getSdcardDir(){
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return Environment.getExternalStorageDirectory().getPath();
		}

		return null;
	}
	public static String getFileSuffix(String fileName){

		if (fileName==null)
			return "";

		int idx = fileName.lastIndexOf(".");
		if (idx==-1)
			return "";

		return fileName.substring(idx+1);
	}
	// return a list containig information(<name, path> pairs) of the file
	public static List<Map<String, Object>> getMediaList(Activity context,
	                                                     String path, final String[] suffixes){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		File dir = new File(path);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				for(String suffix:suffixes)
					if (filename.endsWith(suffix))
						return true;

				return false;
			}
		};

		if (dir.exists() && dir.isDirectory()) {
			for(String fname: dir.list(filter)){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("fname", fname);
				map.put("isChecked", false);
				Bitmap bmp = BitmapUtils.createImageThumbnail(path + "/" + fname, 72);
				map.put("thumbnail", bmp);

				list.add(map);
			}
		}

		return list;
	}
	public static List<String> getMediaNameList(String path, final String[] suffixes){
		List<String> list = null;
		File dir = new File(path);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				for(String suffix:suffixes)
					if (filename.endsWith(suffix))
						return true;

				return false;
			}
		};

		if (dir.exists() && dir.isDirectory())
			list = Arrays.asList(dir.list(filter));

		return list;
	}

}
