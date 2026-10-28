# Generates ModelPassengerLuxSleeper.java and its texture: a luxury sleeping car of the early 1900s, navy and gold,
# clerestory roof, corridor along one side, four sleeping compartments. Model units are pixels (1/16 block); as in
# ModelPassenger_DB_oriental: x along, y up, z across.
import random
from PIL import Image

BAND = 48
NAMES = ['navy','gold','roof','under','wheel','teak','darkwood','carpet','velvet','glass','ceiling','brass','linen','marble','navydark','steel']
B = {n:i for i,n in enumerate(NAMES)}
boxes = []  # (x,y,z,w,h,d,band,comment)
def box(x0,x1,y0,y1,z0,z1,band,c=''):
    boxes.append((x0,y0,z0,x1-x0,y1-y0,z1-z0,band,c))

L = 52      # half length
W = 11      # half width
FLOOR = 10  # floor's top
TOP = 28    # walls' top
# --- bogies and underframe
for cx in (-34, 34):
    for dx in (-6, 6):
        for zs in (-1, 1):
            box(cx+dx-3, cx+dx+3, 0, 6, 4.5 if zs > 0 else -5.5, 5.5 if zs > 0 else -4.5, 'wheel', 'wheel')
        box(cx+dx-0.5, cx+dx+0.5, 2.5, 3.5, -4.5, 4.5, 'under', 'axle')
    for zs in (-1, 1):
        box(cx-11, cx+11, 2, 5, 6 if zs > 0 else -7, 7 if zs > 0 else -6, 'under', 'bogie side frame')
        box(cx-2, cx+2, 1, 3, 6.2 if zs > 0 else -7.2, 7.2 if zs > 0 else -6.2, 'steel', 'axlebox spring')
    box(cx-2, cx+2, 5, 7, -7, 7, 'under', 'bolster')
box(-L, L, 6, 7, -9, 9, 'under', 'underframe')
for zs in (-1, 1):
    box(-L, L, 6, 8, 9.5 if zs > 0 else -10.5, 10.5 if zs > 0 else -9.5, 'under', 'solebar')
for xs in (-1, 1):
    box(-14 if xs < 0 else 6, -6 if xs < 0 else 14, 3, 6, -6, 6, 'under', 'battery box')
    # buffer beam, buffers, coupling hook
    bx = L if xs > 0 else -L-1
    box(bx, bx+1, 6, 9, -W, W, 'under', 'buffer beam')
    for zs in (-1, 1):
        px = L+1 if xs > 0 else -L-3
        box(px, px+2, 6.5, 8.5, 6 if zs > 0 else -8, 8 if zs > 0 else -6, 'steel', 'buffer')
    hx = L+1 if xs > 0 else -L-2
    box(hx, hx+1, 7, 8, -0.5, 0.5, 'steel', 'coupling hook')
# --- floor
box(-L, L, 8, 9, -W, W, 'under', 'floor frame')
box(-44, 44, 9, FLOOR, -10, 10, 'carpet', 'carpet')
for xs in (-1, 1):
    box(44 if xs > 0 else -L, L if xs > 0 else -44, 9, FLOOR, -10, 10, 'darkwood', 'vestibule floor')
    # steps under the doors
    sx = 45 if xs > 0 else -51
    for zs in (-1, 1):
        box(sx, sx+6, 5, 6, 10.5 if zs > 0 else -12.5, 12.5 if zs > 0 else -10.5, 'under', 'step')

# --- side walls: outside navy (half a pixel), inside teak (half a pixel) where there are rooms
def wall_strip(x0, x1, y0, y1, zs, outer='navy', inner='teak'):
    zo = (10.5, 11) if zs > 0 else (-11, -10.5)
    zi = (10, 10.5) if zs > 0 else (-10.5, -10)
    box(x0, x1, y0, y1, zo[0], zo[1], outer)
    if inner: box(x0, x1, y0, y1, zi[0], zi[1], inner)

