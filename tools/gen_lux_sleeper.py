# Generates ModelPassengerLuxSleeper.java and its texture: a luxury sleeping car of the early 1900s, navy and gold,
# clerestory roof, corridor along one side, four sleeping compartments. Model units are pixels (1/16 block); as in
# ModelPassenger_DB_oriental: x along, y up, z across.
#
# Every box has its own place on the texture (TMT's box layout), and every face of it is painted by where it is in
# the car: the panels, mouldings, gold lining, planks and patterns run on across the boxes they are made of.
import math
import random
from PIL import Image

ROOT = '/home/_viguro_/src/tc-neo/'
boxes = []  # (x, y, z, w, h, d, material, comment)


def box(x0, x1, y0, y1, z0, z1, mat, c=''):
    boxes.append((x0, y0, z0, x1 - x0, y1 - y0, z1 - z0, mat, c))


def outside(zs, z0, z1):
    return (z0, z1) if zs > 0 else (-z1, -z0)


L = 52      # half length
W = 11      # half width
FLOOR = 10  # floor's top
TOP = 29.5  # walls' top (a little more room than the old coaches: the walk scan wants 1.2 over the floor)

# ---------------------------------------------------------------- bogies, underframe, ends
for cx in (-34, 34):
    for dx in (-6, 6):
        for zs in (-1, 1):
            box(cx + dx - 3, cx + dx + 3, 0, 6, 4.5 if zs > 0 else -5.5, 5.5 if zs > 0 else -4.5, 'wheel', 'wheel')
        box(cx + dx - 0.5, cx + dx + 0.5, 2.5, 3.5, -4.5, 4.5, 'under', 'axle')
    for zs in (-1, 1):
        z0, z1 = (6, 7) if zs > 0 else (-7, -6)
        box(cx - 11, cx + 11, 2, 5, z0, z1, 'bogie', 'bogie side frame')
        for dx in (-6, 6):
            box(cx + dx - 1.5, cx + dx + 1.5, 1.5, 4.5, z0 + 0.5 * zs, z1 + 0.5 * zs, 'steel', 'axlebox')
        box(cx - 3, cx + 3, 3, 5, z0 + 0.6 * zs, z1 + 0.6 * zs, 'steel', 'bolster spring')
    box(cx - 2, cx + 2, 5, 7, -7, 7, 'bogie', 'bolster')
box(-L, L, 6, 7, -9, 9, 'under', 'underframe')
for zs in (-1, 1):
    box(-L, L, 6, 8, 9.5 if zs > 0 else -10.5, 10.5 if zs > 0 else -9.5, 'solebar', 'solebar')
    # the footboard along the body
    box(-40, 40, 5, 5.5, 10.5 if zs > 0 else -12, 12 if zs > 0 else -10.5, 'darkwood', 'footboard')
    for k in range(-3, 4):
        box(k * 12 - 0.5, k * 12 + 0.5, 5.5, 6.5, 10.75 if zs > 0 else -11.25, 11.25 if zs > 0 else -10.75, 'under', 'footboard bracket')
for xs in (-1, 1):
    box(-14 if xs < 0 else 6, -6 if xs < 0 else 14, 3, 6, -6, 6, 'under', 'battery box')
    box(-26 if xs < 0 else 18, -18 if xs < 0 else 26, 4, 6, -3, 3, 'under', 'gas tank')
    bx = L if xs > 0 else -L - 1
    box(bx, bx + 1, 6, 9, -W, W, 'bufferbeam', 'buffer beam')
    for zs in (-1, 1):
        px = L + 1 if xs > 0 else -L - 3
        box(px, px + 2, 6.75, 8.25, 6.25 if zs > 0 else -7.75, 7.75 if zs > 0 else -6.25, 'steel', 'buffer')
        hx = L + 3 if xs > 0 else -L - 3.5
        box(hx, hx + 0.5, 6.25, 8.75, 5.75 if zs > 0 else -8.25, 8.25 if zs > 0 else -5.75, 'steel', 'buffer head')
    hx = L + 1 if xs > 0 else -L - 2
    box(hx, hx + 1, 7, 8, -0.5, 0.5, 'steel', 'coupling hook')
    # steps under the doors
    sx = 45 if xs > 0 else -51
    for zs in (-1, 1):
        box(sx, sx + 6, 4.5, 5, 10.5 if zs > 0 else -12.5, 12.5 if zs > 0 else -10.5, 'darkwood', 'step')
        box(sx, sx + 6, 7, 7.5, 10.5 if zs > 0 else -12, 12 if zs > 0 else -10.5, 'darkwood', 'step')
        # grab handles beside the doors
        gx = (44.5 if xs > 0 else -45) if True else 0
        box(gx, gx + 0.5, 12, 22, 11 if zs > 0 else -11.5, 11.5 if zs > 0 else -11, 'brass', 'grab handle')

