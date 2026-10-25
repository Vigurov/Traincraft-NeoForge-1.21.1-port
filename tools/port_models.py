#!/usr/bin/env python3
"""Copies the 1.12 TMT train models to the 1.21 tree with the mechanical API changes."""
import os, re, sys, shutil
SRC = os.path.expanduser('~/src/tc-tim-1122/src/main/java/train/render/models')
DST = os.path.expanduser('~/src/tc-neo/src/main/java/train/render/models')
SKIP_DIRS = set()
SKIP = {'ModelColoredArmor.java', 'ModelZeppelinOneBalloon.java', 'ModelZeppelinTwoBalloons.java', 'ModelLines.java',
        # block models that need their tile entities: tools/parked until the blocks are ported
        'ModelWaterWheel.java', 'ModelWindMillWheel.java', 'ModelWindMill.java', 'ModelLantern.java', 'ModelBridgePillar.java'}

def port(s):
    s = s.replace('\r\n', '\n').replace('\r', '\n')
    s = s.replace('import net.minecraft.entity.Entity;', 'import net.minecraft.world.entity.Entity;')
    s = s.replace('import org.lwjgl.opengl.GL11;', 'import fexcraft.tmt.slim.GL11;')
    s = s.replace('import net.minecraft.util.ResourceLocation;', 'import net.minecraft.resources.ResourceLocation;')
    s = s.replace('import net.minecraftforge.fml.client.FMLClientHandler;\n', '')
    s = s.replace('import net.minecraftforge.fml.relauncher.SideOnly;\n', '').replace('import net.minecraftforge.fml.relauncher.Side;\n', '')
    s = re.sub(r'@SideOnly\(Side\.CLIENT\)\s*', '', s)
    s = re.sub(r'FMLClientHandler\.instance\(\)\.getClient\(\)\s*\.renderEngine\s*\.bindTexture\(', 'fexcraft.tmt.slim.TextureManager.bindTexture(', s)
    s = s.replace('Minecraft.getMinecraft().entityRenderer.disableLightmap();', 'fexcraft.tmt.slim.RenderContext.fullBright = true;')
    s = s.replace('Minecraft.getMinecraft().entityRenderer.enableLightmap();', 'fexcraft.tmt.slim.RenderContext.fullBright = false;')
    s = s.replace('import net.minecraft.client.Minecraft;\n', '')
    # new ResourceLocation(a, b) -> fromNamespaceAndPath, new ResourceLocation(x) -> parse
    out, i = [], 0
    while True:
        j = s.find('new ResourceLocation(', i)
        if j < 0:
            out.append(s[i:]); break
        out.append(s[i:j])
        k = j + len('new ResourceLocation(')
        depth, args, cur = 1, [], ''
        while depth:
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
        if len(args) == 2:
            out.append('ResourceLocation.fromNamespaceAndPath(%s,%s)' % (args[0], args[1]))
        else:
            out.append('ResourceLocation.parse(%s)' % args[0])
        i = k + 1
    s = ''.join(out)
    s = re.sub(r'\bentity\.world\b', 'entity.level()', s)
    s = s.replace('import net.minecraft.world.World;', 'import net.minecraft.world.level.Level;').replace('int color, World world', 'int color, Level world')
    return s

n = 0
for root, dirs, files in os.walk(SRC):
    dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
    for f in files:
        if not f.endswith('.java') or f in SKIP:
            continue
        rel = os.path.relpath(os.path.join(root, f), SRC)
        dst = os.path.join(DST, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        open(dst, 'w').write(port(open(os.path.join(root, f), encoding='utf-8', errors='replace').read()))
        n += 1
print(n, 'models')
