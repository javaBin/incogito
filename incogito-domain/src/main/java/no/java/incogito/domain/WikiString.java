package no.java.incogito.domain;

import no.java.ems.wiki.DefaultHtmlWikiSink;
import no.java.ems.wiki.DefaultWikiEngine;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WikiString {
    public final String wikiText;

    public WikiString(String wikiText) {
        this.wikiText = wikiText;
    }

    public String toHtml() {
        try {
            DefaultWikiEngine<DefaultHtmlWikiSink> wikiEngine =
                    new DefaultWikiEngine<DefaultHtmlWikiSink>(new DefaultHtmlWikiSink());
            wikiEngine.transform(wikiText);
            return wikiEngine.getSink().toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert wiki text.", e);
        }
    }
}
