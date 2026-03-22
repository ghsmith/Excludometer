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
        Variant[] variants = evf.findByOrderId("26EU-063O0380");
        
        int x = 0;
        for(Variant variant : variants) {
            
            x++;

            TsvRecord tsvRecord = tsvRecordMap.get(variant.genomeHash).get(0);
            
            boolean b1_1_SourceIsNotLF = !tsvRecord.source.equals("LF");
            boolean b1_2_SourceIsLFAndHighAF = tsvRecord.source.equals("LF") && Float.valueOf(tsvRecord.AF) >= 0.015f;
            
            boolean b2_3_SourceIsHS = tsvRecord.source.equals("HS");
            boolean b2_4_NotSynonymousWithLowAFOP = !variant.mutationTypeDisplay.equals("Synonymous") && Float.valueOf(tsvRecord.AF_Outlier_Pvalue) < 0.1;
            boolean b2_5_SynonymousWithHighAFAndNoBias = variant.mutationTypeDisplay.equals("Synonymous") && Float.valueOf(tsvRecord.AF) > 0.2 && !tsvRecord.HasSampleStrandBias.equals("Yes") && !tsvRecord.HasSeqDirBias.equals("Yes");
            boolean b2_6_CountDetectedGreaterThan0 = variant.detected > 0;
            boolean b2_7_CountTotalLessThan100 = variant.total < 100;
            
            boolean b3_8_NoBias =  !tsvRecord.HasSampleStrandBias.equals("Yes") && !tsvRecord.HasSeqDirBias.equals("Yes");
            boolean b3_9_ArcherMyeloidWithOutlier = tsvRecord.isArcherMyeloidWithOutlier();

            boolean b4_A_CountTotalLessThan200 = variant.total < 200;
            boolean b4_B_CountDetectedGreaterThan2 = variant.detected > 2;
            boolean b4_C_AFAtLeast3 = Float.valueOf(tsvRecord.AF) >= 3f;
            
            String booleanString = null;

            if(variant.tab.equals("Pathogenic")) {
                booleanString = String.format("|--|%s%s%s%s%s|--|%s%s%s|",
                    b2_3_SourceIsHS ? "3" : " ",
                    b2_4_NotSynonymousWithLowAFOP ? "4" : " ",
                    b2_5_SynonymousWithHighAFAndNoBias ? "5" : " ",
                    b2_6_CountDetectedGreaterThan0 ? "6" : " ",
                    b2_7_CountTotalLessThan100 ? "7" : " ",
                    b4_A_CountTotalLessThan200 ? "A" : " ",
                    b4_B_CountDetectedGreaterThan2 ? "B" : " ",
                    b4_C_AFAtLeast3 ? "C" : " "
                );
            }
            else if(variant.tab.equals("Uncertain") || variant.tab.equals("Benign") || variant.tab.equals("Low Quality")) {
                booleanString = String.format("|%s%s|%s%s%s%s%s|%s%s|%s%s%s|",
                    b1_1_SourceIsNotLF ? "1" : " ",
                    b1_2_SourceIsLFAndHighAF ? "2" : " ",
                    b2_3_SourceIsHS ? "3" : " ",
                    b2_4_NotSynonymousWithLowAFOP ? "4" : " ",
                    b2_5_SynonymousWithHighAFAndNoBias ? "5" : " ",
                    b2_6_CountDetectedGreaterThan0 ? "6" : " ",
                    b2_7_CountTotalLessThan100 ? "7" : " ",
                    b3_8_NoBias ? "8" : " ",
                    b3_9_ArcherMyeloidWithOutlier ? "9" : " ",
                    b4_A_CountTotalLessThan200 ? "A" : " ",
                    b4_B_CountDetectedGreaterThan2 ? "B" : " ",
                    b4_C_AFAtLeast3 ? "C" : " "
                );
            }
            else if(variant.tab.equals("Synonymous") || variant.tab.equals("Artifact")) {
                booleanString = String.format("|%s%s|%s%s%s%s%s|--|%s%s%s|",
                    b1_1_SourceIsNotLF ? "1" : " ",
                    b1_2_SourceIsLFAndHighAF ? "2" : " ",
                    b2_3_SourceIsHS ? "3" : " ",
                    b2_4_NotSynonymousWithLowAFOP ? "4" : " ",
                    b2_5_SynonymousWithHighAFAndNoBias ? "5" : " ",
                    b2_6_CountDetectedGreaterThan0 ? "6" : " ",
                    b2_7_CountTotalLessThan100 ? "7" : " ",
                    b4_A_CountTotalLessThan200 ? "A" : " ",
                    b4_B_CountDetectedGreaterThan2 ? "B" : " ",
                    b4_C_AFAtLeast3 ? "C" : " "
                );
            }
            else {
                booleanString = String.format("|XX|XXXXX|XX|XXX|");
            }

            System.out.print(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                booleanString,
                booleanString.replace(" ", "").replace("X", "").contains("||") ? "valid" : "INVALID",
                variant.reason,
                variant.tab,
                variant.gene,
                variant.gChange,
                variant.aaChange,
                variant.mutationTypeDisplay
            ));
            
            System.out.print(String.format("\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%d",
                tsvRecord.source,
                tsvRecord.AF,
                tsvRecord.AF_Outlier_Pvalue,
                tsvRecord.HasSampleStrandBias,
                tsvRecord.HasSeqDirBias,
                tsvRecord.isArcherMyeloidWithOutlier(),
                variant.detected,
                variant.total
            ));

            System.out.println();

        }
        
    }
    
}
