package jisa.experiment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Table-like structure for holding numerical data
 */
public class ResultList extends ResultTable {

    private String[]            names;
    private String[]            units      = null;
    private ArrayList<Result>   rows       = new ArrayList<>();
    private Map<String, String> attributes = new HashMap<>();

    public ResultList(Col... columns) {
        super(columns);
    }

    public ResultList(String... names) {
        super(names);
    }

    public static ResultList loadFile(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String         header = reader.readLine();

        JSONObject attributes = null;
        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        String[] columns = header.split(",");
        Col[]    cols    = new Col[columns.length];

        Pattern pattern = Pattern.compile("(.*)\\s\\[(.*)\\]");

        for (int i = 0; i < cols.length; i++) {

            Matcher matcher = pattern.matcher(columns[i]);
            Col     col;
            if (matcher.find()) {
                col = new Col(matcher.group(1), matcher.group(2));
            } else {
                col = new Col(columns[i]);
            }

            cols[i] = col;

        }

        ResultList list = new ResultList(cols);

        if (attributes != null) {
            for (String key : attributes.keySet()) list.setAttribute(key, attributes.getString(key));
        }

        String line;

        while ((line = reader.readLine()) != null) {

            String[] elements = line.split(",");

            if (elements.length != cols.length) {
                continue;
            }

            double[] data = new double[elements.length];

            for (int i = 0; i < data.length; i++) {
                data[i] = Double.parseDouble(elements[i]);
            }

            list.addData(data);

        }

        reader.close();

        return list;

    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    @Override
    public String getAttribute(String name) {
        return attributes.getOrDefault(name, null);
    }

    @Override
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public void updateColumns() {

    }

    @Override
    protected void addRow(Result row) {
        rows.add(row);
    }

    @Override
    protected void clearData() {
        rows.clear();
    }

    @Override
    public int getNumRows() {
        return rows.size();
    }

    @Override
    public Result getRow(int i) {
        return rows.get(i);
    }

    @Override
    public void removeRow(int i) {
        rows.remove(i);
    }

    @Override
    public void close() {

    }

    @Override
    public Iterator<Result> iterator() {
        return rows.iterator();
    }
}
