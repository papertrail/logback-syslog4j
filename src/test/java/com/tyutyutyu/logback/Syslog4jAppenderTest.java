package com.tyutyutyu.logback;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.tcp.ssl.SSLTCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Syslog4jAppenderTest {

	private ByteArrayOutputStream serverStream;

	@Before
	public void before() {

		serverStream = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(serverStream);

		final TCPNetSyslogServerConfig tcpNetSyslogServerConfig = new TCPNetSyslogServerConfig(45553);
		tcpNetSyslogServerConfig.addEventHandler(new PrintStreamSyslogServerEventHandler(ps));

		final UDPNetSyslogServerConfig udpNetSyslogServerConfig = new UDPNetSyslogServerConfig(45553);
		udpNetSyslogServerConfig.addEventHandler(new PrintStreamSyslogServerEventHandler(ps));

		final SSLTCPNetSyslogServerConfig ssltcpNetSyslogServerConfig = new SSLTCPNetSyslogServerConfig();
		ssltcpNetSyslogServerConfig.setPort(45554);
		ssltcpNetSyslogServerConfig.addEventHandler(new PrintStreamSyslogServerEventHandler(ps));
		ssltcpNetSyslogServerConfig.setKeyStore(this.getClass().getClassLoader().getResource("test-keystore.jks").getFile());
		ssltcpNetSyslogServerConfig.setKeyStorePassword("password");
		ssltcpNetSyslogServerConfig.setTrustStore(this.getClass().getClassLoader().getResource("test-keystore.jks").getFile());
		ssltcpNetSyslogServerConfig.setTrustStorePassword("password");

		SyslogServer.createThreadedInstance("testTcp", tcpNetSyslogServerConfig);
		SyslogServer.createThreadedInstance("testUdp", udpNetSyslogServerConfig);
		SyslogServer.createThreadedInstance("testTls", ssltcpNetSyslogServerConfig);
	}

	@After
	@SuppressWarnings("static-method")
	public void after() {

		SyslogServer.shutdown();
	}

	@Test
	public void testUdpSender() throws JoranException, InterruptedException {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure(this.getClass().getClassLoader().getResourceAsStream("logback-syslog4j-udp.xml"));

		Logger logger = context.getLogger("test-udp");
		logger.info("test message over udp");

		context.stop();
		Thread.sleep(100);

		final String serverData = serverStream.toString();
		assertThat(serverData).contains("test message over udp");
	}

	@Test
	public void testTcpSender() throws JoranException, InterruptedException {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure(this.getClass().getClassLoader().getResourceAsStream("logback-syslog4j-tcp.xml"));

		Logger logger = context.getLogger("test-tcp");
		logger.info("test message over tcp");

		context.stop();
		Thread.sleep(100);

		final String serverData = serverStream.toString();
		assertThat(serverData).contains("test message over tcp");
	}

	@Test
	public void testTlsSender() throws JoranException, InterruptedException {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure(this.getClass().getClassLoader().getResourceAsStream("logback-syslog4j-tls.xml"));

		Logger logger = context.getLogger("test-tls");
		logger.info("test message over tls");

		context.stop();
		Thread.sleep(100);

		final String serverData = serverStream.toString();
		assertThat(serverData).contains("test message over tls");
	}

}
