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
GROOVE = (100, 100, 102)
EDGE = (62, 62, 64)
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
    def __init__(self, gauge=11.0, tie=14.0, spacing=4.0, rows=2, shade=True, edge=False):
        self.img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        self.px = self.img.load()
        self.g, self.tl, self.ts, self.rows, self.shade, self.edge = gauge, tie, spacing, rows, shade, edge

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
            for row, c in ((0, TIE), (1, TIE_DARK))[:self.rows]:
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
                    self.put(x + nx * side * (g - (2 if self.shade else 1)), y + ny * side * (g - (2 if self.shade else 1)), GROOVE)
                if self.shade:
                    self.put(x + nx * side * (g - 1), y + ny * side * (g - 1), RAIL_DARK)
                if self.edge:
                    self.put(x + nx * side * (g + 1), y + ny * side * (g + 1), EDGE)
        for side in (-1, 1):
            for x, y, nx, ny, _ in pts:
                self.put(x + nx * side * g, y + ny * side * g, (212, 213, 212) if groove else RAIL_LIGHT)

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
        edge, self.edge = self.edge, False
        if edge:
            for p in paths:
                self.fill(p, self.g + 4, Image.new('RGBA', (1, 1), EDGE + (255,)))
        for p in paths:
            self.fill(p, self.g + (2 if edge else 3), plate)
        for p in paths:
            self.rails(p, groove=True)
        self.edge = edge

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

# ---------------------------------------------------------------- the pieces, whole, seen from above
#every piece is drawn whole as it lies on the ground, fitted into the icon: a 90 degree turn is a whole quarter
#circle, a switch both its ways; the straights are as long as they are (1 short, 12 across the icon)
PLAN = dict(gauge=6.0, tie=8.0, spacing=3.0, rows=1, shade=False, edge=True)

def fit(path, box=(2.5, 2.5, 13.5, 13.5)):
    """the path (in blocks) scaled and moved to fill the box, keeping its shape"""
    xs, ys = [p[0] for p in path], [p[1] for p in path]
    w, h = max(xs) - min(xs), max(ys) - min(ys)
    k = min((box[2] - box[0]) / (w or 1e-9), (box[3] - box[1]) / (h or 1e-9))
    ox = (box[0] + box[2]) / 2 - (min(xs) + w / 2) * k
    oy = (box[1] + box[3]) / 2 - (min(ys) + h / 2) * k
    return [(x * k + ox, y * k + oy) for x, y in path], k, ox, oy

def place(path, k, ox, oy):
    return [(x * k + ox, y * k + oy) for x, y in path]

def plan(*paths, label=None, embedded=False, under=None, ties_on=None):
    """paths in blocks (y down, the way the piece goes is up), all fitted with one scale; turned round so that it
    comes in at the top and bends to the bottom left, which leaves the top left corner to the number"""
    paths = [[(-x, -y) for x, y in path] for path in paths]
    allp = [p for path in paths for p in path]
    _, k, ox, oy = fit(allp)
    ic = Icon(**PLAN)
    placed = [place(p, k, ox, oy) for p in paths]
    if under:
        under(ic, placed)
    if embedded:
        ic.embedded(*placed)
    else:
        for p in (placed if ties_on is None else placed[:ties_on]):
            ic.ties(p)
        for p in placed:
            ic.rails(p)
    if label:
        ic.label(label)
    return ic

def b_straight(n):
    return line((0, n), (0, 0))

def b_turn90(n):
    r = n - 0.5
    return arc(r, n, r, 180, 90)

def b_turn45(a, b):
    #a 45 degree turn a blocks across and b along: an arc to 45 degrees and the diagonal on
    r = a / (1 - math.cos(math.radians(45))) * 0.6
    p = arc(r, b, r, 180, 135)
    ex, ey = p[-1]
    rest = max(0.0, (b - (b - ey)) )
    return p + line((ex, ey), (ex + ey * 0.0 + (a - (ex)) , ey - (a - ex)))[1:] if a > ex else p

def b_scurve(off, n):
    pts = []
    for t in range(129):
        u = t / 128
        s = u * u * (3 - 2 * u)
        pts.append((off * s, n * (1 - u)))
    return pts

def straight_len(n, diagonal_=False):
    """the straights: the longer, the longer on the icon"""
    px = {1: 5, 3: 9, 6: 13, 12: 18}[n]
    ic = Icon()
    if diagonal_:
        h = px / 2 / math.sqrt(2) * 1.15
        ic.track(line((8 - h, 8 + h), (8 + h, 8 - h)))
    else:
        ic.track(line((8, 8 + px / 2), (8, 8 - px / 2)))
    return ic

