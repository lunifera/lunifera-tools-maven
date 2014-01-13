/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on Xtend Maven Plugin
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 */
package org.lunifera.tooling.entity;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.maven.plugin.logging.Log;

public class MavenLog4JConfigurator {

	protected final static Level LOG4J_DEFAULT_LOG_LEVEL = Level.INFO;

	public void configureLog4j(Log log) {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
			try {
				Logger.getRootLogger().setLevel(LOG4J_DEFAULT_LOG_LEVEL);
			} catch (NoSuchMethodError e) {
				// see http://bugzilla.slf4j.org/show_bug.cgi?id=279
			}
			Logger.getRootLogger().addAppender(createMojoLogAppender(log));
		} else {
			configureLog4j(log, "org.eclipse.entity");
		}
	} 

	protected void configureLog4j(Log log, String name) {
		Logger logger = Logger.getLogger(name);
		logger.setAdditivity(false);
		try {
			logger.setLevel(LOG4J_DEFAULT_LOG_LEVEL);
		} catch (NoSuchMethodError e) {
			// see http://bugzilla.slf4j.org/show_bug.cgi?id=279
		}
		logger.removeAllAppenders();
		logger.addAppender(createMojoLogAppender(log));
	}

	protected AppenderSkeleton createMojoLogAppender(final Log log) {
		return new AppenderSkeleton() {

			@Override
			protected void append(LoggingEvent event) {
				if (event.getMessage() == null) {
					return;
				}
				if (Level.DEBUG == event.getLevel()) {
					log.debug((CharSequence) event.getMessage(), getThrowable(event));
				} else if (Level.INFO == event.getLevel()) {
					log.info((CharSequence) event.getMessage(), getThrowable(event));
				} else if (Level.WARN == event.getLevel()) {
					log.warn((CharSequence) event.getMessage(), getThrowable(event));
				} else if (Level.ERROR == event.getLevel()) {
					log.error((CharSequence) event.getMessage(), getThrowable(event));
				}
			}

			public void close() {
			}

			private Throwable getThrowable(LoggingEvent event) {
				return event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null;
			}

			public boolean requiresLayout() {
				return false;
			}
		};
	}
}
