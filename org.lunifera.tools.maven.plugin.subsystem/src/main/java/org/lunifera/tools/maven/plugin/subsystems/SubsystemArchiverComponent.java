package org.lunifera.tools.maven.plugin.subsystems;

/*
 * #%L
 * Lunifera Maven Tools : Subsystem Plugin
 * %%
 * Copyright (C) 2012 - 2014 C4biz Softwares ME, Loetz KG
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

import java.io.IOException;
import java.util.zip.CRC32;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;

/**
 * 
 * @author cvgaviao
 * 
 */
public class SubsystemArchiverComponent extends JarArchiver {

	public static final String SUBSYSTEM_EXTENSION = "esa";
	private boolean skipMimeEntry = false;

	public SubsystemArchiverComponent() {
		super();
		archiveType = SUBSYSTEM_EXTENSION;
		setEncoding("UTF-8");
	}

	private long calculateCrc(byte[] data) {
		CRC32 crc = new CRC32();
		crc.update(data);
		return crc.getValue();
	}

	@Override
	protected void initZipOutputStream(ZipOutputStream zOut)
			throws IOException, ArchiverException {

		if (!skipWriting && !skipMimeEntry) {

			byte[] mimetypeBytes = "application/vnd.osgi.subsystem"
					.getBytes("UTF-8");
			ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
			mimetypeZipEntry.setMethod(ZipEntry.STORED);
			mimetypeZipEntry.setSize(mimetypeBytes.length);
			mimetypeZipEntry.setCrc(calculateCrc(mimetypeBytes));
			zOut.putNextEntry(mimetypeZipEntry);
			zOut.write(mimetypeBytes);
		}
	}
	
	@Override
	protected void finalizeZipOutputStream(ZipOutputStream zOut)
			throws IOException, ArchiverException {
	}

	public void skipMimeTypeFiTag(boolean skipMimeEntry) {
		this.skipMimeEntry = skipMimeEntry;
		
	}
}