# ---------------------------------------------------------------- floor
# (one surface: a frame under the carpet made a second floor a pixel lower)
for zs in (-1, 1):
    box(-L, L, 8, FLOOR, *outside(zs, 10, W), 'navydark', 'floor edge')
box(-44, 44, 8, FLOOR, -10, 10, 'carpet', 'carpet')
for xs in (-1, 1):
    box(44 if xs > 0 else -L, L if xs > 0 else -44, 8, FLOOR, -10, 10, 'parquet', 'vestibule floor')


# ---------------------------------------------------------------- side walls
def wall_strip(x0, x1, y0, y1, zs, outer='navy', inner='teak'):
    zo = (10.5, 11) if zs > 0 else (-11, -10.5)
    zi = (10, 10.5) if zs > 0 else (-10.5, -10)
    box(x0, x1, y0, y1, zo[0], zo[1], outer)
    if inner:
        box(x0, x1, y0, y1, zi[0], zi[1], inner)


def side(zs, windows):
    wall_strip(-L, L, FLOOR, 16, zs, 'navy', None)
    wall_strip(-L, L, 23, TOP - 1, zs, 'navy', None)
    wall_strip(-L, L, TOP - 1, TOP, zs, 'navydark', None)
    # the gold waist and cant rails, a little proud of the side
    box(-L, L, 15.25, 16, *outside(zs, 11, 11.3), 'gold', 'waist rail')
    box(-L, L, TOP - 1.25, TOP - 0.75, *outside(zs, 11, 11.3), 'gold', 'cant rail')
    box(-L, L, FLOOR, 10.5, *outside(zs, 11, 11.4), 'navydark', 'bottom rail')
    # the inside lining over the rooms
    zi = (10, 10.5) if zs > 0 else (-10.5, -10)
    box(-44, 44, FLOOR, 16, zi[0], zi[1], 'teak', 'lining')
    box(-44, 44, 23, TOP, zi[0], zi[1], 'teak', 'lining')
    xs = [-44] + [v for w in windows for v in w] + [44]
    for i in range(0, len(xs), 2):
        a, b = xs[i], xs[i + 1]
        if b > a:
            wall_strip(a, b, 16, 23, zs)
    for a, b in windows:
        # frames round the windows, outside: gold beading; inside: a dark wood sill and blind box
        box(a - 0.25, b + 0.25, 15.75, 16.25, *outside(zs, 11, 11.25), 'gold', 'window frame')
        box(a - 0.25, b + 0.25, 22.75, 23.25, *outside(zs, 11, 11.25), 'gold', 'window frame')
        box(a - 0.25, a, 16, 23, *outside(zs, 11, 11.25), 'gold', 'window frame')
        box(b, b + 0.25, 16, 23, *outside(zs, 11, 11.25), 'gold', 'window frame')
        box(a, b, 15.5, 16, *(zi if zs < 0 else zi), 'darkwood', 'sill')
        box(a, b, 15.5, 16, *outside(zs, 9.25, 10), 'darkwood', 'sill')
        box(a, b, 22.5, 23.5, *outside(zs, 9.5, 10), 'darkwood', 'blind box')
    # a crest between the gold rails in the middle
    box(-2.5, 2.5, 23.5, 26.5, *outside(zs, 11, 11.2), 'crest', 'crest')
    # the vestibules: doors with a window, a handle; the wall beside them
    for xsg in (-1, 1):
        d0, d1 = (45, 51) if xsg > 0 else (-51, -45)
        e0, e1 = (44, 45) if xsg > 0 else (-45, -44)
        f0, f1 = (51, L) if xsg > 0 else (-L, -51)
        for a, b in ((e0, e1), (f0, f1)):
            wall_strip(a, b, 16, 23, zs, 'navy', None)
        wall_strip(d0, d1, FLOOR, 16, zs, 'door', None)
        wall_strip(d0, d0 + 1.5, 16, 23, zs, 'door', None)
        wall_strip(d1 - 1.5, d1, 16, 23, zs, 'door', None)
        box(d0 + 1.5, d1 - 1.5, 16, 23, *outside(zs, 10.6, 10.8), 'glass', 'door window')
        box(d0 + 1.25, d1 - 1.25, 15.75, 16.25, *outside(zs, 11, 11.25), 'gold', 'door window frame')
        box(d0 + 1.25, d1 - 1.25, 22.75, 23.25, *outside(zs, 11, 11.25), 'gold', 'door window frame')
        box(d1 - 2, d1 - 1, 13, 13.5, *outside(zs, 11, 11.6), 'brass', 'door handle')


