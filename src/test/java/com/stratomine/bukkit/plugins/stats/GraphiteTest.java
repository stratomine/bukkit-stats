package com.stratomine.bukkit.plugins.stats;

import org.junit.Test;

public class GraphiteTest extends TestCase {
	
	@Test
	public void testGenerateLine() {
		assertMatches("^local.test.metric 69 \\d+\n", Graphite.generateLine("local.test.metric", 69));
		assertMatches("^local.test.metric 69.69 \\d+\n", Graphite.generateLine("local.test.metric", 69.69));
	}
	
}
