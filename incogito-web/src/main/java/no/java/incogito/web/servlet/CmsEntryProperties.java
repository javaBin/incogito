package no.java.incogito.web.servlet;

import fj.F;
import no.arktekk.cms.CmsEntry;
import no.arktekk.cms.atompub.AtomPubLink;
import scala.collection.JavaConverters$;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class CmsEntryProperties {
    private CmsEntry cmsEntry;

    public CmsEntryProperties(CmsEntry cmsEntry) {
        this.cmsEntry = cmsEntry;
    }

    public String getTitle() {
        return cmsEntry.title();
    }

    public String getContent() {
        return cmsEntry.content().toString();
    }

    public String getId() {
        return cmsEntry.id().toString();
    }

    public String getSlug() {
        return cmsEntry.slug().toString();
    }

    public List<AtomPubLinkProperties> getLinks() {
        return new ArrayList<AtomPubLinkProperties>(fj.data.List.iterableList(JavaConverters$.MODULE$.seqAsJavaListConverter(cmsEntry.links()).asJava()).map(new F<AtomPubLink, AtomPubLinkProperties>() {
            public AtomPubLinkProperties f(AtomPubLink atomPubLink) {
                return new AtomPubLinkProperties(atomPubLink);
            }
        }).toCollection());
    }

}
