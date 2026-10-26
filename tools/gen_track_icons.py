#!/usr/bin/env python3
"""Draws the 16x16 icons of the classic Traincraft tracks in the look of the 1.21 rail: one picture per kind of
piece (straight, diagonal, 90 and 45 degree turns, parallel curve, switches, crossings, road crossing, slopes) and
one number in one font in the top left corner, the length or the size of the piece in blocks.
  python3 tools/gen_track_icons.py VANILLA_TEXTURES_DIR [OUT_DIR]
VANILLA_TEXTURES_DIR is assets/minecraft/textures of the 1.21.1 client jar (block/ is read from it)."""
import sys, os, math, json, re
from PIL import Image

VAN = sys.argv[1]
RES = os.path.join(os.path.dirname(__file__), '..', 'src/main/resources/assets/traincraft')
OUT = sys.argv[2] if len(sys.argv) > 2 else os.path.join(RES, 'textures/items/tracks')

#the colours of the 1.21 rail
RAIL_LIGHT, RAIL_DARK = (171, 172, 171), (104, 104, 104)
TIE, TIE_DARK, TIE_END = (126, 98, 55), (103, 80, 44), (81, 61, 36)
GROOVE = (58, 58, 58)
TEXT, SHADOW = (255, 255, 255), (63, 63, 63)

def tex(path):
    return Image.open(os.path.join(VAN, path)).convert('RGBA')

def own(path):
    return Image.open(os.path.join(RES, 'textures', path)).convert('RGBA')

# ---------------------------------------------------------------- paths
def line(a, b, n=64):
    return [(a[0] + (b[0] - a[0]) * t / n, a[1] + (b[1] - a[1]) * t / n) for t in range(n + 1)]

def arc(cx, cy, r, a0, a1, n=96):
    return [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * t / n)), cy - r * math.sin(math.radians(a0 + (a1 - a0) * t / n))) for t in range(n + 1)]

def straight(x=8.0):
    return line((x, 17), (x, -1))

def horizontal(y=8.0):
    return line((-1, y), (17, y))

def diagonal(flip=False):
    return line((17, 17), (-1, -1)) if flip else line((-1, 17), (17, -1))

def turn90(r, x0=8.0):
    #from the bottom edge up, bending right round a centre on the bottom edge: the larger the turn, the less of it
    #fits and the straighter it looks
    cx, cy = x0 + r, 16.0
    return line((x0, 18), (x0, 16))[:-1] + arc(cx, cy, r, 180, 90) + line((cx, cy - r), (cx + 3, cy - r))[1:]

def turn45(r, x0=8.0):
    cx, cy = x0 + r, 16.0
    p = line((x0, 18), (x0, 16))[:-1] + arc(cx, cy, r, 180, 135)
    ex, ey = p[-1]
    return p + line((ex, ey), (ex + 20, ey - 20))[1:]

def scurve(x0, x1):
    pts = []
    for t in range(129):
        y = 18 - 20 * t / 128
        u = t / 128
        s = u * u * (3 - 2 * u)
        pts.append((x0 + (x1 - x0) * s, y))
    return pts

# ---------------------------------------------------------------- drawing
def walk(path, step=0.1):
    """points every step along the path with their unit normals (pointing to the right of the way it goes)"""
    out, carry = [], 0
    for i in range(len(path) - 1):
        (ax, ay), (bx, by) = path[i], path[i + 1]
        seg = math.hypot(bx - ax, by - ay)
        if not seg:
            continue
        tx, ty = (bx - ax) / seg, (by - ay) / seg
        t = carry
        while t < seg:
            out.append((ax + tx * t, ay + ty * t, -ty, tx, len(out) * step))
            t += step
        carry = t - seg
    return out

