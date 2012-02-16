package com.stratomine.bukkit.plugins.stats;

import org.junit.Test;

public class GraphiteClientTest extends TestCase {
	
	@Test
	public void testGenerateLine() {
		assertMatches("^stats.local.test.metric 69 \\d+\n", GraphiteClient.generateLine("local.test.metric", 69));
		assertMatches("^stats.local.test.metric 69.69 \\d+\n", GraphiteClient.generateLine("local.test.metric", 69.69));
	}
	
}
