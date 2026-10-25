#!/usr/bin/env python3
"""1.12 TileEntity -> 1.21 BlockEntity rewrites for the classic track tiles (run after port_generic.py)."""
import os, re, sys

NBT = [
    (r'\.getInteger\(', '.getInt('), (r'\.setInteger\(', '.putInt('), (r'\.hasKey\(', '.contains('),
    (r'\.setByte\(', '.putByte('), (r'\.setBoolean\(', '.putBoolean('), (r'\.setDouble\(', '.putDouble('),
    (r'\.setString\(', '.putString('), (r'\.setFloat\(', '.putFloat('), (r'\.setIntArray\(', '.putIntArray('),
    (r'\.setTag\(', '.put('), (r'\.getCompoundTag\(', '.getCompound('), (r'\.setLong\(', '.putLong('),
]


def strip_method(s, signature_regex):
    while True:
        m = re.search(r'(\n[ \t]*@Override[ \t]*\n)?[ \t]*' + signature_regex + r'[^{;]*\{', s)
        if not m:
            return s
        i, depth = m.end(), 1
        while depth:
            depth += {'{': 1, '}': -1}.get(s[i], 0)
            i += 1
        s = s[:m.start()] + s[i:]


def port(s, cls, type_field):
    for a, b in NBT:
        s = re.sub(a, b, s)
    s = re.sub(r'^import (net\.minecraft\.network\.(NetworkManager|Packet|play\.server\..*)|org\.apache\.logging\.log4j\.Level|'
               r'net\.minecraft\.entity\.item\.EntityMinecart|net\.minecraft\.util\.math\..*|net\.minecraft\.block\.state\..*|'
               r'train\.Traincraft|ebf\.tim\.utility\.DebugUtil|net\.minecraftforge\..*);\n', '', s, flags=re.M)
    s = s.replace('import net.minecraft.world.level.block.entity.BlockEntity;',
                  'import net.minecraft.world.level.block.entity.BlockEntity;\n'
                  'import net.minecraft.world.level.block.state.BlockState;\n'
                  'import net.minecraft.core.HolderLookup;\n'
                  'import net.minecraft.core.registries.BuiltInRegistries;\n'
                  'import net.minecraft.resources.ResourceLocation;\n'
                  'import net.minecraft.network.protocol.Packet;\n'
                  'import net.minecraft.network.protocol.game.ClientGamePacketListener;\n'
                  'import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;')
    s = re.sub(r'extends BlockEntity implements net\.minecraft\.util\.ITickable', 'extends BlockEntity', s)
    # constructor
    s = re.sub(r'public ' + cls + r'\(\)\s*\{', 'public ' + cls + '(BlockPos pos, BlockState state) {\n\t\tsuper(train.common.ClassicTracks.'
               + type_field + ', pos, state);', s)
    s = re.sub(r'\bthis\.world\b', 'this.level', s)
    s = re.sub(r'(?<![\w.])world(?=\s*[.,)!=])', 'level', s)
    s = re.sub(r'\bLevel world\b', 'Level level', s)
    s = re.sub(r'(?<![\w.])pos\.', 'worldPosition.', s)
    s = s.replace('getPos()', 'getBlockPos()').replace('getBlockMetadata()', 'getBlockMeta()').replace('getWorld()', 'getLevel()')
    s = s.replace('CommonUtil.getBlockEntity(', 'CommonUtil.getTileEntity(')
    # save / load
    s = re.sub(r'public void readFromNBT\(CompoundTag (\w+)\)\s*\{\s*super\.readFromNBT\(\w+\);',
               r'protected void loadAdditional(CompoundTag \1, HolderLookup.Provider registries) {\n\t\tsuper.loadAdditional(\1, registries);', s)
    s = re.sub(r'public void readFromNBT\(CompoundTag (\w+)\)\s*\{',
               r'protected void loadAdditional(CompoundTag \1, HolderLookup.Provider registries) {\n\t\tsuper.loadAdditional(\1, registries);', s)
    s = strip_method(s, r'public net\.minecraft\.nbt\.CompoundTag writeToNBT\(')
    s = re.sub(r'public void legacy_writeToNBT\(CompoundTag (\w+)\)\s*\{\s*super\.writeToNBT\(\w+\);',
               r'@Override\n\tprotected void saveAdditional(CompoundTag \1, HolderLookup.Provider registries) {\n\t\tsuper.saveAdditional(\1, registries);', s)
    s = re.sub(r'public CompoundTag writeToNBT\(CompoundTag (\w+)\)\s*\{\s*super\.writeToNBT\(\w+\);',
               r'protected void saveAdditional(CompoundTag \1, HolderLookup.Provider registries) {\n\t\tsuper.saveAdditional(\1, registries);', s)
    s = re.sub(r'public void legacy_writeToNBT\(CompoundTag (\w+)\)\s*\{',
               r'@Override\n\tprotected void saveAdditional(CompoundTag \1, HolderLookup.Provider registries) {\n\t\tsuper.saveAdditional(\1, registries);', s)
    s = re.sub(r'\n\s*super\.writeToNBT\(\w+\);', '', s)
    # never called in 1.12 either (a block method left in the tile)
    s = strip_method(s, r'public void breakBlock\(')
    s = strip_method(s, r'public void legacy_breakBlock\(')
    s = re.sub(r'idDrop = Item\.getItemById\((\w+)\.getInt\("idDrop"\)\);',
               r'idDrop = \1.contains("idDrop", 8) ? BuiltInRegistries.ITEM.get(ResourceLocation.parse(\1.getString("idDrop"))) : null;', s)
    s = re.sub(r'(\w+)\.putInt\("idDrop", Item\.getIdFromItem\(idDrop\)\);',
               r'if (idDrop != null) \1.putString("idDrop", BuiltInRegistries.ITEM.getKey(idDrop).toString());', s)
    # packets
    for sig in [r'public net\.minecraft\.network\.play\.server\.SPacketUpdateTileEntity getUpdatePacket\(',
                r'public Packet legacy_getDescriptionPacket\(', r'public void onDataPacket\(', r'public SPacketUpdateTileEntity getUpdatePacket\(',
                r'public CompoundTag getUpdateTag\(', r'public AABB getRenderBoundingBox\(', r'public double getMaxRenderDistanceSquared\(',
                r'public boolean shouldRefresh\(']:
        s = strip_method(s, sig)
    s = re.sub(r'\n[ \t]*private static final int\[\] matrixXZ = \{0,-1,1\}, matrixY = \{0,-1,\+1\};', '', s)
    #drop the class's closing brace (if the strips left it) and add the 1.21 methods
    depth = s.count('{') - s.count('}')
    if depth == 0:
        s = s[:s.rindex('}')]
    s = s + '''
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	/** the 1.7.10 block metadata (track direction, switch state) */
	public int getBlockMeta() {
		BlockState state = getBlockState();
		for (var p : state.getProperties()) {
			if (p.getName().equals("meta")) return (Integer) state.getValue(p);
		}
		return 0;
	}

	/** tell the clients (1.7.10 world.markBlockForUpdate) */
	public void markDirty() {
		if (level != null) {
			setChanged();
			if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}
}
'''
    return s


if __name__ == '__main__':
    base = os.path.expanduser('~/src/tc-neo/src/main/java/train/common/tile/')
    for cls, field in [('TileTCRail', 'TILE_RAIL'), ('TileTCRailGag', 'TILE_RAIL_GAG')]:
        p = base + cls + '.java'
        text = open(p).read()
        open(p, 'w').write(port(text, cls, field))
    print('ok')