class Icon:
    def __init__(self, gauge=11.0, tie=14.0, spacing=4.0):
        self.img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        self.px = self.img.load()
        self.g, self.tl, self.ts = gauge, tie, spacing

    def put(self, x, y, c):
        x, y = int(math.floor(x)), int(math.floor(y))
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[x, y] = c[:3] + ((c[3],) if len(c) > 3 else (255,))

    def get(self, x, y):
        x, y = int(math.floor(x)), int(math.floor(y))
        return self.px[x, y] if 0 <= x < 16 and 0 <= y < 16 else None

    def ties(self, path):
        pts = walk(path)
        h = self.tl / 2
        every = int(round(self.ts / 0.1))
        for i in range(0, len(pts), every):
            for row, c in ((0, TIE), (1, TIE_DARK)):
                j = min(len(pts) - 1, i + row * 10)
                x, y, nx, ny, _ = pts[j]
                k = -h
                while k <= h:
                    self.put(x + nx * k, y + ny * k, c)
                    k += 0.25

    def rails(self, path, groove=False):
        g = self.g / 2
        pts = walk(path)
        #the dark inner side first, the light top over it
        for side in (-1, 1):
            for x, y, nx, ny, _ in pts:
                if groove:
                    self.put(x + nx * side * (g - 2), y + ny * side * (g - 2), GROOVE)
                self.put(x + nx * side * (g - 1), y + ny * side * (g - 1), RAIL_DARK)
        for side in (-1, 1):
            for x, y, nx, ny, _ in pts:
                self.put(x + nx * side * g, y + ny * side * g, RAIL_LIGHT)

    def fill(self, path, width, texture):
        tp = texture.load()
        h = width / 2
        for x, y, nx, ny, _ in walk(path, 0.25):
            k = -h
            while k <= h:
                px, py = x + nx * k, y + ny * k
                if 0 <= px < 16 and 0 <= py < 16:
                    self.put(px, py, tp[int(px) % texture.size[0], int(py) % texture.size[1]])
                k += 0.25

    def track(self, *paths):
        for p in paths:
            self.ties(p)
        for p in paths:
            self.rails(p)

    def embedded(self, *paths, plate=None):
        plate = plate or tex('block/smooth_stone.png')
        for p in paths:
            self.fill(p, self.g + 3, plate)
        for p in paths:
            self.rails(p, groove=True)

    def label(self, text):
        x = 0
        for ch in text:
            glyph = FONT[ch]
            for gy, row in enumerate(glyph):
                for gx, c in enumerate(row):
                    if c == '#':
                        self.put(x + gx + 1, gy + 1, SHADOW)
            for gy, row in enumerate(glyph):
                for gx, c in enumerate(row):
                    if c == '#':
                        self.put(x + gx, gy, TEXT)
            x += len(glyph[0]) + 1

FONT = {
    '0': ['###', '#.#', '#.#', '#.#', '###'],
    '1': ['.#', '##', '.#', '.#', '.#'],
    '2': ['###', '..#', '###', '#..', '###'],
    '3': ['###', '..#', '.##', '..#', '###'],
    '4': ['#.#', '#.#', '###', '..#', '..#'],
    '5': ['###', '#..', '###', '..#', '###'],
    '6': ['###', '#..', '###', '#.#', '###'],
    '7': ['###', '..#', '..#', '.#.', '.#.'],
    '8': ['###', '#.#', '###', '#.#', '###'],
    '9': ['###', '#.#', '###', '..#', '###'],
}

# ---------------------------------------------------------------- slopes, side view
def slope(length, material, top=None, ties=True):
    ic = Icon()
    rise = {6: 11, 12: 8, 18: 5}[length]
    mt = material.load()
    y0, y1 = 15.0, 15.0 - rise
    def surf(x):
        return y0 + (y1 - y0) * x / 15
    for x in range(16):
        ys = surf(x + .5)
        for y in range(16):
            if y + .5 >= ys + 2:
                c = mt[x, y]
                if top and y + .5 < ys + 3:
                    c = top
                ic.put(x, y, c)
    for x in range(16):
        ys = round(surf(x + .5))
        ic.put(x, ys, RAIL_LIGHT)
        ic.put(x, ys + 1, RAIL_DARK)
        if ties and (x % 3 == 1):
            ic.put(x, ys + 2, TIE)
            ic.put(x - 1, ys + 2, TIE_DARK)
    ic.label(str(length))
    return ic

def corner_wedge(ic, material, top=None):
    mt = material.load()
    for x in range(9, 16):
        h = (x - 9) * 5 / 6
        for y in range(16 - int(round(h)) - 1, 16):
            c = top if top and y == 16 - int(round(h)) - 1 else mt[x, y]
            ic.put(x, y, c)

