package excludometer;

import java.util.Map;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
public class TsvRecord {

    int lineNo;
    String chromosome;
    String position;
    String PreferredSymbol;
    String reference;
    String mutation;
    String AO;
    String UAO;
    String AF;
    String gnomAD_AF;
    String consequence;
    String HasSampleStrandBias;
    String HasSeqDirBias;
    String AF_Outlier_Pvalue;

    public TsvRecord(Map<String, Integer> tsvColMap, String tsvLine, int lineNo) {
        this.lineNo = lineNo;
        String[] fields = tsvLine.split("\t",-1);
        chromosome = fields[tsvColMap.get("chromosome")].replace("chr", "");
        position = fields[tsvColMap.get("position")];
        PreferredSymbol = fields[tsvColMap.get("PreferredSymbol")];
        reference = fields[tsvColMap.get("reference")];
        mutation = fields[tsvColMap.get("mutation")];
        AO = fields[tsvColMap.get("AO")];
        UAO = fields[tsvColMap.get("UAO")];
        AF = fields[tsvColMap.get("AF")];
        gnomAD_AF = fields[tsvColMap.get("gnomAD_AF")];
        consequence = fields[tsvColMap.get("consequence")];
        HasSampleStrandBias = fields[tsvColMap.get("HasSampleStrandBias")];
        HasSeqDirBias = fields[tsvColMap.get("HasSeqDirBias")];
        AF_Outlier_Pvalue = fields[tsvColMap.get("AF_Outlier_Pvalue")];
    }

    public boolean isArcherMyeloidWithOutlier() {
        if(
            (HasSampleStrandBias != null && HasSampleStrandBias.length() > 0 && HasSampleStrandBias.startsWith("Yes"))
            && (AO == null || AO.length() == 0 || Integer.valueOf(AO) >= 5)
            && (UAO == null || UAO.length() == 0 || Integer.valueOf(UAO) >= 3)
            && (gnomAD_AF == null || gnomAD_AF.length() == 0 || Float.valueOf(gnomAD_AF) <= 0.05f)
            && (AF_Outlier_Pvalue == null || AF_Outlier_Pvalue.length() == 0 || Float.valueOf(AF_Outlier_Pvalue) <= 0.01f)
            && (AF == null || AF.length() == 0 || Float.valueOf(AF) >= 0.027f)
            && (HasSeqDirBias == null || HasSeqDirBias.length() == 0 || !HasSeqDirBias.equals("Yes"))
        ) {
            for(String consequenceFilter : consequenceFilters) {
                if(consequence == null || consequence.contains(consequenceFilter)) {
                    return true;
                }
            }
        }      
        return false;
    }

    public String getHash() {

        {
            if(reference == null || mutation == null) {
                throw new RuntimeException("error - no reference or mutation");
            }
        }

        // normalize hash to what GO uses
        String hashChr = chromosome.equals("X") ? "XX" : String.format("%02d", Integer.valueOf(chromosome));
        long hashPos = Long.parseLong(position);
        String hashRef = reference;
        String hashMut = mutation;
        if(hashRef.length() < hashMut.length()) {
            while(hashRef.length() > 0 && hashRef.charAt(0) == hashMut.charAt(0)) {
                hashRef = hashRef.substring(1);
                hashMut = hashMut.substring(1);
            }
        }
        if(hashRef.length() > hashMut.length()) {
            while(hashMut.length() > 0 && hashRef.charAt(0) == hashMut.charAt(0)) {
                hashRef = hashRef.substring(1);
                hashMut = hashMut.substring(1);
                hashPos++;
            }
        }

        if(hashRef.length() == 0) {
            return String.format("%s%010dI_%s", hashChr, hashPos, hashMut);
        }
        else if(hashMut.length() == 0) {
            return String.format("%s%010d%sD", hashChr, hashPos, hashRef);
        }
        else {
            return String.format("%s%010d%s_%s", hashChr, hashPos, hashRef, hashMut);
        }

    }

    public static String[] consequenceFilters = {
        "5_prime_UTR_variant",
        "coding_sequence_variant",
        "feature_elongation",
        "feature_truncation",
        "frameshift_variant",
        "incomplete_terminal_codon_variant",
        "inframe_deletion",
        "inframe_insertion",
        "missense_variant",
        "protein_altering_variant",
        "splice_acceptor_variant",
        "splice_donor_variant",
        "splice_region_variant",
        "start_lost",
        "stop_gained",
        "stop_lost",
        "transcript_ablation",
        "transcript_amplification"
    };

    @Override
    public String toString() {
        return "TsvRecord{" + "lineNo=" + lineNo + ", chromosome=" + chromosome + ", position=" + position + ", PreferredSymbol=" + PreferredSymbol + ", reference=" + reference + ", mutation=" + mutation + ", AO=" + AO + ", UAO=" + UAO + ", AF=" + AF + ", gnomAD_AF=" + gnomAD_AF + ", consequence=" + consequence + ", HasSampleStrandBias=" + HasSampleStrandBias + ", HasSeqDirBias=" + HasSeqDirBias + ", AF_Outlier_Pvalue=" + AF_Outlier_Pvalue + '}';
    }

}
