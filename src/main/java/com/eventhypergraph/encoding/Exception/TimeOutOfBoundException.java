package com.eventhypergraph.encoding.Exception;

public class TimeOutOfBoundException extends RuntimeException{
    public TimeOutOfBoundException(){
        super();
    }

    public TimeOutOfBoundException(String message) {
        super(message);
    }
}
