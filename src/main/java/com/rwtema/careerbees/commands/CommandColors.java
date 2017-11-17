package com.rwtema.careerbees.commands;

import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.colors.DebugBeeSpriteColors;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeSpriteColourProvider;
import forestry.apiculture.genetics.alleles.AlleleBeeSpecies;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.util.Random;

public class CommandColors extends CommandBase {

	@Nonnull
	@Override
	public String getName() {
		return "bee_colors";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "bee_colors";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) sender;

		ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);

		if (!heldItem.isEmpty() && BeeManager.beeRoot.isMember(heldItem)) {
			IBee member = BeeManager.beeRoot.getMember(heldItem);
			AlleleBeeSpecies species = (AlleleBeeSpecies) member.getGenome().getActiveAllele(EnumBeeChromosome.SPECIES);

			IBeeSpriteColourProvider beeSpriteColourProvider = ObfuscationReflectionHelper.getPrivateValue(AlleleBeeSpecies.class, species, "beeSpriteColourProvider");

			if (beeSpriteColourProvider instanceof DebugBeeSpriteColors) {
				DebugBeeSpriteColors colors = (DebugBeeSpriteColors) beeSpriteColourProvider;

				int pr = ((colors.primaryColour) >> 16) & 255;
				int pg = ((colors.primaryColour) >> 8) & 255;
				int pb = colors.primaryColour & 255;

				int sr = ((colors.secondaryColour) >> 16) & 255;
				int sg = ((colors.secondaryColour) >> 8) & 255;
				int sb = colors.secondaryColour & 255;

				if (args.length == 0) {
					sender.sendMessage(new TextComponentString(String.format("Cols: (%d %d %d), (%d, %d, %d)", pr, pg, pb, sr, sg, sb)));
				} else if (args.length == 6) {
					Random rand = ((EntityPlayer) sender).world.rand;

					if ("*".equals(args[0])) pr = rand.nextInt(255);
					else if (!"~".equals(args[0])) pr = Integer.valueOf(args[0]);

					if ("*".equals(args[1])) pg = rand.nextInt(255);
					else if (!"~".equals(args[1])) pg = Integer.valueOf(args[1]);

					if ("*".equals(args[2])) pb = rand.nextInt(255);
					else if (!"~".equals(args[2])) pb = Integer.valueOf(args[2]);

					if ("*".equals(args[3])) sr = rand.nextInt(255);
					else if (!"~".equals(args[3])) sr = Integer.valueOf(args[3]);

					if ("*".equals(args[4])) sg = rand.nextInt(255);
					else if (!"~".equals(args[4])) sg = Integer.valueOf(args[4]);

					if ("*".equals(args[5])) sb = rand.nextInt(255);
					else if (!"~".equals(args[5])) sb = Integer.valueOf(args[5]);






					colors.primaryColour = CareerBeeSpecies.col(pr, pg, pb);
					colors.secondaryColour = CareerBeeSpecies.col(sr, sg, sb);
				}
			}
		}
	}
}
