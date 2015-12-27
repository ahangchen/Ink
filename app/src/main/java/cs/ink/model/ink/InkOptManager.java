package cs.ink.model.ink;

import android.content.Context;
import android.view.View;

import cs.ink.R;
import cs.ink.util.SPUtils;

/**
 * Created by cwh on 15-12-27.
 */
public class InkOptManager {
	private View optView;

	private InkOptManager(View optView) {
		this.optView = optView;
	}

	private static InkOptManager instance = null;

	public static InkOptManager getInstance(View optView) {
		if (instance == null) {
			instance = new InkOptManager(optView);
		}
		return instance;
	}

	public static int curOpt = InkOption.BLURRY;

	public void initOpt() {
		Context context = optView.getContext();
		curOpt = SPUtils.getInt(InkOption.OPT, context);
		if (curOpt == -1) {
			SPUtils.putInt(InkOption.OPT, InkOption.BLURRY, context);
			curOpt = InkOption.BLURRY;
		}
		refresh();
	}

	public void set(int newOpt) {
		Context context = optView.getContext();
		SPUtils.putInt(InkOption.OPT, newOpt, context);
		curOpt = newOpt;
		refresh();
	}

	public void refresh() {
		Context context = optView.getContext();
		int inActiveColor = context.getResources().getColor(R.color.transparent);
		int activeColor = context.getResources().getColor(R.color.deep_alp_grey);
		optView.findViewById(R.id.low).setBackgroundColor(curOpt == InkOption.BLURRY ? activeColor : inActiveColor);
		optView.findViewById(R.id.medium).setBackgroundColor(curOpt == InkOption.NORMAL ? activeColor : inActiveColor);
		optView.findViewById(R.id.high).setBackgroundColor(curOpt == InkOption.VIVID ? activeColor : inActiveColor);
	}

	public static int getGaussR() {
		switch (curOpt) {
			case InkOption.VIVID:
				return 15;
			case InkOption.NORMAL:
				return 10;
			case InkOption.BLURRY:
				return 5;
			default:
				return 5;
		}
	}
}
