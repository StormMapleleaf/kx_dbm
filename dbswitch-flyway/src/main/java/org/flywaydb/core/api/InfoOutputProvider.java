package org.flywaydb.core.api;

import org.flywaydb.core.internal.output.InfoOutput;

interface InfoOutputProvider {
    InfoOutput getInfoOutput();
}