comp_windows = []
for i in range(4):
    x0 = -44 + 22 * i
    comp_windows += [(x0 + 3, x0 + 13), (x0 + 15, x0 + 19)]
side(1, comp_windows)
side(-1, [(-44 + 11 * k + 2, -44 + 11 * k + 9) for k in range(8)])

# ---------------------------------------------------------------- ends: the gangway door, bellows, lamp brackets
for xs in (-1, 1):
    x0, x1 = (51, 52) if xs > 0 else (-52, -51)
    box(x0, x1, FLOOR, TOP + 1, -W, -3, 'endwall', 'end wall')
    box(x0, x1, FLOOR, TOP + 1, 3, W, 'endwall', 'end wall')
    box(x0, x1, 26, TOP + 1, -3, 3, 'endwall', 'over the gangway door')
    bx0, bx1 = (52, 53.5) if xs > 0 else (-53.5, -52)
    box(bx0, bx1, FLOOR, 27, -5, -3, 'bellows', 'bellows')
    box(bx0, bx1, FLOOR, 27, 3, 5, 'bellows', 'bellows')
    box(bx0, bx1, 26, 27.5, -5, 5, 'bellows', 'bellows')
    for zs in (-1, 1):
        lx = 52 if xs > 0 else -53
        box(lx, lx + 1, 19, 21, 7 if zs > 0 else -8, 8 if zs > 0 else -7, 'lamp', 'tail lamp')

# ---------------------------------------------------------------- inside: corridor wall, doors, partitions
for i in range(4):
    x0 = -44 + 22 * i
    box(x0, x0 + 15, FLOOR, TOP, -4, -3, 'teak', 'corridor wall')
    box(x0 + 20, x0 + 22, FLOOR, TOP, -4, -3, 'teak', 'corridor wall')
    box(x0 + 15, x0 + 20, 25, TOP, -4, -3, 'teak', 'over the door')
    box(x0 + 15, x0 + 15.5, FLOOR, 25, -4.5, -2.5, 'brass', 'door frame')
    box(x0 + 19.5, x0 + 20, FLOOR, 25, -4.5, -2.5, 'brass', 'door frame')
    box(x0 + 15, x0 + 20, 24.5, 25, -4.5, -2.5, 'brass', 'door frame')
    # the compartment's number plate in the corridor
    box(x0 + 16.5, x0 + 18.5, 25.5, 26.5, -4.3, -4, 'brass', 'number plate')
    # sliding door, open, in front of the wall
    box(x0 + 9.5, x0 + 14.5, FLOOR + 0.5, 24.5, -2.9, -2.5, 'doorpanel', 'compartment door, open')
for px in (-22, 0, 22):
    box(px - 0.5, px + 0.5, FLOOR, TOP, -3, 10, 'teak', 'partition')
for xs in (-1, 1):
    px = 44 if xs > 0 else -44
    box(px - 0.5, px + 0.5, FLOOR, TOP, -3, 10, 'teak', 'end partition')
    box(px - 0.5, px + 0.5, 25, TOP, -10, -4, 'teak', 'over the corridor end')
    box(px - 0.5, px + 0.5, FLOOR, TOP, -4, -3, 'teak', 'corner')

