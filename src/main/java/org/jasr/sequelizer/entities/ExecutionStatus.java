package org.jasr.sequelizer.entities;

import java.util.Date;

public class ExecutionStatus {
    private boolean success;
    private Date date;
    private String message;
    
    public ExecutionStatus(boolean success, Date date, String message) {
        super();
        this.success = success;
        this.date = date;
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
}
