package testing;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.Proxy;
import com.rwtema.careerbees.bees.CustomBeeModel;
import com.rwtema.careerbees.helpers.RandomHelper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(modid = "testmod", version = "1.0.0", clientSideOnly = true)
public class TestMod {
	static final File BLOCKS_TEXTURE_DIR = new File("C:\\extrautils\\beemod\\src\\main\\resources\\assets\\careerbees\\textures\\blocks");
	static final File ITEM_TEXTURE_DIR = new File("C:\\extrautils\\beemod\\src\\main\\resources\\assets\\careerbees\\textures\\items");
	static final File BLOCK_STATES_DIRECTORY = new File("C:\\extrautils\\beemod\\src\\main\\resources\\assets\\careerbees\\blockstates");
	private static final File MODEL_DIRECTORY = new File("C:\\extrautils\\beemod\\src\\main\\resources\\assets\\careerbees\\models");
	static final File ITEM_MODEL_DIRECTORY = new File(MODEL_DIRECTORY, "item");
	static final File BLOCK_MODEL_DIRECTORY = new File(MODEL_DIRECTORY, "block");

	static {
		BeeMod.logger.info("\n\n"+ Stream.of(RandomHelper.permutations)
				.map(l -> Stream.of(l).map(EnumFacing::ordinal).map(Object::toString).collect(Collectors.joining("")))
				.sorted()
				.collect(Collectors.joining("\n")));

		Proxy.itemBlockModelHook = TestMod::createItemBlockHook;
		Proxy.itemModelHook = TestMod::createItemHook;
		CustomBeeModel.modelCreationHook = TestMod::createBeeModels;
	}

	private static void createBeeModels(String suffix, @Nonnull CustomBeeModel customBeeModel) {
		for (Pair<String, ModelResourceLocation> pair : ImmutableList.of(
				Pair.of("drone", customBeeModel.drone_location),
				Pair.of("queen", customBeeModel.queen_location),
				Pair.of("princess", customBeeModel.princess_location))) {

			String resourcePath = pair.getRight().getResourcePath();
			File file = new File(ITEM_MODEL_DIRECTORY, resourcePath + ".json");

			if (file.exists()) {
				return;
			}

			JsonObject model = new JsonObject();
			model.addProperty("parent", "forestry:item/mirror_hands");
			JsonObject textures = new JsonObject();

			int n = customBeeModel.background ? 1 : 0;

			textures.addProperty("layer" + n, "forestry:items/bees/default/" + pair.getLeft() + ".outline");
			textures.addProperty("layer" + (n + 1), "forestry:items/bees/default/body1");
			textures.addProperty("layer" + (n + 2), "forestry:items/bees/default/" + pair.getLeft() + ".body2");
			textures.addProperty("layer" + (customBeeModel.background ? "0" : "3"), "careerbees:items/bees/" + suffix);
			model.add("textures", textures);

			writeJSon(file, model);

			copySafe(new File(ITEM_TEXTURE_DIR, "bees/debug.png"), new File(ITEM_TEXTURE_DIR, "bees/" + suffix + ".png"));
		}
	}

	private static void writeJSon(@Nonnull File file, JsonObject model) {
		try (FileWriter writer = new FileWriter(file.getPath())) {
			JsonWriter jsonWriter = new JsonWriter(writer);
			jsonWriter.setIndent("  ");
			jsonWriter.setLenient(true);
			Streams.write(model, jsonWriter);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createItemBlockHook(ItemBlock block, @Nonnull ModelResourceLocation inventoryModelResourceLocation) {
		String resourcePath = inventoryModelResourceLocation.getResourcePath();
		File fileBlockStates = new File(BLOCK_STATES_DIRECTORY, resourcePath + ".json");
		if (fileBlockStates.exists()) {
			return;
		}

		{
			JsonObject blockstatesJSon = new JsonObject();
			JsonObject variants = new JsonObject();
			JsonObject normal = new JsonObject();
			normal.addProperty("model", "careerbees:" + resourcePath);
			variants.add("normal", normal);
			blockstatesJSon.add("variants", variants);
			writeJSon(fileBlockStates, blockstatesJSon);
		}

		{
			JsonObject model = new JsonObject();
			model.addProperty("parent", "block/cube_all");
			JsonObject textures = new JsonObject();
			textures.addProperty("all", "careerbees:blocks/" + resourcePath);
			model.add("textures", textures);
			writeJSon(new File(BLOCK_MODEL_DIRECTORY, resourcePath + ".json"), model);
		}

		{
			JsonObject model = new JsonObject();
			model.addProperty("parent", "careerbees:block/" + resourcePath);
			writeJSon(new File(BLOCK_MODEL_DIRECTORY, resourcePath + ".json"), model);
		}

		copySafe(new File(ITEM_TEXTURE_DIR, "debug.png"), new File(BLOCKS_TEXTURE_DIR, resourcePath + ".png"));
	}

	private static void createItemHook(@Nonnull ModelResourceLocation modelResourceLocation) {
		String resourcePath = modelResourceLocation.getResourcePath();
		File file = new File(ITEM_MODEL_DIRECTORY, resourcePath + ".json");

		if (file.exists()) {
			return;
		}

		JsonObject model = new JsonObject();
		model.addProperty("parent", "item/generated");
		JsonObject textures = new JsonObject();
		textures.addProperty("layer0", "careerbees:items/" + resourcePath);
		model.add("textures", textures);

		writeJSon(file, model);

		copySafe(new File(ITEM_TEXTURE_DIR, "debug.png"), new File(ITEM_TEXTURE_DIR, resourcePath + ".png"));
	}

	private static void copySafe(@Nonnull File from, @Nonnull File to) {
		if (to.exists()) return;
		try {
			Files.copy(from, to);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
