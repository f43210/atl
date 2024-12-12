package com.example.battery.model;

import lombok.Data;

@Data
public class RespModel {

    /*
    {"code":0,"column_meta":[["affected_rows","INT",4]],"data":[[1]],"rows":1}
     */

    private int code;
    private int rows;

    @Override
    public String toString() {
        return "RespModel{" +
                "code=" + code +
                ", rows=" + rows +
                '}';
    }
}
