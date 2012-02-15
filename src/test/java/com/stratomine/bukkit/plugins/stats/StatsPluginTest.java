package com.stratomine.bukkit.plugins.stats;

import static org.junit.Assert.*;

import org.junit.Test;

public class StatsPluginTest extends TestCase {
	
	@Test
	public void testNormalizeMetric() {
		assertEquals("foo.bar.baz", StatsPlugin.normalizeMetric("foo.bar", "baz"));
		assertEquals("foo.bar.baz", StatsPlugin.normalizeMetric("foo.bar.", "baz"));
		assertEquals("foo.bar.baz", StatsPlugin.normalizeMetric("foo.bar..", "baz"));
		assertEquals("foo.bar.baz", StatsPlugin.normalizeMetric("foo.bar ", " baz"));
	}
	
}
