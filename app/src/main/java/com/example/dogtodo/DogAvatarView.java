package com.example.dogtodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** A small, reusable vector-style dog illustration for the MVP. */
public class DogAvatarView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int dogType = 0;

    public DogAvatarView(Context context) {
        super(context);
        setContentDescription("직업 강아지 캐릭터");
    }

    public void setDogType(int dogType) {
        this.dogType = Math.max(0, Math.min(5, dogType));
        setContentDescription(MainActivity.dogName(this.dogType) + " 캐릭터");
        invalidate();
    }

    private float s(float value) {
        return value * Math.min(getWidth(), getHeight()) / 100f;
    }

    private void fill(Canvas canvas, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setStrokeWidth(s(2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float unit = Math.min(w, h) / 100f;
        canvas.drawColor(Color.TRANSPARENT);

        int[] backgrounds = {Color.rgb(255, 225, 216), Color.rgb(226, 239, 255),
                Color.rgb(226, 247, 235), Color.rgb(255, 241, 198),
                Color.rgb(232, 226, 255), Color.rgb(255, 226, 239)};
        fill(canvas, backgrounds[dogType]);
        canvas.drawCircle(w / 2f, h / 2f, Math.min(w, h) * .47f, paint);

        int fur = Color.rgb(204, 145, 100);
        int furLight = Color.rgb(244, 194, 148);
        int dark = Color.rgb(72, 51, 44);
        int outfit;
        switch (dogType) {
            case 1: outfit = Color.rgb(142, 187, 232); break;
            case 2: outfit = Color.rgb(87, 166, 122); break;
            case 3: outfit = Color.rgb(236, 133, 91); break;
            case 4: outfit = Color.rgb(164, 119, 194); break;
            case 5: outfit = Color.rgb(219, 110, 143); break;
            default: outfit = Color.rgb(95, 137, 196); break;
        }

        // Body and outfit.
        fill(canvas, outfit);
        canvas.drawRoundRect(new RectF(w * .27f, h * .58f, w * .73f, h * .91f), s(12), s(12), paint);
        fill(canvas, furLight);
        canvas.drawOval(new RectF(w * .31f, h * .72f, w * .42f, h * .94f), paint);
        canvas.drawOval(new RectF(w * .58f, h * .72f, w * .69f, h * .94f), paint);

        // Ears behind the head.
        fill(canvas, fur);
        Path leftEar = new Path();
        leftEar.moveTo(w * .31f, h * .36f);
        leftEar.lineTo(w * .18f, h * .24f);
        leftEar.lineTo(w * .25f, h * .53f);
        leftEar.close();
        canvas.drawPath(leftEar, paint);
        Path rightEar = new Path();
        rightEar.moveTo(w * .69f, h * .36f);
        rightEar.lineTo(w * .82f, h * .24f);
        rightEar.lineTo(w * .75f, h * .53f);
        rightEar.close();
        canvas.drawPath(rightEar, paint);

        // Head and muzzle.
        fill(canvas, fur);
        canvas.drawCircle(w / 2f, h * .45f, w * .27f, paint);
        fill(canvas, furLight);
        canvas.drawOval(new RectF(w * .36f, h * .49f, w * .64f, h * .66f), paint);
        fill(canvas, dark);
        canvas.drawCircle(w * .405f, h * .44f, s(3.2f), paint);
        canvas.drawCircle(w * .595f, h * .44f, s(3.2f), paint);
        canvas.drawOval(new RectF(w * .46f, h * .51f, w * .54f, h * .58f), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s(1.6f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        Path smile = new Path();
        smile.moveTo(w * .5f, h * .57f);
        smile.quadTo(w * .47f, h * .61f, w * .44f, h * .59f);
        smile.moveTo(w * .5f, h * .57f);
        smile.quadTo(w * .53f, h * .61f, w * .56f, h * .59f);
        canvas.drawPath(smile, paint);

        drawAccessory(canvas, w, h, outfit, dark);
    }

    private void drawAccessory(Canvas canvas, float w, float h, int outfit, int dark) {
        fill(canvas, dark);
        if (dogType == 1) { // doctor: stethoscope
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(s(2.2f));
            Path path = new Path();
            path.moveTo(w * .39f, h * .68f);
            path.cubicTo(w * .39f, h * .77f, w * .61f, h * .77f, w * .61f, h * .68f);
            canvas.drawPath(path, paint);
            fill(canvas, Color.WHITE);
            canvas.drawRect(w * .47f, h * .69f, w * .53f, h * .78f, paint);
            fill(canvas, Color.rgb(207, 77, 82));
            canvas.drawRect(w * .495f, h * .705f, w * .505f, h * .765f, paint);
            canvas.drawRect(w * .48f, h * .73f, w * .52f, h * .74f, paint);
        } else if (dogType == 2) { // athlete: headband
            fill(canvas, Color.rgb(246, 222, 91));
            canvas.drawRect(w * .27f, h * .31f, w * .73f, h * .36f, paint);
        } else if (dogType == 3) { // chef: hat
            fill(canvas, Color.WHITE);
            canvas.drawRoundRect(new RectF(w * .37f, h * .18f, w * .63f, h * .33f), s(6), s(6), paint);
            canvas.drawCircle(w * .42f, h * .2f, s(7), paint);
            canvas.drawCircle(w * .5f, h * .17f, s(8), paint);
            canvas.drawCircle(w * .58f, h * .2f, s(7), paint);
        } else if (dogType == 4) { // organizer: little clipboard
            fill(canvas, Color.WHITE);
            canvas.drawRoundRect(new RectF(w * .59f, h * .65f, w * .76f, h * .86f), s(2), s(2), paint);
            fill(canvas, outfit);
            canvas.drawRect(w * .62f, h * .71f, w * .72f, h * .72f, paint);
            canvas.drawRect(w * .62f, h * .76f, w * .70f, h * .77f, paint);
        } else if (dogType == 5) { // artist: beret
            fill(canvas, Color.rgb(66, 91, 145));
            canvas.drawOval(new RectF(w * .31f, h * .25f, w * .69f, h * .36f), paint);
            canvas.drawCircle(w * .61f, h * .25f, s(4), paint);
        } else { // student: glasses
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(s(1.6f));
            paint.setColor(Color.rgb(65, 72, 93));
            canvas.drawCircle(w * .405f, h * .44f, s(7), paint);
            canvas.drawCircle(w * .595f, h * .44f, s(7), paint);
            canvas.drawLine(w * .47f, h * .44f, w * .53f, h * .44f, paint);
        }
    }
}
