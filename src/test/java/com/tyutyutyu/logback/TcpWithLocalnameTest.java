package com.tyutyutyu.logback;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpWithLocalnameTest {

	private ByteArrayOutputStream serverStream;

	SyslogServerIF serverIF;

	@Before
	public void before() {

		serverStream = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(serverStream);

		final TCPNetSyslogServerConfig serverConfig = new TCPNetSyslogServerConfig(45553);
		serverConfig.addEventHandler(new PrintStreamSyslogServerEventHandler(ps));

		serverIF = SyslogServer.createThreadedInstance("testTcpWithLocalname", serverConfig);
	}

	@After
	@SuppressWarnings("static-method")
	public void after() {

		SyslogServer.shutdown();
	}

	@Test
	public void testTlsSenderWithLocalName() throws JoranException {

		// given
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure(this.getClass().getClassLoader().getResourceAsStream("logback-syslog4j-tcp-with-localname.xml"));

		Logger logger = context.getLogger("test-tls-with-localname");

		// when
		logger.info("test message over tls with localname");

		context.stop();
		serverIF.shutdown();

		// then
		assertThat(serverStream.toString()).contains("INFO customLocalName syslog-test");
	}

}
