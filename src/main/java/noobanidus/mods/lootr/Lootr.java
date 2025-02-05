package noobanidus.mods.lootr;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import noobanidus.mods.lootr.advancement.GenericTrigger;
import noobanidus.mods.lootr.commands.CommandLootr;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.events.HandleAdvancement;
import noobanidus.mods.lootr.events.HandleBreak;
import noobanidus.mods.lootr.events.HandleCart;
import noobanidus.mods.lootr.init.ModBlocks;
import noobanidus.mods.lootr.init.ModEntities;
import noobanidus.mods.lootr.init.ModItems;
import noobanidus.mods.lootr.init.ModTiles;
import noobanidus.mods.lootr.setup.CommonSetup;
import noobanidus.mods.lootr.setup.Setup;
import noobanidus.mods.lootr.ticker.EntityTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("lootr")
public class Lootr {
  public static final Logger LOG = LogManager.getLogger();
  public static final String MODID = "lootr";
  public static GenericTrigger<Void> CHEST_PREDICATE = null;
  public static GenericTrigger<Void> BARREL_PREDICATE = null;
  public static GenericTrigger<Void> CART_PREDICATE = null;
  public static GenericTrigger<ResourceLocation> ADVANCEMENT_PREDICATE = null;
  public static GenericTrigger<Void> SCORE_PREDICATE = null;
  public static final ResourceLocation CHEST_LOCATION = new ResourceLocation(MODID, "chest_opened");
  public static final ResourceLocation BARREL_LOCATION = new ResourceLocation(MODID, "barrel_opened");
  public static final ResourceLocation CART_LOCATION = new ResourceLocation(MODID, "cart_opened");
  public static final ResourceLocation ADVANCEMENT_LOCATION = new ResourceLocation(MODID, "advancement");
  public static final ResourceLocation SCORE_LOCATION = new ResourceLocation(MODID, "score");
  public CommandLootr COMMAND_LOOTR;
  public static ItemGroup TAB = new ItemGroup(MODID) {
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack makeIcon() {
      return new ItemStack(ModBlocks.CHEST);
    }
  };

  public Lootr() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigManager.COMMON_CONFIG);
    ConfigManager.loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(Lootr.MODID + "-common.toml"));
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    MinecraftForge.EVENT_BUS.addListener(HandleBreak::onBlockBreak);
    MinecraftForge.EVENT_BUS.addListener(HandleCart::onEntityJoin);
    MinecraftForge.EVENT_BUS.addListener(HandleCart::onEntityTrack);
    MinecraftForge.EVENT_BUS.addListener(HandleAdvancement::onAdvancement);
    MinecraftForge.EVENT_BUS.addListener(EntityTicker::onServerTick);
    MinecraftForge.EVENT_BUS.addListener(this::onCommands);

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Setup::client);

    modBus.addListener(CommonSetup::init);
    modBus.addGenericListener(TileEntityType.class, ModTiles::registerTileEntityType);
    modBus.addGenericListener(Block.class, ModBlocks::registerBlocks);
    modBus.addGenericListener(EntityType.class, ModEntities::registerEntityType);
    modBus.addGenericListener(Item.class, ModItems::registerItems);
  }

  public void onCommands(RegisterCommandsEvent event) {
    COMMAND_LOOTR = new CommandLootr(event.getDispatcher());
    COMMAND_LOOTR.register();
  }
}
