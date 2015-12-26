package cs.ink.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import cs.ink.R;

/**
 * Created by cwh on 15-12-26
 */
public class SlideFrame {
	// 可以直接在Activity中使用，对比V4中的SlidingPaneLayout
	private View contentView;
	private ViewGroup root;
	private Context context;
	private SlideListener slideListener;
	private HideListener hideListener;

	public View getContentView() {
		return contentView;
	}

	public void setSlideListener(SlideListener slideListener) {
		this.slideListener = slideListener;
	}

	public void setHideListener(HideListener hideListener) {
		this.hideListener = hideListener;
	}

	public SlideFrame(ViewGroup root, Context context, int resId, int width) {
		this.root = root;
		this.context = context;
		contentView = wrapContent(context, resId, root, ViewUtils.dip2px(context, width));
	}

	public View wrapContent(Context context, int resId, ViewGroup root, int width) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		// root参数为null则返回生成的view, root参数不为null则返回root
		ViewGroup contentRoot = (ViewGroup) layoutInflater.inflate(resId, null);
		//params参数需要重新设置
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
		contentRoot.setLayoutParams(lp);
		//避免事件继续分发
		contentRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		return contentRoot;
	}

	private boolean showing = false;

	public boolean isShowing() {
		return showing;
	}

	public void slide() {
		//拦截，只add一次
		if (isShowing()) return;
		root.addView(contentView);
		if (slideListener != null) {
			slideListener.onSlide();
		}
		Animation slideAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_left);
		contentView.startAnimation(slideAnim);
		showing = true;
	}

	public void hide() {
		if (!isShowing()) return;
		if (hideListener != null) {
			hideListener.onHide();
		}
		Animation slideAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_left);
		contentView.startAnimation(slideAnim);
		root.removeView(contentView);
		showing = false;
	}

	private interface SlideListener {
		void onSlide();
	}

	private interface HideListener {
		void onHide();
	}
}
