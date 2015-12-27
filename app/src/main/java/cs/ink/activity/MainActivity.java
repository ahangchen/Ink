package cs.ink.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cs.ink.R;
import cs.ink.model.ink.InkHelper;
import cs.ink.model.ink.InkOptManager;
import cs.ink.model.ink.InkOption;
import cs.ink.util.concurrent.Task;
import cs.ink.util.concurrent.TaskCallback;
import cs.ink.util.concurrent.ThreadUtils;
import cs.ink.util.img.BitmapUtils;
import cs.ink.util.img.ImageProcessUtils;
import cs.ink.view.SlideFrame;

public class MainActivity extends BaseActivity {
	private ImageView topbarIcon;
	private TextView topbarLabel;
	private ImageView img;
	private TextView mask;
	private Button clear;
	private Button ink;
	private Button save;

	private Bitmap bitmap;

	public void initView() {
		img = (ImageView) findViewById(R.id.image);
		mask = (TextView) findViewById(R.id.mask);
		clear = (Button) findViewById(R.id.clear);
		ink = (Button) findViewById(R.id.ink);
		save = (Button) findViewById(R.id.save);
		topbarIcon = (ImageView) findViewById(R.id.topbar_icon);
		topbarLabel = (TextView) findViewById(R.id.topbar_label);
		View content = slideFrame.getContentView();
		content.findViewById(R.id.high).setOnClickListener(MainActivity.this);
		content.findViewById(R.id.medium).setOnClickListener(MainActivity.this);
		content.findViewById(R.id.low).setOnClickListener(MainActivity.this);
	}

	@Override
	public void initDataSource() {
		InkOptManager.getInstance(slideFrame.getContentView()).initOpt();
	}

	public void bind() {
		mask.setOnClickListener(this);
		img.setOnClickListener(this);
		clear.setOnClickListener(this);
		ink.setOnClickListener(this);
		save.setOnClickListener(this);
		topbarIcon.setOnClickListener(this);
		topbarLabel.setOnClickListener(this);
	}

	public void unbind() {
		clear.setOnClickListener(null);
		ink.setOnClickListener(null);
		save.setOnClickListener(null);
		mask.setOnClickListener(null);
		img.setOnClickListener(null);
		topbarIcon.setOnClickListener(null);
		topbarLabel.setOnClickListener(null);
	}

	@Override
	public void onRelease() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		System.gc();
	}

	@Override
	protected void onDragRight() {
		slideFrame.slide();
	}

	@Override
	protected void onDragLeft() {
		slideFrame.hide();
	}

	int REQUEST_CODE_GALLERY = 0;

	private void selectImageFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_CODE_GALLERY);
	}


	private void img2Ink() {
		if (bitmap == null) {
			Toast.makeText(context, R.string.no_img, Toast.LENGTH_LONG).show();
			return;
		}
		TaskCallback callback = new TaskCallback();
		callback.setOnSuccess(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						img.setImageBitmap(bitmap);
						Toast.makeText(context, R.string.ink_complete, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		callback.setOnError(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, context.getString(R.string.ink_failed), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		Task ink = new Task() {
			@Override
			public boolean run() {
				try {
					Bitmap oldBitmap = bitmap;
					bitmap = ImageProcessUtils.getInkImg(bitmap, InkOptManager.getGaussR(), 2);
					BitmapUtils.recycleBitmap(oldBitmap);
				} catch (Exception e) {
					Log.d("ahang", "ink failed", e);
					return false;
				}
				return true;
			}
		};
		ink.setCallback(callback);
		ThreadUtils.runInBackGround(ink);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.topbar_label:
			case R.id.topbar_icon:
				slideFrame.slide();
				break;
			case R.id.mask:
			case R.id.image:
				selectImageFromGallery();
				break;
			case R.id.ink:
				img2Ink();
				break;
			case R.id.save:
				InkHelper.saveImg(bitmap, this);
				break;
			case R.id.clear:
				mask.setVisibility(View.VISIBLE);
				BitmapUtils.recycleBitmap(bitmap);
				img.setImageResource(R.mipmap.ico);
				System.gc();
				break;
			case R.id.high:
				InkOptManager.getInstance(slideFrame.getContentView()).set(InkOption.VIVID);
				Log.d("ahang", "high");
				break;
			case R.id.medium:
				InkOptManager.getInstance(slideFrame.getContentView()).set(InkOption.NORMAL);
				Log.d("ahang", "medium");
				break;
			case R.id.low:
				InkOptManager.getInstance(slideFrame.getContentView()).set(InkOption.BLURRY);
				Log.d("ahang", "low");
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		Uri uri = data.getData();
		if (uri != null) {
			try {
				Bitmap oldBitmap = bitmap;
				bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				BitmapUtils.recycleBitmap(oldBitmap);
				//直接拿可能太大，要压缩一下
				oldBitmap = bitmap;
				bitmap = BitmapUtils.compress(bitmap);
				BitmapUtils.recycleBitmap(oldBitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap oldBitmap = bitmap;
				Bitmap image = extras.getParcelable("data");
				if (image != null) {
					bitmap = image;
				}
				BitmapUtils.recycleBitmap(oldBitmap);
				//直接拿可能太大，要压缩一下
				oldBitmap = bitmap;
				try {
					bitmap = BitmapUtils.compress(bitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				BitmapUtils.recycleBitmap(oldBitmap);
			}
		}
		img.setImageDrawable(null);
		img.setImageBitmap(bitmap);
		mask.setVisibility(View.GONE);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected boolean needDrag(MotionEvent ev) {
		return true;
	}

	@Override
	protected boolean onBackPress() {
		if (slideFrame.isShowing()) {
			InkOptManager.getInstance(slideFrame.getContentView()).refresh();
			slideFrame.hide();
			return true;
		} else {
			return false;
		}
	}
}
