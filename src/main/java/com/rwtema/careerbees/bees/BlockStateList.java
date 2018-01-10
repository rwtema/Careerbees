package com.rwtema.careerbees.bees;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockStateList {
	public final List<IBlockState> stateList = new ArrayList<>();

	public BlockStateList() {

	}

	@Nonnull
	public BlockStateList add(@Nonnull Block b){
		stateList.addAll(b.getBlockState().getValidStates());
		return this;
	}

	public BlockStateList(@Nonnull Block b) {
		add(b);
	}

	public BlockStateList(IBlockState b) {
		add(b);
	}

	@Nonnull
	public BlockStateList add(IBlockState b){
		stateList.add(b);
		return this;

	}

	@Nonnull
	public IBlockState[] toArray(){
		return stateList.toArray(new IBlockState[stateList.size()]);
	}

	@Nonnull
	public static IBlockState[] of(@Nonnull Block... blocks) {
		BlockStateList blockStateList = new BlockStateList();
		for (Block block : blocks) {
			blockStateList.add(block);
		}
		return blockStateList.toArray();
	}
}
