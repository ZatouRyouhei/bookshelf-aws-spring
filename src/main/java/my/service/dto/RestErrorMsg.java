package my.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
public class RestErrorMsg {
    private Integer lineNo;
    private String errorMsg;

    public RestErrorMsg(Integer lineNo, String errorMsg) {
        this.lineNo = lineNo;
        this.errorMsg = errorMsg;
    }
}
