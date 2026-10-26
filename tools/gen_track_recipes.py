#!/usr/bin/env python3
"""Writes data/traincraft/recipe/*.json from the 1.12 classic track recipes (train.common.ClassicTracks)."""
import re, os, json

SRC = os.path.expanduser('~/src/tc-tim-1122/src/main/java/train/common/ClassicTracks.java')
OUT = os.path.expanduser('~/src/tc-neo/src/main/resources/data/traincraft/recipe')
TABLE = {
    'Items.IRON_INGOT': {'item': 'minecraft:iron_ingot'}, 'Items.IRON_NUGGET': {'item': 'minecraft:iron_nugget'},
    'Blocks.PLANKS': {'tag': 'minecraft:planks'}, 'Blocks.GRAVEL': {'item': 'minecraft:gravel'},
    'Blocks.SNOW': {'item': 'minecraft:snow_block'}, 'Blocks.RAIL': {'item': 'minecraft:rail'},
    'new ItemStack(Items.DYE, 1, 1)': {'item': 'minecraft:red_dye'},
    'ballast': {'item': 'minecraft:gravel'},  # 1.12: TiM ballast block, not ported yet
    'new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, 15)': {'item': 'minecraft:black_terracotta'},
    'new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, 3)': {'item': 'minecraft:light_blue_terracotta'},
    'new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, 7)': {'item': 'minecraft:gray_terracotta'},
    'new ItemStack(Blocks.WOOL, 1, 0)': {'item': 'minecraft:white_wool'},
}


def ing(expr):
    expr = expr.strip()
    m = re.match(r'ItemIDs\.(\w+)\.item$', expr)
    if m:
        return {'item': 'traincraft:' + m.group(1).lower()}
    return TABLE[expr]


def split_args(s):
    args, cur, d, q = [], '', 0, False
    for c in s:
        if c == '"':
            q = not q
        if not q and c in '([':
            d += 1
        if not q and c in ')]':
            d -= 1
        if c == ',' and d == 0 and not q:
            args.append(cur); cur = ''
        else:
            cur += c
    args.append(cur)
    return [a.strip() for a in args]


src = open(SRC).read().replace('\r', '')
# only pieces that have an item (1.12 had recipes for a few it never made)
registered = {m.lower() for m in re.findall(r'ItemIDs\.(\w+)\.item = new ItemTCRail', src)}
# the tight 90 degree turns are no longer made (ItemIDs.isRetired)
registered -= {'tcrail1x1turn', 'tcrailmediumturn', 'tcrailembedded1x1turn', 'tcrailembeddedmediumturn'}
# recipes that used the medium turn take the large turn instead
SUBST = {'ItemIDs.tcRailMediumTurn.item': 'ItemIDs.tcRailLargeTurn.item', 'ItemIDs.tcRailEmbeddedMediumTurn.item': 'ItemIDs.tcRailEmbeddedLargeTurn.item'}
os.makedirs(OUT, exist_ok=True)
#only its own files (the classic track recipes): the folder holds the mod's other recipes too
for f in os.listdir(OUT):
    if f.startswith('tcrail'):
        os.remove(os.path.join(OUT, f))
n, names = 0, {}
for m in re.finditer(r'Recipes\.(addRecipe|addShapelessRecipe)\((.*)\);', src):
    kind, a = m.group(1), split_args(m.group(2))
    #ingredients only (a[0] is the result)
    for k, v in SUBST.items():
        a = [a[0]] + [x.replace(k, v) for x in a[1:]]
    om = re.match(r'new ItemStack\(ItemIDs\.(\w+)\.item,\s*(\d+)\)', a[0])
    result = {'id': 'traincraft:' + om.group(1).lower(), 'count': int(om.group(2))}
    used = [om.group(1).lower()] + [x.lower() for x in re.findall(r'ItemIDs\.(\w+)\.item', ','.join(a[1:]))]
    if any(u not in registered for u in used):
        continue
    name = om.group(1).lower()
    names[name] = names.get(name, 0) + 1
    fname = name + ('' if names[name] == 1 else '_%d' % names[name])
    if kind == 'addRecipe':
        rows = [r.strip('"') for r in a[1:] if r.startswith('"')]
        rest = [r for r in a[1:] if not r.startswith('"')]
        key = {rest[i].strip("'"): ing(rest[i + 1]) for i in range(0, len(rest), 2)}
        # trim blank rows and columns, like 1.12 Recipes.clean
        rows = [r for r in rows if r.strip()]
        w = max(len(r) for r in rows)
        rows = [r.ljust(w) for r in rows]
        cols = [i for i in range(w) if any(r[i] != ' ' for r in rows)]
        rows = [r[cols[0]:cols[-1] + 1] for r in rows]
        d = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': rows, 'key': key, 'result': result}
    else:
        d = {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [ing(x) for x in a[1:]], 'result': result}
    json.dump(d, open(os.path.join(OUT, fname + '.json'), 'w'), indent=1)
    n += 1
print(n, 'recipes')
