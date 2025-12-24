package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MigrationInfoDumper {
        private MigrationInfoDumper() {
    }

        public static String dumpToAsciiTable(MigrationInfo[] migrationInfos) {





        List<String> columns = Arrays.asList("Category", "Version", "Description", "Type", "Installed On", "State"



        );

        List<List<String>> rows = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            List<String> row = Arrays.asList(
                    getCategory(migrationInfo),
                    getVersionStr(migrationInfo),
                    migrationInfo.getDescription(),
                    migrationInfo.getType().name(),
                    DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()),
                    migrationInfo.getState().getDisplayName()



            );
            rows.add(row);
        }

        return new AsciiTable(columns, rows, true, "", "No migrations found").render();
    }

    static String getCategory(MigrationInfo migrationInfo) {
        if (migrationInfo.getType().isSynthetic()) {
            return "";
        }
        if (migrationInfo.getVersion() == null) {
            return "Repeatable";
        }





        return "Versioned";
    }

    private static String getVersionStr(MigrationInfo migrationInfo) {
        return migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
    }



































}