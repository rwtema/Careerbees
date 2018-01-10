package com.rwtema.careerbees.lang;

import com.google.common.collect.Maps;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.ClientRunnable;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class Lang {
	@Nullable
	private final static TreeMap<String, String> lang = BeeMod.deobf_folder ? new TreeMap<>() : null;
	private final static HashMap<String, String> textKey = new HashMap<>();
	@Nullable
	private final static HashMap<String, String> existingMCLangMap;
	@Nonnull
	private final static HashMap<String, String> injectingMCLangMap;
	private static final int MAX_KEY_LEN = 32;
	private static final TObjectIntHashMap<String> numRandomEntries = new TObjectIntHashMap<>();
	private static int size = 0;

	static {
		if (BeeMod.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			try {
				FileInputStream fis = null;
				try {
					File file = getMissedEntriesFile();
					fis = new FileInputStream(file);
					readStream(fis, true);
				} finally {
					if (fis != null)
						fis.close();
				}
			} catch (FileNotFoundException ignore) {

			} catch (IOException e) {
				e.printStackTrace();
			}

			BeeMod.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					ResourceLocation resourceLocation = new ResourceLocation(BeeMod.RESOURCE_FOLDER, "lang/en_US.lang");
					try {
						IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
						InputStream stream = null;
						try {
							stream = resource.getInputStream();
							readStream(stream, false);
						} finally {
							if (stream != null)
								stream.close();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					createMissedFile();
				}
			});

			LanguageMap instance = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, null, "instance");
			existingMCLangMap = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, instance, "languageList");
			size = existingMCLangMap.size();
			injectingMCLangMap = Maps.newHashMap();
		} else {
			existingMCLangMap = null;
			injectingMCLangMap = Maps.newHashMap();
		}
	}

	public static void init() {
		BeeMod.logger.info("Lang Initialized");
	}


	public static void readStream(@Nonnull InputStream stream, boolean safe) {
		Map<String, String> langMap = LanguageMap.parseLangFile(stream);
		if (safe) {
			for (Map.Entry<String, String> entry : langMap.entrySet()) {
				String key = makeKey(entry.getValue());
				if (!key.equals(entry.getKey()))
					lang.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
			}
		} else {
			for (Map.Entry<String, String> entry : langMap.entrySet()) {
				lang.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
			}

		}
	}

	@Nonnull
	public static String translate(@Nonnull String text) {
		return translatePrefix(text);
	}

	@Nonnull
	public static String translatePrefix(@Nonnull String text) {
		String key = getKey(text);
		return translate(key, text);
	}

	public static String getKey(@Nonnull String text) {
		String key = textKey.get(text);
		if (key == null) {
			key = makeKey(text);
			textKey.put(text, key);
			if (BeeMod.deobf_folder) {
				translate(key, text);
			}
		}
		return key;
	}

	private static String makeKey(@Nonnull String text) {
		String key;
		String t = stripText(text);
		key = "BeeMod.text." + t;
		return key;
	}

	@Nonnull
	public static String stripText(@Nonnull String text) {
		String t = text.replaceAll("([^A-Za-z\\s])", "").trim();
		t = t.replaceAll("\\s+", ".").toLowerCase();
		if (t.length() > MAX_KEY_LEN) {
			int n = t.indexOf('.', MAX_KEY_LEN);
			if (n != -1)
				t = t.substring(0, n);
		}
		return t;
	}

	@Nonnull
	public static String translate(@Nonnull String key, @Nonnull String _default) {
		if (BeeMod.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			if (size != existingMCLangMap.size()) {
				existingMCLangMap.putAll(injectingMCLangMap);
				size = existingMCLangMap.size();
			}
		}

		if (I18n.canTranslate(key))
			return I18n.translateToLocal(key);
		initKey(key, _default);
		return _default;
	}

	public static String initKey(String key, @Nonnull String _default) {
		if (BeeMod.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			if (!_default.equals(lang.get(key))) {
				lang.put(key, _default);
				createMissedFile();
			}

			if (!existingMCLangMap.containsKey(key)) {
				injectingMCLangMap.put(key, _default);
				existingMCLangMap.put(key, _default);
			}
		}
		return key;
	}

	public static void createMissedFile() {
		PrintWriter out = null;
		try {
			try {
				File file = getMissedEntriesFile();
				if (file.getParentFile() != null) {
					if (file.getParentFile().mkdirs())
						BeeMod.logger.info("Making Translation Directory");
				}

				out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				String t = null;
				for (Map.Entry<String, String> entry : lang.entrySet()) {
					String key_prefix = entry.getKey();
					if (key_prefix.startsWith("careerbees.")) {
						key_prefix = key_prefix.substring("careerbees.".length());
					}

					int i = key_prefix.indexOf('.');
					if (i < 0) {
						i = 1;
					}

					key_prefix = key_prefix.substring(0, i);
					if (t != null) {
						if (!t.equals(key_prefix)) {
							out.println("");
						}
					}
					t = key_prefix;

					out.println(entry.getKey().toLowerCase() + "=" + entry.getValue());
				}
			} finally {
				if (out != null)
					out.close();
			}
			out = null;


			try {
				File file = getNonTrivialFile();
				if (file.getParentFile() != null) {
					if (file.getParentFile().mkdirs())
						BeeMod.logger.info("Making Translation Directory");
				}

				out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				String t = null;
				for (Map.Entry<String, String> entry : lang.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key.equals(makeKey(value))) {
						continue;
					}

					int i = key.indexOf('.');
					if (i < 0) {
						i = 1;
					}

					String s = key.substring(0, i);
					if (t != null) {
						if (!t.equals(s)) {
							out.println("");
						}
					}
					t = s;

					out.println(key.toLowerCase() + "=" + value);
				}
			} finally {
				if (out != null)
					out.close();
			}


		} catch (Exception err) {
			err.printStackTrace();
		}
	}


	@Nonnull
	private static File getMissedEntriesFile() {
		return new File(new File(new File("."), "debug_text"), "missed_en_US.lang");
	}

	@Nonnull
	private static File getNonTrivialFile() {
		return new File(new File(new File("."), "debug_text"), "non_trivial_en_US.lang");
	}

	@Nonnull
	public static TextComponentTranslation chat(@Nonnull String message, Object... args) {
		String key = getKey(message);
		if (I18n.canTranslate(key))
			return new TextComponentTranslation(key, args);

		return new TextComponentTranslation(message, args);
	}

	@Nonnull
	public static TextComponentTranslation chat(boolean dummy, @Nonnull String key, @Nonnull String _default, Object... args) {
		return new TextComponentTranslation(translate(key, _default), args);
	}


	public static String translateArgs(boolean dummy, @Nonnull String key, @Nonnull String _default, Object... args) {
		String translate = translate(key, _default);
		try {
			return String.format(translate, args);
		} catch (IllegalFormatException err) {
			throw new RuntimeException("Message: \"" + _default + "\" with key : \"" + key + "\" and translation: \"" + translate + "\"", err);
		}
	}

	public static String translateArgs(@Nonnull String message, Object... args) {
		String translate = Lang.translate(message);
		try {
			return String.format(translate, args);
		} catch (IllegalFormatException err) {
			throw new RuntimeException("Message: \"" + message + "\" with key : \"" + getKey(message) + "\" and translation: \"" + translate + "\"", err);
		}
	}

	public static String getItemName(@Nonnull Block block) {
		return getItemName(new ItemStack(block));
	}

	public static String getItemName(@Nonnull Item item) {
		return getItemName(new ItemStack(item));
	}

	public static String getItemName(@Nonnull ItemStack stack) {
		return stack.getDisplayName();
	}

	public static String random(@Nonnull String key) {
		return random(key, BeeMod.RANDOM);
	}

	public static String random(@Nonnull String key, @Nonnull Random rand) {
		int n = getNumSelections(key);
		if (n == 0) {
			return I18n.translateToLocal(key);
		} else {
			return I18n.translateToLocal(key + "." + rand.nextInt(n));
		}
	}

	public static String random(String key, int index) {
		int n = getNumSelections(key);
		int i = Math.abs(index) % n;
		return I18n.translateToLocal(key + "." + i);
	}

	private static int getNumSelections(String key) {
		int i;
		if (numRandomEntries.containsKey(key)) {
			i = numRandomEntries.get(key);
		} else {
			i = 0;
			while (I18n.canTranslate(key + "." + i)) {
				i++;
			}
			i++;
			numRandomEntries.put(key, i);
		}
		return i;
	}
}
