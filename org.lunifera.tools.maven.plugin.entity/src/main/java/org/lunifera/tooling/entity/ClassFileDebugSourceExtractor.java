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

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import com.google.common.io.Files;

public class ClassFileDebugSourceExtractor {

	protected static class Visitor extends ClassVisitor {
		protected String source;

		public Visitor(int api) {
			super(api);
		}

		@Override
		public void visitSource(String arg0, String arg1) {
			this.source = arg0;
		}
	}

	public String getDebugSourceFileName(File classFile) throws IOException {
		ClassReader cr = new ClassReader(Files.toByteArray(classFile));
		Visitor visitor = new Visitor(0);
		cr.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
		return visitor.source;
	}
}
