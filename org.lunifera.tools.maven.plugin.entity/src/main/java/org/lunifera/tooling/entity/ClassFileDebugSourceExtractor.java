package org.lunifera.tooling.entity;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.io.Files;

public class ClassFileDebugSourceExtractor {

	protected static class Visitor extends EmptyVisitor {
		protected String source;

		@Override
		public void visitSource(String arg0, String arg1) {
			this.source = arg0;
		}
	}

	public String getDebugSourceFileName(File classFile) throws IOException {
		ClassReader cr = new ClassReader(Files.toByteArray(classFile));
		Visitor visitor = new Visitor();
		cr.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
		return visitor.source;
	}
}
