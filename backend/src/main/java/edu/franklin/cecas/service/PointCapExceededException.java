package edu.franklin.cecas.service;

public class PointCapExceededException extends RuntimeException {
    
    public PointCapExceededException(String message) {
        super(message);
    }
}