package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.output.InfoOutput;
import org.flywaydb.core.internal.output.InfoOutputFactory;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class MigrationInfoServiceImpl implements MigrationInfoService {
        private final MigrationResolver migrationResolver;

    private final Context context;

        private final SchemaHistory schemaHistory;

        private MigrationVersion target;

        private boolean outOfOrder;

        private final boolean pending;

        private final boolean missing;

        private final boolean ignored;

        private final boolean future;

        private List<MigrationInfoImpl> migrationInfos;

        public MigrationInfoServiceImpl(MigrationResolver migrationResolver,
                                    SchemaHistory schemaHistory, final Configuration configuration,
                                    MigrationVersion target, boolean outOfOrder,
                                    boolean pending, boolean missing, boolean ignored, boolean future) {
        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.context = new Context() {
            @Override
            public Configuration getConfiguration() {
                return configuration;
            }
        };
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.pending = pending;
        this.missing = missing;
        this.ignored = ignored;
        this.future = future;
    }

        public void refresh() {
        Collection<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(context);
        List<AppliedMigration> appliedMigrations = schemaHistory.allAppliedMigrations();

        MigrationInfoContext context = new MigrationInfoContext();
        context.outOfOrder = outOfOrder;
        context.pending = pending;
        context.missing = missing;
        context.ignored = ignored;
        context.future = future;
        context.target = target;

        Map<Pair<MigrationVersion, Boolean>, ResolvedMigration> resolvedVersioned = new TreeMap<>();
        Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap<>();

        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            MigrationVersion version = resolvedMigration.getVersion();
            if (version != null) {
                if (version.compareTo(context.lastResolved) > 0) {
                    context.lastResolved = version;
                }
                resolvedVersioned.put(Pair.of(version,



                                false), resolvedMigration);
            } else {
                resolvedRepeatable.put(resolvedMigration.getDescription(), resolvedMigration);
            }
        }

        List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList<>();
        List<AppliedMigration> appliedRepeatable = new ArrayList<>();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            MigrationVersion version = appliedMigration.getVersion();
            if (version == null) {
                appliedRepeatable.add(appliedMigration);
                continue;
            }
            if (appliedMigration.getType() == MigrationType.SCHEMA) {
                context.schema = version;
            }
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                context.baseline = version;
            }





            appliedVersioned.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
        }

        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            MigrationVersion version = av.getLeft().getVersion();
            if (version != null) {
                if (version.compareTo(context.lastApplied) > 0) {



                        context.lastApplied = version;



                } else {
                    av.getRight().outOfOrder = true;
                }
            }
        }

        if (MigrationVersion.CURRENT == target) {
            context.target = context.lastApplied;
        }

        List<MigrationInfoImpl> migrationInfos1 = new ArrayList<>();
        Set<ResolvedMigration> pendingResolvedVersioned = new HashSet<>(resolvedVersioned.values());
        for (Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
            ResolvedMigration resolvedMigration = resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType().isUndo()));
            if (resolvedMigration != null



            ) {
                pendingResolvedVersioned.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder



            ));
        }

        for (ResolvedMigration prv : pendingResolvedVersioned) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false



            ));
        }


        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            if (!context.latestRepeatableRuns.containsKey(appliedRepeatableMigration.getDescription())
                    || (appliedRepeatableMigration.getInstalledRank() > context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription()))) {
                context.latestRepeatableRuns.put(appliedRepeatableMigration.getDescription(), appliedRepeatableMigration.getInstalledRank());
            }
        }

        Set<ResolvedMigration> pendingResolvedRepeatable = new HashSet<>(resolvedRepeatable.values());
        for (AppliedMigration appliedRepeatableMigration : appliedRepeatable) {
            ResolvedMigration resolvedMigration = resolvedRepeatable.get(appliedRepeatableMigration.getDescription());
            int latestRank = context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription());
            if (resolvedMigration != null && appliedRepeatableMigration.getInstalledRank() == latestRank
                    && resolvedMigration.checksumMatches(appliedRepeatableMigration.getChecksum())) {
                pendingResolvedRepeatable.remove(resolvedMigration);
            }
            migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false



            ));
        }

        for (ResolvedMigration prr : pendingResolvedRepeatable) {
            migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false



            ));
        }

        Collections.sort(migrationInfos1);
        migrationInfos = migrationInfos1;
    }

























    @Override
    public MigrationInfo[] all() {
        return migrationInfos.toArray(new MigrationInfo[0]);
    }

    @Override
    public MigrationInfo current() {
        MigrationInfo current = null;
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()




                    && migrationInfo.getVersion() != null
                    && (current == null || migrationInfo.getVersion().compareTo(current.getVersion()) > 0)) {
                current = migrationInfo;
            }
        }
        if (current != null) {
            return current;
        }

        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfoImpl migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isApplied()




            ) {
                return migrationInfo;
            }
        }

        return null;
    }

    @Override
    public MigrationInfoImpl[] pending() {
        List<MigrationInfoImpl> pendingMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }

        return pendingMigrations.toArray(new MigrationInfoImpl[0]);
    }

    @Override
    public MigrationInfoImpl[] applied() {
        List<MigrationInfoImpl> appliedMigrations = new ArrayList<>();
        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isApplied()) {
                appliedMigrations.add(migrationInfo);
            }
        }

        return appliedMigrations.toArray(new MigrationInfoImpl[0]);
    }

        public MigrationInfo[] resolved() {
        List<MigrationInfo> resolvedMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isResolved()) {
                resolvedMigrations.add(migrationInfo);
            }
        }

        return resolvedMigrations.toArray(new MigrationInfo[0]);
    }

        public MigrationInfo[] failed() {
        List<MigrationInfo> failedMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }

        return failedMigrations.toArray(new MigrationInfo[0]);
    }

        public MigrationInfo[] future() {
        List<MigrationInfo> futureMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS)
                    || (migrationInfo.getState() == MigrationState.FUTURE_FAILED))




            ) {
                futureMigrations.add(migrationInfo);
            }
        }

        return futureMigrations.toArray(new MigrationInfo[0]);
    }

        public MigrationInfo[] outOfOrder() {
        List<MigrationInfo> outOfOrderMigrations = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
                outOfOrderMigrations.add(migrationInfo);
            }
        }

        return outOfOrderMigrations.toArray(new MigrationInfo[0]);
    }




















        public String validate() {
        StringBuilder builder = new StringBuilder();
        boolean hasFailures = false;

        for (MigrationInfoImpl migrationInfo : migrationInfos) {
            String message = migrationInfo.validate();
            if (message != null) {
                if (!hasFailures)
                    builder.append("\n");

                builder.append(message + "\n");
                hasFailures = true;
            }
        }
        return (hasFailures) ? builder.toString() : null;
    }

    @Override
    public InfoOutput getInfoOutput() {
        InfoOutputFactory infoOutputFactory = new InfoOutputFactory();
        return infoOutputFactory.create(this.context.getConfiguration(), this.all(), this.current());
    }
}