package com.rwtema.careerbees.helpers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import forestry.api.genetics.IAllele;

public class GenomeHolder {
	private static final BiMap<IAllele, Short> indexes = HashBiMap.create();

	public short getIndex(IAllele allele) {

		synchronized (indexes) {
			return indexes.computeIfAbsent(allele, k -> (short) indexes.size());
		}
	}


}
