package excludometer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
public class Excludometer {

    static final Logger logger = Logger.getLogger(Excludometer.class.getName());

    public static void main(String[] args) throws IOException {
        
        TsvRecordFinder trf = new TsvRecordFinder();
        Map<String, List<TsvRecord>> tsvRecordMap = trf.findByFileNameMap("26EU-063O0380-HARRELL-OMMP-M_S14.vcf.summary.tsv");
        
        for(List<TsvRecord> tsvRecordList : tsvRecordMap.values()) {
            if(tsvRecordList.size() > 1) {
                logger.severe(String.format("variant with genome hash = '%s' appears in TSV multiple times, only the first occurrence will be used", tsvRecordList.get(0).getHash()));
            }
        }
        
        ExcludedVariantsFinder evf = new ExcludedVariantsFinder();
        evf.u = args[0];
        evf.p = args[1];
        Map<String, Variant> variantMap = evf.findByOrderIdMap("26EU-063O0380");
        
        for(Variant variant : variantMap.values()) {
            System.out.println(variant);
            System.out.println(tsvRecordMap.get(variant.genomeHash).get(0));
        }
    }
    
}
