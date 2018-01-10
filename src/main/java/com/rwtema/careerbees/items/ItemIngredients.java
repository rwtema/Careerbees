package com.rwtema.careerbees.items;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.helpers.NameHelper;
import com.rwtema.careerbees.helpers.StringHelper;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemIngredients extends Item {
	@Nonnull
	private final static TIntObjectHashMap<IngredientType> map;

	static {
		map = new TIntObjectHashMap<>();
		for (IngredientType ingredientType : IngredientType.values()) {
			map.put(ingredientType.meta, ingredientType);
		}
	}

	public ItemIngredients() {
		setHasSubtypes(true);
		setRegistryName(BeeMod.MODID, "ingredients");
		setUnlocalizedName(BeeMod.MODID + ":ingredients");
		if (BeeMod.deobf_folder)
			for (IngredientType ingredientType : IngredientType.values()) {
				Lang.translate(getUnlocalizedName(ingredientType) + ".name", ingredientType.name().toLowerCase());
			}

		this.setCreativeTab(BeeMod.creativeTab);
	}

	public static IngredientType getIngredientType(@Nonnull ItemStack stack) {
		IngredientType ingredientType = map.get(stack.getMetadata());
		if (ingredientType == null) return IngredientType.BLANK;
		return ingredientType;
	}

	public static Stream<ItemStack> getBarkStacksStream() {
		return OreDictionary.getOres("logWood").stream().flatMap(
				t -> {
					if (t.getMetadata() == OreDictionary.WILDCARD_VALUE) {
						NonNullList<ItemStack> re = NonNullList.create();
						t.getItem().getSubItems(CreativeTabs.SEARCH, re);
						return re.stream();
					} else return Stream.of(t);
				})
				.map(s -> s.writeToNBT(new NBTTagCompound()))
				.map(tag -> {
					ItemStack copy = IngredientType.BARK.get();
					copy.setTagInfo("bark", tag);
					return copy;
				});
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Nullable
	@Override
	public Entity createEntity(World world, Entity location, @Nonnull ItemStack itemstack) {
		return getIngredientType(itemstack).createEntity(world, location, itemstack);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(@Nonnull ItemStack stack) {
		return getUnlocalizedName(getIngredientType(stack));
	}

	@Nonnull
	private String getUnlocalizedName(@Nonnull IngredientType ingredientType) {
		return super.getUnlocalizedName() + "." + ingredientType.name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int getEntityLifespan(@Nonnull ItemStack itemStack, World world) {
		return getIngredientType(itemStack).getEntityLifespan(itemStack, world);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		for (IngredientType ingredientType : IngredientType.values()) {
			if (ingredientType.isInCreativeTab(tab, this.isInCreativeTab(tab))) {
				if (ingredientType.meta >= 0) {
					ItemStack stack = ingredientType.get();
					ingredientType.addCreativeStacks(items, tab, stack);
				}
			}
		}

	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(@Nonnull ItemStack stack, int tintIndex) {
		IngredientType ingredientType = getIngredientType(stack);
		return ingredientType.getColor(stack, tintIndex);
	}

	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		IngredientType ingredientType = getIngredientType(stack);
		ingredientType.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		return getIngredientType(stack).onItemRightClick(worldIn, playerIn, handIn, stack);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return getIngredientType(stack).getDisplayName(stack);
	}

	public enum IngredientType implements Supplier<ItemStack>, Predicate<ItemStack> {
		BLANK(0) {
			@Override
			public ItemStack get() {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean isInCreativeTab(CreativeTabs tab, boolean inCreativeTab) {
				return false;
			}
		},
		BARK(1) {
			@Override
			public int getColor(ItemStack stack, int tintIndex) {
				if (tintIndex == 1) return 0xff808080;
				return super.getColor(stack, tintIndex);
			}

			@Override
			public boolean isInCreativeTab(CreativeTabs tab, boolean inCreativeTab) {
				return tab == BeeMod.creativeTabSpammy;
			}

			@Override
			public void addCreativeStacks(@Nonnull NonNullList<ItemStack> items, CreativeTabs tab, ItemStack stack) {
				OreDictionary.getOres("logWood").stream().flatMap(
						t -> {
							if (t.getMetadata() == OreDictionary.WILDCARD_VALUE) {
								NonNullList<ItemStack> re = NonNullList.create();
								t.getItem().getSubItems(CreativeTabs.SEARCH, re);
								return re.stream();
							} else return Stream.of(t);
						})
						.map(s -> s.writeToNBT(new NBTTagCompound()))
						.map(tag -> {
							ItemStack copy = BARK.get();
							copy.setTagInfo("bark", tag);
							return copy;
						}).forEach(items::add);
			}

			@Override
			public String getDisplayName(@Nonnull ItemStack stack) {
				NBTTagCompound tagCompound;

				if ((tagCompound = stack.getTagCompound()) != null) {
					ItemStack bark = new ItemStack(tagCompound.getCompoundTag("bark"));
					if (!bark.isEmpty())
						return super.getDisplayName(stack) + " - " + bark.getDisplayName();

				}
				return super.getDisplayName(stack);
			}
		},
		PHEREMONES(2) {
			@Override
			@SideOnly(Side.CLIENT)
			public int getColor(@Nonnull ItemStack stack, int tintIndex) {
				NBTTagCompound tagCompound;

				if (tintIndex > 0 && tintIndex <= 2 && (tagCompound = stack.getTagCompound()) != null) {
					IAllele species = AlleleManager.alleleRegistry.getAllele(tagCompound.getString("species"));
					if (species instanceof IAlleleBeeSpecies) {
						return ((IAlleleBeeSpecies) species).getSpriteColour(tintIndex - 1);
					}
				}
				return super.getColor(stack, tintIndex);
			}

			@Override
			public boolean isInCreativeTab(CreativeTabs tab, boolean inCreativeTab) {
				return tab == BeeMod.creativeTabSpammy;
			}

			@Override
			public void addCreativeStacks(@Nonnull NonNullList<ItemStack> items, CreativeTabs tab, ItemStack stack) {
				for (IBee iBee : BeeManager.beeRoot.getIndividualTemplates()) {
					if (iBee.isSecret()) continue;
					items.add(ItemPheremoneFrame.getPheremoneStack(iBee.getGenome().getPrimary()));
				}
			}

			@Override
			public String getDisplayName(@Nonnull ItemStack stack) {
				NBTTagCompound tagCompound;

				if ((tagCompound = stack.getTagCompound()) != null) {
					IAllele species = AlleleManager.alleleRegistry.getAllele(tagCompound.getString("species"));
					if (species instanceof IAlleleBeeSpecies) {
						return super.getDisplayName(stack) + " - " + species.getAlleleName();
					}
				}
				return super.getDisplayName(stack);
			}
		},

		DUSTIRON(4, "dustIron"),
		DUSTGOLD(5, "dustGold"),
		DUSTCOPPER(6, "dustCopper"),
		DUSTTIN(7, "dustTin"),
		DUSTBRONZE(8, "dustBronze"),
//		NOTE(3) {
//			@Override
//			public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn, ItemStack stack) {
//
//				return super.onItemRightClick(worldIn, playerIn, handIn, stack);
//			}
//		},
		REPORT(9) {
			@Override
			public int getEntityLifespan(ItemStack itemStack, World world) {
				return 10;
			}

			@Override
			public void addInformation(@Nonnull ItemStack stack, World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt != null) {
					String name;
					if (nbt.hasKey("custom_name")) {
						name = nbt.getString("custom_name");
					} else {
						name = I18n.translateToLocal(nbt.getString("name"));
					}

					String dimname = Lang.translate("Unknown Dimension");
					int dim = nbt.getInteger("world_dim");
					if (DimensionManager.isDimensionRegistered(dim)) {
						DimensionType providerType = DimensionManager.getProviderType(dim);
						dimname = StringHelper.capFirst(providerType.getName());
					}

					long time = nbt.getLong("world_time");
					long time_day = time % 24000L;
					long time_hour = time_day / 1000L;
					int time_minute = (int) ((time_day % 1000L) / 1000F * 60F);

					tooltip.add(Lang.translateArgs(
							"On Day %s at %s, in the \"%s\" dimension, an incident occured in which a subject (identified as \"%s\") was caught trespassing in restricted area. Subject was duly cautioned, and then stung to death.",
							(int) (time / 24000L),
							String.format("%02d:%02d", time_hour, time_minute),
							dimname,
							name));

					NBTTagList drops = nbt.getTagList("drops", Constants.NBT.TAG_COMPOUND);
					if (drops.tagCount() > 0) {
						tooltip.add("");
						tooltip.add(Lang.translate("Confiscated Items:"));
						for (int i = 0; i < drops.tagCount(); i++) {
							NBTTagCompound compoundTagAt = drops.getCompoundTagAt(i);
							ItemStack itemStack = new ItemStack(compoundTagAt);
							itemStack.setCount(compoundTagAt.getInteger("Count"));
							if (!itemStack.isEmpty()) {
								tooltip.add(" -" + itemStack.getCount() + "x " + itemStack.getDisplayName());
							}
						}
					}

					int officer_id = Short.toUnsignedInt(nbt.getShort("officer_name"));

					tooltip.add("");
					tooltip.add(Lang.translateArgs("Signed: Officer %s (#%s)", NameHelper.QUEEN_NAMES.getName(officer_id), officer_id));
				}
			}
		},
//		TAX_RECEIPT(10) {
//			@Override
//			public int getEntityLifespan(ItemStack itemStack, World world) {
//				return 1 + world.rand.nextInt(120);
//			}
//
//			@Override
//			public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//				NBTTagCompound nbt = stack.getTagCompound();
//				if (nbt != null) {
//
//					String name;
//					long seed = nbt.getByte("seed");
//					if (nbt.hasKey("custom_name")) {
//						name = nbt.getString("custom_name");
//						seed = seed * 31 + name.hashCode();
//					} else {
//						String nbtString = nbt.getString("name");
//						seed = seed * 31 + nbtString.hashCode();
//						name = I18n.translateToLocal(nbtString);
//					}
//
//
//					tooltip.add(Lang.translateArgs("Taxpayer: %s", name));
//					tooltip.add("------------");
//
//
//					NBTTagList drops = nbt.getTagList("drops", Constants.NBT.TAG_COMPOUND);
//					if (drops.tagCount() > 0) {
//						List<String> entries = new ArrayList<>(drops.tagCount());
//						List<Integer> nums = new ArrayList<>(drops.tagCount());
//						for (int i = 0; i < drops.tagCount(); i++) {
//							NBTTagCompound compoundTagAt = drops.getCompoundTagAt(i);
//							ItemStack itemStack = new ItemStack(compoundTagAt);
//							itemStack.setCount(compoundTagAt.getInteger("Count"));
//							seed = seed * 31 + itemStack.getMetadata();
//							seed = seed * 31 + itemStack.getCount();
//							if (!itemStack.isEmpty()) {
//								entries.add(itemStack.getDisplayName());
//								nums.add(itemStack.getCount());
//							}
//						}
//
//						if (!entries.isEmpty()) {
//							Random random = new Random(seed);
////							float total = nums.stream().mapToInt(Integer::intValue).sum();
//							for (int i = 0; i < entries.size(); i++) {
//								String s = NameHelper.TAXES.getName(random.nextInt());
//								tooltip.add(s + ": " + entries.get(i) + " (" + nums.get(i) + ")");
//							}
//						}
//
//					}
//
//					tooltip.add("------------");
//
//				}
//			}
//		},
		INGOTHONEYCOLM(10, "ingotHoneyComb");

		public final int meta;
		@Nullable
		public final String oreDic;
		@SideOnly(Side.CLIENT)
		public ModelResourceLocation mrl;

		IngredientType(int meta) {
			this(meta, null);
		}

		IngredientType(int meta, String oreDic) {
			this.meta = meta;

			this.oreDic = oreDic;
		}


		@Override
		public ItemStack get() {
			return new ItemStack(BeeMod.instance.itemIngredients, 1, meta);
		}


		@Override
		public boolean test(@Nonnull ItemStack stack) {
			return !stack.isEmpty() && stack.getItem() == BeeMod.instance.itemIngredients && stack.getMetadata() == ordinal();
		}

		@SideOnly(Side.CLIENT)
		public int getColor(ItemStack stack, int tintIndex) {
			return 0xffffff;
		}

		public String getDisplayName(ItemStack stack) {
			return I18n.translateToLocal(BeeMod.instance.itemIngredients.getUnlocalizedName() + "." + name().toLowerCase(Locale.ENGLISH) + ".name").trim();
		}

		public boolean isInCreativeTab(CreativeTabs tab, boolean inCreativeTab) {
			return inCreativeTab;
		}

		public void addCreativeStacks(@Nonnull NonNullList<ItemStack> items, CreativeTabs tab, ItemStack stack) {
			items.add(stack);
		}


		@Nonnull
		public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn, @Nonnull ItemStack stack) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
		}

		public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		}

		@Nullable
		public Entity createEntity(World world, Entity location, ItemStack itemstack) {
			return null;
		}

		public int getEntityLifespan(ItemStack itemStack, World world) {
			return 6000;
		}
	}
}
