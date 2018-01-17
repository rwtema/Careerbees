package com.rwtema.careerbees.items;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.entity.EntityBeeSwarm;
import com.rwtema.careerbees.gui.ContainerBeeGun;
import com.rwtema.careerbees.gui.GuiBeeGun;
import com.rwtema.careerbees.gui.GuiHandler;
import com.rwtema.careerbees.handlers.FakeHousing;
import com.rwtema.careerbees.handlers.SimpleFluidTank;
import com.rwtema.careerbees.lang.Lang;
import com.rwtema.careerbees.networking.BeeNetworking;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBeeGun extends Item implements GuiHandler.ItemStackGuiContainer {
	static int MAX_HONEY = 1000;
	static int HONEY_PER_SHOT = 10;
	int selectedChamber;
	@SideOnly(Side.CLIENT)
	ItemStack curItem;
	int timeout = 0;

	public ItemBeeGun() {
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
		return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F / 2, 1.0F, 1.0F);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		HoneyTank capability = Validate.notNull((HoneyTank) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
		return 1- capability.getStorage() / (double) capability.getCapacity();
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		BeeStackHandler beeStackHandler = new BeeStackHandler(stack);
		HoneyTank honeyTank = new HoneyTank(stack);
		return new ICapabilityProvider() {
			@Override
			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
				return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
			}

			@Nullable
			@Override
			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(beeStackHandler);
				else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
					return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(honeyTank);
				else if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
					return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(honeyTank);
				else return null;
			}
		};
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return EnumActionResult.FAIL;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack heldItem = playerIn.getHeldItem(handIn);
		if (heldItem.getItem() != this) return new ActionResult<>(EnumActionResult.FAIL, heldItem);

		if (worldIn.isRemote) {
			sendSlot(playerIn, heldItem);
			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		}

		if (playerIn.isSneaking()) {
			playerIn.openGui(BeeMod.instance, 0, worldIn, handIn == EnumHand.MAIN_HAND ? playerIn.inventory.currentItem : playerIn.inventory.mainInventory.size() + playerIn.inventory.armorInventory.size(), 0, 0);
			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		}

		heldItem = heldItem.copy();

		HoneyTank honeyTank = (HoneyTank) Validate.notNull(heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));

		if (honeyTank.getStorage() == 0) return new ActionResult<>(EnumActionResult.FAIL, heldItem);


		IBee bee;
		NBTTagCompound nbt = heldItem.getTagCompound();
		if (nbt == null) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		int slot = nbt.getInteger("slot");
		IItemHandler capability = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (capability == null) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		ItemStack stackInSlot = capability.getStackInSlot(slot).copy();
		if (stackInSlot.isEmpty()) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		bee = BeeManager.beeRoot.getMember(stackInSlot);

		if (bee == null) {
			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		}

		if (BeeManager.beeRoot.getType(stackInSlot) == EnumBeeType.DRONE) {
			capability.extractItem(slot, 1, false);
		}

		honeyTank.setStorage(Math.max(honeyTank.getStorage() - HONEY_PER_SHOT, 0));

		EntityBeeSwarm swarm = new EntityBeeSwarm(worldIn, BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.QUEEN), playerIn);
		Vec3i territory = bee.getGenome().getTerritory();
		float v = ((territory.getX() + territory.getY() + territory.getZ()) * 0.3F) / (9 + 6 + 9);
		swarm.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, v, 1.0F);

		worldIn.spawnEntity(swarm);

		return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
	}

	public void sendSlot(EntityPlayer playerIn, ItemStack heldItem) {
		int invslot;
		for (invslot = 0; invslot < playerIn.inventory.getSizeInventory(); invslot++) {
			if (playerIn.inventory.getStackInSlot(invslot) == heldItem) {
				break;
			}
		}

		if (invslot == playerIn.inventory.getSizeInventory()) {
			throw new IllegalStateException();
		}

		BeeNetworking.net.sendToServer(new PacketSlotSelection((byte) selectedChamber, invslot));
	}

