package org.jasr.sequelizer.entities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowCountCallbackHandler;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;

public class CSVExporterCountCallbackHandler extends RowCountCallbackHandler implements AutoCloseable {
    private CSVWriter      writer;
    private int            flushCount;
    private List<String[]> rowsBuffer;
    private String         csvFolder;
    private SqlJob         job;
    private boolean        firstRow = true;

    public CSVExporterCountCallbackHandler(SqlJob job, String csvFolder, int flushCount) {
        this.flushCount = flushCount;
        this.rowsBuffer = new ArrayList<>(flushCount);
        this.csvFolder = csvFolder;
        this.job = job;
    }

    @Override
    protected void processRow(ResultSet rs, int rowNum) throws SQLException {

        if (firstRow) {
            try {
                File csvFile = Paths.get(csvFolder, job.getProject(), job.csvFileNameOnly()).toFile();
                csvFile.getParentFile().mkdirs();
                csvFile.createNewFile();
                writer = new CSVWriter(new FileWriter(csvFile));
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            writer.writeNext(new ResultSetHelperService().getColumnNames(rs));
            firstRow = !firstRow;
        }

        String[] row = new String[rs.getMetaData().getColumnCount()];
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++)
            row[i] = String.valueOf(rs.getObject(i + 1));

        rowsBuffer.add(row);
        if (rowNum % flushCount == 0) {
            write();
        }

    }

    private void write() {
        writer.writeAll(rowsBuffer);
        writer.flushQuietly();
        rowsBuffer.clear();
    }

    @Override
    public void close() throws Exception {
        write();
        writer.close();
    }
}
