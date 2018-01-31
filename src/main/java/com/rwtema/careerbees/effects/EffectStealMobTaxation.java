package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.helpers.MethodAccessor;
import com.rwtema.careerbees.helpers.PrivateHelper;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EffectStealMobTaxation extends EffectSteal<EntityLiving> {


	public static final EffectStealMobTaxation INSTANCE = new EffectStealMobTaxation("taxation");

	public EffectStealMobTaxation(String steal) {
		super(steal, 100);
	}

	MethodAccessor.NoParams<ResourceLocation, EntityLiving> lootTableMethod = new MethodAccessor.NoParams<ResourceLocation, EntityLiving>(EntityLiving.class, "getLootTable", "func_184276_b", "func_184647_J" );

	@Override
	protected boolean steal(@Nonnull EntityLiving livingBase, @Nonnull IBeeHousing housing, EffectSteal effectSteal) {
		List<ItemStack> grabbed_stacks = new ArrayList<>();

		if(!livingBase.attackEntityFrom(DamageSource.CACTUS, 0)) return false;

		WorldServer worldServer = (WorldServer) livingBase.world;


		ResourceLocation lootTable = lootTableMethod.invoke(livingBase);
		if (lootTable == null) return false;

		LootTable loottable = livingBase.world.getLootTableManager().getLootTableFromLocation(lootTable);

		FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(worldServer);
		LootContext.Builder lootcontext$builder = (new LootContext.Builder(worldServer))
//				.withLootedEntity(livingBase)
				.withDamageSource(new EntityDamageSource("taxes", fakePlayer))
				.withPlayer(fakePlayer);
//				.withLuck(2);
		for (int i = 0; i < 1; i++) {

			insertLoop:
			for (ItemStack stack : loottable.generateLootForPools(livingBase.world.rand, lootcontext$builder.build())) {
				for (ItemStack grabbed_stack : grabbed_stacks) {
					if (ItemHandlerHelper.canItemStacksStack(grabbed_stack, stack)) {
						int newCount = Math.min(stack.getCount(), grabbed_stack.getMaxStackSize() - grabbed_stack.getCount());
						if (newCount <= 0) {
							continue insertLoop;
						}
						stack.setCount(newCount);
						break;
					}
				}

				ItemStack newStack = tryAdd(stack, housing.getBeeInventory());
				if (newStack == stack || newStack.getCount() == stack.getCount()) {
					continue;
				}
				stack.shrink(newStack.getCount());
				grabbed_stacks.add(stack.copy());
			}
		}

//		if(grabbed_stacks.isEmpty()) return true;
//
//
//		NBTTagCompound tag = new NBTTagCompound();
//
//		if (livingBase.hasCustomName()) {
//			tag.setString("custom_name", livingBase.getCustomNameTag());
//		} else {
//			String s = EntityList.getEntityString(livingBase);
//			if (s == null) s = "generic";
//			tag.setString("name", "entity." + s + ".name");
//		}
//
//		NBTTagList list = new NBTTagList();
//		for (ItemStack grabbed_stack : grabbed_stacks) {
//			NBTTagCompound nbt = grabbed_stack.writeToNBT(new NBTTagCompound());
//			nbt.setInteger("Count", grabbed_stack.getCount());
//			list.appendTag(nbt);
//		}
//
//		tag.setTag("drops", list);
//
//		tag.setByte("seed", (byte) worldServer.rand.nextInt());
//
//		ItemStack product = ItemIngredients.IngredientType.TAX_RECEIPT.get();
//		product.setTagCompound(tag);
//		housing.getBeeInventory().addProduct(product, false);
		return true;
	}

	@Override
	protected boolean canHandle(EntityLiving livingBase) {
		return livingBase instanceof IMob;
	}

	@Nonnull
	@Override
	protected Class<EntityLiving> getEntityClazz() {
		return EntityLiving.class;
	}
}
