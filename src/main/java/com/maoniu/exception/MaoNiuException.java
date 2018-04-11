package com.maoniu.exception;

/**
 * Created by Administrator on 2018/4/10.
 */
public class MaoNiuException extends Exception{

    public MaoNiuException(){
        super();
    }

    public MaoNiuException(String message){
        super(message);
    }

    public MaoNiuException(String message, Throwable cause) {
        super(message, cause);
    }


    public MaoNiuException(Throwable cause) {
        super(cause);
    }


}
