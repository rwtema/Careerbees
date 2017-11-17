package com.rwtema.careerbees.effects;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.lang.Lang;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

public abstract class EffectAttributeBoost extends EffectItemModification {
	protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
	private final static String attributeModifiersCustom = "AttributeModifiersCustom";
	static HashSet<UUID> instances = new HashSet<>();

	static {
		MinecraftForge.EVENT_BUS.register(EffectAttributeBoost.class);
	}

	final UUID uuid;
	final HashMap<EntityEquipmentSlot, UUID> map = new HashMap<>();

	public EffectAttributeBoost(String name, float baseTicksBetweenProcessing, UUID uuid) {
		this(name, false, false, baseTicksBetweenProcessing, 1, uuid);
	}

	public EffectAttributeBoost(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing, UUID uuid) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
		this.uuid = uuid;

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			UUID uuid1 = slot.ordinal() == 0 ? uuid : new UUID(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits() + slot.ordinal());
			map.put(slot, uuid1);
		}
		map.put(null, new UUID(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits() - 1));

		instances.addAll(map.values());
	}

	@SubscribeEvent
	public static void onSpawn(PlayerEvent.PlayerRespawnEvent event) {
		recheckAttributes(event.player);
	}

	@SubscribeEvent
	public static void onSpawn(PlayerEvent.PlayerLoggedInEvent event) {
		recheckAttributes(event.player);
	}

	@SubscribeEvent
	public static void onInvChange(LivingEquipmentChangeEvent event) {
		recheckAttributes(event.getEntityLiving());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void addTooltips(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey(attributeModifiersCustom, 9) || tagCompound.getTagList(attributeModifiersCustom, 10).tagCount() <= 0)
			return;

		List<String> list = event.getToolTip();

		list.add(TextFormatting.GOLD + Lang.translate("Career Bee Modifiers:"));
		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
			Multimap<String, AttributeModifier> multimap = getModifiers(itemStack, entityequipmentslot);
			if (multimap == null || multimap.isEmpty()) continue;

//			list.add("");
			list.add(" " + TextFormatting.GOLD + I18n.translateToLocal("item.modifiers." + entityequipmentslot.getName()));

			for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
				AttributeModifier attributemodifier = entry.getValue();
				double d0 = attributemodifier.getAmount();
				double d1;

				if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
					d1 = d0;
				} else {
					d1 = d0 * 100.0D;
				}

				if (d0 > 0.0D) {
					list.add(TextFormatting.BLUE + "  " + I18n.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					list.add(TextFormatting.RED + "  " + I18n.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
				}
			}

		}
	}

	public static void recheckAttributes(EntityLivingBase entityLivingBase) {
		for (IAttributeInstance instance : entityLivingBase.getAttributeMap().getAllAttributes()) {
			for (UUID uuid1 : instances) {
				instance.removeModifier(uuid1);
			}
		}

		for (EntityEquipmentSlot entityEquipmentSlot : EntityEquipmentSlot.values()) {
			ItemStack itemStackFromSlot = entityLivingBase.getItemStackFromSlot(entityEquipmentSlot);
			HashMultimap<String, AttributeModifier> modifiers = getModifiers(itemStackFromSlot, entityEquipmentSlot);
			if (modifiers != null) {
				entityLivingBase.getAttributeMap().applyAttributeModifiers(modifiers);
			}
		}
	}

	private static HashMultimap<String, AttributeModifier> getModifiers(ItemStack stack, @Nullable EntityEquipmentSlot equipmentSlot) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound != null && tagCompound.hasKey(attributeModifiersCustom, 9)) {
			HashMultimap<String, AttributeModifier> multimap = HashMultimap.create();
			NBTTagList nbttaglist = tagCompound.getTagList(attributeModifiersCustom, 10);

			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

				if (attributemodifier != null &&
						(!nbttagcompound.hasKey("Slot", 8) || equipmentSlot == null || nbttagcompound.getString("Slot").equals(equipmentSlot.getName()))
						&& attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L) {
					multimap.put(nbttagcompound.getString("AttributeName"), attributemodifier);
				}
			}

			return multimap;
		}
		return null;
	}

	public ItemStack boostStat(ItemStack stack, IAttribute attribute, @Nullable EntityEquipmentSlot slot, int operation, DoubleUnaryOperator unaryOperator, double initialMissingValue) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		UUID uuid = map.get(slot);
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
			stack.setTagCompound(tagCompound);
		}
		if (!tagCompound.hasKey(attributeModifiersCustom, 9)) {
			NBTTagList nbttaglist = new NBTTagList();
			AttributeModifier modifier = new AttributeModifier(uuid, "Bee Modification", unaryOperator.applyAsDouble(initialMissingValue), operation);
			nbttaglist.appendTag(getAttributeModifierNBT(attribute, slot, modifier));
			tagCompound.setTag(attributeModifiersCustom, nbttaglist);
			return stack;
		} else {
			NBTTagList nbttaglist = tagCompound.getTagList(attributeModifiersCustom, 10);

			for (int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

				if (attributemodifier != null &&
						uuid.equals(attributemodifier.getID())) {
					double amountIn = unaryOperator.applyAsDouble(attributemodifier.getAmount());
					if (amountIn == attributemodifier.getAmount() && operation == attributemodifier.getOperation()) {
						return null;
					}
					AttributeModifier modifier = new AttributeModifier(uuid, "Bee Modification", amountIn, operation);
					NBTTagCompound nbt = getAttributeModifierNBT(attribute, slot, modifier);
					nbttaglist.set(i, nbt);
					return stack;
				}
			}

			AttributeModifier modifier = new AttributeModifier(uuid, "Bee Modification", unaryOperator.applyAsDouble(initialMissingValue), operation);
			nbttaglist.appendTag(getAttributeModifierNBT(attribute, slot, modifier));
			return stack;
		}
	}

	private NBTTagCompound getAttributeModifierNBT(IAttribute attribute, EntityEquipmentSlot slot, AttributeModifier modifier) {
		NBTTagCompound nbt = SharedMonsterAttributes.writeAttributeModifierToNBT(modifier);
		nbt.setString("AttributeName", attribute.getName());
		if (slot != null)
			nbt.setString("Slot", slot.getName());
		return nbt;
	}

	protected static DoubleUnaryOperator getDoubleUnaryOperator(double max, double mult, double granularity) {
		return t -> t > max ? max : Math.ceil(t * granularity + mult * (max - t)) / granularity;
	}

	public static void test(DoubleUnaryOperator unaryOperator, double initialMissingValue){

		StringBuilder builder = new StringBuilder("\n");
		double t = initialMissingValue;
		double prev;
		int n = 0;
		do {
			n++;
			builder.append(t).append(' ');
			prev = t;
			t = unaryOperator.applyAsDouble(t);
		}while (prev != t);
		builder.append('\n').append(n);
		BeeMod.logger.info(builder.toString());
	}

}