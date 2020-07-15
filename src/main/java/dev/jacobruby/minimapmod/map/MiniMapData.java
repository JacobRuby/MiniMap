package dev.jacobruby.minimapmod.map;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;

/***
 * Class for handling chunk scanning and color calculation.
 */
public class MiniMapData extends MapData {
    public MiniMapData() {
        super("minimap");
    }

    @Override
    public void markDirty() {
    }

    @Override
    public void setDirty(boolean isDirty) {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    /**
     * Scans the blocks within {@code l + j1} meters of the player. This method updates this class's xCenter and zCenter
     * variables to the {@code viewer}'s position. When this method returns control, {@code colors} will be updated to
     * the latest available world information including block updates in the previous tick.
     *
     * @param worldIn the world to render to the mini-map data.
     * @param viewer the player viewing the map.
     */
    public void update(World worldIn, EntityPlayer viewer) {
        {
            /* Right now 'i' is useless unless you want a lower-resolution map */
            int i = 1 << this.scale;
            int j = this.xCenter = (int) viewer.posX;
            int k = this.zCenter = (int) viewer.posZ;
            int l = 64;
            int i1 = 64;

            int j1 = 128 / i;

            /* Used for the nether and the end */
            boolean cave = worldIn.provider.getHasNoSky();
            /* Make 'cave' true for cave view in the overworld */

            BlockPos playerPos = new BlockPos(viewer.posX, viewer.posY, viewer.posZ);
            int ground = (int) viewer.posY;
            int g1 = 0;

            /* This reduces "flashing" when jumping around. If you're within 3 blocks of the ground, it just starts
             * from the ground */
            if (cave) {
                IBlockState iBlockState;
                Chunk chunk = worldIn.getChunkFromBlockCoords(playerPos);

                BlockPos.MutableBlockPos groundPos = new BlockPos.MutableBlockPos();

                do {
                    iBlockState = chunk.getBlockState(groundPos.set(playerPos.getX(), ground, playerPos.getZ()));
                } while (iBlockState.getBlock().getMapColor(iBlockState) == MapColor.airColor && --ground > 0 && ++g1 < 3);
            }

            int caveHeight = (int) viewer.posY;

            /* Gets the first non-air-colored block above your head. That point is used as the highest y level that
             * will be scanned in search of an air block */
            if (cave) {
                IBlockState iBlockState;
                Chunk chunk = worldIn.getChunkFromBlockCoords(playerPos);

                BlockPos.MutableBlockPos caveHeightPos = new BlockPos.MutableBlockPos();

                do {
                    iBlockState = chunk.getBlockState(caveHeightPos.set(playerPos.getX(), caveHeight, playerPos.getZ()));
                } while (iBlockState.getBlock().getMapColor(iBlockState) == MapColor.airColor && ++caveHeight < 255);

                /* Subtracts one if it's odd, this makes it less psychedelic when running around as
                 * there's fewer changes. */
                caveHeight &= -2;
            }

            for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
                /* d0 is used as a placeholder for the previous scanned block's y level */
                double d0 = 0.0D;

                for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                    if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {

                        /* Calculate world coordinates */
                        int k2 = (j / i + k1 - 64) * i;
                        int l2 = (k / i + l1 - 64) * i;

                        MapColor mapColor;
                        Chunk chunk = worldIn.getChunkFromBlockCoords(new BlockPos(k2, 0, l2));

                        if (!chunk.isEmpty()) {
                            int i3 = k2 & 15;
                            int j3 = l2 & 15;
                            int k3 = 0;
                            double d1 = 0.0D;

                            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                            int k4 = cave ? ground : chunk.getHeightValue(i3, j3) + 1;

                            k4 = Math.max(0, k4);

                            int k5 = k4;
                            IBlockState iblockstate;

                            label541:
                            {
                                iblockstate = chunk.getBlockState(blockpos$mutableblockpos.set(i3, k4, j3));

                                if (cave && iblockstate.getBlock().getMapColor(iblockstate) != MapColor.airColor) {
                                    /* If the block is solid, start scanning upward for air */
                                    do {
                                        k4++;

                                        if (k4 >= caveHeight) {
                                            /* If the scan reached the cave height calculated earlier, then use the
                                             * current block, and make it dark to make solid walls more distinct */
                                            d0 += 2;
                                            /* This makes it so the current block is "2 blocks lower than the previous block" */
                                            break label541;
                                        }

                                        iblockstate = chunk.getBlockState(blockpos$mutableblockpos.set(i3, k4, j3));
                                    } while (iblockstate.getBlock().getMapColor(iblockstate) != MapColor.airColor);
                                }

                                boolean voidLoop = false;

                                /* Now scan downward to find the first non-air-colored block */
                                while (iblockstate.getBlock().getMapColor(iblockstate) == MapColor.airColor) {
                                    --k4;

                                    if (k4 < 0) {
                                        /* If we hit the void, start back up from the cave height and go down */
                                        voidLoop = true;
                                        k4 = caveHeight;
                                    }

                                    if (voidLoop && k4 <= k5) {
                                        /* If we reach where we started, there are no visible blocks, display air */
                                        break;
                                    }

                                    iblockstate = chunk.getBlockState(blockpos$mutableblockpos.set(i3, k4, j3));
                                }

                                if (k4 > 0 && iblockstate.getBlock().getMaterial().isLiquid()) {
                                    /* If we hit a liquid, calculate the distance between the surface of the liquid,
                                    * and the solid block below it, depth. */

                                    int l4 = k4 - 1;

                                    while (true) {
                                        Block block = chunk.getBlock(i3, l4--, j3);
                                        ++k3;

                                        if (l4 <= 0 || !block.getMaterial().isLiquid()) {
                                            break label541;
                                        }
                                    }
                                }
                            }

                            d1 += (double) k4 / (double) (i * i);
                            mapColor = iblockstate.getBlock().getMapColor(iblockstate);

                            double d2;
                            int i5;

                            if (mapColor == MapColor.waterColor) {
                                /* If it's water, calculate shading based on depth, calculated earlier */
                                k3 = k3 / (i * i);
                                d2 = (double) k3 * 0.1D + (double) (k2 + l2 & 1) * 0.2D;
                                i5 = 1;

                                if (d2 < 0.5D) {
                                    i5 = 2;
                                }

                                if (d2 > 0.9D) {
                                    i5 = 0;
                                }

                                if (d2 > 1.5D) {
                                    i5 = 3;
                                }
                            } else {
                                /* Otherwise, calculate shading based on height difference from the previous block */
                                d2 = (d1 - d0) * 4.0D / (double) (i + 4) + ((double) (k2 + l2 & 1) - 0.5D) * 0.4D;
                                i5 = 1;

                                if (d2 > 0.6D) {
                                    i5 = 2;
                                }

                                if (d2 < -0.6D) {
                                    i5 = 0;
                                }

                                if (d2 < -1.0D) {
                                    i5 = 3;
                                }
                            }

                            /* Update d0 to this block's y level */
                            d0 = d1;

                            if (l1 >= 0) {
                                /* Calculate color, and store */
                                byte b1 = (byte) (mapColor.colorIndex * 4 + i5);
                                this.colors[k1 + l1 * 128] = b1;
                            }
                        }
                    }
                }
            }
        }
    }
}
