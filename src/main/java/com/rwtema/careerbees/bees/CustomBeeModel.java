package com.rwtema.careerbees.bees;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.ClientRunnable;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeModelProvider;
import forestry.api.core.IModelManager;
import forestry.api.genetics.AlleleManager;
import forestry.core.config.Constants;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class CustomBeeModel implements IBeeModelProvider {
	public static final Set<String> SUFFIXES = new HashSet<>();
	public static BiConsumer<String, CustomBeeModel> modelCreationHook;
	@SideOnly(Side.CLIENT)
	private static IAlleleBeeSpecies throwback;
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation drone_location;
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation queen_location;
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation princess_location;
	public final String suffix;
	public boolean background;

	public CustomBeeModel(final String suffix) {
		this(suffix, false);
	}

	public CustomBeeModel(final String suffix, boolean background) {
		this.suffix = suffix;
		this.background = background;
		SUFFIXES.add(suffix);

		ClientRunnable.safeRun(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				drone_location = modelLocation("drone", suffix);
				queen_location = modelLocation("queen", suffix);
				princess_location = modelLocation("princess", suffix);

				registerModels(suffix);
			}
		});

		if (modelCreationHook != null) {
			modelCreationHook.accept(suffix, this);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels(String suffix) {
		doRegister(suffix, "queen", "bee_queen_ge");
		doRegister(suffix, "princess", "bee_princess_ge");
		doRegister(suffix, "drone", "bee_drone_ge");
	}

	private static void doRegister(String suffix, String queen, String bee_queen_ge) {
		ModelBakery.registerItemVariants(
				Validate.notNull(Item.REGISTRY.getObject(new ResourceLocation("forestry", bee_queen_ge))),
				CustomBeeModel.resourceLocation(queen, suffix));
	}

	@SideOnly(Side.CLIENT)
	private static ResourceLocation resourceLocation(String name, String mining) {
		return new ResourceLocation(BeeMod.MODID + ":bees/" + name + "_" + mining);
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	private ModelResourceLocation modelLocation(String name, String mining) {
		return new ModelResourceLocation(BeeMod.MODID + ":bees/" + name + "_" + mining, "inventory");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(@Nonnull Item item, @Nonnull IModelManager manager) {

	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModel(@Nonnull EnumBeeType type) {
		switch (type) {
			case DRONE:
				return drone_location;
			case QUEEN:
				return queen_location;
			case PRINCESS:
				return princess_location;
		}
		if (throwback == null) {
			throwback = Validate.notNull((IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(Constants.MOD_ID + ".speciesForest"));
		}

		return throwback.getModel(type);
	}
}
