import os
import sys
import time
import sdl2
import sdl2.ext

# Инициализация SDL
sdl2.ext.init()

# Создаем прозрачное окно поверх всех
window = sdl2.ext.Window("Overlay", size=(1080, 2400), flags=sdl2.SDL_WINDOW_BORDERLESS | sdl2.SDL_WINDOW_ALWAYS_ON_TOP)
window.show()

renderer = sdl2.ext.Renderer(window)
renderer.blend_mode = sdl2.SDL_BLENDMODE_BLEND
renderer.clear((0, 0, 0, 0)) # Прозрачный фон

# Рисуем стрелку
def draw_arrow(x1, y1, x2, y2):
    renderer.draw_line((x1, y1), (x2, y2), color=(92, 156, 230, 255))
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    length = 40
    x3 = x2 - length * math.cos(angle - math.pi / 6)
    y3 = y2 - length * math.sin(angle - math.pi / 6)
    x4 = x2 - length * math.cos(angle + math.pi / 6)
    y4 = y2 - length * math.sin(angle + math.pi / 6)
    renderer.draw_line((x2, y2), (int(x3), int(y3)), color=(92, 156, 230, 255))
    renderer.draw_line((x2, y2), (int(x4), int(y4)), color=(92, 156, 230, 255))
    renderer.present()

# Тестовая стрелка
print("Overlay started!")
while True:
    draw_arrow(540, 1500, 540, 1200)
    time.sleep(1)
