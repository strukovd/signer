package kg.gazprom.signer.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class RoundedImageView extends androidx.appcompat.widget.AppCompatImageView {

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawableImage = getDrawable();
        if (drawableImage == null) {
            super.onDraw(canvas);
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap bitmapImage = ((BitmapDrawable) drawableImage).getBitmap();
        Bitmap roundedBitmap = getRoundedBitmap(bitmapImage);

        canvas.drawBitmap(roundedBitmap, 0, 0, null);
    }

    private Bitmap getRoundedBitmap(Bitmap imageBitmap) { // создает круглую картинку на основе переданного параметром bitmapImage
        // Создаем пустой Bitmap, с размерами view
        Bitmap newBitmap = Bitmap.createBitmap(
                getWidth(),
                getHeight(),
//                bitmapImage.getWidth(),
//                bitmapImage.getHeight(),
                Bitmap.Config.ARGB_8888);

        final int color = 0xff424242;
        final Paint paintConfig = new Paint();
        final Rect rect = new Rect(0, 0, getWidth(), getHeight());
        final RectF rectF = new RectF(rect);

        final float roundPx = getWidth() / 2f;

        paintConfig.setAntiAlias(true);
        paintConfig.setColor(color); // цвет заливки


        // Canvas для рисования на битмапе
        Canvas canvasImage = new Canvas(newBitmap);
        canvasImage.drawARGB(0, 0, 0, 0); // Заливаем прозрачным фоном
        canvasImage.drawRoundRect(rectF, roundPx, roundPx, paintConfig); // Рисуем круг (маска)
        paintConfig.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); // Включаем режим рисования, только по маске (по кружку, где пикселы не прозрачны)

        // Изменим масштаб картинки (под ширину view компонента)
        float scale = Math.min(getWidth() / (float) imageBitmap.getWidth(), getHeight() / (float) imageBitmap.getHeight());
        int newWidth = (int) (imageBitmap.getWidth() * scale);
        int newHeight = (int) (imageBitmap.getHeight() * scale);

        Bitmap scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true);

        canvasImage.drawBitmap(scaledImageBitmap, rect, rect, paintConfig); // Рисуем Bitmap по маске
        return newBitmap;
    }
}
