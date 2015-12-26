package cs.ink.util.img;

import android.graphics.Bitmap;

/**
 * Created by cwh on 15-12-24.
 */
public class ImageProcessUtils {
	public static float KR = 0.299f;
	public static float KG = 0.587f;
	public static float KB = 0.114f;
	public static int[] discolor(Bitmap bitmap) {

		int picHeight = bitmap.getHeight();
		int picWidth = bitmap.getWidth();

		int[] pixels = new int[picWidth * picHeight];
		bitmap.getPixels(pixels, 0, picWidth, 0, 0, picWidth, picHeight);
		for (int i = 0; i < picHeight; ++i) {
			for (int j = 0; j < picWidth; ++j) {
				int index = i * picWidth + j;
				int color = pixels[index];
				int r = (color & 0x00ff0000) >> 16;
				int g = (color & 0x0000ff00) >> 8;
				int b = (color & 0x000000ff);
				int grey = (int) (r * KR + g * KG + b * KB);
				pixels[index] = grey << 16 | grey << 8 | grey | 0xff000000;
			}
		}
		System.gc();

		return pixels;
	}

	public static int[] reverseColor(int[] pixels) {

		int length = pixels.length;
		int[] result = new int[length];
		for (int i = 0; i < length; ++i) {
			int color = pixels[i];
			int b = 255 - (color & 0x000000ff);
			result[i] = b << 16 | b << 8 | b | 0xff000000;
		}
		return result;

	}

	public static void gaussBlur(int[] data, int width, int height, int radius,
	                             float sigma) {

		float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
		float pb = -1.0f / (2 * sigma * sigma);

		// generate the Gauss Matrix
		float[] gaussMatrix = new float[radius * 2 + 1];
		float gaussSum = 0f;
		for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
			float g = (float) (pa * Math.exp(pb * x * x));
			gaussMatrix[i] = g;
			gaussSum += g;
		}

		for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
			gaussMatrix[i] /= gaussSum;
		}

		// x direction
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				float b = 0;
				gaussSum = 0;
				for (int j = -radius; j <= radius; ++j) {
					int k = x + j;
					if (k >= 0 && k < width) {
						int index = y * width + k;
						int color = data[index];
						int cb = (color & 0x000000ff);
						b += cb * gaussMatrix[j + radius];
						gaussSum += gaussMatrix[j + radius];
					}
				}

				int index = y * width + x;
				int cb = (int) (b / gaussSum);

				data[index] = cb << 16 | cb << 8 | cb | 0xff000000;
			}
		}

		// y direction
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				float b = 0;
				gaussSum = 0;
				for (int j = -radius; j <= radius; ++j) {
					int k = y + j;
					if (k >= 0 && k < height) {
						int index = k * width + x;
						int color = data[index];
						int cb = (color & 0x000000ff);
						b += cb * gaussMatrix[j + radius];

						gaussSum += gaussMatrix[j + radius];
					}
				}

				int index = y * width + x;
				int cb = (int) (b / gaussSum);
				data[index] = cb << 16 | cb << 8 | cb | 0xff000000;
			}
		}
	}

	public static void colorDodge(int[] baseColor, int[] mixColor) {

		for (int i = 0, length = baseColor.length; i < length; ++i) {
			int bColor = baseColor[i];
			int bb = (bColor & 0x000000ff);

			int mColor = mixColor[i];
			int mb = (mColor & 0x000000ff);
			int nb = colorDodgeFormular(bb, mb);

			baseColor[i] = nb << 16 | nb << 8 | nb | 0xff000000;
		}

	}

	private static int colorDodgeFormular(int base, int mix) {
		int result = base + (base * mix) / (255 - mix);
		result = result > 255 ? 255 : result;
		return result;

	}


	public static Bitmap getInkImg(Bitmap src, int r, int fai) {
		int width = src.getWidth();
		int height = src.getHeight();

		int[] pixels = discolor(src);
		int[] copixels = reverseColor(pixels);
		gaussBlur(copixels, width, height, r, fai);
		colorDodge(pixels, copixels);

		return Bitmap.createBitmap(pixels, width, height,
				Bitmap.Config.RGB_565);
	}
}