for i in range(4):
    x0 = -44 + 22 * i
    s0 = x0 + 0.5
    box(s0, s0 + 7, FLOOR + 1, 14, -2.5, 10, 'velvet', 'sofa seat')
    box(s0, s0 + 7, FLOOR, FLOOR + 1, -2.5, 10, 'darkwood', 'sofa base')
    box(s0 + 6.75, s0 + 7.25, FLOOR, 13.5, -2.5, 10, 'darkwood', 'sofa front')
    box(s0, s0 + 2, 14, 22, -2.5, 10, 'velvet', 'sofa back')
    box(s0, s0 + 2.5, 22, 22.5, -2.5, 10, 'darkwood', 'sofa back top')
    box(s0, s0 + 3.5, 14, 16.5, -3, -2.5, 'darkwood', 'armrest')
    box(s0, s0 + 2, 24, 27, -2.5, 10, 'berth', 'upper berth, folded')
    box(s0 + 2, s0 + 2.5, 24, 27, -2.5, 10, 'darkwood', 'berth edge')
    box(s0 + 0.5, s0 + 2.5, 27, 27.5, -2, 9.5, 'brass', 'berth rail')
    box(s0 + 1.5, s0 + 3, 19, 21, 3, 6, 'linen', 'pillow')
    box(x0 + 14, x0 + 21, 16, 17, 7, 10, 'darkwood', 'table')
    box(x0 + 17, x0 + 18, FLOOR, 16, 8, 9, 'brass', 'table leg')
    box(x0 + 16, x0 + 17, 17, 19, 8, 9, 'lamp', 'table lamp')
    box(x0 + 19, x0 + 21.5, FLOOR, 18, 1, 6, 'cabinet', 'washstand')
    box(x0 + 18.75, x0 + 21.5, 18, 18.5, 0.75, 6.25, 'marble', 'washstand top')
    box(x0 + 21.25, x0 + 21.5, 18.75, 24.25, 1.25, 5.75, 'gold', 'mirror frame')
    box(x0 + 21, x0 + 21.25, 19, 24, 1.5, 5.5, 'mirror', 'mirror')
    # a luggage rack over the door
    box(x0 + 9, x0 + 21, 25, 25.5, -2.5, 1, 'brass', 'luggage rack')
box(-44, 44, 19, 19.5, -10, -9.5, 'brass', 'handrail')

# ---------------------------------------------------------------- ceiling and the clerestory roof
box(-L, L, TOP, TOP + 1, -W, W, 'ceiling', 'ceiling')
box(-L - 1, L + 1, TOP + 1, TOP + 1.5, -W - 1, W + 1, 'roof', 'roof')
box(-L - 1, L + 1, TOP + 1.5, TOP + 2, -W, W, 'roof', 'roof')
box(-L - 1, L + 1, TOP + 2, TOP + 2.5, -9.5, 9.5, 'roof', 'roof')
box(-L - 1, L + 1, TOP + 2.5, TOP + 3, -7.5, 7.5, 'roof', 'roof')
for zs in (-1, 1):
    box(-L - 1, L + 1, TOP + 1, TOP + 1.5, *outside(zs, W + 1, W + 1.5), 'gold', 'rain strip')
box(-46, 46, TOP + 3, TOP + 6, -5, -4.5, 'clerestory', 'clerestory side')
box(-46, 46, TOP + 3, TOP + 6, 4.5, 5, 'clerestory', 'clerestory side')
box(-46, 46, TOP + 3, TOP + 6, -4.5, 4.5, 'ceiling', 'clerestory inside')
box(-47, 47, TOP + 6, TOP + 6.5, -6, 6, 'roof', 'clerestory roof')
box(-47, 47, TOP + 6.5, TOP + 7, -5, 5, 'roof', 'clerestory roof')
for xs in (-1, 1):
    e0, e1 = (46, 47) if xs > 0 else (-47, -46)
    box(e0, e1, TOP + 3, TOP + 6, -5, 5, 'roof', 'clerestory end')
