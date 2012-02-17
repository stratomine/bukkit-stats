package com.stratomine.bukkit.plugins.stats;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class StatsdClient {
	
	private static Random random = new Random();
	
	private InetAddress host;
	private int port;
	private DatagramSocket socket;
	private String namespace = "";
	
	public StatsdClient(String host, int port) throws UnknownHostException, SocketException {
		this(InetAddress.getByName(host), port);
	}
	
	public StatsdClient(InetAddress host, int port) throws SocketException {
		this.host = host;
		this.port = port;
		this.socket = new DatagramSocket();
	}
	
	public void decrement(String metric) {
		decrement(metric, -1, 1.0);
	}

	public void decrement(String metric, double sampleRate) {
		decrement(metric, -1, sampleRate);
	}

	public void decrement(String metric, int delta) {
		decrement(metric, delta, 1.0);
	}

	public void decrement(String metric, int delta, double sampleRate) {
		delta = delta < 0 ? delta : -delta;
		increment(metric, delta, sampleRate);
	}

	public void increment(String metric) {
		increment(metric, 1, 1.0);
	}
	
	public void increment(String metric, double sampleRate) {
		increment(metric, 1, sampleRate);
	}
	
	public void increment(String metric, int delta) {
		increment(metric, delta, 1.0);
	}
	
	public void increment(String metric, int delta, double sampleRate) {
		send(String.format("%s:%s|c", metric, delta), sampleRate);
	}
	
	private void send(String metric, double sampleRate) {
		metric = normalizeMetric(metric);
		if (sampleRate < 1.0) {
			if (random.nextDouble() <= sampleRate) {
				send(String.format("%s|@%f", metric, sampleRate));
			}
		} else {
			send(metric);
		}
	}
	
	private void send(String metric) {
		try {
			byte[] data = metric.getBytes("utf-8");
			socket.send(new DatagramPacket(data, data.length, host, port));
		} catch (IOException e) {
			// Fail silently
		}
	}
	
	private String normalizeMetric(String metric) {
		return StatsPlugin.normalizeMetric(getNamespace(), metric);
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
