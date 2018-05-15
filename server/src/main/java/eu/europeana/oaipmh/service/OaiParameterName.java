package eu.europeana.oaipmh.service;

import java.util.HashSet;
import java.util.Set;

public enum OaiParameterName {

    METADATA_PREFIX("metadataPrefix"), FROM("from"), UNTIL("until"), SET("set"), RESUMPTION_TOKEN("resumptionToken"), VERB("verb"), IDENTIFIER("identifier");

    private final String parameterName;

    private OaiParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    private static Set<String> allValues= new HashSet<>();

    static{
        for (OaiParameterName parameterName : OaiParameterName.values()) {
            allValues.add(parameterName.parameterName);
        }
    }

    public static boolean contains(String value){
        return allValues.contains(value);
    }

    public static OaiParameterName fromString(String parameterName) {
        for (OaiParameterName param : values()) {
            if (param.parameterName.equals(parameterName)) {
                return param;
            }
        }
        throw new IllegalArgumentException("No OaiParameterName enum value found for \"" + parameterName + "\"");
    }

    @Override
    public String toString() {
        return parameterName;
    }
}
