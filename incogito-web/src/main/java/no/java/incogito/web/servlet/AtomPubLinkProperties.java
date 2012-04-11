package no.java.incogito.web.servlet;

import no.arktekk.cms.atompub.AtomPubLink;
import scala.Option;

import javax.activation.MimeType;
import java.net.URL;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class AtomPubLinkProperties {
    private AtomPubLink atomPubLink;

    public AtomPubLinkProperties(AtomPubLink atomPubLink) {
        this.atomPubLink = atomPubLink;
    }

    public URL getHref() {
        return atomPubLink.href();
    }

    public MimeType getMimeType() {
        Option<MimeType> mimeTypeOption = atomPubLink.mimeType();
        if (mimeTypeOption.isDefined()) {
            return null;
        }
        return mimeTypeOption.get();
    }

    public String getRel() {
        return atomPubLink.rel();
    }
}