def side(zs, windows):
    # windows: list of (x0, x1) openings between y 16 and 23, inside -44..44
    # lower panel, gold line, upper panel, cornice over the whole length
    wall_strip(-L, L, FLOOR, 15, zs, 'navy', None)
    box(-L, L, 15, 16, (11 if zs > 0 else -11.25), (11.25 if zs > 0 else -11), 'gold', 'gold line')
    wall_strip(-L, L, 15, 16, zs, 'navy', None)
    wall_strip(-L, L, 23, 27, zs, 'navy', None)
    box(-L, L, 27, 28, (11 if zs > 0 else -11.25), (11.25 if zs > 0 else -11), 'gold', 'gold line')
    wall_strip(-L, L, 27, TOP, zs, 'navydark', None)
    # the inner lining over the rooms, below and above the windows
    zi = (10, 10.5) if zs > 0 else (-10.5, -10)
    box(-44, 44, FLOOR, 16, zi[0], zi[1], 'teak', 'lining')
    box(-44, 44, 23, TOP, zi[0], zi[1], 'teak', 'lining')
    # piers between the windows
    xs = [-44] + [v for w in windows for v in w] + [44]
    for i in range(0, len(xs), 2):
        a, b = xs[i], xs[i+1]
        if b > a: wall_strip(a, b, 16, 23, zs)
    # window frames (dark wood sills)
    for a, b in windows:
        box(a, b, 15.5, 16, zi[0]-0.5 if zs > 0 else zi[1], zi[0] if zs > 0 else zi[1]+0.5, 'darkwood', 'sill')
    # the vestibules: doors (navy panels with a window), the wall beside them
    for xsg in (-1, 1):
        d0, d1 = (45, 51) if xsg > 0 else (-51, -45)
        e0, e1 = (44, 45) if xsg > 0 else (-45, -44)
        f0, f1 = (51, L) if xsg > 0 else (-L, -51)
        for a, b in ((e0, e1), (f0, f1)):
            wall_strip(a, b, 16, 23, zs, 'navy', None)
        # door: lower, upper, beside its window
        wall_strip(d0, d1, FLOOR, 16, zs, 'navy', None)
        wall_strip(d0, d0+1.5, 16, 23, zs, 'navy', None)
        wall_strip(d1-1.5, d1, 16, 23, zs, 'navy', None)
        zg = (10.6, 10.8) if zs > 0 else (-10.8, -10.6)
        box(d0+1.5, d1-1.5, 16, 23, zg[0], zg[1], 'glass', 'door window')
        zh = (11, 11.5) if zs > 0 else (-11.5, -11)
        box(d1-2, d1-1, 13, 14, zh[0], zh[1], 'brass', 'door handle')

# compartments: x0 = -44 + 22 i; window side (z+): a large window and a small one
comp_windows = []
for i in range(4):
    x0 = -44 + 22*i
    comp_windows += [(x0+3, x0+13), (x0+15, x0+19)]
side(1, comp_windows)
# corridor side (z-): evenly
cor_windows = [(-44 + 11*k + 2, -44 + 11*k + 9) for k in range(8)]
side(-1, cor_windows)

# --- ends: gangway door in the middle
for xs in (-1, 1):
    x0, x1 = (51, 52) if xs > 0 else (-52, -51)
    box(x0, x1, FLOOR, TOP+1, -W, -3, 'navy', 'end wall')
    box(x0, x1, FLOOR, TOP+1, 3, W, 'navy', 'end wall')
    box(x0, x1, 26, TOP+1, -3, 3, 'navy', 'over the gangway door')
    # gangway bellows round the door
    bx0, bx1 = (52, 53) if xs > 0 else (-53, -52)
    box(bx0, bx1, FLOOR, 27, -5, -3, 'under', 'bellows')
    box(bx0, bx1, FLOOR, 27, 3, 5, 'under', 'bellows')
    box(bx0, bx1, 26, 27, -3, 3, 'under', 'bellows')

