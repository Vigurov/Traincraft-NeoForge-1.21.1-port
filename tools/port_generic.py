#!/usr/bin/env python3
"""Mechanical 1.12 -> 1.21 (NeoForge, Mojang names) rewrites for ported Traincraft/TiM code.
Usage: port_generic.py SRC_ROOT DST_ROOT relpath... (paths relative to the roots, java files or folders)."""
import os, re, sys

IMPORTS = {
    'net.minecraft.world.World': 'net.minecraft.world.level.Level',
    'net.minecraft.world.IBlockAccess': 'net.minecraft.world.level.BlockGetter',
    'net.minecraft.util.math.BlockPos': 'net.minecraft.core.BlockPos',
    'net.minecraft.util.math.MathHelper': 'net.minecraft.util.Mth',
    'net.minecraft.util.math.AxisAlignedBB': 'net.minecraft.world.phys.AABB',
    'net.minecraft.util.math.RayTraceResult': 'net.minecraft.world.phys.HitResult',
    'net.minecraft.util.EnumFacing': 'net.minecraft.core.Direction',
    'net.minecraft.util.ResourceLocation': 'net.minecraft.resources.ResourceLocation',
    'net.minecraft.item.ItemStack': 'net.minecraft.world.item.ItemStack',
    'net.minecraft.item.Item': 'net.minecraft.world.item.Item',
    'net.minecraft.init.Items': 'net.minecraft.world.item.Items',
    'net.minecraft.init.Blocks': 'net.minecraft.world.level.block.Blocks',
    'net.minecraft.block.Block': 'net.minecraft.world.level.block.Block',
    'net.minecraft.block.material.Material': None,
    'net.minecraft.tileentity.TileEntity': 'net.minecraft.world.level.block.entity.BlockEntity',
    'net.minecraft.entity.player.EntityPlayer': 'net.minecraft.world.entity.player.Player',
    'net.minecraft.entity.Entity': 'net.minecraft.world.entity.Entity',
    'net.minecraft.nbt.NBTTagCompound': 'net.minecraft.nbt.CompoundTag',
    'net.minecraft.util.text.TextComponentString': 'net.minecraft.network.chat.Component',
    'net.minecraftforge.fml.relauncher.Side': None,
    'net.minecraftforge.fml.relauncher.SideOnly': None,
    'ebf.XmlBuilder': 'ebf.tim.utility.XmlBuilder',
    'org.lwjgl.opengl.GL11': 'fexcraft.tmt.slim.GL11',
    'org.lwjgl.util.vector.Matrix2f': 'traincompat.Matrix2f',
    'org.lwjgl.util.vector.Vector2f': 'traincompat.Vector2f',
}

WORDS = [
    (r'\bWorld\b', 'Level'),
    (r'\bIBlockAccess\b', 'BlockGetter'),
    (r'\bEntityPlayer\b', 'Player'),
    (r'\bTileEntity\b', 'BlockEntity'),
    (r'\bNBTTagCompound\b', 'CompoundTag'),
    (r'\bAxisAlignedBB\b', 'AABB'),
    (r'\bEnumFacing\b', 'Direction'),
    (r'\bMathHelper\.', 'Mth.'),
    (r'\.getTileEntity\(', '.getBlockEntity('),
    (r'\.removeTileEntity\(', '.removeBlockEntity('),
    (r'\.isRemote\b', '.isClientSide'),
    (r'\.isSneaking\(\)', '.isShiftKeyDown()'),
    (r'\.isBlockPowered\(', '.hasNeighborSignal('),
    (r'\.isAirBlock\(', '.isEmptyBlock('),
    (r'\.capabilities\.isCreativeMode\b', '.isCreative()'),
    (r'\.getHeldItemMainhand\(\)', '.getMainHandItem()'),
    (r'\.canPlayerEdit\(', '.mayUseItemAt('),
    (r'\.rotationYaw\b', '.getYRot()'),
    (r'\.rotationPitch\b', '.getXRot()'),
    (r'\bnew ItemStack\(([^,()]+), *([^,()]+), *[^,()]+\)', r'new ItemStack(\1, \2)'),
    (r'@SideOnly\(Side\.CLIENT\)\s*', ''),
]


def port(s):
    s = s.replace('\r\n', '\n').replace('\r', '\n')
    for a, b in IMPORTS.items():
        if b is None:
            s = s.replace('import %s;\n' % a, '')
        else:
            s = s.replace('import %s;' % a, 'import %s;' % b)
    for a, b in WORDS:
        s = re.sub(a, b, s)
    return s


def main():
    src, dst = sys.argv[1], sys.argv[2]
    n = 0
    for rel in sys.argv[3:]:
        base = os.path.join(src, rel)
        paths = [base] if base.endswith('.java') else [os.path.join(r, f) for r, _, fs in os.walk(base) for f in fs if f.endswith('.java')]
        for p in paths:
            out = os.path.join(dst, os.path.relpath(p, src))
            os.makedirs(os.path.dirname(out), exist_ok=True)
            open(out, 'w').write(port(open(p, encoding='utf-8', errors='replace').read()))
            n += 1
    print(n, 'files')


if __name__ == '__main__':
    main()
