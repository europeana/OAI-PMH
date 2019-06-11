package eu.europeana.oaipmh.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Temporary class so we will redirect urls from the old oai server to new urls
 * Note that the verbController will redirect all /oaicat/OAIHandler?verb=.... requests
 */
@RestController
@RequestMapping("/oaicat")
public class RedirectController {

    @GetMapping({"/index.html", "/index.shtml"})
    public ModelAndView redirectIndex() {
        RedirectView red = new RedirectView("../index.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/getRecord.html", "/getRecord.shtml"})
    public ModelAndView redirectGetRecord() {
        RedirectView red = new RedirectView("../getRecord.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/identify.html", "/identify.shtml"})
    public ModelAndView redirectIdentify() {
        RedirectView red = new RedirectView("../identify.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listIdentifiers.html", "/listIdentifiers.shtml"})
    public ModelAndView redirectlistIdentifiers() {
        RedirectView red = new RedirectView("../listIdentifiers.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listIdentifiersResumption.html", "/listIdentifiersResumption.shtml"})
    public ModelAndView redirectlistIdentifiersResumption() {
        RedirectView red = new RedirectView("../listIdentifiersResumption.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listMetadataFormats.html", "/listMetadataFormats.shtml"})
    public ModelAndView redirectlistMetadataFormats() {
        RedirectView red = new RedirectView("../listMetadataFormats.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listRecords.html", "/listRecords.shtml"})
    public ModelAndView redirectlistRecords() {
        RedirectView red = new RedirectView("../listRecords.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listRecordsResumption.html", "/listRecordsResumption.shtml"})
    public ModelAndView redirectlistRecordsResumption() {
        RedirectView red = new RedirectView("../listRecordsResumption.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listSets.html", "/listSets.shtml"})
    public ModelAndView redirectlistSets() {
        RedirectView red = new RedirectView("../listSets.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }

    @GetMapping({"/listSetsResumption.html", "/listSetsResumption.shtml"})
    public ModelAndView redirectlistSetsResumption() {
        RedirectView red = new RedirectView("../listSetsResumption.html", true);
        red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return new ModelAndView(red);
    }


}
