package com.rwtema.careerbees.gui;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.effects.settings.Setting;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class GuiSettings extends GuiContainer {
	final static int RIGHT_COLUMN = 166;
	private static final ResourceLocation SETTINGS_GUI_TEXTURE = new ResourceLocation(BeeMod.MODID, "textures/gui/settings.png");
	private static final int NUM_LINES = 11;
	private static final int START_X = 10;
	private final static int ENTRY_WIDTH = 81;
	@Nonnull
	private final ContainerSettings settings;
	final int offset = 0;
	private final EntityPlayer player;

	public GuiSettings(EntityPlayer player, int slot) {
		super(new ContainerSettings(player, slot));
		this.player = player;
		settings = ((ContainerSettings) inventorySlots);
		this.xSize = 194;
		this.ySize = 219;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	public void drawStackFromForeground(@Nonnull ItemStack stack, int x, int y) {
		drawStackFromForeground(stack, x, y, null);
	}

	public void drawStackFromForeground(@Nonnull ItemStack stack, int x, int y, @Nullable String altText) {
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = fontRenderer;
		this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText);
		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
		RenderHelper.disableStandardItemLighting();
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(SETTINGS_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			if (isPointInRegion(18 + 151, 17, 16, 16, mouseX, mouseY)) {
				ItemStack itemStack = player.inventory.getItemStack();
				if (itemStack.isEmpty()) {
					NBTTagCompound settingsTag = getSettingsTag(false);
					if (settingsTag != null) {
						settingsTag.removeTag("species");
					}
					settings.sendUpdate();
				} else {
					IBee member = BeeManager.beeRoot.getMember(itemStack);
					if (member != null) {
						NBTTagCompound settingsTag = getSettingsTag(true);
						settingsTag.setString("species", member.getGenome().getPrimary().getUID());
						settings.sendUpdate();
					}
				}
				return;
			} else {
				SettingsEntry[] assignedEntries = getAssignedEntries();
				for (int i = 0; i < assignedEntries.length; i++) {
					SettingsEntry entry = assignedEntries[i];
					if (entry != null) {
						int y = 20 + i * 9;
						switch (entry.setting.getType()) {
							case TEXT:
								break;
							case BUTTON:
								int buttonWidth = entry.getButtonWidth(this);
								if (isPointInRegion(RIGHT_COLUMN - buttonWidth, y + 2, buttonWidth, 14, mouseX, mouseY)) {
									Object o1 = entry.setting.nextEntry(entry.value);
									NBTTagCompound settingsTag = getSettingsTag(true);
									settingsTag.setTag(entry.setting.getKeyname(), entry.setting.toNBT(o1));
									settings.sendUpdate();

								}
								break;
							case ITEMSTACK:
								if (isPointInRegion(RIGHT_COLUMN - 18, y, 18, 18, mouseX, mouseY)) {
									ItemStack itemStack = player.inventory.getItemStack();
									Setting<ItemStack, ?> setting = entry.setting;
									if (setting.isAcceptable(itemStack)) {
										itemStack = setting.overrideInput(itemStack);
										NBTBase base = setting.toNBT(itemStack);
										NBTTagCompound settingsTag = getSettingsTag(true);
										settingsTag.setTag(setting.getKeyname(), base);
										settings.sendUpdate();
									}
									return;
								}
								break;
						}
					}
				}
			}
		}


		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Nonnull
	public NBTTagCompound getSettingsTag(boolean init) {
		ItemStack stack = settings.stack;

		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null) {
			if (!init) return new NBTTagCompound();
			tagCompound = new NBTTagCompound();
			stack.setTagCompound(tagCompound);
		}
		if (tagCompound.hasKey("settings", Constants.NBT.TAG_COMPOUND)) {
			return tagCompound.getCompoundTag("settings");
		} else {
			if (!init) return new NBTTagCompound();
			NBTTagCompound value = new NBTTagCompound();
			tagCompound.setTag("settings", value);
			return value;
		}
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(SETTINGS_GUI_TEXTURE);
		this.drawTexturedModalRect(169, 36, 240, 0, 16, 15);

		ItemStack stack = settings.stack;

		this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 126, 4210752);

		this.fontRenderer.drawSplitString("", 8, 13, 143, 0xff000000);

		NBTTagCompound settingsTag = getSettingsTag(false);

		String speciesName = settingsTag.getString("species");
		IAllele allele = AlleleManager.alleleRegistry.getAllele(speciesName);
		if (!(allele instanceof IAlleleBeeSpecies) || !CareerBeeSpecies.registeredSpecies.contains(allele)) {
			this.fontRenderer.drawString(stack.getDisplayName(), 8, 6, 4210752);
			return;
		}

		this.fontRenderer.drawString(stack.getDisplayName() + " - " + allele.getAlleleName() , 8, 6, 4210752);

		IAlleleBeeSpecies species = (IAlleleBeeSpecies) allele;
		EffectBase effectBase = EffectBase.registeredEffectSpecies.get(species);
		if (effectBase != null) {
			IAllele[] template = BeeManager.beeRoot.getTemplate(species);

			ItemStack memberStack = BeeManager.beeRoot.getMemberStack(BeeManager.beeRoot.templateAsIndividual(template), EnumBeeType.DRONE);
			this.drawStackFromForeground(memberStack, 18 + 151, 17, null);

			SettingsEntry[] assignedEntries = getAssignedEntries();
			for (int i = 0; i < assignedEntries.length; i++) {
				if (assignedEntries[i] != null) {
					assignedEntries[i].draw(this, i, 20 + i * 9);
				}
			}
		}
	}

	@Nullable
	IAlleleBeeSpecies getSpecies() {
		String speciesName = getSettingsTag(false).getString("species");
		IAllele allele = AlleleManager.alleleRegistry.getAllele(speciesName);
		if (allele instanceof IAlleleBeeSpecies && CareerBeeSpecies.registeredSpecies.contains(allele)) {
			return (IAlleleBeeSpecies) allele;
		}
		return null;
	}

	@Nullable
	EffectBase getEffect() {
		IAlleleBeeSpecies species = getSpecies();
		return species != null ? EffectBase.registeredEffectSpecies.get(species) : null;
	}

	@Nonnull
	SettingsEntryList getEntries() {
		NBTTagCompound settingsTag = getSettingsTag(false);
		EffectBase effect = getEffect();
		if (effect == null) return new SettingsEntryList(ImmutableList.of());
		ImmutableList.Builder<SettingsEntry> builder = ImmutableList.builder();

		for (Setting setting : effect.settings) {
			Object o;
			if (settingsTag.hasKey(setting.getKeyname(), setting.getExpectedType())) {
				NBTBase tag = settingsTag.getTag(setting.getKeyname());
				//noinspection unchecked
				o = setting.fromNBT(tag);

			} else {
				o = setting.getDefault();
			}
			builder.add(new SettingsEntry(setting, o));
		}

		return new SettingsEntryList(builder.build());
	}

	@Nonnull
	public TIntObjectHashMap<SettingsEntry> getVisibleEntries() {
		SettingsEntryList entries = getEntries();
		int n = 0;
		TIntObjectHashMap<SettingsEntry> map = new TIntObjectHashMap<>();
		for (SettingsEntry entry : entries) {
			if (entry.setting.shouldBeVisible(entries)) {
				map.put(n, entry);
				n += entry.setting.getType().height;
			}
		}

		return map;
	}

	@Nonnull
	public SettingsEntry[] getAssignedEntries() {
		TIntObjectHashMap<SettingsEntry> visibleEntries = getVisibleEntries();
		int startPoint = MathHelper.clamp(offset, 0, Math.max(0, offset - NUM_LINES));
		SettingsEntry[] settingsEntries = new SettingsEntry[NUM_LINES];

		for (int i = 0; i < NUM_LINES; i++) {
			settingsEntries[i] = visibleEntries.get(startPoint + i);
		}
		return settingsEntries;
	}

	enum Button_State {
		DEFAULT(0, 0XFFE0E0E0),
		PRESSED(1, 0XFFE0E0E0),
		HOVER(2, 0XFFFFFFA0);
		final int index;
		final int textcolor;

		Button_State(int index, int textcolor) {
			this.index = index;
			this.textcolor = textcolor;
		}
	}

	public static class SettingsEntry<V, NBT extends NBTBase> {
		public final Setting<V, NBT> setting;
		public final V value;

		final Button_State button_state = Button_State.DEFAULT;


		public SettingsEntry(Setting<V, NBT> setting, V value) {
			this.setting = setting;
			this.value = value;
		}

		public void draw(@Nonnull GuiSettings guiSettings, int row, int y) {
			List<String> strings = guiSettings.fontRenderer.listFormattedStringToWidth(setting.getKeyname(), ENTRY_WIDTH);
			int height = setting.getType().height;
			int color = 0XFFE0E0E0;
			if (height == 2 && strings.size() == 1) {
				guiSettings.drawString(guiSettings.fontRenderer, strings.get(0) + ":", START_X, y + 4, color);
			} else if (height == 2 && strings.size() >= 2) {
				int w = guiSettings.fontRenderer.drawStringWithShadow(strings.get(0), START_X, y, color);
				w = Math.max(w, guiSettings.fontRenderer.drawStringWithShadow(strings.get(1), START_X, y + 9, color));
				guiSettings.fontRenderer.drawStringWithShadow(":", START_X + w, y + 4, color);
			} else if (height == 1) {
				guiSettings.drawString(guiSettings.fontRenderer, strings.get(0) + ":", START_X, y, color);
			}

			switch (setting.getType()) {
				case TEXT:

					break;
				case BUTTON:
					int w = getButtonWidth(guiSettings);
					int w2 = w / 2;
					int texy = button_state.index * 14;
					guiSettings.mc.getTextureManager().bindTexture(SETTINGS_GUI_TEXTURE);
					guiSettings.drawTexturedModalRect(RIGHT_COLUMN - w, y + 2, 212, 33 + texy, w2, 14);
					guiSettings.drawTexturedModalRect(RIGHT_COLUMN - w + w2, y + 2, 256 - w2, 33 + texy, w2, 14);
					if (w > 44) {
						for (int dx = RIGHT_COLUMN - w + w2; dx < RIGHT_COLUMN - w + w2; dx += 12) {
							int dx2 = Math.min(dx + 12, RIGHT_COLUMN - w + w2);
							if (dx2 == dx) break;
							int w3 = dx2 - dx;
							guiSettings.drawTexturedModalRect(dx, y + 2, 244, 75 + texy, w3, 14);
						}
					}
					guiSettings.drawCenteredString(guiSettings.fontRenderer, setting.format(value), RIGHT_COLUMN - w2, y + 5, button_state.textcolor);
					break;

				case ITEMSTACK:
					guiSettings.mc.getTextureManager().bindTexture(SETTINGS_GUI_TEXTURE);
					guiSettings.drawTexturedModalRect(RIGHT_COLUMN - 18, y, 238, 15, 18, 18);
					if (value instanceof ItemStack && !((ItemStack) value).isEmpty()) {
						guiSettings.drawStackFromForeground(((ItemStack) value), RIGHT_COLUMN - 18 + 1, y + 1);
					}
					break;
			}
		}

		private int getButtonWidth(@Nonnull GuiSettings guiSettings) {
			return setting.getEntries().stream().map(setting::format).mapToInt(guiSettings.fontRenderer::getStringWidth).max().orElseThrow(RuntimeException::new) + 4;
		}
	}

	public class SettingsEntryList implements IEffectSettingsHolder, Iterable<SettingsEntry> {
		final List<SettingsEntry> list;

		public SettingsEntryList(List<SettingsEntry> list) {
			this.list = list;
		}

		@Nonnull
		@Override
		public <V> V getValue(@Nonnull Setting<V, ?> setting) {
			return (V) list.stream().filter(t -> t.setting == setting).findFirst().map(t -> t.value).orElse(setting.getDefault());
		}

		@Nonnull
		@Override
		public Iterator<SettingsEntry> iterator() {
			return list.iterator();
		}
	}
}