//	@Nullable
//	public IBee getCurrentSelectedBee(ItemStack heldItem) {
//		NBTTagCompound nbt = heldItem.getTagCompound();
//		if (nbt == null) return null;
//		int slot = nbt.getInteger("slot");
//		IItemHandler capability = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		if (capability == null)return null;
//		ItemStack stackInSlot = capability.getStackInSlot(slot);
//		if (stackInSlot.isEmpty()) return null;
//		return BeeManager.beeRoot.getMember(stackInSlot);
//	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void hudDraw(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
			return;
		}

		if (timeout <= 0) return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null) return;

		ItemStack stack = mc.player.inventory.getCurrentItem();
		if (stack.isEmpty() || stack.getItem() != this) return;

		event.setCanceled(true);

		GuiIngameForge currentScreen = (GuiIngameForge) mc.ingameGUI;
		ScaledResolution resolution = event.getResolution();
		float partialTicks = event.getPartialTicks();

		int x0 = resolution.getScaledWidth() / 2;
		int y0 = resolution.getScaledHeight() / 2;

		mc.getTextureManager().bindTexture(GuiBeeGun.BEEGUN_GUI_TEXTURE);

		currentScreen.drawTexturedModalRect(x0 - 70 / 2, y0 - 72 / 2, 183, 14, 70, 72);

		IItemHandler itemHandler = Validate.notNull(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
		for (int i = 0; i < 6; i++) {
			ItemStack stackInSlot = itemHandler.getStackInSlot(i);
			if (stackInSlot.isEmpty()) continue;
			GlStateManager.enableDepth();
			int[] beeCoord = ContainerBeeGun.beeCoords[i];
			int x = beeCoord[0] - 81 + x0 - 70 / 4 + 6 + 3;
			int y = beeCoord[1] - (18 + 66) / 2 + y0 - 72 / 4 + 4 + 6;
			mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player, stackInSlot, x, y);
			mc.getRenderItem().renderItemOverlayIntoGUI(currentScreen.getFontRenderer(), stackInSlot, x, y, null);
		}

		if (selectedChamber >= 0 && selectedChamber < 6) {
			mc.getTextureManager().bindTexture(GuiBeeGun.BEEGUN_GUI_TEXTURE);
			int[] beeCoord = ContainerBeeGun.beeCoords[selectedChamber];
			int x = beeCoord[0] - 81 + x0 - 70 / 4;
			int y = beeCoord[1] - 42 + y0 - 70 / 4 + 2;
			currentScreen.drawTexturedModalRect(x, y, 37, 182, 34, 30);
		}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (timeout > 0) {
			timeout--;
		}
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player == null) return;
		ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
		if (heldItem != curItem) {
			if (heldItem.getItem() == this) {
				NBTTagCompound tagCompound = heldItem.getTagCompound();
				selectedChamber = tagCompound == null ? 0 : tagCompound.getInteger("slot");
			}
		}
		curItem = heldItem;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void scroll(@Nonnull MouseEvent event) {
		int k = event.getDwheel();
		if (k == 0) return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;
		if (player == null || mc.currentScreen != null) return;
		if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			ItemStack currentItem = player.inventory.getCurrentItem();
			if (currentItem.getItem() == this) {
				if (k > 0) {
					selectedChamber = (this.selectedChamber + 1) % 6;
				} else if (k < 0) {
					selectedChamber = (this.selectedChamber - 1);
					if (selectedChamber < 0) {
						selectedChamber = 5;
					}
				} else {
					return;
				}
				timeout = 20 * 2;

				IItemHandler capability = currentItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				ItemStack stackInSlot = capability.getStackInSlot(selectedChamber);

				ITextComponent chat = stackInSlot.isEmpty() ? Lang.chat("Empty") : stackInSlot.getTextComponent();
				mc.player.sendStatusMessage(chat, true);
				sendSlot(player, currentItem);
				event.setCanceled(true);
			}
		}
	}

	@Override
	public Container getContainer(EntityPlayer player, ItemStack stack, int slot) {
		return new ContainerBeeGun(player, slot);
	}

	@Override
	public Object getGui(EntityPlayer player, ItemStack stack, int slot) {
		return new GuiBeeGun(new ContainerBeeGun(player, slot), player);
	}

	public static class FakeHousingPlayer extends FakeHousing {
		final Entity player;
		final BlockPos target;
		final ItemStack queen;

		public FakeHousingPlayer(Entity player, BlockPos target, ItemStack queen) {
			this.player = player;
			this.target = target;
			this.queen = queen;
		}

		@Override
		protected boolean addProduct(@Nonnull ItemStack product, boolean all) {
			float f = 0.5F;
			World world = getWorldObj();
			BlockPos pos = getCoordinates();
			double d0 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			double d1 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			double d2 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, product.copy());
			entityitem.setDefaultPickupDelay();
			world.spawnEntity(entityitem);
			return true;
		}

		@Override
		protected ItemStack getQueen() {
			return queen;
		}

		@Nonnull
		@Override
		public World getWorldObj() {
			return player.world;
		}

		@Nonnull
		@Override
		public BlockPos getCoordinates() {
			return target;
		}
	}

	private static class BeeStackHandler implements IItemHandlerModifiable {
		private ItemStack stack;
		private ItemStackHandler handler = new ItemStackHandler(6) {
			@Override
			protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
				return BeeManager.beeRoot.isMember(stack) ? stack.getMaxStackSize() : 0;
			}
		};

		public BeeStackHandler(ItemStack stack) {
			this.stack = stack;

		}

		@Override
		public int getSlots() {
			return 6;
		}

		@Override
		@Nonnull
		public ItemStack getStackInSlot(int slot) {
			return getHandler().getStackInSlot(slot);
		}

		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			return save(getHandler().insertItem(slot, stack, simulate));
		}

		@Override
		@Nonnull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return save(getHandler().extractItem(slot, amount, simulate));
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		public <T> T save(T value) {
			NBTTagCompound nbtTagCompound = handler.serializeNBT();
			stack.setTagInfo("items", nbtTagCompound);
			return value;
		}

		public ItemStackHandler getHandler() {
			NBTTagCompound tagCompound = stack.getTagCompound();
			if (tagCompound == null) {
				tagCompound = new NBTTagCompound();
			} else {
				tagCompound = tagCompound.getCompoundTag("items");
			}
			handler.deserializeNBT(tagCompound);
			return handler;
		}

		@Override
		public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
			getHandler().setStackInSlot(slot, stack);
			save(null);
		}
	}

	public static class PacketSlotSelection extends BeeNetworking.MessageClientToServer {
		byte slot;
		int invslot;

		@SuppressWarnings("unused")
		public PacketSlotSelection() {

		}

		public PacketSlotSelection(byte slot, int invslot) {
			this.slot = slot;
			this.invslot = invslot;
		}

		@Override
		protected void runServer(MessageContext ctx, EntityPlayerMP player) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(invslot);
			if (stackInSlot.getItem() == BeeMod.instance.itemBeeGun) {
				stackInSlot.setTagInfo("slot", new NBTTagByte(slot));
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			slot = buf.readByte();
			invslot = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeByte(slot);
			buf.writeInt(invslot);
		}
	}

	private static class HoneyTank extends SimpleFluidTank implements IFluidHandlerItem {
		ItemStack stack;

		public HoneyTank(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int getStorage() {
			NBTTagCompound nbt = stack.getTagCompound();
			return nbt != null ? nbt.getInteger("honey") : 0;
		}

		@Override
		public void setStorage(int storage) {
			stack.setTagInfo("honey", new NBTTagInt(storage));
		}

		@Override
		public int getCapacity() {
			return MAX_HONEY;
		}

		@Override
		public Fluid getAllowedFluid() {
			return FluidRegistry.getFluid("for.honey");
		}

		@Nonnull
		@Override
		public ItemStack getContainer() {
			return stack;
		}
	}
}
