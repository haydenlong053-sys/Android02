package com.cboda.intelligent.widge;

import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class OverLinkMovementBroadMethod extends LinkMovementMethod {

	public static boolean canScroll = false;

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
		int action = event.getAction();

		if(action == MotionEvent.ACTION_MOVE){
			if(!canScroll){
				return true;
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}

	public static MovementMethod getInstance() {
		if (sInstance == null)
			sInstance = new OverLinkMovementBroadMethod();

		return sInstance;
	}

	private static OverLinkMovementBroadMethod sInstance;
	private static Object FROM_BELOW = new NoCopySpan.Concrete();
}
