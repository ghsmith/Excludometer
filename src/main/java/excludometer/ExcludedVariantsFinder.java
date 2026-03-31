package excludometer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.NewCookie;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
public class ExcludedVariantsFinder {

    static final Logger logger = Logger.getLogger(ExcludedVariantsFinder.class.getName());

    NewCookie jSessionId = null;

    String u;
    String p;
    
    public Variant[] findByOrderId(String orderId) throws InterruptedException {
        
        // get session cookie for an interactive GO session
        {
            ClientConfig cc = new ClientConfig().connectorProvider(new ApacheConnectorProvider());  
            cc.register(JacksonFeature.class);
            cc.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
            Client client = ClientBuilder.newClient(cc);
            client.register(HttpAuthenticationFeature.basic(u, p));
            jSessionId = client.target("https://goprod.eushc.org/clinical-app/workbench/permissions")
                .request()
                .get()
                .getCookies()
                .get("JSESSIONID");
            Thread.sleep(100);
        }
        
        Variant[] variants;

        ClientConfig cc = new ClientConfig().connectorProvider(new ApacheConnectorProvider());  
        cc.register(JacksonFeature.class);
        cc.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
        Client client = ClientBuilder.newClient(cc);

        // request excluded variants
        {
            variants = client.target(String.format("https://goprod.eushc.org/clinical-app/workbench/analyses/%s-Combo/SNV/excludedVariants", orderId))
                .request()
                .cookie(jSessionId)
                .get(Variant[].class);
            Thread.sleep(100);
        }

        // get the counts for each variant
        int x = 0;
        for(Variant variant : variants) {
            x++;
            VariantDTO variantDTO = client.target(String.format("https://goprod.eushc.org/clinical-app/reportedmuts?ref=%s&chromosome=%s&start=%s&alt=%s", variant.ref, variant.chromosome, variant.start, variant.alt))
                .request()
                .cookie(jSessionId)
                .get(VariantDTO.class);
            Thread.sleep(100);
            for(ReportedDTO reportedDTO : variantDTO.reportedMuts) {
                variant.total++;
                if(reportedDTO.detected) {
                    variant.detected++;
                }
            }
            if(x % 10 == 0) {
                logger.info(String.format("[%3d/%3d] retreiving excluded variants from GO...", x, variants.length));
            }
        }
        
        return variants;
        
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariantDTO {
        @JsonProperty
        public ReportedDTO[] reportedMuts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReportedDTO {
        @JsonProperty
        public boolean detected;
    }
    
}
