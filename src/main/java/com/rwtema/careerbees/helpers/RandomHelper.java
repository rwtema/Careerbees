package com.rwtema.careerbees.helpers;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomHelper {
	public final static EnumFacing[][] permutations = theHellIsThis();
	public static final Random random = new Random();

	private static EnumFacing[][] theHellIsThis() {
		List<List<EnumFacing>> collect = ImmutableList.of(ImmutableList.of());
		Comparator<EnumFacing> naturalOrder = Comparator.naturalOrder();
		Comparator<Iterable<EnumFacing>> sorter = Comparators.lexicographical(naturalOrder);
		for (int i = 6; i >= 1; i--) {
			Map<HashSet<EnumFacing>, List<List<EnumFacing>>> m = collect.stream().collect(Collectors.groupingBy(Sets::newHashSet));

			collect = m.entrySet().stream().flatMap(
					(entry) -> {
						HashSet<EnumFacing> key = entry.getKey();
						List<List<EnumFacing>> oldValues = entry.getValue();
						List<EnumFacing> newValues = Stream.of(EnumFacing.values()).filter(o -> !key.contains(o)).collect(Collectors.toList());
						int oldSize = oldValues.size();
						int newSize = newValues.size();
						List<List<EnumFacing>> oldValuesList;
						List<EnumFacing> newValuesList;
						if (oldSize == newSize) {
							oldValuesList = oldValues;
							newValuesList = newValues;
						} else if (newSize > oldSize) {
							oldValuesList = IntStream.range(0, newSize / oldSize)
									.mapToObj(p -> oldValues)
									.flatMap(Collection::stream)
									.collect(Collectors.toList());
							newValuesList = newValues;
						} else if (newSize < oldSize) {
							oldValuesList = oldValues;
							newValuesList = IntStream.range(0, oldSize / newSize)
									.mapToObj(p -> newValues)
									.flatMap(Collection::stream)
									.collect(Collectors.toList());
						} else {
							throw new RuntimeException();
						}

						oldValuesList.<List<EnumFacing>>sort(sorter);
						newValuesList.sort(Comparator.naturalOrder());

						List<List<EnumFacing>> collect1 = Streams.zip(
								oldValuesList.stream(),
								newValuesList.stream(),
								(List<EnumFacing> a, EnumFacing b) -> ImmutableList.<EnumFacing>builder().addAll(a).add(b).build()
						).collect(Collectors.toList());
						return collect1.stream();
					}).collect(Collectors.toList());

		}
		collect.sort(sorter);
		return collect.stream().map(s -> s.stream().toArray(EnumFacing[]::new)).toArray(EnumFacing[][]::new);
	}

	public static EnumFacing[] getPermutation(@Nonnull Random random) {
		return permutations[random.nextInt(60)];
	}

	public static EnumFacing[] getPermutation() {
		return getPermutation(random);
	}

}
