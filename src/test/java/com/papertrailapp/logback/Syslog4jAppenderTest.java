package com.papertrailapp.logback;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.tcp.ssl.SSLTCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Syslog4jAppenderTest extends TestCase {
    ByteArrayOutputStream serverStream;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public Syslog4jAppenderTest(String testName)
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( Syslog4jAppenderTest.class );
    }

    protected void setUp() {
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

    protected void tearDown() {
        SyslogServer.shutdown();
    }

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
        assertTrue("Server received: " + serverData, serverData.contains("test message over udp"));
    }

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
        assertTrue("Server received: " + serverData, serverData.contains("test message over tcp"));
    }

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
        assertTrue("Server received: " + serverData, serverData.contains("test message over tls"));
    }
}
