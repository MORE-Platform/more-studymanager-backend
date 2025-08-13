package io.redlink.more.studymanager.core.properties.model;

import java.util.List;


public class ChoiceList extends Value<List>{
    int maxSize=10;
    int minSize= 2;
    

    public ChoiceList(String id ){
        super(id);
    }

    @Override
    public Class<List> getValueType() {
        return List.class;
    }

    

    @Override
    public String getType() {
        return "CHOICELIST";
    }

    public int getMinSize() {
        return minSize;
    }


    public ChoiceList setMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public ChoiceList setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }
}
