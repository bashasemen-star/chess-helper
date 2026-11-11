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

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;

import java.util.List;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private ArrowView arrowView;

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

        Button btnTest = new Button(this);
        btnTest.setText("ТЕСТ ДВИЖКА");
        btnTest.setOnClickListener(v -> testEngine());

        Button btnOn = new Button(this);
        btnOn.setText("ВКЛЮЧИТЬ СТРЕЛКУ");
        btnOn.setOnClickListener(v -> showOverlay());

        layout.addView(btnTest);
        layout.addView(btnOn);
        setContentView(layout);
    }

    private void testEngine() {
        try {
            Board board = new Board();
            List<Move> moves = MoveGenerator.generateLegalMoves(board);
            if (!moves.isEmpty()) {
                Move bestMove = moves.get(0);
                Toast.makeText(this, "Движок работает! Ход: " + bestMove, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Нет доступных ходов", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            canvas.drawLine(540, 1600, 540, 1300, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(540, 1300);
            path.lineTo(500, 1360);
            path.lineTo(580, 1360);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
