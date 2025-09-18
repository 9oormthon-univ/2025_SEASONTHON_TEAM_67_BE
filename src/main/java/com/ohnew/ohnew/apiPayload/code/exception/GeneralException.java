package com.ohnew.ohnew.apiPayload.code.exception;


import com.ohnew.ohnew.apiPayload.code.BaseErrorCode;
import com.ohnew.ohnew.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;

@Getter
@AllArgsConstructor
@Log4j2
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() {
        return this.code.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }

}
