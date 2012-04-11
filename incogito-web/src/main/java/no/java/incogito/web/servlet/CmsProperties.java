package no.java.incogito.web.servlet;

import no.arktekk.cms.CmsClient;
import no.arktekk.cms.CmsEntry;
import no.arktekk.cms.CmsSlug;
import scala.Option;
import scala.collection.Seq$;
import scalaz.NonEmptyList$;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class CmsProperties {
    private CmsClient cmsClient;
    private String pathInfo;

    public CmsProperties(CmsClient cmsClient, String pathInfo) {
        this.cmsClient = cmsClient;
        this.pathInfo = pathInfo;
    }

    public CmsEntryProperties getPage() {
        Option<CmsEntry> cmsEntryOption = cmsClient.fetchPageBySlug(NonEmptyList$.MODULE$.<CmsSlug>apply(CmsSlug.fromString(pathInfo), Seq$.MODULE$.empty()));
        if (cmsEntryOption.isEmpty())
            return null;
        return new CmsEntryProperties(cmsEntryOption.get());
    }

}