package org.flywaydb.core.internal.resource.filesystem;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomStrippingReader;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;

public class FileSystemResource extends LoadableResource {

    private static final Log LOG = LogFactory.getLog(FileSystemResource.class);









        private final File file;
    private final String relativePath;
    private final Charset encoding;




        public FileSystemResource(Location location, String fileNameWithPath, Charset encoding



    ) {
        this.file = new File(new File(fileNameWithPath).getPath());
        this.relativePath = location == null ? file.getPath() : location.getPathRelativeToThis(file.getPath()).replace("\\", "/");
        this.encoding = encoding;



    }

        @Override
    public String getAbsolutePath() {
        return file.getPath();
    }

        @Override
    public String getAbsolutePathOnDisk() {
        return file.getAbsolutePath();
    }

    @Override
    public Reader read() {
        try {
            return Channels.newReader(FileChannel.open(file.toPath(), StandardOpenOption.READ), encoding.newDecoder(), 4096);
        } catch (IOException e){
            LOG.debug("Unable to load filesystem resource" + file.getPath() + " using FileChannel.open." +
                    " Falling back to FileInputStream implementation. Exception message: " + e.getMessage());
        }

        try {
            return new BufferedReader(new BomStrippingReader(new InputStreamReader(new FileInputStream(file), encoding)));
        } catch (IOException e) {
            throw new FlywayException("Unable to load filesystem resource: " + file.getPath() + " (encoding: " + encoding + ")", e);
        }
    }












        @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }
}