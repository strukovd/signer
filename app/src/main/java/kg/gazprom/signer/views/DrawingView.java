package kg.gazprom.signer.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawingView extends View {
    DrawingView dv ;

    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private Path mPath;
    private Paint penPaintConfig;
    Context context;
    private Path circlePath;
    private Paint circlePaintConfig;

    public DrawingView(Context c) {
        super(c);
        context=c;

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaintConfig = new Paint();
        circlePath = new Path();
        circlePaintConfig.setAntiAlias(true);
        circlePaintConfig.setColor(Color.BLUE);
        circlePaintConfig.setStyle(Paint.Style.STROKE);
        circlePaintConfig.setStrokeJoin(Paint.Join.MITER);
        circlePaintConfig.setStrokeWidth(4f);

        penPaintConfig = new Paint();
        penPaintConfig.setAntiAlias(true);
        penPaintConfig.setDither(true);
        penPaintConfig.setColor(0xFF002b59);
        penPaintConfig.setStyle(Paint.Style.STROKE);
        penPaintConfig.setStrokeJoin(Paint.Join.ROUND);
        penPaintConfig.setStrokeCap(Paint.Cap.ROUND);
        penPaintConfig.setStrokeWidth(3);
    }
    public DrawingView(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
        context=c;

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaintConfig = new Paint();
        circlePath = new Path();
        circlePaintConfig.setAntiAlias(true);
        circlePaintConfig.setColor(Color.BLUE);
        circlePaintConfig.setStyle(Paint.Style.STROKE);
        circlePaintConfig.setStrokeJoin(Paint.Join.MITER);
        circlePaintConfig.setStrokeWidth(4f);

        penPaintConfig = new Paint();
        penPaintConfig.setAntiAlias(true);
        penPaintConfig.setDither(true);
        penPaintConfig.setColor(0xFF002b59);
        penPaintConfig.setStyle(Paint.Style.STROKE);
        penPaintConfig.setStrokeJoin(Paint.Join.ROUND);
        penPaintConfig.setStrokeCap(Paint.Cap.ROUND);
        penPaintConfig.setStrokeWidth(12);
    }

    private void initPaintConfigs() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        Log.w("mPath: ", mPath.toString());
        Log.w("mPaint: ", Boolean.toString(penPaintConfig == null) ); //TODO: mPaint is null


        canvas.drawPath( mPath, penPaintConfig);
        canvas.drawPath( circlePath, circlePaintConfig);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, penPaintConfig);
        // kill this so we don't double draw
        mPath.reset();
    }

    public void clearDrawing() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath.reset();
        circlePath.reset();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
}
