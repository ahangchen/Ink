package cs.ink.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cs.ink.R;
import cs.ink.model.InkHelper;
import cs.ink.util.concurrent.Task;
import cs.ink.util.concurrent.TaskCallback;
import cs.ink.util.concurrent.ThreadUtils;
import cs.ink.util.img.BitmapUtils;
import cs.ink.util.img.ImageProcessUtils;
import cs.ink.view.GestureListener;
import cs.ink.view.SlideFrame;

public class MainActivity extends Activity implements View.OnClickListener {
	//will use base activity
	Context context;

	private ImageView img;
	private TextView mask;
	private Button clear;
	private Button ink;
	private Button save;
	private ViewGroup rootView;
	private SlideFrame slideFrame;

	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		initView();
	}

	private void initView() {
		img = (ImageView) findViewById(R.id.image);
		mask = (TextView) findViewById(R.id.mask);
		clear = (Button) findViewById(R.id.clear);
		ink = (Button) findViewById(R.id.ink);
		save = (Button) findViewById(R.id.save);
		rootView = (ViewGroup) findViewById(R.id.root_view).getParent();
		slideFrame = new SlideFrame(rootView, context, R.layout.quality_chooser, 250);
		View content = slideFrame.getContentView();
		content.findViewById(R.id.high).setOnClickListener(MainActivity.this);
		content.findViewById(R.id.medium).setOnClickListener(MainActivity.this);
		content.findViewById(R.id.low).setOnClickListener(MainActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bind();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbind();
	}


	private void bind() {
		mask.setOnClickListener(this);
		img.setOnClickListener(this);
		clear.setOnClickListener(this);
		ink.setOnClickListener(this);
		save.setOnClickListener(this);
		rootView.setOnTouchListener(new SlideListener(context));
		rootView.setLongClickable(true);
	}

	private void unbind() {
		clear.setOnClickListener(null);
		ink.setOnClickListener(null);
		save.setOnClickListener(null);
		mask.setOnClickListener(null);
		img.setOnClickListener(null);
		rootView.setOnClickListener(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null) {
			bitmap.recycle();
		}
		System.gc();
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
					bitmap = ImageProcessUtils.getInkImg(bitmap, 5, 2);
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
				img.setImageBitmap(null);
				System.gc();
				break;
			case R.id.high:
				Log.d("ahang", "high");
				break;
			case R.id.medium:
				Log.d("ahang", "medium");
				break;
			case R.id.low:
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
		img.setImageBitmap(bitmap);
		mask.setVisibility(View.GONE);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class SlideListener extends GestureListener {
		public SlideListener(Context context) {
			super(context);
		}

		@Override
		public boolean onDragLeft() {
			slideFrame.hide();
			return false;
		}

		@Override
		public boolean onDragRight() {
			slideFrame.slide();
			return false;
		}
	}
}
