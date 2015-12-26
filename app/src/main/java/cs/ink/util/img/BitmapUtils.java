package cs.ink.util.img;

/**
 * Created by cwh on 15-12-24.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import cs.ink.util.DeviceUtils;
import cs.ink.util.FileUtils;

public class BitmapUtils {
	private static final String TAG = "BitmapUtil";
	private static final int MAX_PIXELS_THUMBNAIL = 512 * 512;
	private static final int UNCONSTRAINED = -1;

	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	public static Bitmap decodeFromBytes(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public static Bitmap decodeBitmapFromPath(String path) {
		try {
			File file = new File(path);
			if (!file.exists() || !file.isFile())
				return null;

			BitmapFactory.Options bfOptions = new BitmapFactory.Options();
			bfOptions.inPreferredConfig = Bitmap.Config.RGB_565;
			bfOptions.inInputShareable = true;
			bfOptions.inDither = false;
			bfOptions.inPurgeable = true;
			bfOptions.inTempStorage = new byte[32 * 1024];

			FileInputStream fs = null;
			try {
				fs = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			}
			Bitmap bmp = null;
			if (fs != null)
				try {
					bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
							bfOptions);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				} finally {
					if (fs != null) {
						try {
							fs.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
			return bmp;
		} catch (Throwable t) {
			if (t != null)
				Log.e("exception", "BitmapUtil.decodeBitmapFromPath(String path) Exception " + t.getMessage());
			return null;
		}
	}

	public static Bitmap decodeBitmapFromPathUri(Uri pathUri, Activity activity) {
		try {
			int size = 800;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			ParcelFileDescriptor parcelFileDescriptor = activity
					.getContentResolver().openFileDescriptor(pathUri, "r");
			FileDescriptor fileDescriptor = parcelFileDescriptor
					.getFileDescriptor();

			Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,
					null, options);

			int h = options.outHeight;
			int w = options.outWidth;
			if (h == 0 || w == 0) {
				return null;
			}
			int reqHeight = h > w ? size : (size * h) / w;
			int reqWidth = w > h ? size : (size * w) / h;
			if (reqHeight == 0 || reqWidth == 0) {
				return null;
			}
			if (options.outWidth > reqWidth
					&& options.outHeight > reqHeight) {
				options.inSampleSize = computeSampleSize(options, -1,
						reqWidth * reqHeight);
			}
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
			return ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		} catch (Throwable t) {
			if (t != null)
				Log.e("exception", "BitmapUtil.decodeBitmapFromPathUri(Uri pathUri, Activity activity) Exception " + t.getMessage());
			return null;
		}
	}

	/**
	 * 按屏幕最大值读取Bitmap
	 *
	 * @param path
	 * @param activity
	 * @return
	 */
	public static Bitmap decodeBitmapFromPath(String path, Activity activity) {
		try {
			int[] size = DeviceUtils.getScreenSize(activity);
			int screenWidth = size[0];
			int screenHeight = size[1];

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			if (options.outWidth > screenWidth
					&& options.outHeight > screenHeight) {
				options.inSampleSize = computeSampleSize(options, -1,
						screenWidth * screenHeight);
			}
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(path, options);
			return bitmap;
		} catch (Throwable t) {
			if (t != null)
				Log.e("exception", "BitmapUtil.decodeBitmapFromPath(String path, Activity activity) Exception " + t.getMessage());
			return null;
		}
	}

	/**
	 * 生成图片缩略图，按假定显示最大分辨率{@code MAX_PIXELS_THUMBNAIL}来计算inSampleSize；
	 * 再按指定宽高生成缩略图
	 *
	 * @param filePath
	 * @param size     区域大小
	 * @return
	 */
	public static Bitmap createImageThumbnail(String filePath, int size) {
		Bitmap bitmap = decodeFromFile(filePath, size, size);
		return ThumbnailUtils.extractThumbnail(bitmap, size, size,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}

	/**
	 * 生成图片缩略图
	 *
	 * @param filePath
	 * @param size     区域大小
	 * @return
	 */
	public static Bitmap createImageThumbnailScale(String filePath, int size) {
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, option);
		int h = option.outHeight;
		int w = option.outWidth;
		if (h == 0 || w == 0) {
			return null;
		}
		int reqHeight = h > w ? size : (size * h) / w;
		int reqWidth = w > h ? size : (size * w) / h;
		if (reqHeight == 0 || reqWidth == 0) {
			return null;
		}
		bitmap = decodeFromFile(filePath, reqWidth, reqHeight);
		return ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}

	private static Bitmap decodeFromFile(String filePath, int reqWidth,
	                                     int reqHeight) {
		Bitmap bitmap = null;
		FileInputStream stream = null;

		try {
			stream = new FileInputStream(filePath);
			FileDescriptor fd = stream.getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			int maxPixels = MAX_PIXELS_THUMBNAIL;
			int targetSize = reqHeight > reqWidth ? reqWidth : reqHeight;
			options.inSampleSize = computeSampleSize(options, targetSize,
					maxPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (IOException ex) {
			if (ex != null)
				Log.e("exception", "BitmapUtil.decodeFromFile Exception " + ex.getMessage());
		} catch (OutOfMemoryError oom) {
			if (oom != null)
				Log.e("exception", "BitmapUtil.decodeFromFile Exception " + oom.getMessage());
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ex) {
				Log.e("ahang", "decodeFromFile", ex);
			}
		}
		return bitmap;
	}

	private static int computeSampleSize(BitmapFactory.Options options,
	                                     int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
	                                            int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
		if (filename == null) {
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
	                                        int reqHeight) {
		final int bitmapHeight = options.outHeight;
		final int bitmapWidth = options.outWidth;
		float sampleSize = 1;
		float widthSize = bitmapWidth * 1.0f / reqWidth;
		float heightSize = bitmapHeight * 1.0f / reqHeight;
		sampleSize = widthSize > heightSize ? heightSize : widthSize;
		int q = (int) (Math.log10(sampleSize) / Math.log10(2));
		return (int) Math.pow(2, q);
	}

	/**
	 * 保存Bitmap到目标路径
	 *
	 * @param bitmap
	 * @param path
	 * @return
	 */
	public static boolean save(Bitmap bitmap, String path) {
		if (bitmap == null || path == null)
			return false;
		OutputStream out = null;
		try {
			out = new FileOutputStream(path);
			CompressFormat format = null;
			String ext = FileUtils.getFileSuffix(path);
			if (ext != null) {
				if (ext.equals("jpg") || ext.equals("jpeg"))
					format = CompressFormat.JPEG;
			}
			if (format == null) {
				format = CompressFormat.PNG;
			}
			return bitmap.compress(format, 100, out);
		} catch (FileNotFoundException e) {
			Log.e("exception", e.getMessage());
			return false;
		} catch (Throwable t) {
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					Log.e("exception", e.getMessage());
				}
			}
		}
	}

	public static InputStream bitmap2InputStream(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			return inputStream;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap compressImage(Bitmap image) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		image.recycle();
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		baos.close();
		isBm.close();
		return bitmap;
	}

	public static Bitmap getCompressImage(String srcPath) throws IOException {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}

	public static Bitmap compress(Bitmap image) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		baos.close();
		isBm.close();
		return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}

}