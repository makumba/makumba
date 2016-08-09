/*
 * Created on Dec 6, 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.commons;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.providers.Configuration;
import org.makumba.providers.TransactionProvider;

public class FileOnDisk {

    public static void main(String[] argv) throws LogicException {
        Transaction t = TransactionProvider.getInstance().getConnectionTo(argv[0]);
        Vector<Dictionary<String, Object>> v = t.executeQuery("SELECT f." + argv[2] + " AS cont, f." + argv[2]
                + ".name as name FROM " + argv[1] + " f", null);
        int n = 0;

        long base = new Date().getTime();
        String fileType = argv[1] + "->" + argv[2];
        String dataSource = argv[0];

        String uri = getFileOnDiskURI(fileType, dataSource);

        for (Dictionary<String, Object> d : v) {
            Pointer p = (Pointer) d.get("cont");
            if (p == null) {
                continue;
            }
            Dictionary<String, Object> dt = null;
            Object name = d.get("name");
            try {
                dt = t.executeQuery(
                    "SELECT dt.content as content, dt.contentLength as contentLength FROM " + fileType
                            + " dt WHERE dt=$1", p).get(0);

            } catch (Throwable thr) {
                System.err.println(p + "  " + name);
                thr.printStackTrace();
                continue;
            }

            System.out.print(new Date().getTime() - base);
            System.out.print("  ");
            System.out.print(n++);
            System.out.print("/");
            System.out.print(v.size());
            System.out.print("  ");
            System.out.print(dt.get("contentLength"));
            System.out.print("  ");

            Text content = (Text) dt.get("content");
            writeFile(uri, p, (String) name, content);
            System.out.println(content.length() + " " + name + "  " + p.longValue());
        }
    }

    static public String getFileOnDiskURI(String fileType, String dataSource) {
        return Configuration.getDataSourceConfiguration(dataSource).get("file#" + fileType);
    }

    static public Text readFile(String uri, Pointer key, String name, int len) throws IOException {

        URL url = new URL(uri + getName(key, name) + ";type=i");
        URLConnection urlc = url.openConnection();
        InputStream is = urlc.getInputStream(); // To download
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        zis.getNextEntry();
        return new Text(zis, len);
    }

    static public void writeFile(String uri, Pointer key, String name, Text content) throws LogicException {
        if (content == null) {
            return;
        }

        try {
            URL url = new URL(uri + getName(key, name) + ";type=i");
            URLConnection urlc = url.openConnection();
            // InputStream is = urlc.getInputStream(); // To download
            OutputStream sink = urlc.getOutputStream(); // To upload

            ZipOutputStream os = new ZipOutputStream(new BufferedOutputStream(sink));
            os.putNextEntry(new ZipEntry(name));
            content.writeTo(os);
            os.flush();
            os.close();
        } catch (IOException ioe) {
            throw new LogicException(ioe);
        }
    }

    // FIXME: the file name format could be indicated in the file# configuration line
    static public String getName(Pointer key, String name) {
        String filename = name.replace(' ', '_').replace('%', '_') + "__" + key.longValue();
        return filename;
    }
}
