package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private ArrowView arrowView;
    private boolean overlayVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btnUp = new Button(this);
        btnUp.setText("ВВЕРХ");
        btnUp.setOnClickListener(v -> moveArrow(0, -50));

        Button btnDown = new Button(this);
        btnDown.setText("ВНИЗ");
        btnDown.setOnClickListener(v -> moveArrow(0, 50));

        Button btnLeft = new Button(this);
        btnLeft.setText("ВЛЕВО");
        btnLeft.setOnClickListener(v -> moveArrow(-50, 0));

        Button btnRight = new Button(this);
        btnRight.setText("ВПРАВО");
        btnRight.setOnClickListener(v -> moveArrow(50, 0));

        Button btnOn = new Button(this);
        btnOn.setText("ВКЛЮЧИТЬ СТРЕЛКУ");
        btnOn.setOnClickListener(v -> showOverlay());

        layout.addView(btnOn);
        layout.addView(btnUp);
        layout.addView(btnDown);
        layout.addView(btnLeft);
        layout.addView(btnRight);
        setContentView(layout);
    }

    private void moveArrow(int dx, int dy) {
        if (arrowView != null) {
            arrowView.move(dx, dy);
        } else {
            Toast.makeText(this, "Сначала включи стрелку", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOverlay() {
        if (overlayVisible) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        arrowView = new ArrowView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        try {
            windowManager.addView(arrowView, params);
            overlayVisible = true;
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class ArrowView extends View {
        private Paint paint;
        private int offsetX = 0;
        private int offsetY = 0;

        public ArrowView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.parseColor("#5C9CE6"));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(25f);
            paint.setAntiAlias(true);
        }

        public void move(int dx, int dy) {
            offsetX += dx;
            offsetY += dy;
            invalidate(); // Перерисовываем
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int sx = 540 + offsetX;
            int sy = 1600 + offsetY;
            int ex = 540 + offsetX;
            int ey = 1300 + offsetY;

            canvas.drawLine(sx, sy, ex, ey, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(ex, ey);
            path.lineTo(ex - 40, ey + 60);
            path.lineTo(ex + 40, ey + 60);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
