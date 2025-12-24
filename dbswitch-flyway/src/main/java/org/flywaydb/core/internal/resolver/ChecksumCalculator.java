package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomFilter;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class ChecksumCalculator {
    private ChecksumCalculator() {
    }

        public static int calculate(LoadableResource... loadableResources) {
        int checksum;




            checksum = calculateChecksumForResource(loadableResources[0]);












        return checksum;
    }

    private static int calculateChecksumForResource(LoadableResource resource) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(resource.read(), 4096);

            String line = bufferedReader.readLine();

            if (line != null) {
                line = BomFilter.FilterBomFromString(line);

                do {
                    crc32.update(StringUtils.trimLineBreak(line).getBytes(StandardCharsets.UTF_8));
                } while ((line = bufferedReader.readLine()) != null);
            }
        } catch (IOException e) {
            throw new FlywayException("Unable to calculate checksum of " + resource.getFilename() + "\r\n" + e.getMessage(), e);
        } finally {
            IOUtils.close(bufferedReader);
        }

        return (int) crc32.getValue();
    }

















}