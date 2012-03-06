package no.java.incogito.web.servlet;

import no.arktekk.cms.*;
import no.arktekk.cms.ConsoleLogger$;
import no.arktekk.cms.atompub.AtomPubClient$;
import no.arktekk.cms.atompub.AtomPubClientConfiguration;
import no.arktekk.cms.atompub.CachingAbderaClient$;
import no.arktekk.cms.atompub.ProxyConfiguration;
import org.apache.abdera.protocol.client.RequestOptions;
import org.joda.time.Minutes;
import scala.Function2;
import scala.runtime.AbstractFunction2;
import scala.Option;
import scala.Option$;
import scala.Some;
import scala.runtime.BoxedUnit;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.joda.time.Minutes.minutes;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class CmsClientFactory {

    private URL pagesUrl;
    private URL postsUrl;

    public CmsClientFactory(URL postsUrl, URL pagesUrl) {
        this.postsUrl = postsUrl;
        this.pagesUrl = pagesUrl;
    }

    public CmsClient build() {
        return createCmsClient();
    }

    private DefaultCmsClient createCmsClient() {
        Logger logger = ConsoleLogger$.MODULE$;
        Option<Minutes> minutesOption = some(minutes(10));
        Option<RequestOptions> requestOptions = some(CachingAbderaClient$.MODULE$.confluenceFriendlyRequestOptions());
        Option<ProxyConfiguration> proxyConfigurationOption = (Option<ProxyConfiguration>) Option$.MODULE$.<ProxyConfiguration>apply(null);
        AtomPubClientConfiguration atomPubClientConfiguration = new AtomPubClientConfiguration(logger, "CMS", createTempDirectory(), proxyConfigurationOption, minutesOption, requestOptions);
        CmsClient.Configuration configuration = new CmsClient.ExplicitConfiguration(postsUrl, pagesUrl);
        Function2<URL, URL, BoxedUnit> doNothingFunction = createDoNothingFunction2();
        return new DefaultCmsClient(logger, AtomPubClient$.MODULE$.apply(atomPubClientConfiguration), configuration, doNothingFunction);
    }

    private static <I1, I2, R> Function2<I1, I2, R> createDoNothingFunction2() {
        return new AbstractFunction2<I1, I2, R>() {
            public R apply(I1 url1, I2 url2) {
                return null;
            }
        };
    }

    private static <T> Some some(T t) {
        return new Some<T>(t);
    }

    private static File createTempDirectory() {
        File cmsCacheDir;
        try {
            cmsCacheDir = File.createTempFile("cms_cache", null);
            assert (cmsCacheDir.delete());
            assert (cmsCacheDir.mkdir());
            return cmsCacheDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
