package eu.europeana.oaipmh.model.request;

public class ErrorRequest extends OAIRequest {

    public ErrorRequest() {}

    public ErrorRequest(String verb, String baseUrl) {
      super(verb, baseUrl);
    }
}
