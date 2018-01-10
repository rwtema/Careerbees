package com.rwtema.careerbees;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.blocks.BlockAlvearyHiveFrameHolder;
import com.rwtema.careerbees.blocks.BlockFlowerPedastal;
import com.rwtema.careerbees.blocks.TileAlvearyHiveFrameHolder;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.entity.EntityChunkData;
import com.rwtema.careerbees.gui.GuiHandler;
import com.rwtema.careerbees.items.*;
import com.rwtema.careerbees.lang.Lang;
import com.rwtema.careerbees.networking.BeeNetworking;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

@Mod(modid = BeeMod.MODID, version = BeeMod.VERSION, dependencies = "required-after:forestry")
public class BeeMod {
	public static final String MODID = "careerbees";
	public static final String VERSION = "1.0";
	public static final EntityEquipmentSlot[] ARMOR_SLOTS = {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
	public static final boolean deobf, deobf_folder;
	public static final String RESOURCE_FOLDER = "careerbees";
	public static final Random RANDOM = new Random();
	public static final Logger logger = LogManager.getLogger(BeeMod.MODID);

	@SidedProxy(serverSide = "com.rwtema.careerbees.Proxy", clientSide = "com.rwtema.careerbees.ProxyClient")
	public static Proxy proxy;
	@Mod.Instance(value = BeeMod.MODID)
	public static BeeMod instance;
	public static BeeModCreativeTab creativeTab;
	public static BeeModCreativeTab creativeTabSpammy;


	static {
		boolean d;
		try {
			World.class.getMethod("getBlockState", BlockPos.class);
			d = true;
			logger.info("Dev Enviroment detected. Releasing hounds...");
		} catch (@Nonnull NoSuchMethodException | SecurityException e) {
			d = false;
		}
		deobf = d;


		if (deobf) {
			URL resource = BeeMod.class.getClassLoader().getResource(BeeMod.class.getName().replace('.', '/').concat(".class"));
			deobf_folder = resource != null && "file".equals(resource.getProtocol());
		} else
			deobf_folder = false;

	}

	public BlockAlvearyHiveFrameHolder alvearyHiveFrameHolder;
	public ItemBlock alvearyHiveFrameHolderItemBlock;
	public BlockFlowerPedastal plantFrame;
	public ItemBlock plantFrameItemBlock;
	public ItemEternalFrame itemEternalFrame;
	public ItemMutationFrame itemMutationFrame;
	public ItemPoisonFrame itemPoisonFrame;
	public ItemPheremoneFrame itemPheremoneFrame;
	public ItemSettingsFrame itemSettingsFrame;
	public ItemIngredients itemIngredients;
	public EnumMap<EntityEquipmentSlot, ItemBeeArmor> beeArmors;
	public ItemBeeGun itemBeeGun;

	@Nonnull
	public static <T extends Item> EnumMap<EntityEquipmentSlot, T> initArmors(@Nonnull Function<EntityEquipmentSlot, T> constructor) {
		EnumMap<EntityEquipmentSlot, T> map = new EnumMap<>(EntityEquipmentSlot.class);
		for (EntityEquipmentSlot armorSlot : BeeMod.ARMOR_SLOTS) {
			map.put(armorSlot, constructor.apply(armorSlot));
		}
		return map;
	}

	public void initEntries() {
		itemIngredients = new ItemIngredients();
		itemPoisonFrame = (ItemPoisonFrame) new ItemPoisonFrame(0.1F).setUnlocalizedName(BeeMod.MODID + ":poisonframe").setRegistryName(BeeMod.MODID + ":poison_frame");
		alvearyHiveFrameHolder = new BlockAlvearyHiveFrameHolder();
		alvearyHiveFrameHolderItemBlock = new ItemBlock(alvearyHiveFrameHolder);
		plantFrame = new BlockFlowerPedastal();
		plantFrameItemBlock = new ItemBlock(plantFrame);
//		plantFrameItemBlock = new ItemMultiTexture(plantFrame, plantFrame, stack -> plantFrame.getStateFromMeta(stack.getMetadata()).getValue(BlockFlowerPedastal.PLANT_TYPE).getName());
		itemMutationFrame = (ItemMutationFrame) new ItemMutationFrame().setUnlocalizedName(BeeMod.MODID + ":mutationframe").setRegistryName(BeeMod.MODID + ":mutation_frame");
		itemEternalFrame = (ItemEternalFrame) new ItemEternalFrame().setUnlocalizedName(BeeMod.MODID + ":eternalframe").setRegistryName(BeeMod.MODID + ":eternalframe");
		beeArmors = initArmors(ItemBeeArmor::new);
		itemPheremoneFrame = (ItemPheremoneFrame) new ItemPheremoneFrame().setUnlocalizedName(BeeMod.MODID + ":pheremoneframe").setRegistryName(BeeMod.MODID + ":pheremone_frame");
		itemBeeGun = (ItemBeeGun) new ItemBeeGun().setUnlocalizedName(BeeMod.MODID + ":beegun").setRegistryName(BeeMod.MODID + ":beegun");
		itemSettingsFrame = (ItemSettingsFrame) (new ItemSettingsFrame().setUnlocalizedName(BeeMod.MODID + ":settingsframe").setRegistryName(BeeMod.MODID+":settings_frame"));
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		BeeNetworking.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		creativeTab = new BeeModCreativeTab(BeeMod.MODID);
		creativeTabSpammy = new BeeModCreativeTab(BeeMod.MODID + ".spam");
		initEntries();
		registerBlockItemBlock(plantFrame, plantFrameItemBlock);
		registerBlockItemBlock(alvearyHiveFrameHolder, alvearyHiveFrameHolderItemBlock);
		registerItem(itemMutationFrame);
		registerItem(itemEternalFrame);
		registerItem(itemBeeGun);
		registerItem(itemPoisonFrame);
		registerItem(itemPheremoneFrame);
		registerItem(itemIngredients);
		registerItem(itemSettingsFrame);
		registerArmours(beeArmors);

		GameRegistry.registerTileEntity(TileFlowerPedastal.class, BeeMod.MODID + ":flower.pedastal");
		GameRegistry.registerTileEntity(TileAlvearyHiveFrameHolder.class, BeeMod.MODID + ":alveary.frame");

		EntityChunkData.init();

		proxy.preInit();

		CareerBeeSpecies.init();

		OreDictionary.registerOre("flower", new ItemStack(Blocks.RED_FLOWER, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("flower", new ItemStack(Blocks.YELLOW_FLOWER, 1, OreDictionary.WILDCARD_VALUE));
	}

	private void registerBlockItemBlock(@Nonnull Block block, @Nonnull ItemBlock itemBlock) {
		ForgeRegistries.BLOCKS.register(block);
		registerItem(itemBlock.setRegistryName(Validate.notNull(block.getRegistryName())));
	}

	private void registerItem(@Nonnull Item item) {
		ForgeRegistries.ITEMS.register(item);
		if (BeeMod.deobf_folder) {
			Lang.translate(item.getUnlocalizedName() + ".name", item.getClass().getSimpleName());
		}
	}

	private <T extends Item> void registerArmours(@Nonnull EnumMap<EntityEquipmentSlot, T> map) {
		for (T armor : map.values()) {
			registerItem(armor);
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		CareerBeeSpecies.register();
		proxy.init();

		ItemStack bark_oak = ItemIngredients.IngredientType.BARK.get();
		bark_oak.setTagInfo("bark", (new ItemStack(Blocks.LOG)).writeToNBT(new NBTTagCompound()));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(Blocks.LOG), "ss", "ss", 's', bark_oak) {
			@Nonnull
			@Override
			public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
				ItemStack base = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stackInSlot = inv.getStackInSlot(i);
					if (!stackInSlot.isEmpty()) {
						ItemStack bark = Optional.ofNullable(stackInSlot.getTagCompound())
								.map(t1 -> t1.getCompoundTag("bark"))
								.map(ItemStack::new)
								.filter(s -> !s.isEmpty())
								.orElse(new ItemStack(Blocks.LOG));
						if (base == null) {
							base = bark;
						} else if (!ItemHandlerHelper.canItemStacksStack(bark, base)) {
							return ItemStack.EMPTY;
						}
					}
				}
				if (base == null) return ItemStack.EMPTY;
				return base;
			}

		}.setRegistryName(new ResourceLocation(MODID, "bark_wood")));

		Stream.of(ItemIngredients.IngredientType.values())
				.filter(t -> t.oreDic != null)
				.forEach(t ->
						OreDictionary.registerOre(t.oreDic, t.get())
				);

		FurnaceRecipes.instance().addSmeltingRecipe(ItemIngredients.IngredientType.DUSTIRON.get(), new ItemStack(Items.IRON_INGOT), 0);
		FurnaceRecipes.instance().addSmeltingRecipe(ItemIngredients.IngredientType.DUSTGOLD.get(), new ItemStack(Items.GOLD_INGOT), 0);
		for (Pair<ItemIngredients.IngredientType, String> pair : ImmutableList.of(
				Pair.of(ItemIngredients.IngredientType.DUSTCOPPER, "ingotCopper"),
				Pair.of(ItemIngredients.IngredientType.DUSTTIN, "ingotTin"),
				Pair.of(ItemIngredients.IngredientType.DUSTBRONZE, "ingotBronze")
		)) {
			ItemStack input = pair.getKey().get();
			NonNullList<ItemStack> ores = OreDictionary.getOres(pair.getValue());
			ores.stream()
					.max(Comparator.comparingInt(s1 -> Validate.notNull(s1.getItem().getRegistryName()).getResourceDomain().equals("forestry") ? 1 : 0))
					.ifPresent(output -> FurnaceRecipes.instance().addSmeltingRecipe(input, output, 0));
		}
	}
}



