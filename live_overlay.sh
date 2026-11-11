#!/data/data/com.termux/files/usr/bin/bash

# Бесконечный цикл
while true; do
    # 1. Делаем скриншот
    su -c "screencap -p /sdcard/screen.png"
    
    # 2. Накладываем стрелку (координаты e2-e4)
    ffmpeg -y -i /sdcard/screen.png -vf "drawbox=x=540:y=1500:w=10:h=300:color=blue@1:t=fill,drawbox=x=520:y=1200:w=40:h=10:color=blue@1:t=fill" /sdcard/result.png
    
    # 3. Показываем результат через системный просмотрщик (поверх всего)
    su -c "am start -a android.intent.action.VIEW -d file:///sdcard/result.png -t image/png --activity-clear-top"
    
    # 4. Ждем 2 секунды
    sleep 2
done
