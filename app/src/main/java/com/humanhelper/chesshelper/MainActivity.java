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
    
    private int startX = 540;
    private int startY = 1600;
    private int endX = 540;
    private int endY = 1300;
    
    private int savedStartX = 540;
    private int savedStartY = 1600;
    private int savedEndX = 540;
    private int savedEndY = 1300;

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

        Button btnOn = new Button(this);
        btnOn.setText("ВКЛЮЧИТЬ СТРЕЛКУ");
        btnOn.setOnClickListener(v -> showOverlay());

        Button btnSave = new Button(this);
        btnSave.setText("СОХРАНИТЬ КООРДИНАТЫ");
        btnSave.setOnClickListener(v -> {
            savedStartX = startX; savedStartY = startY;
            savedEndX = endX; savedEndY = endY;
            Toast.makeText(this, "Координаты сохранены", Toast.LENGTH_SHORT).show();
        });

        Button btnTest = new Button(this);
        btnTest.setText("ТЕСТ: ПОКАЗАТЬ ХОД");
        btnTest.setOnClickListener(v -> {
            startX = savedStartX; startY = savedStartY;
            endX = savedEndX; endY = savedEndY;
            if (arrowView != null) arrowView.invalidate();
            Toast.makeText(this, "Стрелка на сохраненных координатах", Toast.LENGTH_SHORT).show();
        });

        Button btnStartUp = new Button(this);
        btnStartUp.setText("НИЗ ВВЕРХ");
        btnStartUp.setOnClickListener(v -> { startY -= 50; updateArrow(); });

        Button btnStartDown = new Button(this);
        btnStartDown.setText("НИЗ ВНИЗ");
        btnStartDown.setOnClickListener(v -> { startY += 50; updateArrow(); });

        Button btnEndUp = new Button(this);
        btnEndUp.setText("ВЕРХ ВВЕРХ");
        btnEndUp.setOnClickListener(v -> { endY -= 50; updateArrow(); });

        Button btnEndDown = new Button(this);
        btnEndDown.setText("ВЕРХ ВНИЗ");
        btnEndDown.setOnClickListener(v -> { endY += 50; updateArrow(); });

        layout.addView(btnOn);
        layout.addView(btnSave);
        layout.addView(btnTest);
        layout.addView(btnStartUp);
        layout.addView(btnStartDown);
        layout.addView(btnEndUp);
        layout.addView(btnEndDown);
        setContentView(layout);
    }

    private void showOverlay() {
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
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateArrow() {
        if (arrowView != null) arrowView.invalidate();
    }

    public class ArrowView extends View {
        private Paint paint;
        public ArrowView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.parseColor("#5C9CE6"));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(25f);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawLine(startX, startY, endX, endY, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(endX, endY);
            path.lineTo(endX - 40, endY + 60);
            path.lineTo(endX + 40, endY + 60);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
