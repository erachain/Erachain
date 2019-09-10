package org.erachain.rocksDB.exceptions;

public class WrongParseException extends RuntimeException {

    public WrongParseException(Exception e) {
        super(e);
    }
}