# --- inside: the corridor's wall with the compartments' doors, the partitions
for i in range(4):
    x0 = -44 + 22*i
    box(x0, x0+15, FLOOR, TOP, -4, -3, 'teak', 'corridor wall')
    box(x0+20, x0+22, FLOOR, TOP, -4, -3, 'teak', 'corridor wall')
    box(x0+15, x0+20, 25, TOP, -4, -3, 'teak', 'over the door')
    box(x0+15, x0+15.5, FLOOR, 25, -4.5, -2.5, 'brass', 'door frame')
    box(x0+19.5, x0+20, FLOOR, 25, -4.5, -2.5, 'brass', 'door frame')
for px in (-22, 0, 22):
    box(px-0.5, px+0.5, FLOOR, TOP, -3, 10, 'teak', 'partition')
for xs in (-1, 1):
    px = 44 if xs > 0 else -44
    box(px-0.5, px+0.5, FLOOR, TOP, -3, 10, 'teak', 'end partition')
    box(px-0.5, px+0.5, 25, TOP, -10, -4, 'teak', 'over the corridor end')
    box(px-0.5, px+0.5, FLOOR, TOP, -4, -3, 'teak', 'corner')

# each compartment: a sofa (by day; its back the lower berth's), the upper berth folded up, a table by the
# window, a washstand
for i in range(4):
    x0 = -44 + 22*i
    s0 = x0 + 0.5
    box(s0, s0+7, FLOOR, 14, -2.5, 10, 'velvet', 'sofa seat')
    box(s0+6.5, s0+7, FLOOR, 13.5, -2.5, 10, 'darkwood', 'sofa front')
    box(s0, s0+2, 14, 22, -2.5, 10, 'velvet', 'sofa back')
    box(s0, s0+3, 14, 17, -3, -2.5, 'darkwood', 'armrest')
    box(s0, s0+2, 24, 27, -2.5, 10, 'linen', 'upper berth, folded')
    box(s0+2, s0+2.5, 24, 27, -2.5, 10, 'darkwood', 'berth edge')
    box(x0+14, x0+21, 16, 17, 7, 10, 'darkwood', 'table')
    box(x0+17, x0+18, FLOOR, 16, 8, 9, 'brass', 'table leg')
    box(x0+19, x0+21.5, FLOOR, 18, 1, 6, 'darkwood', 'washstand')
    box(x0+19, x0+21.5, 18, 18.5, 1, 6, 'marble', 'washstand top')
    box(x0+21, x0+21.5, 19, 24, 1.5, 5.5, 'glass', 'mirror')
# a brass handrail along the corridor's windows
box(-44, 44, 19, 19.5, -10, -9.5, 'brass', 'handrail')

# --- ceiling and the clerestory roof
box(-L, L, TOP, TOP+1, -W, W, 'ceiling', 'ceiling')
box(-L-1, L+1, TOP+1, TOP+2, -W-1, W+1, 'roof', 'roof')
box(-L-1, L+1, TOP+2, TOP+3, -9, 9, 'roof', 'roof')
box(-46, 46, TOP+3, TOP+6, -5, -4, 'navydark', 'clerestory side')
box(-46, 46, TOP+3, TOP+6, 4, 5, 'navydark', 'clerestory side')
for k in range(11):
    cx = -42 + 8.4*k
    box(cx, cx+4, TOP+3.75, TOP+5.25, 5, 5.2, 'glass', 'clerestory window')
    box(cx, cx+4, TOP+3.75, TOP+5.25, -5.2, -5, 'glass', 'clerestory window')
box(-46, 46, TOP+3, TOP+6, -4, 4, 'ceiling', 'clerestory inside')
box(-47, 47, TOP+6, TOP+7, -6, 6, 'roof', 'clerestory roof')
for xs in (-1, 1):
    e0, e1 = (46, 47) if xs > 0 else (-47, -46)
    box(e0, e1, TOP+3, TOP+6, -5, 5, 'roof', 'clerestory end')
    # lamp vents on the roof
for k in range(4):
    cx = -33 + 22*k
    box(cx-1, cx+1, TOP+7, TOP+8, -1, 1, 'steel', 'vent')

