package plugins.org.craftercms.plugin.docusign.utils

import org.craftercms.engine.service.SiteItemService
import java.io.InputStream
import java.io.ByteArrayOutputStream

class CrafterCMSContentHelpers {
    static def getContentBytes(SiteItemService siteItemService, String path) {
        def content = siteItemService.getRawContent(path)
        def is = content.getInputStream()

        ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        int nRead
        byte[] data = new byte[4]

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead)
        }

        buffer.flush()
        return buffer.toByteArray()
    }

    static def getInputStream(SiteItemService siteItemService, String path) {
        def content = siteItemService.getRawContent(path)
        return content.getInputStream()
    }
}