package org.lunifera.tools.maven.plugin.subsystems;

import java.io.IOException;
import java.util.zip.CRC32;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;

public class SubsystemArchiverComponent extends JarArchiver {

    public static final String SUBSYSTEM_EXTENSION = "esa";

    public SubsystemArchiverComponent() {
        super();
        archiveType = SUBSYSTEM_EXTENSION;
        setEncoding("UTF8");
    }

    private long calculateCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    @Override
    protected void initZipOutputStream(ZipOutputStream zOut)
            throws IOException, ArchiverException {
        byte[] mimetypeBytes = "application/vnd.osgi.subsystem"
                .getBytes("UTF-8");
        ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
        mimetypeZipEntry.setMethod(ZipEntry.STORED);
        mimetypeZipEntry.setSize(mimetypeBytes.length);
        mimetypeZipEntry.setCrc(calculateCrc(mimetypeBytes));
        zOut.putNextEntry(mimetypeZipEntry);
        zOut.write(mimetypeBytes);

        super.initZipOutputStream(zOut);
    }
}
