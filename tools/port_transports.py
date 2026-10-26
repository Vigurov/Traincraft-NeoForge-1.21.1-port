#!/usr/bin/env python3
"""Ports the 1.12 TiM transport definitions (train.entity.trains / rollingStock) to the 1.21 tree, writes
train/library/TrainRegistry.java (the 1.12 list and order; remasters off like the 1.12 default) and item models."""
import os, re, json

SRC = os.path.expanduser('~/src/tc-tim-1122/src/main/java/train')
DST = os.path.expanduser('~/src/tc-neo/src/main/java/train')
RES = os.path.expanduser('~/src/tc-neo/src/main/resources/assets/traincraft')
SKIP = {'EntityJukeBoxCart'}  # needs the MP3 player and its GUI: later


def strip_method(s, signature_regex):
    """removes a method (annotation included) whose header matches the regex"""
    while True:
        m = re.search(r'(\n[ \t]*@Override[ \t]*\n)?[ \t]*' + signature_regex + r'[^{;]*\{', s)
        if not m:
            return s
        i = m.end()
        depth = 1
        while depth:
            if s[i] == '{':
                depth += 1
            elif s[i] == '}':
                depth -= 1
            i += 1
        s = s[:m.start()] + s[i:]


def port(s, cls):
    s = s.replace('\r\n', '\n').replace('\r', '\n')
    imports = {
        'import net.minecraft.world.World;': 'import net.minecraft.world.level.Level;\nimport net.minecraft.world.entity.EntityType;',
        'import net.minecraft.item.ItemStack;': 'import net.minecraft.world.item.ItemStack;',
        'import net.minecraft.item.Item;': 'import net.minecraft.world.item.Item;',
        'import net.minecraft.init.Items;': 'import net.minecraft.world.item.Items;',
        'import net.minecraft.init.Blocks;': 'import net.minecraft.world.level.block.Blocks;',
        'import net.minecraft.util.ResourceLocation;': 'import net.minecraft.resources.ResourceLocation;',
        'import net.minecraft.block.Block;': 'import net.minecraft.world.level.block.Block;',
    }
    for a, b in imports.items():
        s = s.replace(a, b)
    for dead in ['import ebf.tim.items.ItemTransport;',
                 'import train.Traincraft;', 'import java.util.UUID;', 'import train.blocks.TCBlocks;',
                 'import net.minecraftforge.fml.relauncher.SideOnly;', 'import net.minecraftforge.fml.relauncher.Side;',
                 'import ebf.tim.utility.FuelHandler;']:
        s = s.replace(dead + '\n', '')
    s = re.sub(r'@SideOnly\(Side\.CLIENT\)\s*', '', s)
    if 'import ebf.tim.entities.GenericRailTransport;' not in s:
        s = s.replace('package train.entity.', 'import ebf.tim.entities.GenericRailTransport;\npackage train.entity.', 0)
        s = re.sub(r'(package [^;]+;\n)', r'\1\nimport ebf.tim.entities.GenericRailTransport;', s, count=1)
    # constructors
    s = re.sub(r'public ' + cls + r'\(\s*World\s+(\w+)\s*\)\s*\{\s*super\(\s*\w+\s*\);\s*\}',
               'public ' + cls + r'(EntityType<? extends GenericRailTransport> type, Level \1) {\n        super(type, \1);\n    }', s)
    s = strip_method(s, r'public ' + cls + r'\(\s*UUID')
    # the item lives in the registry now
    s = re.sub(r'[ \t]*public static final Item thisItem\s*=[^;]*;\n', '', s)
    s = strip_method(s, r'public Item getItem\(\)')
    # recipes: 1.12 items that changed name or lost their metadata in 1.21 (dyes: plain Items.DYE was ink, black)
    dyes = ['BLACK', 'RED', 'GREEN', 'BROWN', 'BLUE', 'PURPLE', 'CYAN', 'LIGHT_GRAY', 'GRAY', 'PINK', 'LIME', 'YELLOW',
            'LIGHT_BLUE', 'MAGENTA', 'ORANGE', 'WHITE']
    s = re.sub(r'new ItemStack\(Items\.DYE,\s*(\d+),\s*(\d+)\)', lambda m: 'new ItemStack(Items.%s_DYE, %s)' % (dyes[int(m.group(2))], m.group(1)), s)
    s = s.replace('Items.DYE', 'Items.BLACK_DYE').replace('Blocks.PLANKS', 'Blocks.OAK_PLANKS').replace('Blocks.LOG', 'Blocks.OAK_LOG')
    s = s.replace('Items.BOAT', 'Items.OAK_BOAT')
    # the TiM items are registry entries now (both an ItemLike and a Holder: name the item)
    s = re.sub(r'\b(TiMItems|TiMOres)\.(\w+)\b(?!\s*[.(])', r'\1.\2.get()', s)
    s = re.sub(r'\bthis\.rotationYaw\b', 'this.getYRot()', s)
    # the table it is made on, by name (1.12: the block)
    s = re.sub(r'public Block getCraftingTable\(\)\s*\{\s*return TCBlocks\.trainTable(Tier\d);\s*\}',
               lambda m: 'public String getCraftingTable() {\n        return "traintable%s";\n    }' % m.group(1).lower(), s)
    s = resource_locations(s)
    s = re.sub(r'(?<![\w.])world\s*==\s*null', 'level()==null', s)
    s = re.sub(r'(?<![\w.])world\s*!=\s*null', 'level()!=null', s)
    s = re.sub(r'(?<![\w.])getWorld\(\)', 'level()', s)
    return s


