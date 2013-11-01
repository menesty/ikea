package org.menesty.ikea.ui;

public interface TaskProgressLog {

    public void addLog(String log);

    public void updateLog(String log);

    public void done();
}
