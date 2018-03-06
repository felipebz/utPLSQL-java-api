package org.utplsql.api.reporter;

import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.STRUCT;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/** This class manages the instantiation of reporters.
 * One can register a supplier method for a specific name which will then be callable via createReporter(name)
 *
 * @author pesse
 */
public final class ReporterFactory implements ORADataFactory {

    public static class ReporterInfo {
        public ReporterInfo(BiFunction<String, Object[], ? extends Reporter> factoryMethod, String description) {
            this.factoryMethod = factoryMethod;
            this.description = description;
        }
        public BiFunction<String, Object[], ? extends Reporter> factoryMethod;
        public String description;
    }

    private Map<String, ReporterInfo> reportFactoryMethodMap = new HashMap<>();
    
    public ReporterFactory() {
        registerDefaultReporters();
    }

    /** Registers the default reporters, provided with utPLSQL core
     */
    private void registerDefaultReporters() {
        Arrays.stream(CoreReporters.values())
                .forEach(r -> registerReporterFactoryMethod(r.name(), r.getFactoryMethod(), r.getDescription()));
    }

    /** Registers a creation method for a specified reporter name. Overrides eventually existing creation method
     *
     * @param reporterName the reporter's name to register
     * @param factoryMethod the method which will return the reporter
     * @param description the description of the reporter
     * @return Object with information about the registered reporter
     */
    public synchronized ReporterInfo registerReporterFactoryMethod( String reporterName, BiFunction<String, Object[], ? extends Reporter> factoryMethod, String description) {
        return reportFactoryMethodMap.put(reporterName.toUpperCase(), new ReporterInfo(factoryMethod, description));
    }

    /** Unregisters a specified reporter name.
     *
     * @param reporterName the reporter's name to unregister
     * @return information about the reporter which was previously registered or null
     */
    public synchronized ReporterInfo unregisterReporterFactoryMethod( String reporterName ) {
        return reportFactoryMethodMap.remove(reporterName.toUpperCase());
    }

    /** Returns a new reporter of the given name.
     * If no specific ReporterFactoryMethod is registered, returns a default {Reporter}
     *
     * @param reporterName the reporter's name to create a new instance of
     * @param attributes attributes from STRUCT
     * @return A reporter
     */
    public Reporter createReporter(String reporterName, Object[] attributes) {

        reporterName = reporterName.toUpperCase();
        BiFunction<String, Object[], ? extends Reporter> supplier = DefaultReporter::new;

        if ( reportFactoryMethodMap.containsKey(reporterName)) {

            ReporterInfo ri = reportFactoryMethodMap.get(reporterName);
            if (ri == null)
                throw new RuntimeException("ReporterInfo for " + reporterName + " was null");

            supplier = ri.factoryMethod;
        }

        if ( supplier == null )
            throw new RuntimeException("No factory method for " + reporterName);

        return supplier.apply( reporterName, attributes );
    }

    /** Returns a new reporter of the given name (or should do so).
     * If no specific ReporterFactoryMethod is registered, returns a default {Reporter}
     */
    public Reporter createReporter( String reporterName ) {
        return createReporter(reporterName, null);
    }

    /** Returns a new reporter of the given DefaultReporter type
     *
     * @param reporter
     * @return
     */
    public Reporter createReporter( CoreReporters reporter ) {
        return createReporter(reporter.name());
    }

    /** Returns a set of all registered reporter's names
     *
     * @return Set of reporter names
     */
    public Map<String, String> getRegisteredReporterInfo() {
        Map<String, String> descMap = new HashMap<>(reportFactoryMethodMap.size());

        for (Map.Entry<String, ReporterInfo> entry : reportFactoryMethodMap.entrySet()) {
            descMap.put(entry.getKey(), entry.getValue().description);
        }
        return descMap;
    }

    @Override
    public ORAData create(Datum d, int sqlType) throws SQLException {
        if (d == null) return null;
        if ( d instanceof STRUCT) {
            String sqlName = ((STRUCT)d).getDescriptor().getName();
            return createReporter(sqlName, ((STRUCT)d).getAttributes());
        }

        return null;
    }
}
