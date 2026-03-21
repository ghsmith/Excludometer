package excludometer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variant {

    @JsonProperty
    public String genomeHash;
    @JsonProperty
    public String chromosome;
    @JsonProperty
    public String start;
    @JsonProperty
    public String ref;
    @JsonProperty
    public String alt;
    @JsonProperty
    public String classification;
    @JsonProperty
    public String mutationTypeDisplay;
    @JsonProperty
    public String gene;
    @JsonProperty
    public String gChange;
    @JsonProperty
    public String aaChange;
    @JsonProperty
    public String tab;
    @JsonProperty
    public String reason;
    @JsonProperty
    public String query;
    
    int total = 0;
    int detected = 0;

    @Override
    public String toString() {
        return "Variant{" + "genomeHash=" + genomeHash + ", chromosome=" + chromosome + ", start=" + start + ", ref=" + ref + ", alt=" + alt + ", classification=" + classification + ", mutationTypeDisplay=" + mutationTypeDisplay + ", gene=" + gene + ", gChange=" + gChange + ", aaChange=" + aaChange + ", tab=" + tab + ", reason=" + reason + ", total=" + total + ", detected=" + detected + '}';
    }
    
}