for k in range(4):
    cx = -33 + 22 * k
    box(cx - 1, cx + 1, TOP + 7, TOP + 8, -1, 1, 'steel', 'lamp vent')
    box(cx - 1.5, cx + 1.5, TOP + 8, TOP + 8.5, -1.5, 1.5, 'steel', 'lamp vent cap')

# ================================================================ texture atlas
def tex_dim(v):
    # TMT rounds a box's sizes for its texture: whole, at least 1
    if v % 1 != 0:
        v = 1 if v < 1 else int(v) + (1 if v % 1 > 0.5 else 0)
    return int(v)


# texels per model pixel: the boxes are made SCALE times larger and drawn 1/SCALE (TMT gives a box one texel
# per unit of its size), so the lines can be finer than a pixel
SCALE = 2
ATLAS_W = 2048
placed = []  # (u, v) per box
order = sorted(range(len(boxes)), key=lambda i: -(tex_dim(boxes[i][5] * SCALE) + tex_dim(boxes[i][4] * SCALE)))
shelf_y, shelf_h, cur_x = 0, 0, 0
uv = [None] * len(boxes)
for i in order:
    x, y, z, w, h, d, mat, c = boxes[i]
    tw, th, td = tex_dim(w * SCALE), tex_dim(h * SCALE), tex_dim(d * SCALE)
    fw, fh = 2 * (tw + td), td + th
    if cur_x + fw > ATLAS_W:
        shelf_y += shelf_h + 1
        cur_x, shelf_h = 0, 0
    uv[i] = (cur_x, shelf_y)
    cur_x += fw + 1
    shelf_h = max(shelf_h, fh)
ATLAS_H = shelf_y + shelf_h + 1
ATLAS_H = 1 << (ATLAS_H - 1).bit_length()

# ================================================================ painting
rnd = random.Random(11)


def clamp(c):
    return tuple(max(0, min(255, int(round(v)))) for v in c)


