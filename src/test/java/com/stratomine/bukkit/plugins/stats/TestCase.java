package com.stratomine.bukkit.plugins.stats;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;

public abstract class TestCase {
	
	public static void assertMatches(String pattern, String actual) {
		assertThat(actual, matchesPattern(pattern));
	}
	
	private static Matcher<String> matchesPattern(String pattern) {
		return RegexMatcher.matches(pattern);
	}
	
}
