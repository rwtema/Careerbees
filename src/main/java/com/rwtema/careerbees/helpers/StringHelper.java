package com.rwtema.careerbees.helpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class StringHelper {

	public static String capFirstMulti(String s) {
		return Arrays.stream(s.split(" ")).map(StringHelper::capFirst).collect(Collectors.joining(" "));
	}

	public static String capFirst(String s) {
		switch (s.length()) {
			case 0:
				return s;
			case 1:
				return s.toUpperCase(Locale.ENGLISH);
			default:
				if (Character.isUpperCase(s.charAt(0))) {
					return s;
				}
				return s.substring(0, 1).toUpperCase() + s.substring(1);
		}
	}

	public static HashMap<String, String> abbreviate(Iterable<String> strings) {
		return abbreviate(Sets.newHashSet(strings), 0);
	}

	private static HashMap<String, String> abbreviate(Iterable<String> strings, int i) {
		HashMultimap<Character, String> stringMap = HashMultimap.create();
		HashMap<String, String> result = Maps.newHashMap();
		for (String string : strings) {
			if (i < string.length()) {
				stringMap.put(string.charAt(i), string);
			} else {
				result.put(string, string);
			}
		}
		for (Character character : stringMap.keySet()) {
			Set<String> set = stringMap.get(character);
			if (set.size() == 1) {
				String s = set.iterator().next();
				result.put(s, s.substring(0, i));
			} else if(!set.isEmpty()) {
				result.putAll(abbreviate(set, i + 1));
			}
		}
		return result;
	}
}
