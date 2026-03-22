package excludometer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

        System.out.print(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
            "order",
            "#",
            "booleanString",
            "interp",
            "reason",
            "tab",
            "gene",
            "hgvsc",
            "aachange"
        ));

        System.out.print(String.format("\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
            "classification",
            "mutationType",
            "source",
            "af",
            "afop",
            "strandBias",
            "seqDirBias",
            "archerMyeloid",
            "countDet",
            "countTot",
            "gnomadAF"
        ));
        
        System.out.println();
        
        System.out.println();
        System.out.println("BOOLEAN CONDITIONS");
        System.out.println();
        System.out.println("Group 1 (not used in 'Pathogenic' tab):");
        System.out.println("1 - Source is not LowFreq variant caller.");
        System.out.println("2 - Source is LowFreq variant caller, but AF >= 1.5%.");
        System.out.println();
        System.out.println("Group 2:");
        System.out.println("3 - Source is HotSpot variant caller.");
        System.out.println("4 - NOT synonymous and has an AF outlier p-value < 0.1");
        System.out.println("5 - Synonymous, has an AF > 0.2, and has no strand or sequence bias.");
        System.out.println("6 - Detected count > 0.");
        System.out.println("7 - Total count < 100.");
        System.out.println();
        System.out.println("Group 3 (not used in 'Pathogenic' or 'Synonymous' tabs):");
        System.out.println("8 - Has no strand or sequence bias.");
        System.out.println("9 - Meets 'Archer Myeloid w/Outlier' criteria.");
        System.out.println();
        System.out.println("Group 4:");
        System.out.println("A - Total count < 200.");
        System.out.println("B - Detected count > 2.");
        System.out.println("C - AF >= 3%.");
        System.out.println();
        System.out.println("A variant exclusion is valid when: (1 OR 2) AND (3 OR 4 OR 5 OR 6 OR 7) AND (8 OR 9) AND (A OR B OR C) == FALSE.");
        System.out.println();
        
        String fileName;

        BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));

        while((fileName = brIn.readLine()) != null) {

            fileName = fileName.trim();
            
            logger.info(String.format("processing '%s'...", fileName));
            
            String orderId = fileName.substring(0, 13);

            TsvRecordFinder trf = new TsvRecordFinder();
            Map<String, List<TsvRecord>> tsvRecordMap = trf.findByFileNameMap(fileName);

            for(List<TsvRecord> tsvRecordList : tsvRecordMap.values()) {
                if(tsvRecordList.size() > 1) {
                    logger.severe(String.format("variant with genome hash = '%s' appears in TSV multiple times, only the first occurrence will be used", tsvRecordList.get(0).getHash()));
                }
            }

            ExcludedVariantsFinder evf = new ExcludedVariantsFinder();
            evf.u = args[0];
            evf.p = args[1];
            Variant[] variants = evf.findByOrderId(orderId);

            int x = 0;

            for(Variant variant : variants) {

                x++;

                TsvRecord tsvRecord = tsvRecordMap.get(variant.genomeHash).get(0);

                boolean b1_1_SourceIsNotLF = !tsvRecord.source.equals("LF");
                boolean b1_2_SourceIsLFAndHighAF = tsvRecord.source.equals("LF") && Float.valueOf(tsvRecord.AF) >= 0.015f;

                boolean b2_3_SourceIsHS = tsvRecord.source.equals("HS");
                boolean b2_4_NotSynonymousWithLowAFOP = !variant.tab.equals("Synonymous") && Float.valueOf(tsvRecord.AF_Outlier_Pvalue) < 0.1;
                boolean b2_5_SynonymousWithHighAFAndNoBias = variant.tab.equals("Synonymous") && Float.valueOf(tsvRecord.AF) > 0.2 && !tsvRecord.HasSampleStrandBias.equals("Yes") && !tsvRecord.HasSeqDirBias.equals("Yes");
                boolean b2_6_CountDetectedGreaterThan0 = variant.detected > 0;
                boolean b2_7_CountTotalLessThan100 = variant.total < 100;

                boolean b3_8_NoBias =  !tsvRecord.HasSampleStrandBias.equals("Yes") && !tsvRecord.HasSeqDirBias.equals("Yes");
                boolean b3_9_ArcherMyeloidWithOutlier = tsvRecord.isArcherMyeloidWithOutlier();

                boolean b4_A_CountTotalLessThan200 = variant.total < 200;
                boolean b4_B_CountDetectedGreaterThan2 = variant.detected > 2;
                boolean b4_C_AFAtLeast3 = Float.valueOf(tsvRecord.AF) >= 0.03f;

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

                System.out.print(String.format("%s\t%03d\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                    orderId,
                    x,
                    booleanString,
                    booleanString.replace(" ", "").replace("X", "").contains("||") ? "valid" : "INVALID",
                    variant.reason,
                    variant.tab,
                    variant.gene,
                    variant.gChange,
                    variant.aaChange
                ));

                System.out.print(String.format("\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%d\t%s",
                    variant.classification,
                    variant.mutationTypeDisplay,
                    tsvRecord.source,
                    tsvRecord.AF,
                    tsvRecord.AF_Outlier_Pvalue,
                    tsvRecord.HasSampleStrandBias,
                    tsvRecord.HasSeqDirBias,
                    tsvRecord.isArcherMyeloidWithOutlier(),
                    variant.detected,
                    variant.total,
                    tsvRecord.gnomAD_AF
                ));
                
                if(args.length > 2) {
                    System.out.print(String.format("\tAO=%s, UAO=%s, gnomad_AF=%s, AF_Outlier_Pvalue=%s, AF=%s, HasSeqDirBias=%s, consequence=%s",
                        tsvRecord.AO,
                        tsvRecord.UAO,
                        tsvRecord.gnomAD_AF,
                        tsvRecord.AF_Outlier_Pvalue,
                        tsvRecord.AF,
                        tsvRecord.HasSeqDirBias,
                        tsvRecord.consequence
                    ));
                }

                System.out.println();

            }
            
        }
        
    }
    
}
