package com.rwtema.careerbees.recipes;

import com.mojang.authlib.GameProfile;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import forestry.api.apiculture.*;
import forestry.api.genetics.IAllele;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nonnull;
import java.util.UUID;

import static net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG;

public class PlayerSpawnHandler {
	private final String name;
	private final UUID DireID;
	private final CareerBeeEntry beeSpecies;

	public static void registerBeeSpawn(String name, UUID direID, CareerBeeEntry beeSpecies){
		MinecraftForge.EVENT_BUS.register(new PlayerSpawnHandler(name, direID, beeSpecies));
	}

	public PlayerSpawnHandler(String name, UUID direID, CareerBeeEntry beeSpecies) {
		this.name = name;
		DireID = direID;
		this.beeSpecies = beeSpecies;
	}

	@SubscribeEvent
	public void onSpawn(@Nonnull PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		GameProfile gameProfile = player.getGameProfile();
		if (BeeMod.deobf || this.DireID.equals(gameProfile.getId())) {
			NBTTagCompound tag = event.player.getEntityData();
			if (!tag.hasKey(PERSISTED_NBT_TAG)) {
				tag.setTag(PERSISTED_NBT_TAG, new NBTTagCompound());
			}
			if (tag.getCompoundTag(PERSISTED_NBT_TAG).getBoolean("spawn_"+name)) {
				return;
			}
			tag.getCompoundTag(PERSISTED_NBT_TAG).setBoolean("spawn_"+name, true);
			IAllele[] template = BeeManager.beeRoot.getTemplate(beeSpecies.species);
			IBeeGenome iBeeGenome = BeeManager.beeRoot.templateAsGenome(template, template);
			IBee bee = BeeManager.beeRoot.getBee(iBeeGenome);

			player.inventory.addItemStackToInventory(BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.PRINCESS));
			for (int i = 0; i < 3; i++) {
				player.inventory.addItemStackToInventory(BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.DRONE));
			}
		}
	}
}
