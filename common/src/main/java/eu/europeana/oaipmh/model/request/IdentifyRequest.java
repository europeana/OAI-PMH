package eu.europeana.oaipmh.model.request;

public class IdentifyRequest extends OAIRequest {

    private static final long serialVersionUID = 6358603107528447909L;

    public IdentifyRequest() {}

    public IdentifyRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
    }
}
