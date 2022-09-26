package io.fleethub.objects;

import java.util.ArrayList;
import java.util.List;

enum Type {Device,URL,IP}
public class Bucket {
    private final String _id;
    private Type _type;
    private List<Data> _data;
    public Bucket(String id) {
        this._id=id;
        _data = new ArrayList<>();
    }

    public void addData(Data data) {
        _data.add(data);
    }
    public void setData(List<Data> dataList) {
        this._data = dataList;
    }

    public List<Data> getData() {return this._data;}

    public String getId() { return this._id;}

}
