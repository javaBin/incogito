package no.java.incogito.web.servlet;

import no.arktekk.cms.CmsClient;
import no.arktekk.cms.CmsEntry;
import no.arktekk.cms.CmsSlug;
import org.junit.Test;
import scala.collection.Seq$;
import scala.Option;
import scalaz.NonEmptyList$;

import java.net.URL;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class TullTest {

    @Test
    public void test() throws Exception {
        CmsClient cmsClient = new CmsClientFactory(new URL("http://wiki.java.no/poop"), new URL("http://wiki.java.no/rest/atompub/latest/spaces/javazone2012/pages/12682119/children")).createCmsClient();
        Option<CmsEntry> cmsEntryOption = cmsClient.fetchPageBySlug(NonEmptyList$.MODULE$.<CmsSlug>apply(CmsSlug.fromString("about"), Seq$.MODULE$.empty()));
        System.out.println("Entry: " + cmsEntryOption);
    }

}
