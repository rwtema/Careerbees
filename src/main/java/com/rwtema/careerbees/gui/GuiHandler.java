package com.rwtema.careerbees.gui;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class GuiHandler implements IGuiHandler {
	private static final TIntObjectHashMap<ElementCreator<? extends Container>> serverGuiContainer = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<ElementCreator<? extends IGuiWrapper>> clientGuiContainer = new TIntObjectHashMap<>();

	static {
		register(0, new ElementCreator<Container>() {
			@Nullable
			@Override
			public Container getElement(int ID, @Nonnull EntityPlayer player, World world, int x, int y, int z) {
				ItemStack stackInSlot = player.inventory.getStackInSlot(x);
				Item item = stackInSlot.getItem();
				if (item instanceof ItemStackGuiContainer) {
					return ((ItemStackGuiContainer) item).getContainer(player, stackInSlot, x);
				}
				return null;
			}
		}, new ElementCreator<IGuiWrapper>() {
			@Nullable
			@Override
			public IGuiWrapper getElement(int ID, @Nonnull EntityPlayer player, World world, int x, int y, int z) {
				ItemStack stackInSlot = player.inventory.getStackInSlot(x);
				Item item = stackInSlot.getItem();
				if (item instanceof ItemStackGuiContainer) {
					return new IGuiWrapper() {
						@Override
						@SideOnly(Side.CLIENT)
						public Object getGui() {
							return ((ItemStackGuiContainer) item).getGui(player, stackInSlot, x);
						}
					};
				}
				return null;
			}
		});

		registerTE(1, tile -> tile instanceof ITileGui, ITileGui::createContainer, ITileGui::createGui);
	}

	public static void register(int ID, ElementCreator<? extends Container> server, ElementCreator<? extends IGuiWrapper> client) {
		if (serverGuiContainer.put(ID, server) != null || clientGuiContainer.put(ID, client) != null) {
			throw new RuntimeException(ID + " is already in use");
		}
	}

	public static <T> void registerTE(int ID, @Nonnull Predicate<TileEntity> isTile, @Nonnull BiFunction<T, EntityPlayer, ? extends Container> serverContainer, @Nonnull BiFunction<T, EntityPlayer, ?> clientGuiContainer) {
		register(ID,
				new fromTE<Container, T>() {
					@Override
					protected boolean isValid(TileEntity tileEntity) {
						return isTile.test(tileEntity);
					}

					@Nullable
					@Override
					public Container createObject(T tile, EntityPlayer player) {
						return serverContainer.apply(tile, player);
					}
				}, new fromTE<IGuiWrapper, T>() {
					@Override
					protected boolean isValid(TileEntity tileEntity) {
						return isTile.test(tileEntity);
					}

					@Nullable
					@Override
					public IGuiWrapper createObject(T tile, EntityPlayer player) {
						return new IGuiWrapper() {
							@Override
							@SideOnly(Side.CLIENT)
							public Object getGui() {
								return clientGuiContainer.apply(tile, player);
							}
						};
					}
				}
		);
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		ElementCreator<? extends Container> creator = serverGuiContainer.get(ID);
		if (creator != null) {
			return creator.getElement(ID, player, world, x, y, z);
		}
		return null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		ElementCreator<? extends IGuiWrapper> creator = clientGuiContainer.get(ID);
		if (creator != null) {
			IGuiWrapper wrapper = creator.getElement(ID, player, world, x, y, z);
			return wrapper != null ? wrapper.getGui() : null;
		}
		return null;
	}

	public interface ITileGui {
		Container createContainer(EntityPlayer player);

		Object createGui(EntityPlayer player);
	}

	public interface ItemStackGuiContainer {
		Container getContainer(EntityPlayer player, ItemStack stack, int slot);

		@SideOnly(Side.CLIENT)
		Object getGui(EntityPlayer player, ItemStack stack, int slot);
	}

	public interface ElementCreator<E> {
		@Nullable
		E getElement(int ID, EntityPlayer player, World world, int x, int y, int z);
	}

	public interface IGuiWrapper {
		Object getGui();
	}

	public static abstract class fromTE<E, T> implements ElementCreator<E> {
		@Override
		public E getElement(int ID, EntityPlayer player, @Nonnull World world, int x, int y, int z) {
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			if (!isValid(tileEntity)) return null;
			T tile;
			try {
				//noinspection unchecked
				tile = (T) tileEntity;
			} catch (ClassCastException ignore) {
				return null;
			}
			if (tile != null) {
				return createObject(tile, player);
			}
			return null;
		}

		protected abstract boolean isValid(TileEntity tileEntity);

		@Nullable
		public abstract E createObject(T tile, EntityPlayer player);
	}
}