def main():
    os.makedirs(OUT, exist_ok=True)
    gravel, planks = tex('block/gravel.png'), tex('block/oak_planks.png')
    dirt, grass_top = tex('block/dirt.png'), (99, 152, 52)
    ballast, snow = own('blocks/ballast_test.png'), own('blocks/snowgravel.png')
    asph = {'': Image.new('RGBA', (16, 16), (34, 35, 37, 255)), '1': Image.new('RGBA', (16, 16), (52, 56, 55, 255)),
            '2': Image.new('RGBA', (16, 16), (122, 122, 118, 255))}
    ap = asph[''].load()
    for i in range(0, 256, 7):
        ap[(i * 5) % 16, (i * 3 + i // 16) % 16] = (24, 25, 26, 255)
    icons = {}
    sizes = {'small': 1, 'medium': 3, 'long': 6, 'verylong': 12}
    for e in ('', 'embedded'):
        emb = bool(e)
        for k, n in sizes.items():
            for diag in (False, True):
                ic = Icon(**PLAN)
                px = {1: 4, 3: 8, 6: 12, 12: 18}[n]
                if diag:
                    h = px / 2 / math.sqrt(2) * 1.15
                    p = line((9 - h, 9 + h), (9 + h, 9 - h))
                else:
                    p = line((10, 9 + px / 2), (10, 9 - px / 2))
                (ic.embedded if emb else ic.track)(p)
                ic.label(str(n))
                icons[f'tcrail{e}{k}{"diagonal" if diag else ""}straight'] = ic
        for k, n in (('1x1', 1), ('medium', 3), ('large', 5), ('verylarge', 10), ('superlarge', 16), ('29x29', 29), ('32x32', 32)):
            icons[f'tcrail{e}{k}turn'] = plan(b_turn90(n), label=str(n), embedded=emb)
        for k, a, b in (('medium45degreeturn', 3, 4), ('large45degreeturn', 3, 6), ('verylarge45degreeturn', 4, 8),
                        ('superlarge45degreeturn', 5, 11), ('45degreeturn9x20', 9, 20), ('45degreeturn10x22', 10, 22)):
            r = b * 1.4
            p = arc(r, b, r, 180, 135)
            icons[f'tcrail{e}{k}'] = plan(p, label=str(b), embedded=emb)
        for k, off, n in (('smallparallelcurve', 2, 8), ('mediumparallelcurve', 3, 12), ('largeparallelcurve', 4, 16), ('20x2scurve', 2, 20)):
            icons[f'tcrail{e}{k}'] = plan(b_scurve(off * 1.6, n), label=str(n), embedded=emb)
        for k, n in (('mediumswitch', 4), ('largeswitch', 6), ('verylargeswitch', 11)):
            icons[f'tcrail{e}{k}'] = plan(b_straight(n), b_turn90(n), label=str(n), embedded=emb)
        for k, off, n in (('mediumparallelswitch', 4, 11), ('largeparallelswitch', 4, 17)):
            icons[f'tcrail{e}{k}'] = plan(b_straight(n), b_scurve(off * 1.5, n), label=str(n), embedded=emb)
        for k, n in (('medium45degreeswitch', 5), ('large45degreeswitch', 8)):
            r = n * 1.4
            icons[f'tcrail{e}{k}'] = plan(b_straight(n), arc(r, n, r, 180, 135), label=str(n), embedded=emb)
        m = 6
        hor, ver = line((-m, 0), (m, 0)), line((0, m), (0, -m))
        d1, d2 = line((-m * .75, m * .75), (m * .75, -m * .75)), line((-m * .75, -m * .75), (m * .75, m * .75))
        icons[f'tcrail{e}twowayscrossing'] = plan(ver, hor, embedded=emb, ties_on=1)
        icons[f'tcrail{e}diamondcrossing'] = plan(ver, d1, embedded=emb, ties_on=1)
        icons[f'tcrail{e}doublediamondcrossing'] = plan(ver, d1, d2, embedded=emb, ties_on=1)
        icons[f'tcrail{e}diagonaltwowayscrossing'] = plan(d1, d2, embedded=emb, ties_on=1)
        icons[f'tcrail{e}fourwayscrossing'] = plan(ver, hor, d1, d2, embedded=emb, ties_on=1)
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
    for k, n in (('', 6), ('large', 12), ('verylarge', 18)):
        icons[f'tcrail{k}slopewood'] = slope(n, planks)
        icons[f'tcrail{k}slopegravel'] = slope(n, gravel)
        icons[f'tcrail{k}slopeballast'] = slope(n, ballast)
        icons[f'tcrail{k}slopesnowgravel'] = slope(n, snow, top=(240, 251, 251))
        icons[f'tcrail{k}slopedynamic'] = slope(n, dirt, top=grass_top)
        icons[f'tcrailembedded{k}slopedynamic'] = slope(n, dirt, top=grass_top, ties=False)
    #curved slopes: the whole quarter circle on its bank, the bank lighter where it is higher
    def bank(ic, placed):
        p = placed[0]
        pts = walk(p, 0.2)
        total = pts[-1][4]
        for x, y, nx, ny, s in pts:
            k = -6.0
            while k <= 6.0:
                if abs(k) >= 4.5:
                    f = s / total
                    c = tuple(int(v * (0.55 + 0.7 * f)) for v in grass_top)
                    ic.put(x + nx * k, y + ny * k, tuple(min(255, v) for v in c))
                k += 0.25
    for k, n in (('large', 5), ('verylarge', 9), ('superlarge', 16)):
        for e in ('', 'embedded'):
            icons[f'tcrail{e}{k}curvedslopedynamic'] = plan(b_turn90(n), label=str(n), embedded=bool(e), under=bank)
    for name, ic in icons.items():
        ic.img.save(os.path.join(OUT, name + '.png'))
    return icons

if __name__ == '__main__':
    icons = main()
    print(len(icons), 'icons')
