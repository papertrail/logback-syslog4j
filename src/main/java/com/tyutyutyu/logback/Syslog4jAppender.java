package com.tyutyutyu.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.SyslogRuntimeException;

public class Syslog4jAppender<E> extends AppenderBase<E> {

	private SyslogIF syslog;

	private SyslogConfigIF syslogConfig;

	private Layout<E> layout;

	@Override
	protected void append(E loggingEvent) {

		syslog.log(getSeverityForEvent(loggingEvent), layout.doLayout(loggingEvent));
	}

	@Override
	public void start() {

		super.start();

		synchronized (this) {
			try {
				Class<?> syslogClass = syslogConfig.getSyslogClass();
				syslog = (SyslogIF) syslogClass.newInstance();

				syslog.initialize(syslogClass.getSimpleName(), syslogConfig);
			} catch (ClassCastException cse) {
				throw new SyslogRuntimeException(cse);
			} catch (IllegalAccessException iae) {
				throw new SyslogRuntimeException(iae);
			} catch (InstantiationException ie) {
				throw new SyslogRuntimeException(ie);
			}
		}
	}

	@Override
	public void stop() {

		super.stop();

		synchronized (this) {
			if (syslog != null) {
				syslog.shutdown();
				syslog = null;
			}
		}
	}

	/**
	 * Convert a level to equivalent syslog severity. Only levels for printing methods i.e DEBUG, WARN, INFO and ERROR are converted.
	 *
	 * @see ch.qos.logback.core.net.SyslogAppenderBase#getSeverityForEvent(java.lang.Object)
	 */
	private static int getSeverityForEvent(Object eventObject) {

		if (eventObject instanceof ILoggingEvent) {
			ILoggingEvent event = (ILoggingEvent) eventObject;
			return LevelToSyslogSeverity.convert(event);
		} else {
			return SyslogConstants.LEVEL_INFO;
		}
	}

	public SyslogConfigIF getSyslogConfig() {

		return syslogConfig;
	}

	public void setSyslogConfig(SyslogConfigIF syslogConfig) {

		this.syslogConfig = syslogConfig;
	}

	public Layout<E> getLayout() {

		return layout;
	}

	public void setLayout(Layout<E> layout) {

		this.layout = layout;
	}

}
