package com.rwtema.careerbees.bees;

import com.google.common.collect.*;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.helpers.CollectionHelper;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeMutationBuilder;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IMutationBuilder;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeeMutationTree {
	List<Entry> entries = new ArrayList<>();
	HashMultimap<SpeciesEntry, Entry> recipes = HashMultimap.create();

	public void add(SpeciesEntry a, SpeciesEntry b, SpeciesEntry result, double chance) {
		add(a, b, result, chance, null);
	}

	public void add(SpeciesEntry a, SpeciesEntry b, SpeciesEntry result, double chance, Function<IBeeMutationBuilder, IMutationBuilder> requirements) {
		Entry entry = new Entry(a, b, result, chance, requirements);
		entries.add(entry);
		recipes.put(result, entry);
	}

	public List<SpeciesEntry> getVanillaParents(SpeciesEntry entry) {
		LinkedList<SpeciesEntry> toCheck = new LinkedList<>();
		toCheck.add(entry);
		ArrayList<SpeciesEntry> results = new ArrayList<>();
		SpeciesEntry poll;
		while ((poll = toCheck.poll()) != null) {
			if (poll.isVanilla()) {
				results.add(poll);
			}
			for (Entry e : recipes.get(poll)) {
				toCheck.add(e.a);
				toCheck.add(e.b);
			}
		}
		return results;
	}

	public List<SpeciesEntry> getParents(SpeciesEntry entry) {
		if (!recipes.containsKey(entry)) {
			return ImmutableList.of();
		}
		ArrayList<SpeciesEntry> results = new ArrayList<>();
		for (Entry entry1 : recipes.get(entry)) {
			results.add(entry1.a);
			results.add(entry1.b);
		}

		for (int i = 0; i < results.size(); i++) {
			for (Entry entry1 : recipes.get(entry)) {
				results.add(entry1.a);
				results.add(entry1.b);
			}
		}

		return results;
	}

	public int getComplexity(SpeciesEntry spc) {
		return recipes.get(spc).stream()
				.mapToInt(entry -> 1 + Math.max(getComplexity(entry.a), getComplexity(entry.b)))
				.min()
				.orElse(0);
	}

	public Set<HashSet<SpeciesEntry>> getLeastParents(SpeciesEntry spc) {
		Set<HashSet<SpeciesEntry>> entries = new HashSet<>();
		if (!recipes.containsKey(spc)) return ImmutableSet.of(Sets.newHashSet(spc));

		for (Entry e : recipes.get(spc)) {
			if (e.a == e.b) {
				for (HashSet<SpeciesEntry> sa : getLeastParents(e.a)) {
					HashSet<SpeciesEntry> s = new HashSet<>();
					s.addAll(sa);
					s.add(spc);
					entries.add(s);
				}
			} else {
				for (HashSet<SpeciesEntry> sa : getLeastParents(e.a)) {
					for (HashSet<SpeciesEntry> sb : getLeastParents(e.b)) {
						HashSet<SpeciesEntry> s = new HashSet<>();
						s.addAll(sa);
						s.addAll(sb);
						s.add(spc);
						entries.add(s);
					}
				}
			}
		}

		return entries;
	}

	public void registerMutations() {
		if (BeeMod.deobf) {
			BeeMod.logger.info(entries.stream()
					.flatMap(e -> Stream.of(e.a, e.b, e.result))
					.distinct()
					.sorted(Comparator.comparingInt(this::getComplexity).thenComparing(Object::toString))
					.map(s -> s + " " + getComplexity(s))
					.collect(Collectors.joining("\n")));
		}

		for (BeeMutationTree.Entry entry : entries) {
			ArrayList<SpeciesEntry> pair = Lists.newArrayList(entry.a, entry.b);
			pair.sort(
					CollectionHelper.firstCheckEqualThen(
							Comparator.comparing(SpeciesEntry::isVanilla)
									.thenComparingInt(this::getComplexity)
					)
			);

			IAlleleBeeSpecies primary = pair.get(0).get();
			IAlleleBeeSpecies secondary = pair.get(1).get();

			IBeeMutationBuilder mutation = BeeManager.beeMutationFactory.createMutation(primary, secondary, BeeManager.beeRoot.getTemplate(entry.result.get()), (int) Math.round(100 * entry.chance));
			if (entry.requirement != null) {
				mutation = (IBeeMutationBuilder) entry.requirement.apply(mutation);
			}
			mutation.build();
		}
	}

	public interface SpeciesEntry extends Supplier<IAlleleBeeSpecies> {
		boolean isVanilla();
	}

	public static class VanillaEntry implements SpeciesEntry {
		final String name;

		public VanillaEntry(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof VanillaEntry)) return false;

			VanillaEntry that = (VanillaEntry) o;

			return name != null ? name.equals(that.name) : that.name == null;
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "VanillaEntry{" +
					"name='" + name + '\'' +
					'}';
		}

		@Override
		public boolean isVanilla() {
			return true;
		}

		@Override
		public IAlleleBeeSpecies get() {
			return Validate.notNull((IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(name));
		}
	}

	public class Entry {
		public final SpeciesEntry a;
		public final SpeciesEntry b;
		public final SpeciesEntry result;
		public final double chance;
		public final Function<IBeeMutationBuilder, IMutationBuilder> requirement;

		public Entry(SpeciesEntry a, SpeciesEntry b, SpeciesEntry result, double chance, Function<IBeeMutationBuilder, IMutationBuilder> requirement) {
			this.a = a;
			this.b = b;
			this.result = result;
			this.chance = chance;
			this.requirement = requirement;
		}
	}
}

