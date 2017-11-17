package com.rwtema.careerbees.bees;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.List;

public class BlockStateList {
	public List<IBlockState> stateList = new ArrayList<>();

	public BlockStateList() {

	}

	public BlockStateList add(Block b){
		stateList.addAll(b.getBlockState().getValidStates());
		return this;
	}

	public BlockStateList(Block b) {
		add(b);
	}

	public BlockStateList(IBlockState b) {
		add(b);
	}

	public BlockStateList add(IBlockState b){
		stateList.add(b);
		return this;

	}

	public IBlockState[] toArray(){
		return stateList.toArray(new IBlockState[stateList.size()]);
	}

	public static IBlockState[] of(Block... blocks) {
		BlockStateList blockStateList = new BlockStateList();
		for (Block block : blocks) {
			blockStateList.add(block);
		}
		return blockStateList.toArray();
	}
}
