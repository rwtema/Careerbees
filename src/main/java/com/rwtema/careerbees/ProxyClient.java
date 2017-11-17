package com.rwtema.careerbees;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rwtema.careerbees.bees.CustomBeeModel;
import com.rwtema.careerbees.blocks.TESRPlantFrame;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.commands.CommandColors;
import com.rwtema.careerbees.tooltip.TooltipHandler;
import com.rwtema.careerbees.items.ItemIngredients;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
	static HashMap<TextureAtlasSprite, ModelBark> models = new HashMap<>();

	public static TextureAtlasSprite exclamation_sprite;

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static String wrap(Object k) {
		String s = k.toString();
		return s.toLowerCase().replaceAll("[\\s\\.]+", "_");
	}

	@Override
	public void preInit() {

		registerBlockModel(BeeMod.instance.plantFrameItemBlock);
		registerBlockModel(BeeMod.instance.alvearyHiveFrameHolderItemBlock);
		registerModel(BeeMod.instance.itemPheremoneFrame);
		registerModel(BeeMod.instance.itemMutationFrame);
		registerModel(BeeMod.instance.itemPoisonFrame);
		registerModel(BeeMod.instance.itemSettingsFrame);

		BeeMod.instance.beeArmors.values().forEach(this::registerModel);

		EnumMap<ItemIngredients.IngredientType, ModelResourceLocation> map = new EnumMap<>(ItemIngredients.IngredientType.class);

		for (ItemIngredients.IngredientType ingredientType : ItemIngredients.IngredientType.values()) {
			ResourceLocation registryName = BeeMod.instance.itemIngredients.getRegistryName();
			ModelResourceLocation modelResourceLocation = new ModelResourceLocation(wrap(registryName + "." + ingredientType.name().toLowerCase(Locale.ENGLISH)), "inventory");
			ingredientType.mrl = modelResourceLocation;
			map.put(ingredientType, modelResourceLocation);
			ModelLoader.setCustomModelResourceLocation(BeeMod.instance.itemIngredients, ingredientType.meta, modelResourceLocation);
			ModelBakery.registerItemVariants(BeeMod.instance.itemIngredients, modelResourceLocation);
			if (Proxy.itemModelHook != null) {
				Proxy.itemModelHook.accept(modelResourceLocation);
			}
		}
		ModelLoader.setCustomMeshDefinition(BeeMod.instance.itemIngredients, stack -> map.get(ItemIngredients.getIngredientType(stack)));


		if (BeeMod.deobf) {
			ClientCommandHandler.instance.registerCommand(new CommandColors());
		}

		CustomBeeModel.SUFFIXES.forEach(CustomBeeModel::registerModels);

		new TooltipHandler().init();
	}

	@SubscribeEvent
	public void registerTexture(TextureStitchEvent.Pre event){
		exclamation_sprite = event.getMap().registerSprite(new ResourceLocation(BeeMod.MODID, "items/exclamation"));
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void registerModels(ModelBakeEvent event) {

		IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		models.clear();
		IBakedModel base = modelRegistry.getObject(ItemIngredients.IngredientType.BARK.mrl);
		if (base != null && !(base instanceof ModelWithOverrides)) {
			modelRegistry.putObject(ItemIngredients.IngredientType.BARK.mrl, new ModelWithOverrides(base) {
				@Override
				public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
					NBTTagCompound tagCompound = stack.getTagCompound();
					if (tagCompound == null) return null;
					ItemStack itemStack = new ItemStack(tagCompound.getCompoundTag("bark"));
					if (itemStack.isEmpty()) return null;
					ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
					IBakedModel itemModel = mesher.getItemModel(itemStack);
					itemModel = itemModel.getOverrides().handleItemState(itemModel, itemStack, world, entity);
					return models.computeIfAbsent(itemModel.getParticleTexture(), tex -> new ModelBark(originalModel, tex));
				}
			});
		}
	}

	private void registerBlockModel(final ItemBlock item) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(wrap(item.getRegistryName()), "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, modelResourceLocation);
		ModelBakery.registerItemVariants(item, modelResourceLocation);
		ModelLoader.setCustomMeshDefinition(item, stack -> modelResourceLocation);
		if (Proxy.itemBlockModelHook != null)
			Proxy.itemBlockModelHook.accept(item, modelResourceLocation);
	}

	private void registerModel(final Item item) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(wrap(item.getRegistryName()), "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, modelResourceLocation);
		ModelBakery.registerItemVariants(item, modelResourceLocation);
		ModelLoader.setCustomMeshDefinition(item, stack -> modelResourceLocation);
		if (Proxy.itemModelHook != null)
			Proxy.itemModelHook.accept(modelResourceLocation);
	}

	@Override
	public void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileFlowerPedastal.class, new TESRPlantFrame());
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(BeeMod.instance.itemIngredients::getColorFromItemstack, BeeMod.instance.itemIngredients);
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void run(ClientRunnable runnable) {
		runnable.run();
	}

	public static class ModelBark extends ModelDelegate {
		final TextureAtlasSprite sprite;
		HashMap<EnumFacing, List<BakedQuad>> transformedQuads = new HashMap<>();

		public ModelBark(IBakedModel base, TextureAtlasSprite sprite) {
			super(base);
			this.sprite = sprite;
		}

		@Nonnull
		@Override
		public TextureAtlasSprite getParticleTexture() {
			return sprite;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			List<BakedQuad> quadList = transformedQuads.get(side);
			if (quadList == null || BeeMod.deobf_folder) {
				List<BakedQuad> quads = super.getQuads(state, null, rand);
				if (quads.isEmpty()) {
					transformedQuads.put(side, ImmutableList.of());
					return ImmutableList.of();
				}

				quadList = Lists.newArrayList();
				transformedQuads.put(side, quadList);

				Set<TextureAtlasSprite> texes = new HashSet<>();
				HashMultimap<Integer, TextureAtlasSprite> sprites = HashMultimap.create();

				for (BakedQuad quad : quads) {
					if (quad.getFace().getAxis() != EnumFacing.Axis.Z) {
						quadList.add(new BakedQuadRetextured(quad, sprite));
					}
					sprites.put(quad.getTintIndex(), quad.getSprite());
				}

				if (side == null) {
					for (Integer tintIndex : sprites.keySet()) {
						List<BakedQuad> q = new ArrayList<>();
						for (TextureAtlasSprite tex : sprites.get(tintIndex)) {
							q.addAll(ItemTextureQuadConverter.convertTexture(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), tex, sprite, 8.5f / 16f, EnumFacing.SOUTH, 0xffffffff));
							q.addAll(ItemTextureQuadConverter.convertTexture(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), tex, sprite, 7.5f / 16f, EnumFacing.NORTH, 0xffffffff));
						}
						if (tintIndex == 0) {
							quadList.addAll(q);
						} else {
							for (BakedQuad quad : q) {
								quadList.add(new BakedQuad(
										Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length),
										tintIndex,
										FaceBakery.getFacingFromVertexData(quad.getVertexData()),
										quad.getSprite(),
										quad.shouldApplyDiffuseLighting(),
										quad.getFormat()));
							}
						}

					}
				}
			}

			return quadList;
		}


	}

	public static abstract class ModelWithOverrides extends ModelDelegate {

		public ModelWithOverrides(IBakedModel base) {
			super(base);
		}

		@Nonnull
		@Override
		public ItemOverrideList getOverrides() {
			return new ItemOverrideList(Lists.newArrayList()) {
				@Nonnull
				@Override
				public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
					IBakedModel iBakedModel = ModelWithOverrides.this.handleItemState(originalModel, stack, world, entity);
					if (iBakedModel == null)
						return base.getOverrides().handleItemState(originalModel, stack, world, entity);
					return iBakedModel;
				}
			};
		}

		public abstract IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity);

		@Nonnull
		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull ItemCameraTransforms.TransformType cameraTransformType) {
			return Pair.of(this, null);
		}
	}
}
