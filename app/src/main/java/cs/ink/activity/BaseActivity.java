package cs.ink.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import cs.ink.R;
import cs.ink.view.SlideFrame;

public abstract class BaseActivity extends Activity implements View.OnClickListener {

	protected Context context;

	protected ViewGroup rootView;
	protected SlideFrame slideFrame;

	public static String TAG = "BaseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);
		context = this;
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		init();
	}

	private void init() {
		rootView = (ViewGroup) findViewById(R.id.root_view).getParent();
		slideFrame = new SlideFrame(rootView, context, R.layout.slide_container, 200);
		initView();
		initDataSource();
	}

	abstract public void initView();

	abstract public void initDataSource();

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		bind();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		unbind();
	}


	abstract protected void bind();

	abstract protected void unbind();

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		onRelease();
	}

	protected abstract void onRelease();

	protected abstract void onDragRight();

	protected abstract void onDragLeft();

	private VelocityTracker mVelocityTracker;
	private double lastX;
	private double lastY;

	/**
	 * 右拖返回
	 */
	private boolean drag(float velocityX, float velocityY, float currentX, float currentY) {
		if (Math.abs((currentY - lastY)
				/ Math.abs(currentX - lastX)) < 0.8) {
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

	protected boolean needDrag(MotionEvent ev) {
		return false;
	}

	private static int mMaximumVelocity;

	/**
	 * touch事件
	 *
	 * @param ev 会被修改
	 */
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (needDrag(ev)) {
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
						if (drag(mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity(),
								ev.getRawX(), ev.getRawY())) {
							ev.setAction(MotionEvent.ACTION_CANCEL);
							return false;
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
		return super.dispatchTouchEvent(ev);
	}

	protected boolean onMenuPress() {
		return false;
	}

	protected boolean onBackPress() {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (onMenuPress()) return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK){
			if (onBackPress()) return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
