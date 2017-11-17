package com.rwtema.careerbees.helpers;

import org.apache.commons.lang3.Validate;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CollectionHelper {

	public static <T> Comparator<T> firstCheckEqualThen(Comparator<T> andThen){
		return (o1, o2) -> {
			if(o1 == o2) return 0;
			return andThen.compare(o1, o2);
		};
	}
}