def resource_locations(s):
    """new ResourceLocation(a, b) -> fromNamespaceAndPath, new ResourceLocation(x) -> parse"""
    out, i = [], 0
    while True:
        j = s.find('new ResourceLocation(', i)
        if j < 0:
            out.append(s[i:])
            return ''.join(out)
        out.append(s[i:j])
        k = j + len('new ResourceLocation(')
        depth, args, cur = 1, [], ''
        while True:
            c = s[k]
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0:
                    break
            if c == ',' and depth == 1:
                args.append(cur); cur = ''
            else:
                cur += c
            k += 1
        args.append(cur)
        out.append('fexcraft.tmt.slim.TextureManager.loc(%s,%s)' % (args[0], args[1]) if len(args) == 2 else 'fexcraft.tmt.slim.TextureManager.parse(%s)' % args[0])
        i = k + 1


def transport_name(s):
    m = re.search(r'transportName\(\)\s*\{\s*return\s*"([^"]*)"', s)
    return m.group(1) if m else None


classes = {}
for sub in ['trains', 'rollingStock']:
    os.makedirs(os.path.join(DST, 'entity', sub), exist_ok=True)
    for f in sorted(os.listdir(os.path.join(SRC, 'entity', sub))):
        if not f.endswith('.java'):
            continue
        cls = f[:-5]
        if cls in SKIP:
            continue
        src = open(os.path.join(SRC, 'entity', sub, f), encoding='utf-8', errors='replace').read()
        out = port(src, cls)
        open(os.path.join(DST, 'entity', sub, f), 'w').write(out)
        name = transport_name(src.replace('\r', ''))
        classes[cls] = (sub, (name or cls).replace(' ', '').lower())

# registration order from the 1.12 list, without the blocks only made for remasters (REMASTERS=1 default)
reg = open(os.path.join(SRC, 'library', 'TrainRegistry.java'), encoding='utf-8').read().replace('\r', '')
order, skip_depth, depth = [], None, 0
for line in reg.split('\n'):
    if 'REMASTERS!=1' in line:
        skip_depth = depth
    for m in re.finditer(r'new (Entity\w+)\(', line):
        if skip_depth is None and m.group(1) in classes and m.group(1) not in order:
            order.append(m.group(1))
    depth += line.count('{') - line.count('}')
    if skip_depth is not None and depth <= skip_depth and '}' in line:
        skip_depth = None

names = {}
lines = []
for cls in order:
    sub, name = classes[cls]
    if name in names.values():
        name = cls.replace('Entity', '', 1).lower()
    names[cls] = name
    lines.append('        TiMGenericRegistry.registerTransport("%s", %s.class, %s::new);' % (name, cls, cls))
open(os.path.join(DST, 'library', 'TrainRegistry.java'), 'w').write('''package train.library;

import ebf.tim.registry.TiMGenericRegistry;
import train.entity.rollingStock.*;
import train.entity.trains.*;

/**
 * the Traincraft transports, in the 1.12 order (generated by tools/port_transports.py)
 */
public class TrainRegistry {
    public static void register() {
%s
    }
}
''' % '\n'.join(lines))

# item models: the 1.12 item icons
os.makedirs(os.path.join(RES, 'models', 'item'), exist_ok=True)
icons = os.path.join(RES, 'textures', 'items', 'transports')
missing = []
for cls, name in names.items():
    tex = 'item.%s.png' % name
    if os.path.exists(os.path.join(icons, tex)):
        layer = 'traincraft:items/transports/item.%s' % name
    else:
        layer = 'traincraft:items/transports/item.locodieselchme3'
        missing.append(name)
    json.dump({'parent': 'item/generated', 'textures': {'layer0': layer}},
              open(os.path.join(RES, 'models', 'item', name + '.json'), 'w'))
print(len(classes), 'classes,', len(order), 'registered,', len(missing), 'without icon:', missing[:10])
