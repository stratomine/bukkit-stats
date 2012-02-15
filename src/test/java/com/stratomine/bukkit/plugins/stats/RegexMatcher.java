package com.stratomine.bukkit.plugins.stats;

import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexMatcher extends BaseMatcher<String> {
	
	private final Pattern pattern;
	
	public RegexMatcher(String pattern) {
		this(Pattern.compile(pattern));
	}
	
	public RegexMatcher(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public void describeTo(Description description) {
		description.appendText("matches regular expression ").appendValue(pattern);
	}
	
	public boolean matches(Object input) {
		return pattern.matcher((String)input).matches();
	}
	
	public static RegexMatcher matches(String pattern) {
		return matches(Pattern.compile(pattern));
	}
	
	public static RegexMatcher matches(Pattern pattern) {
		return new RegexMatcher(pattern);
	}

}
