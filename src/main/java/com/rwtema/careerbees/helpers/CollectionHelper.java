package com.rwtema.careerbees.helpers;

import javax.annotation.Nonnull;
import java.util.Comparator;

public class CollectionHelper {

	public static <T> Comparator<T> firstCheckEqualThen(@Nonnull Comparator<T> andThen){
		return (o1, o2) -> {
			if(o1 == o2) return 0;
			return andThen.compare(o1, o2);
		};
	}
}
