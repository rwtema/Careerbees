package com.rwtema.careerbees.helpers;

import java.util.*;

public class RandomSelector<E> {
	private final Random random;
	private E value = null;
	private int n = 0;

	public RandomSelector(Random random) {
		this.random = random;
	}

	public RandomSelector<E> select(E newValue) {
		n++;
		if (n == 1 || random.nextInt(n) == 0) value = newValue;
		return this;
	}

	public RandomSelector<E> selectAny(Iterable<? extends E> iterables) {
		iterables.forEach(this::select);
		return this;
	}

	public RandomSelector<E> selectAny(Collection<? extends E> collection) {
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

	public RandomSelector<E> selectAny(List<? extends E> list) {
		int s = list.size();
		if (s == 0) return this;
		int k = random.nextInt(s + n);
		n += s;
		if (k < s) {
			value = list.get(k);
		}
		return this;
	}

	public E get() {
		return value;
	}

	public Optional<E> getOptional() {
		if (n == 0) return Optional.empty();
		return Optional.ofNullable(value);
	}
}
