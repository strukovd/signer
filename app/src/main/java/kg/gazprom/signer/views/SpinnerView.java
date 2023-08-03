package kg.gazprom.signer.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;


public class SpinnerView extends View {

    private final Paint mPaint;
    private final Handler mHandler;
    private final Runnable mRunnable;

    private float mAngle;

    public SpinnerView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mAngle += 10;
                if (mAngle >= 360) {
                    mAngle = 0;
                }
                invalidate();
                mHandler.postDelayed(this, 50);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.post(mRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int radius = Math.min(cx, cy) / 2;
        canvas.save();
        canvas.rotate(mAngle, cx, cy);
        canvas.drawCircle(cx, cy, radius, mPaint);
        canvas.restore();
    }
}
