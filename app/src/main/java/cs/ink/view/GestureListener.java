package cs.ink.view;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;

/**
 * 实现监听左右滑动的事件，哪个view需要的时候直接setOnTouchListener就可以用了 
 * 需要在前面设置LongClickable
 */
public abstract class GestureListener extends SimpleOnGestureListener implements OnTouchListener {
	/** 左右滑动的最短距离 */
	private int distance = 100;
	/** 左右滑动的最大速度 */
	private int velocity = 100;

	private GestureDetector gestureDetector;

	public GestureListener(Context context) {
		gestureDetector = new GestureDetector(context, this);
	}

	/**
	 * 向左滑的时候调用的方法
	 * @return
	 */
	public abstract boolean onDragLeft();

	/**
	 * 向右滑的时候调用的方法
	 * @return
	 */
	public abstract boolean onDragRight();

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	                       float velocityY) {
		// e1：第1个ACTION_DOWN MotionEvent
		// e2：最后一个ACTION_MOVE MotionEvent  
		// velocityX：X轴上的移动速度（像素/秒）  
		// velocityY：Y轴上的移动速度（像素/秒）  

		// 向左滑  
		if (e1.getX() - e2.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			onDragLeft();
		}
		// 向右滑  
		if (e2.getX() - e1.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			onDragRight();
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return false;
	}

}