def mix(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def shade(c, f):
    return tuple(v * f for v in c)


def hsh(*a):
    # a stable pseudo-random number in [0, 1) for those numbers
    h = 2166136261
    for v in a:
        h = ((h ^ (int(math.floor(v * 4)) & 0xffffffff)) * 16777619) & 0xffffffff
    return (h % 10007) / 10007.0


NAVY = (24, 34, 68)
NAVY_D = (14, 21, 44)
GOLD = (214, 172, 58)
TEAK = (128, 74, 34)
TEAK_D = (92, 50, 22)
DARKWOOD = (70, 38, 18)
CREAM = (226, 214, 184)
BRASS = (196, 156, 62)


def in_panel(a, lo, hi, border):
    return lo + border <= a < hi - border


def mat_navy(x, y, z, n):
    # panels between the windows and over the length: raised beading (lighter) at the edges, a fine gold line inset
    c = NAVY
    if n[1] != 0 or n[0] != 0:
        return shade(c, 0.85)
    yy = y
    # bands: lower panel 10.5..15.25, window band 16..23, upper panel 23.25..26.75
    if 10.5 <= yy < 15.25:
        y0, y1 = 10.5, 15.25
    elif 23.25 <= yy < TOP - 1.25:
        y0, y1 = 23.25, TOP - 1.25
    else:
        return shade(c, 0.95 + 0.05 * hsh(x, y))
    p = 11.0
    px0 = math.floor((x + 44) / p) * p - 44
    lx = x - px0
    ly = y - y0
    hy = y1 - y0
    if lx < 0.5 or ly > hy - 0.5:
        return mix(c, (64, 82, 128), 0.55)  # beading, lit (from above, left)
    if lx > p - 0.5 or ly < 0.5:
        return shade(c, 0.55)  # beading, its shadow
    if (abs(lx - 1.25) < 0.25 or abs(lx - (p - 1.25)) < 0.25) and 1.0 <= ly <= hy - 1.0 or \
            (abs(ly - 1.25) < 0.25 or abs(ly - (hy - 1.25)) < 0.25) and 1.0 <= lx <= p - 1.0:
        return GOLD
    return shade(c, 0.97 + 0.06 * hsh(x, y, z))


def mat_navydark(x, y, z, n):
    return shade(NAVY_D, 1.1 if y > TOP - 0.5 else 0.9)


def mat_gold(x, y, z, n):
    t = 1.0 + (0.18 if n[1] > 0 else -0.25 if n[1] < 0 else 0.0)
    return shade(mix(GOLD, (250, 220, 120), 0.3 * hsh(x, y, z)), t)


def mat_door(x, y, z, n):
    # the vestibule door: navy, two panels edged with gold
    c = NAVY
    lx = (x - 45) if x > 0 else (x + 51)
    if lx < 0.4 or lx > 5.6 or y < FLOOR + 0.4:
        return shade(c, 0.7)
    if y < 16 and (abs(lx - 1) < 0.35 or abs(lx - 5) < 0.35 or abs(y - 11) < 0.35 or abs(y - 15) < 0.35):
        return GOLD
    return shade(c, 1.05)


def mat_endwall(x, y, z, n):
    if n[0] == 0:
        return NAVY
    # boards, vertical
    return shade(NAVY, 0.85 + 0.1 * ((int(math.floor(z + 20)) % 2)))


def mat_teak(x, y, z, n):
    # panelled: stiles and rails of darker wood, panels of figured teak, a carved line inside them
    along = x if n[0] == 0 else z
    p = 5.5
    a = (along + 100) % p
    yy = y - FLOOR
    if yy < 1.0:
        return DARKWOOD  # skirting
    if abs(yy - 6.0) < 0.5 or abs(yy - 17.5) < 0.5:
        return shade(DARKWOOD, 1.15)  # rails
    if a < 0.7:
        return shade(TEAK_D, 0.95)  # stiles
    if abs(a - 1.2) < 0.3 or abs(a - (p - 0.5)) < 0.3:
        return shade(TEAK, 1.18)  # lit moulding
    grain = math.sin((y * 0.9 + 3 * math.sin(along * 0.7)) * 1.7)
    return shade(TEAK, 0.92 + 0.08 * grain + 0.05 * hsh(along, y))


def mat_darkwood(x, y, z, n):
    grain = math.sin((x + z) * 1.3 + 2 * math.sin(y * 0.8))
    return shade(DARKWOOD, 0.95 + 0.1 * grain + (0.12 if n[1] > 0 else 0))


def mat_parquet(x, y, z, n):
    if n[1] <= 0:
        return DARKWOOD
    i, j = math.floor(x / 2), math.floor(z / 2)
    t = 0.8 + 0.25 * ((i + j) % 2) + 0.06 * hsh(i, j)
    return shade((140, 92, 46), t)


def mat_carpet(x, y, z, n):
    if n[1] <= 0:
        return DARKWOOD
    red = (118, 22, 32)
    # a border by the walls, a pattern of gold diamonds with a dot
    if abs(z) > 9.0 or (-4.5 < z < -2.5):
        return (60, 14, 22) if abs(z) > 9.5 else (170, 130, 50)
    a, b = (x % 4) - 2, (z % 4) - 2
    if abs(abs(a) + abs(b) - 1.5) < 0.4:
        return (180, 140, 56)
    if abs(a) + abs(b) < 0.5:
        return (210, 180, 90)
    return shade(red, 0.95 + 0.08 * hsh(x, z))


def mat_velvet(x, y, z, n):
    c = (138, 26, 40)
    if n[1] < 0:
        return shade(c, 0.5)
    # deep buttoned: a dark dimple on a grid, the cushion lit round it
    u = z
    v = y if n[1] == 0 else x
    a, b = (u % 2.5) - 1.25, (v % 2.5) - 1.25
    r = math.hypot(a, b)
    if r < 0.35:
        return shade(c, 0.45)
    return shade(c, 1.12 - 0.2 * r + 0.05 * hsh(x, y, z))


def mat_glass(x, y, z, n):
    t = (x + y * 0.8) % 7
    return (70, 96, 112) if t < 1.0 else (44, 62, 76)


def mat_mirror(x, y, z, n):
    t = (z * 1.3 + y) % 5
    return (210, 222, 228) if t < 0.8 else (150, 166, 176)


def mat_ceiling(x, y, z, n):
    if n[1] > 0:
        return shade(CREAM, 0.8)
    # ribs every compartment's width, a gilt line along
    if (x + 44) % 5.5 < 0.5:
        return shade(CREAM, 0.82)
    if abs(abs(z) - 7.5) < 0.3:
        return (200, 166, 80)
    return shade(CREAM, 0.97 + 0.04 * hsh(x, z))


def mat_clerestory(x, y, z, n):
    if n[2] == 0:
        return NAVY_D
    # small windows in a row along it
    cx = (x + 46) % 8.4
    if 2.2 < cx < 6.2 and TOP + 3.8 < y < TOP + 5.2:
        return mat_glass(x, y, z, n) if n[2] * z > 0 else (240, 228, 180)
    return NAVY_D


def mat_roof(x, y, z, n):
    c = (150, 146, 138)
    if n[1] > 0:
        # canvas, painted; seams across
        if (x + 100) % 6 < 0.5:
            return shade(c, 0.8)
        return shade(c, 0.95 + 0.07 * hsh(x, z))
    return shade(c, 0.7)


def mat_under(x, y, z, n):
    return shade((34, 34, 36), 0.9 + 0.15 * hsh(x, y, z))


def mat_solebar(x, y, z, n):
    if n[2] != 0 and (x % 3) < 0.5 and abs(y - 7) < 0.4:
        return (78, 78, 82)  # rivets
    return (28, 28, 30)


def mat_bogie(x, y, z, n):
    if n[2] != 0 and (x % 4) < 0.6 and abs(y - 4.5) < 0.35:
        return (80, 80, 84)
    return (40, 40, 42) if n[1] <= 0 else (54, 54, 56)


def mat_wheel(x, y, z, n):
    if n[2] == 0:
        return (90, 90, 94) if n[1] < 0 else (60, 60, 62)
    cx = (math.floor(x / 6 + 0.5)) * 6 if False else None
    # the wheel's centre from the box: nearest of the wheel centres
    centres = [c + d for c in (-34, 34) for d in (-6, 6)]
    wc = min(centres, key=lambda c: abs(c - x))
    r = math.hypot(x - wc, y - 3)
    if r > 3.05:
        return (24, 24, 26)
    if r > 2.5:
        return (150, 150, 156)  # the tyre, bright
    if r < 0.7:
        return (70, 70, 74)  # boss
    ang = math.atan2(y - 3, x - wc)
    return (58, 58, 62) if (math.degrees(ang) % 36) < 12 else (36, 36, 38)


def mat_steel(x, y, z, n):
    return shade((120, 122, 128), 1.1 if n[1] > 0 else 0.85)


def mat_bufferbeam(x, y, z, n):
    return (150, 30, 30) if n[0] != 0 else (40, 20, 20)


def mat_brass(x, y, z, n):
    return shade(BRASS, 1.2 if n[1] > 0 else 0.9 + 0.2 * hsh(x, y, z))


def mat_linen(x, y, z, n):
    return shade((236, 232, 222), 0.95 + 0.05 * hsh(x, y, z))


def mat_berth(x, y, z, n):
    # folded up: its underside shows, wood, with a blanket's edge in red
    if n[0] != 0 and abs(y - 25.5) < 0.6:
        return (150, 30, 40)
    return mat_teak(x, y, z, n)


def mat_marble(x, y, z, n):
    v = math.sin(x * 2.1 + z * 1.3 + 2 * math.sin(z))
    return (230, 230, 226) if v < 0.85 else (170, 170, 168)


def mat_cabinet(x, y, z, n):
    if n[0] != 0 and abs(y - 14) < 0.35:
        return BRASS
    return mat_darkwood(x, y, z, n)


def mat_doorpanel(x, y, z, n):
    # the compartment's sliding door: teak with a frosted glass light at the top
    if 18 < y < 23.5:
        # an etched glass light, framed
        if y < 18.5 or y > 23 or (x % 5) < 0.5 or (x % 5) > 4.5:
            return mat_darkwood(x, y, z, n)
        a, b = (x % 1.5) - 0.75, (y % 1.5) - 0.75
        return (178, 182, 168) if abs(abs(a) + abs(b) - 0.5) < 0.2 else (150, 156, 146)
    return mat_teak(x, y, z, n)


def mat_bellows(x, y, z, n):
    return shade((26, 24, 22), 0.8 + 0.3 * ((math.floor(x * 2) + math.floor(y)) % 2))


def mat_lamp(x, y, z, n):
    return (250, 214, 120) if n[1] == 0 else (40, 30, 20)


def mat_crest(x, y, z, n):
    # a gold oval with a navy field and a gold star in it
    a, b = x / 2.5, (y - 25) / 1.5
    r = a * a + b * b
    if r > 1.0:
        return NAVY
    if r > 0.6:
        return GOLD
    if abs(a) < 0.15 or abs(b) < 0.2:
        return GOLD
    return NAVY_D


MATS = {k[4:]: v for k, v in globals().items() if k.startswith('mat_')}

img = Image.new('RGB', (ATLAS_W, ATLAS_H), (255, 0, 255))
pix = img.load()


def paint_face(u0, v0, cols, rows, coord, normal, mat):
    f = MATS[mat]
    for i in range(cols):
        for j in range(rows):
            x, y, z = coord((i + 0.5) / cols, (j + 0.5) / rows)
            pix[u0 + i, v0 + j] = clamp(f(x, y, z, normal))


for bi, (x, y, z, w, h, d, mat, c) in enumerate(boxes):
    u, v = uv[bi]
    tw, th, td = tex_dim(w * SCALE), tex_dim(h * SCALE), tex_dim(d * SCALE)
    x1, y1, z1 = x + w, y + h, z + d
    # TMT's layout (ModelRendererTurbo.addRectShape): s, t across the rectangle from its left/top
    paint_face(u + td + tw, v + td, td, th, lambda s, t: (x1, y + t * h, z + s * d), (1, 0, 0), mat)        # +x
    paint_face(u, v + td, td, th, lambda s, t: (x, y + t * h, z1 - s * d), (-1, 0, 0), mat)                # -x
    paint_face(u + td, v, tw, td, lambda s, t: (x + s * w, y, z1 - t * d), (0, -1, 0), mat)                # y0
    paint_face(u + td + tw, v, tw, td, lambda s, t: (x + s * w, y1, z + t * d), (0, 1, 0), mat)            # y1
    paint_face(u + td, v + td, tw, th, lambda s, t: (x + s * w, y + t * h, z), (0, 0, -1), mat)            # z0
    paint_face(u + 2 * td + tw, v + td, tw, th, lambda s, t: (x1 - s * w, y + t * h, z1), (0, 0, 1), mat)  # z1

img.save(ROOT + 'src/main/resources/assets/traincraft/textures/trains/passenger_lux_sleeper.png')

# ================================================================ the java
def fmt(v):
    s = ('%.3f' % v).rstrip('0').rstrip('.')
    return s + 'F'


out = ['''package train.render.models;

import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;

/**
 * A luxury sleeping car of the early 1900s, made for this port (written by tools/gen_lux_sleeper.py, with its
 * texture): navy and gold, a clerestory roof, a corridor along one side, four sleeping compartments (a velvet sofa,
 * the upper berth folded up, a table by the window, a washstand with a mirror), vestibules with doors at both ends.
 * x along, y up, z across, in pixels.
 */
public class ModelPassengerLuxSleeper extends ModelBase {
    static final int TEX_W = %d, TEX_H = %d;

    public ModelPassengerLuxSleeper() {
        bodyModel = new ModelRendererTurbo[%d];
''' % (ATLAS_W, ATLAS_H, len(boxes))]
for i, (x, y, z, w, h, d, mat, c) in enumerate(boxes):
    u, v = uv[i]
    out.append('        bodyModel[%d] = new ModelRendererTurbo(this, %d, %d, TEX_W, TEX_H);%s' % (i, u, v, (' // ' + c) if c else ''))
    out.append('        bodyModel[%d].addBox(%s, %s, %s, %s, %s, %s, 0, %s);' % (i, fmt(x * SCALE), fmt(y * SCALE), fmt(z * SCALE),
                                                                         fmt(w * SCALE), fmt(h * SCALE), fmt(d * SCALE), fmt(1.0 / SCALE)))
out.append('    }\n}\n')
open(ROOT + 'src/main/java/train/render/models/ModelPassengerLuxSleeper.java', 'w').write('\n'.join(out))
print(len(boxes), 'boxes, texture', ATLAS_W, 'x', ATLAS_H)
