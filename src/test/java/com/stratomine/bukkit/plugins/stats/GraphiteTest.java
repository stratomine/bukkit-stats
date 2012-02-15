package com.stratomine.bukkit.plugins.stats;

import org.junit.Before;
import org.junit.Test;

public class GraphiteTest extends TestCase {
	
	private Graphite graphite;
	
	@Before
	public void setup() {
		graphite = new Graphite("localhost");
	}
	
	@Test
	public void testGenerateLine() {
		assertMatches("^local.test.metric 69 \\d+$", graphite.generateLine("local.test.metric", 69));
		assertMatches("^local.test.metric 69.69 \\d+$", graphite.generateLine("local.test.metric", 69.69));
	}
	
}
