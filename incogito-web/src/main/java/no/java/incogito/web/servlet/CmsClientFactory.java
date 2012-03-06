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
import scala.None$;
import scala.Option;
import scala.Option$;
import scala.Some;
import scala.runtime.BoxedUnit;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.joda.time.Minutes.minutes;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class CmsClientFactory {

    static private DefaultCmsClient createCmsClient() {
        Logger logger = ConsoleLogger$.MODULE$;
        Option<Minutes> minutesOption = some(minutes(10));
        Option<RequestOptions> requestOptions = some(CachingAbderaClient$.MODULE$.confluenceFriendlyRequestOptions());
        Option<ProxyConfiguration> proxyConfigurationOption = (Option<ProxyConfiguration>) Option$.MODULE$.<ProxyConfiguration>apply(null);
        AtomPubClientConfiguration atomPubClientConfiguration = new AtomPubClientConfiguration(logger, "CMS", createTempDirectory(), proxyConfigurationOption, minutesOption, requestOptions);
        CmsClient.Configuration configuration = new CmsClient.ExplicitConfiguration(url("http://wiki.java.no/poop"), url("http://wiki.java.no/rest/atompub/latest/spaces/javazone2012/pages/12682119/children"));
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

    private static URL url(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Some some(T t) {
        return new Some<T>(t);
    }

    static private File createTempDirectory() {
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

    public CmsClient build() {
        return createCmsClient();
    }

}
