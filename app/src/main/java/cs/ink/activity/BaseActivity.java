package cs.ink.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import cs.ink.R;
import cs.ink.view.GestureListener;
import cs.ink.view.SlideFrame;

public abstract class BaseActivity extends Activity implements View.OnClickListener {

	protected Context context;

	protected ViewGroup rootView;
	protected SlideFrame slideFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		init();
	}

	private void init() {
		rootView = (ViewGroup) findViewById(R.id.root_view).getParent();
		slideFrame = new SlideFrame(rootView, context, R.layout.quality_chooser, 250);
//		View content = slideFrame.getContentView();
//		content.findViewById(R.id.high).setOnClickListener(MainActivity.this);
//		content.findViewById(R.id.medium).setOnClickListener(MainActivity.this);
//		content.findViewById(R.id.low).setOnClickListener(MainActivity.this);
		initView();
		initDataSource();
	}

	abstract public void initView();
	abstract public void initDataSource();

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


	protected void bind() {
//		mask.setOnClickListener(this);
//		img.setOnClickListener(this);
//		clear.setOnClickListener(this);
//		ink.setOnClickListener(this);
//		save.setOnClickListener(this);
		rootView.setOnTouchListener(new SlideListener(context));
		rootView.setLongClickable(true);
	}

	protected void unbind() {
//		clear.setOnClickListener(null);
//		ink.setOnClickListener(null);
//		save.setOnClickListener(null);
//		mask.setOnClickListener(null);
//		img.setOnClickListener(null);
		rootView.setOnClickListener(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		onRelease();
//		if (bitmap != null) {
//			bitmap.recycle();
//		}
//		System.gc();
	}

	protected abstract void onRelease();

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

	protected abstract void onDragRight();
	protected abstract void onDragLeft();

	private VelocityTracker mVelocityTracker;
	private double lastX;
	private double lastY;
	/**
	 * 右拖返回
	 */
	private boolean dragRight(float velocityX, float velocityY, float currentX, float currentY) {
		if (velocityY < 900
				&& Math.abs((currentY - lastY)
				/ Math.max(currentX - lastX, 1)) < 0.8) {
			// 移动角度小于60度
			if (velocityX > 1000) {
				onDragRight();
			} else if (velocityX < -1000) {
				onDragLeft();
			}
			return true;
		}
		return false;
	}

	protected boolean onDrag(MotionEvent ev) {
		return true;
	}
	private static int mMaximumVelocity;
	/**
	 * touch事件
	 * @param ev 会被修改
	 */
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (onDrag(ev)) {
			// 右划返回
			switch (ev.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					if (mVelocityTracker == null) {
						mVelocityTracker = VelocityTracker.obtain();
					} else {
						mVelocityTracker.clear();
					}
					lastX = ev.getRawX();
					lastY = ev.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					if (mVelocityTracker != null) {
						mVelocityTracker.addMovement(ev);
					}
					break;
				case MotionEvent.ACTION_UP:
					if (mVelocityTracker != null) {
						mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
						if (dragRight(mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity(),
								ev.getRawX(), ev.getRawY())) {
							ev.setAction(MotionEvent.ACTION_CANCEL);
						}
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					if (mVelocityTracker != null) {
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
					break;
			}

		} else if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		return false;
	}

}