# ---------------------------------------------------------------- the pieces
def main():
    os.makedirs(OUT, exist_ok=True)
    gravel, planks = tex('block/gravel.png'), tex('block/oak_planks.png')
    dirt, grass_top = tex('block/dirt.png'), (99, 152, 52)
    ballast, snow = own('blocks/ballast_test.png'), own('blocks/snowgravel.png')
    smooth = tex('block/smooth_stone.png')
    asph = {'': Image.new('RGBA', (16, 16), (34, 35, 37, 255)), '1': Image.new('RGBA', (16, 16), (52, 56, 55, 255)),
            '2': Image.new('RGBA', (16, 16), (122, 122, 118, 255))}
    #a little grain in the black asphalt
    ap = asph[''].load()
    for i in range(0, 256, 7):
        ap[(i * 5) % 16, (i * 3 + i // 16) % 16] = (24, 25, 26, 255)
    icons = {}
    sizes = {'small': 1, 'medium': 3, 'long': 6, 'verylong': 12}
    for e in ('', 'embedded'):
        def make(*paths, label=None, **kw):
            ic = Icon(**kw)
            (ic.embedded if e else ic.track)(*paths)
            if label:
                ic.label(label)
            return ic
        for k, n in sizes.items():
            icons[f'tcrail{e}{k}straight'] = make(straight(), label=str(n))
            icons[f'tcrail{e}{k}diagonalstraight'] = make(diagonal(), label=str(n))
        for k, n, r in (('1x1', 1, 5), ('medium', 3, 6.5), ('large', 5, 8), ('verylarge', 10, 11), ('superlarge', 16, 14), ('29x29', 29, 20), ('32x32', 32, 24)):
            icons[f'tcrail{e}{k}turn'] = make(turn90(r), label=str(n))
        for k, n, r in (('medium45degreeturn', 4, 7), ('large45degreeturn', 6, 9), ('verylarge45degreeturn', 8, 11),
                        ('superlarge45degreeturn', 11, 14), ('45degreeturn9x20', 20, 20), ('45degreeturn10x22', 22, 24)):
            icons[f'tcrail{e}{k}'] = make(turn45(r, 6), label=str(n))
        for k, n, (a, b) in (('smallparallelcurve', 8, (5, 11)), ('mediumparallelcurve', 12, (5, 11)), ('largeparallelcurve', 16, (5, 11)), ('20x2scurve', 20, (5, 11))):
            icons[f'tcrail{e}{k}'] = make(scurve(a, b), label=str(n), gauge=9, tie=12)
        for k, n, r in (('mediumswitch', 4, 8), ('largeswitch', 6, 10), ('verylargeswitch', 11, 12)):
            icons[f'tcrail{e}{k}'] = make(straight(5), turn90(r, 5), label=str(n), gauge=8, tie=10)
        for k, n in (('mediumparallelswitch', 11), ('largeparallelswitch', 17)):
            icons[f'tcrail{e}{k}'] = make(straight(5), scurve(5, 11), label=str(n), gauge=8, tie=10)
        for k, n, r in (('medium45degreeswitch', 5, 9), ('large45degreeswitch', 8, 13)):
            icons[f'tcrail{e}{k}'] = make(straight(5), turn45(r, 5), label=str(n), gauge=8, tie=10)
        cross = dict(gauge=8, tie=11)
        icons[f'tcrail{e}twowayscrossing'] = make(straight(), horizontal(), **cross)
        icons[f'tcrail{e}diamondcrossing'] = make(straight(), diagonal(), **cross)
        icons[f'tcrail{e}doublediamondcrossing'] = make(straight(), diagonal(), diagonal(True), **cross)
        icons[f'tcrail{e}diagonaltwowayscrossing'] = make(diagonal(), diagonal(True), **cross)
        icons[f'tcrail{e}fourwayscrossing'] = make(straight(), horizontal(), diagonal(), diagonal(True), gauge=7, tie=9)
    #road crossings: the asphalt between and beside the rails
    for k, a in asph.items():
        ic = Icon()
        ic.fill(straight(), 14, a)
        ic.rails(straight(), groove=True)
        icons[f'tcrailsmallroadcrossing{k}'] = ic
    ic = Icon()
    ic.fill(straight(), 14, dirt)
    for y in range(16):
        for x in (1, 14):
            ic.put(x, y, grass_top)
    ic.rails(straight(), groove=True)
    icons['tcrailsmallroadcrossingdynamic'] = ic
    #slopes: the rise of one block over 6, 12 or 18 blocks, on what the slope is built of
    for k, n in (('', 6), ('large', 12), ('verylarge', 18)):
        icons[f'tcrail{k}slopewood'] = slope(n, planks)
        icons[f'tcrail{k}slopegravel'] = slope(n, gravel)
        icons[f'tcrail{k}slopeballast'] = slope(n, ballast)
        icons[f'tcrail{k}slopesnowgravel'] = slope(n, snow, top=(240, 251, 251))
        icons[f'tcrail{k}slopedynamic'] = slope(n, dirt, top=grass_top)
        icons[f'tcrailembedded{k}slopedynamic'] = slope(n, dirt, top=grass_top, ties=False)
    for k, n, r in (('large', 5, 8), ('verylarge', 9, 10.5), ('superlarge', 16, 14)):
        for e in ('', 'embedded'):
            ic = Icon()
            p = turn90(r)
            (ic.embedded if e else ic.track)(p)
            corner_wedge(ic, dirt, grass_top)
            ic.label(str(n))
            icons[f'tcrail{e}{k}curvedslopedynamic'] = ic
    for name, ic in icons.items():
        ic.img.save(os.path.join(OUT, name + '.png'))
    return icons

if __name__ == '__main__':
    icons = main()
    print(len(icons), 'icons')
