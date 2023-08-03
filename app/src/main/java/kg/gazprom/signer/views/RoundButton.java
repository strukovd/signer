package kg.gazprom.signer.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatButton;


public class RoundButton extends AppCompatButton {
    public RoundButton(Context context) {
        super(context);
        init();
    }
    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RoundButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        // Установить радиус границы в половину высоты кнопки
        // setHeight(getWidth());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(getWidth(), getHeight());
        shape.setColor(Color.RED);
        setBackground(shape);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Высота будет равна ширине
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Меняем размер шрифта на кнопке
        float viewSize = Math.min(w, h);
        float textSize = viewSize * 0.4f; // 40% от размера кнопки
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
