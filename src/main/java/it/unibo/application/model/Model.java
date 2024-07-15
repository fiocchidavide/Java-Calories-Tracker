package it.unibo.application.model;

import java.sql.Connection;

public class Model {

    private final Connection connection;
    
    public Model(final Connection connection){
        this.connection = connection;
    }
}
