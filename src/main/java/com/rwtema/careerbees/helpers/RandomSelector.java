package com.rwtema.careerbees.helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class RandomSelector<E> {
	private final Random random;
	@Nullable
	private E value = null;
	private int n = 0;

	public RandomSelector(Random random) {
		this.random = random;
	}

	@Nonnull
	public RandomSelector<E> select(E newValue) {
		n++;
		if (n == 1 || random.nextInt(n) == 0) value = newValue;
		return this;
	}

	@Nonnull
	public RandomSelector<E> selectAny(@Nonnull Iterable<? extends E> iterables) {
		iterables.forEach(this::select);
		return this;
	}

	@Nonnull
	public RandomSelector<E> selectAny(@Nonnull Collection<? extends E> collection) {
		int s = collection.size();
		if (s == 0) return this;
		int k = random.nextInt(s + n);
		n += s;
		if (k < s) {
			Iterator<? extends E> iterator = collection.iterator();
			for (int i = 0; iterator.hasNext() && i <= k; i++) {
				value = iterator.next();
			}
			return this;
		}

		return this;
	}

	@Nonnull
	public RandomSelector<E> selectAny(@Nonnull List<? extends E> list) {
		int s = list.size();
		if (s == 0) return this;
		int k = random.nextInt(s + n);
		n += s;
		if (k < s) {
			value = list.get(k);
		}
		return this;
	}

	@Nullable
	public E get() {
		return value;
	}

	public Optional<E> getOptional() {
		if (n == 0) return Optional.empty();
		return Optional.ofNullable(value);
	}
}
