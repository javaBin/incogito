package no.java.incogito.web.servlet;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.management.ManagementService;
import no.arktekk.cms.*;
import no.arktekk.cms.ConsoleLogger$;
import no.arktekk.cms.atompub.AtomPubClient$;
import no.arktekk.cms.atompub.CachingAbderaClient$;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import scala.None$;
import scala.Option;
import scala.Option$;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
@Component("cmsHttpRequestHandler")
public class CMSHttpRequestHandler implements HttpRequestHandler {

    private CacheManager cacheManager;
    private ManagementService managementService;
    private CmsClient cmsClient;

    public CMSHttpRequestHandler() {
        //cacheManager = configureCache(createTempDirectory());
        //managementService = createManagementService(cacheManager);
        cmsClient = new CmsClientFactory().build();
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.replaceAll("^.*\\/", "");
        System.out.println("Path: " + pathInfo);
        Option<CmsEntry> cmsEntryOption = cmsClient.fetchPageBySlug(CmsSlug.fromString(pathInfo));
        System.out.println("Entry: " + cmsEntryOption);
    }
/*
    private ManagementService createManagementService(CacheManager cacheManager) {
        return new ManagementService(
                cacheManager,
                ManagementFactory.getPlatformMBeanServer(),
                true, true, true, true);
    }

    static private CacheManager configureCache(File cmsCacheDir) {
        CacheConfiguration atomCache = new CacheConfiguration().timeToLiveSeconds(10 * 60).
                timeToIdleSeconds(10 * 60).
                maxElementsInMemory(1000).
                maxElementsOnDisk(1000).
                //        diskPersistent(true).   The objects must be serializable first
                        name("atom");

        CacheManager cacheManager = CacheManager.create(new Configuration()
                .diskStore(new DiskStoreConfiguration().path(cmsCacheDir.toString()))
                .defaultCache(new CacheConfiguration())
                .cache(atomCache));
        cacheManager.setName("cms-client-cache");
        return cacheManager;
    }
  */
}
