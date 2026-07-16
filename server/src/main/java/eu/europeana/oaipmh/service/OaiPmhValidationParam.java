package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.*;
import java.util.*;
import java.util.Set;

import static eu.europeana.oaipmh.service.OaiParameterName.FROM;
import static eu.europeana.oaipmh.service.OaiParameterName.IDENTIFIER;
import static eu.europeana.oaipmh.service.OaiParameterName.METADATA_PREFIX;
import static eu.europeana.oaipmh.service.OaiParameterName.RESUMPTION_TOKEN;
import static eu.europeana.oaipmh.service.OaiParameterName.SET;
import static eu.europeana.oaipmh.service.OaiParameterName.UNTIL;

/**
 * This class provides constant definitions and parameter mapping logic for validating
 * the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH) requests.
 * It includes configurations for valid verbs, mandatory parameters, valid parameter sets,
 * and exclusive parameter constraints associated with specific verbs.
 *
 * Fields in this class define the structure and rules required for validation, including:
 * - Valid OAI-PMH verb names.
 * - Mandatory parameters for each verb.
 * - Valid parameter combinations allowed for each verb.
 * - Mutually exclusive parameter sets for specific cases.
 * @author Srishti Singh
 * @since 2026-07-16
 */
public class OaiPmhValidationParam {

    public static final String PARAMETER_INFO = "Parameter \"%s\" ";

    public static final Set<String> VALID_VERBS = Set.of(
            Identify.class.getSimpleName(),
            ListIdentifiers.class.getSimpleName(),
            GetRecord.class.getSimpleName(),
            ListSets.class.getSimpleName(),
            ListMetadataFormats.class.getSimpleName(),
            ListRecords.class.getSimpleName()
    );

    public static final Map<String, Set<OaiParameterName>> MANDATORY_VERB_PARAMETERS = Map.of(
            ListIdentifiers.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX),

            GetRecord.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX, IDENTIFIER),

            ListRecords.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX)
    );

    public static final Map<String, Set<OaiParameterName>> VALID_VERB_PARAMETERS = Map.of(
            Identify.class.getSimpleName(),
            EnumSet.noneOf(OaiParameterName.class),

            ListIdentifiers.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX, FROM, UNTIL, SET, RESUMPTION_TOKEN),

            GetRecord.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX, IDENTIFIER),

            ListSets.class.getSimpleName(),
            EnumSet.of(FROM, UNTIL, RESUMPTION_TOKEN),

            ListMetadataFormats.class.getSimpleName(),
            EnumSet.of(IDENTIFIER),

            ListRecords.class.getSimpleName(),
            EnumSet.of(METADATA_PREFIX, FROM, UNTIL, SET, RESUMPTION_TOKEN)
    );

    public static final Map<OaiParameterName, Set<OaiParameterName>> EXCLUSIVE_PARAMETERS =
            new EnumMap<>(Map.of(
                    METADATA_PREFIX,
                    EnumSet.of(RESUMPTION_TOKEN),

                    RESUMPTION_TOKEN,
                    EnumSet.of(METADATA_PREFIX, FROM, IDENTIFIER, SET, UNTIL),

                    FROM,
                    EnumSet.of(RESUMPTION_TOKEN, IDENTIFIER),

                    UNTIL,
                    EnumSet.of(RESUMPTION_TOKEN, IDENTIFIER),

                    SET,
                    EnumSet.of(RESUMPTION_TOKEN, IDENTIFIER)
            ));

}
