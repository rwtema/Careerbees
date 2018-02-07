package com.rwtema.careerbees.effects;

import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class EffectJazz extends EffectBase {
	public static final EffectBase INSTANCE = new EffectJazz("jazz");

	public EffectJazz(String rawname) {
		super(rawname);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static final List<SoundEvent> INSTRUMENTS = Lists.newArrayList(
			SoundEvents.BLOCK_NOTE_HARP,
			SoundEvents.BLOCK_NOTE_BASEDRUM,
			SoundEvents.BLOCK_NOTE_SNARE,
			SoundEvents.BLOCK_NOTE_HAT,
			SoundEvents.BLOCK_NOTE_BASS,
			SoundEvents.BLOCK_NOTE_FLUTE,
			SoundEvents.BLOCK_NOTE_BELL,
			SoundEvents.BLOCK_NOTE_GUITAR,
			SoundEvents.BLOCK_NOTE_CHIME,
			SoundEvents.BLOCK_NOTE_XYLOPHONE);

	@Override
	public boolean isValidSpecies(IAlleleBeeSpecies species) {
		return true;
	}

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event){
		if (event.phase == TickEvent.Phase.START) {
			time++;
		}
	}

	int time;

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		World worldIn = housing.getWorldObj();
		BlockPos pos = housing.getCoordinates();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		Vec3d territory = getTerritory(genome, housing).scale(0.5);

		Random random = new Random();

		int time = this.time / 4;
		random.setSeed((time / (40*3))+MathHelper.getPositionRandom(housing.getCoordinates()));
		if( worldIn.rand.nextInt(2) != 0){
			return super.doFX(genome, storedData, housing);
		}
//
		random.setSeed(time / 10);
		int param = getRand(random.nextInt(24), random.nextInt(24), worldIn.rand);

		net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(worldIn, pos, worldIn.getBlockState(pos), param, 0);
		if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) {
			param = e.getVanillaNoteId();
			float f = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);
			SoundEvent blockNoteGuitar = SoundEvents.BLOCK_NOTE_GUITAR;


			worldIn.spawnParticle(EnumParticleTypes.NOTE,
					(double) pos.getX() + worldIn.rand.nextDouble(),
					(double) pos.getY() + 1.2D,
					(double) pos.getZ() + worldIn.rand.nextDouble(),
					(double) param / 24.0D,
					0.0D, 0.0D);

			float volume = 3F;
			for (int i = 0; i < 10; i++) {
				territory = territory.scale(1.3);
				volume *= 2.0 / 3.0;
				AxisAlignedBB bounds = new AxisAlignedBB(housing.getCoordinates()).grow(territory.x, territory.y, territory.z);
				if (bounds.intersects(player.getEntityBoundingBox())) {
					worldIn.playSound(player, pos, blockNoteGuitar, SoundCategory.RECORDS, volume, f);
					break;
				}
			}

		}
		return super.doFX(genome, storedData, housing);
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		return storedData;
	}
}
