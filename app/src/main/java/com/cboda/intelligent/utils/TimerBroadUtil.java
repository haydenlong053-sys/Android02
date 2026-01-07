package com.cboda.intelligent.utils;


import android.os.CountDownTimer;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.app.LiXinBroadApp;

import java.util.Random;

/**
 * 获取验证码倒计时
 */
public class TimerBroadUtil {

    private TextView tv;
    private CountDownListener mCountDownListener;

    public TimerBroadUtil(TextView tv) {
        this.tv = tv;
    }

    public TimerBroadUtil(TextView tv, CountDownListener mCountDownListener) {
        this.tv = tv;
        this.mCountDownListener = mCountDownListener;

    }

    public void timers() {
        timer.start();
    }

    public void countDown() {
        count.start();
    }


    public CountDownTimer timer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tv.setEnabled(false);
//            tv.setBackgroundResource(R.drawable.bg_rect_unable_50dp);
            tv.setText((millisUntilFinished / 1000) + "s");
        }

        @Override
        public void onFinish() {
            tv.setEnabled(true);
//            tv.setBackgroundResource(R.drawable.selector_btn_round_main);
            tv.setText(LiXinBroadApp.Companion.getMContext().getString(R.string.重新获取));
        }
    };
    public CountDownTimer count = new CountDownTimer(1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tv.setEnabled(false);
        }

        @Override
        public void onFinish() {
            tv.setEnabled(true);
            mCountDownListener.sendMessage(true);
        }
    };

    public interface CountDownListener {
        void sendMessage(boolean isFinish);
    }

    /**
     * 获取随机验证码
     */
    public static String getNum() {
        StringBuilder sb = new StringBuilder();
        //随机生成6位数  发送到聚合
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(10);
            sb.append(a);
        }
        return sb.toString();
    }

}
