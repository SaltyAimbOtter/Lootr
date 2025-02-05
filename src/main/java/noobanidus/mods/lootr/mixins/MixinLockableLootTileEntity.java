package noobanidus.mods.lootr.mixins;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.api.ILootTile;
import noobanidus.mods.lootr.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.Set;

@Mixin(LockableLootTileEntity.class)
public class MixinLockableLootTileEntity {
  private final Logger log = LogManager.getLogger(Lootr.MODID);

  @Inject(method = "setLootTable(Lnet/minecraft/world/IBlockReader;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/ResourceLocation;)V",
      at = @At("HEAD"))
  private static void setLootTable(IBlockReader reader, Random rand, BlockPos pos, ResourceLocation lootTableIn, CallbackInfo info) {
    if (ConfigManager.getLootBlacklist().contains(lootTableIn)) {
      return;
    }
    if (reader instanceof IServerWorld) {
      BlockState state = reader.getBlockState(pos);
      BlockState replacement = ConfigManager.replacement(state);
      if (replacement != null) {
        IServerWorld world = (IServerWorld) reader;
        RegistryKey<World> key = world.getLevel().dimension();
        if (ConfigManager.isDimensionBlocked(key)) {
          return;
        }
        IChunk chunk = world.getChunk(pos);
        chunk.removeBlockEntity(pos);
        if (state.getProperties().contains(ChestBlock.WATERLOGGED)) {
          replacement = replacement.setValue(ChestBlock.WATERLOGGED, state.getValue(ChestBlock.WATERLOGGED));
        }
        world.setBlock(pos, replacement, 2);
        TileEntity te = replacement.getBlock().createTileEntity(replacement, reader);
        if (te != null) {
          chunk.setBlockEntity(pos, te);
        }
      }
    }
  }

  private static final ResourceLocation REPURPOSED_END_SHULKER = new ResourceLocation("repurposed_structures", "chests/dungeon/end");

  @Inject(method = "setLootTable(Lnet/minecraft/util/ResourceLocation;J)V", at = @At("HEAD"))
  private void setLootTable(ResourceLocation table, long seed, CallbackInfo info) {
    if (this instanceof ILootTile || !ConfigManager.REPORT_TABLES.get()) {
      return;
    }

    Set<String> modids = Sets.newHashSet("apotheosis", "artifacts");
    if (table != null && modids.contains(table.getNamespace()) || REPURPOSED_END_SHULKER.equals(table)) {
      return;
    }


    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

    log.error("\n=================================================" +
        "\n  Lootr detected a loot chest creation that it " +
        "\n  can't replace. Please consider reporting it!" +
        "\n    Tile: " + this +
        "\n    Table: " + table +
        "\n    Location: " + ((LockableLootTileEntity) (Object) (this)).getBlockPos().toString() +
        "\n    Stack: " + stacktrace[3].toString() +
        "\n           " + stacktrace[4].toString() +
        "\n           " + stacktrace[5].toString());
  }
}