# ---- the java
out = []
out.append('''package train.render.models;

import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;

/**
 * A luxury sleeping car of the early 1900s (made for this port by tools/gen_lux_sleeper.py): navy and gold, a clerestory
 * roof, a corridor along one side, four sleeping compartments (a sofa by day, the upper berth folded up, a table
 * by the window, a washstand), vestibules with doors at both ends. x along, y up, z across, in pixels.
 */
public class ModelPassengerLuxSleeper extends ModelBase {
    static final int TEX_W = 512, TEX_H = %d;

    public ModelPassengerLuxSleeper() {
        bodyModel = new ModelRendererTurbo[%d];
''' % (BAND*len(NAMES), len(boxes)))
rnd = random.Random(7)
def fmt(v):
    s = ('%.3f' % v).rstrip('0').rstrip('.')
    return s + 'F'
for i,(x,y,z,w,h,d,band,c) in enumerate(boxes):
    if isinstance(band,str): band = B[band]
    fw = 2*(w+d); fh = d+h
    assert fw <= 512 and fh <= BAND, (c, fw, fh)
    u = rnd.randint(0, int(512 - fw - 1)) if fw < 510 else 0
    v = band*BAND + rnd.randint(0, int(BAND - fh)) if fh < BAND else band*BAND
    out.append('        bodyModel[%d] = new ModelRendererTurbo(this, %d, %d, TEX_W, TEX_H);%s' % (i, u, v, (' // ' + c) if c else ''))
    out.append('        bodyModel[%d].addBox(%s, %s, %s, %s, %s, %s);' % (i, fmt(x), fmt(y), fmt(z), fmt(w), fmt(h), fmt(d)))
out.append('    }\n}\n')
open('/home/_viguro_/src/tc-neo/src/main/java/train/render/models/ModelPassengerLuxSleeper.java','w').write('\n'.join(out))

# ---- the texture
H = BAND*len(NAMES)
img = Image.new('RGB', (512, H))
px = img.load()
def noise(c, a=6):
    return tuple(max(0, min(255, v + rnd.randint(-a, a))) for v in c)
base = {
 'navy': (27, 38, 74), 'gold': (201, 162, 39), 'roof': (176, 172, 162), 'under': (30, 30, 32),
 'wheel': (58, 58, 60), 'teak': (122, 74, 36), 'darkwood': (74, 42, 20), 'carpet': (110, 30, 36),
 'velvet': (125, 31, 43), 'glass': (40, 58, 70), 'ceiling': (217, 207, 179), 'brass': (176, 141, 58),
 'linen': (232, 228, 218), 'marble': (208, 208, 204), 'navydark': (18, 28, 51), 'steel': (110, 112, 116)}
for bi, n in enumerate(NAMES):
    c = base[n]
    for yy in range(BAND):
        for xx in range(512):
            col = c
            if n == 'teak':
                g = ((xx * 7 + (yy // 3) * 3) % 23 == 0) or (xx % 16 == 0)
                col = tuple(v - 14 for v in c) if g else c
            elif n == 'darkwood':
                col = tuple(v - 10 for v in c) if (xx % 11 == 0) else c
            elif n == 'carpet':
                col = (150, 110, 50) if ((xx + yy) % 8 == 0 or (xx - yy) % 8 == 0) else c
            elif n == 'velvet':
                col = tuple(v - 18 for v in c) if (xx % 4 == 0) else c
            elif n == 'glass':
                col = tuple(min(255, v + (20 if (xx + yy) % 9 < 2 else 0)) for v in c)
            elif n == 'roof':
                col = tuple(v - 12 for v in c) if (xx % 8 == 0) else c
            elif n == 'navy':
                col = c
            px[xx, bi*BAND + yy] = noise(col, 4 if n in ('navy', 'navydark', 'gold', 'brass') else 6)
img.save('/home/_viguro_/src/tc-neo/src/main/resources/assets/traincraft/textures/trains/passenger_lux_sleeper.png')
print(len(boxes), 'boxes')
