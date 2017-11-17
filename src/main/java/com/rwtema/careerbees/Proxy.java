package com.rwtema.careerbees;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Proxy {
	@SideOnly(Side.CLIENT)
	public static Consumer<ModelResourceLocation> itemModelHook;
	@SideOnly(Side.CLIENT)
	public static BiConsumer<ItemBlock, ModelResourceLocation> itemBlockModelHook;

	public void preInit(){

	}

	public void run(ClientRunnable runnable) {

	}

	public boolean isClient(){
		return false;
	}

	public void init() {


	}
}
