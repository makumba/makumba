package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.makumba.DataDefinition;
import org.makumba.providers.datadefinition.makumba.RecordInfo;
import org.makumba.providers.datadefinition.mdd.MDDProvider;

/**
 * Compares MDDs provided by two separate MDD providers
 * @author manu
 * @version $Id: MDDComparator.java,v 1.1 Apr 28, 2010 9:39:39 AM manu Exp $
 */
public class MDDComparator {

    private static final String TEMP = "temp";

    public static void main(String... args) {

        // create a temporary directory to read the files using the "webappRoot" configuration
        File tempDir = new File(TEMP + "/WEB-INF/classes/dataDefinitions/");
        tempDir.deleteOnExit();
        tempDir.mkdir();

        RecordInfo.setWebappRoot(TEMP);
        MDDProvider.setWebappRoot(TEMP);

        try {

            // extract the MDDs so the providers can access them in a normal fashion
            File f = new File(MDDComparator.class.getResource("mdd-corpus.zip").getPath());
            ZipFile zf = new ZipFile(f);
            extractMDDsFile(zf, tempDir);

            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                if (!ze.getName().endsWith(".mdd")) {
                    continue;
                }

                System.out.println("==== Reading MDD " + ze.getName());
                String type = ze.getName().substring(0, ze.getName().lastIndexOf("."));
                DataDefinition dd1 = RecordInfo.getRecordInfo(type);
                DataDefinition dd2 = MDDProvider.getMDD(type);
                compare(dd1, dd2);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract all corpus MDDs in a zip file
     */
    private static void extractMDDsFile(ZipFile zf, File tempDir) throws IOException {

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            if (!ze.getName().endsWith(".mdd")) {
                continue;
            }

            BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
            int size;
            byte[] buffer = new byte[2048];
            int n = ze.getName().lastIndexOf(File.separator);
            File dir = new File(tempDir.getPath() + File.separator + ze.getName().substring(0, n));
            dir.mkdirs();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dir.getPath() + File.separator
                    + ze.getName().substring(n)), buffer.length);
            while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }

            bos.flush();
            bos.close();
            bis.close();
        }

    }

    /**
     * Compares the structure of two DataDefinition-s and their underlying FieldDefinition-s
     */
    private static void compare(DataDefinition dd1, DataDefinition dd2) {
        // playground for Gwen
        System.out.println(dd1.getName());
        System.out.println(dd2.getName());
        
    }

}
