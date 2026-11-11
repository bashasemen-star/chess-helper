import subprocess
import time
from PIL import Image, ImageDraw

print("Жди 5 секунд, открой игру...")
time.sleep(5)

subprocess.run(["su", "-c", "screencap -p /sdcard/screen.png"])

img = Image.open("/sdcard/screen.png").convert("RGBA")
draw = ImageDraw.Draw(img)

# СИНЯЯ СТРЕЛКА (e2-e4)
start_x, start_y = 540, 1500
end_x, end_y = 540, 1200

draw.line((start_x, start_y, end_x, end_y), fill=(92, 156, 230, 255), width=25)
draw.line((end_x, end_y, end_x - 40, end_y + 60), fill=(92, 156, 230, 255), width=25)
draw.line((end_x, end_y, end_x + 40, end_y + 60), fill=(92, 156, 230, 255), width=25)

img.save("/sdcard/result.png")
print("Готово! Смотри /sdcard/result.png")
