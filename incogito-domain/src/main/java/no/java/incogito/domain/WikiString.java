package no.java.incogito.domain;

import fj.F;
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

    public static final F<String, WikiString> constructor = new F<String, WikiString>() {
        public WikiString f(String wikiText) {
            return new WikiString(wikiText);
        }
    };

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

    // -----------------------------------------------------------------------
    // First-Order Functions
    // -----------------------------------------------------------------------

    public static final F<WikiString, String> toHtml = new F<WikiString, String>() {
        public String f(WikiString wikiString) {
            return wikiString.toHtml();
        }
    };
}
