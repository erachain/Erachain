package org.erachain.dbs.rocksDB.exceptions;

public class WrongParseException extends RuntimeException {

    public WrongParseException(Exception e) {
        super(e);
    }
}
