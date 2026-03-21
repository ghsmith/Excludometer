package excludometer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
public class TsvRecordFinder {
    
    public TsvRecord[] findByFileName(String fileName) throws IOException {

        List<TsvRecord> tsvRecordList = new ArrayList<>();
        
        try (BufferedReader brTsv = new BufferedReader(new FileReader(fileName))) {
            Map<String, Integer> tsvColMap = new HashMap<>();
            int x = 0;
            String colLine = brTsv.readLine();
            for(String col : colLine.split("\t")) {
                tsvColMap.put(col, x++);
            }
            if(!(
                    tsvColMap.get("chromosome") == 2
                    && tsvColMap.get("position") == 3
                    && tsvColMap.get("PreferredSymbol") == 51
                    && tsvColMap.get("reference") == 4
                    && tsvColMap.get("mutation") == 5
                    && tsvColMap.get("AO") == 10
                    && tsvColMap.get("UAO") == 14
                    && tsvColMap.get("AF") == 11
                    && tsvColMap.get("gnomAD_AF") == 73
                    && tsvColMap.get("consequence") == 58
                    && tsvColMap.get("HasSampleStrandBias") == 33
                    && tsvColMap.get("HasSeqDirBias") == 40
                    && tsvColMap.get("AF_Outlier_Pvalue") == 119
                    )) {
                throw new RuntimeException("error - fields numbers not as expected");
            }
            int lineNo = 1;
            String tsvLine;
            while((tsvLine = brTsv.readLine()) != null) {
                lineNo++;
                TsvRecord tsvRecord = new TsvRecord(tsvColMap, tsvLine, lineNo);
                tsvRecordList.add(tsvRecord);
            }
        }

        return tsvRecordList.toArray(new TsvRecord[0]);
        
    }

    public Map<String, List<TsvRecord>> findByFileNameMap(String fileName) throws IOException {
        Map<String, List<TsvRecord>> tsvRecordMap = new HashMap<>();
        for(TsvRecord tsvRecord : findByFileName(fileName)) {
            if(tsvRecordMap.get(tsvRecord.getHash()) == null) {
                tsvRecordMap.put(tsvRecord.getHash(), new ArrayList<>());
            }
            tsvRecordMap.get(tsvRecord.getHash()).add(tsvRecord);
        }
        return tsvRecordMap;
    }
    
}
