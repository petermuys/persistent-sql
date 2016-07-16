package com.persistentbit.sql.objectmappers;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.ImTools;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An implementation of an {@link ObjectWriter} that uses reflection to write an object to a database table row.<br>
 * Internally, {@link ImTools} is used to get all the properties from an Object.<br>
 * @see ObjectRowMapper
 */
public class DefaultObjectWriter implements ObjectWriter{
    private final ImTools im;


    private final Predicate<Class>    canWriteToRow;
    private PMap<String,ObjectWriter> fieldWriters = PMap.empty();

    public DefaultObjectWriter(Class cls,Predicate<Class> canWriteToRow){
        this.im = ImTools.get(cls);
        this.canWriteToRow = canWriteToRow;
    }

    @Override
    public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
        if(obj == null){
            return;
        }
        fieldWriters.forEach(t -> {
            Object fieldValue = im.get(obj,t._1);
            t._2.write(fieldValue,masterWriter,result);
        });
    }

    public DefaultObjectWriter addAllFields(){
        return addAllFieldsExcept();
    }

    public DefaultObjectWriter addAllFieldsExcept(String...fieldNames){
        PSet<String>  exclude= PStream.from(fieldNames).pset();
        PStream<ImTools.Getter> getters = im.getFieldGetters();
        fieldWriters = fieldWriters.plusAll(getters.filter(g -> exclude.contains(g.propertyName) == false).map(g -> {
            ObjectWriter fw;
            if(canWriteToRow.test(g.field.getType())){
                fw = new ObjectWriter() {
                    @Override
                    public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
                        result.write(g.propertyName,obj);
                    }

                    @Override
                    public String toString() {
                        return "FieldValueWriter(field = " + g.propertyName+")";
                    }
                };

            } else {
                fw = new ObjectWriter(){
                    @Override
                    public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
                        masterWriter.write(obj,masterWriter,result);
                    }

                    @Override
                    public String toString() {
                        return "FieldObjectWriter(" + g.propertyName + ")";
                    }
                };

            }
            return new Tuple2<>(g.propertyName,fw);
        }));
        return this;
    }

    @Override
    public String toString() {
        return "DefaultObjectWriter(cls=" + im.getObjectClass().getSimpleName() + ", fieldWriters=" + fieldWriters.values().toString(",") + ")";
    }

    public DefaultObjectWriter rename(String fieldName, String propertyName){
        ObjectWriter orgWriter = getObjectWriter(fieldName);
        fieldWriters = fieldWriters.put(fieldName, new ObjectWriter() {
            @Override
            public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
                orgWriter.write(obj, masterWriter, new WritableRow() {
                    @Override
                    public WritableRow write(String name, Object value) {
                        if(name.equalsIgnoreCase(fieldName)){
                            name = propertyName;
                        }
                        result.write(name,value);
                        return this;
                    }
                });
            }
        });
        return this;
    }

    private ObjectWriter getObjectWriter(String fieldName) {
        ObjectWriter orgWriter = fieldWriters.get(fieldName);
        if(orgWriter == null){
            throw new IllegalArgumentException("Can't find field '" + fieldName + "'. Add the fields first with addAllFields() or addAllFieldsExcept()");
        }
        return orgWriter;
    }


    public DefaultObjectWriter mapToProperty(String fieldName,Function<Object,Object> fromFieldToProperty){
        ObjectWriter orgWriter = getObjectWriter(fieldName);
        fieldWriters = fieldWriters.put(fieldName, new ObjectWriter() {
            @Override
            public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
                orgWriter.write(obj, masterWriter, new WritableRow() {
                    @Override
                    public WritableRow write(String name, Object value) {
                        result.write(name,fromFieldToProperty.apply(value));
                        return this;
                    }
                });
            }
        });
        return this;
    }

    public DefaultObjectWriter  prefix(String fieldName, String propertyPrefix){
        ObjectWriter orgWriter = getObjectWriter(fieldName);
        fieldWriters = fieldWriters.put(fieldName, new ObjectWriter() {
            @Override
            public void write(Object obj, ObjectWriter masterWriter, WritableRow result) {
                orgWriter.write(obj, masterWriter, new WritableRow() {
                    @Override
                    public WritableRow write(String name, Object value) {
                        result.write(propertyPrefix+name,value);
                        return this;
                    }
                });
            }
        });
        return this;
    }


    public DefaultObjectWriter setFieldWriter(String name, ObjectWriter fieldWriter){
        fieldWriters = fieldWriters.put(name,fieldWriter);
        return this;
    }
}
