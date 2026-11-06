package raf.console.quran7hours.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SuraTicksView extends View {

    private List<Integer> timingsMs = new ArrayList<>();
    private int totalDuration = 1;
    private Paint paint;

    public SuraTicksView(Context c, AttributeSet a) {
        super(c, a);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#88000000")); // полупрозрачные метки
        paint.setStrokeWidth(3f);
    }

    public void setTimings(List<Integer> list, int durationMs) {
        this.timingsMs = list;
        this.totalDuration = durationMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (timingsMs == null || timingsMs.isEmpty()) return;

        float w = getWidth();
        float h = getHeight();

        for (int t : timingsMs) {
            float x = (float) t / totalDuration * w;
            canvas.drawLine(x, h/3f, x, h*2/3f, paint);
        }
    }
}
