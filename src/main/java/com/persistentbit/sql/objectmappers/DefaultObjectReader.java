package com.persistentbit.sql.objectmappers;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.ImTools;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An implementation of an {@link ObjectReader} that uses reflection to read an object from a database table row.<br>
 * Internally, {@link ImTools} is used to get all the properties from an Object.<br>
 * @see ObjectRowMapper
 */
public class DefaultObjectReader implements ObjectReader{
    private final ImTools im;
    private PMap<String,ObjectReader> fieldReaders = PMap.empty();
    private final Predicate<Class> canWriteToRow;

    public DefaultObjectReader(Class cls,Predicate<Class> canWriteToRow){
        im = ImTools.get(cls);
        this.canWriteToRow = canWriteToRow;
    }

    @Override
    public Object read(String name, Function<Class,ObjectReader> readerSupplier, ReadableRow properties) {

        PMap<String,Object> map = fieldReaders.mapKeyValues(t ->

            Tuple2.of(t._1,read(t._1,readerSupplier,properties))
        );
        if(map.values().find(v -> v!= null).isPresent() == false){
            //Not 1 property is set, assuming this is a null value
            return null;
        }
        return im.createNew(map);
    }

    private Object mapProperty(Class type, Object value){
        if(value == null){
            return null;
        }
        if(type.equals(Integer.class) || type.equals(int.class)){
            return ((Number)value).intValue();

        }
        if(type.equals(Long.class) || type.equals(long.class)){
            return ((Number)value).longValue();

        }
        return value;
    }


    public DefaultObjectReader addAllFields(){
        return addAllFieldsExcept();
    }

    public DefaultObjectReader addAllFieldsExcept(String...fieldNames){
        PSet<String> exclude= PStream.from(fieldNames).pset();
        PStream<ImTools.Getter> getters = im.getFieldGetters();
        fieldReaders = fieldReaders.plusAll(getters.filter(g -> exclude.contains(g.propertyName) == false).map(g ->
                Tuple2.of(g.propertyName,new ObjectReader(){
                @Override
                public Object read(String name, Function<Class, ObjectReader> readerSupplier, ReadableRow properties) {
                    return readerSupplier.apply(g.field.getType()).read(g.propertyName,readerSupplier,properties);
                }
            })

        ));
        return this;
    }

    public DefaultObjectReader rename(String fieldName, String propertyName){
        ObjectReader orgReader = getObjectReader(fieldName);
        fieldReaders = fieldReaders.put(fieldName, new ObjectReader() {
            @Override
            public String toString() {
                return "RenamedReader(fieldName=" + fieldName + ", propName=" + propertyName + ", reader=" + orgReader + ")";
            }

            @Override
            public Object read(String name,Function<Class, ObjectReader> masterReader, ReadableRow properties) {
                return orgReader.read(propertyName,masterReader,properties);
            }
        });
        return this;
    }

    public DefaultObjectReader mapToField(String fieldName, Function<Object,Object> fromPropertyToField){
        ObjectReader orgReader = getObjectReader(fieldName);
        fieldReaders = fieldReaders.put(fieldName, new ObjectReader() {
            @Override
            public Object read(String name,Function<Class, ObjectReader> masterReader, ReadableRow properties) {
                return fromPropertyToField.apply(orgReader.read(fieldName,masterReader,properties));
            }
        });
        return this;
    }

    public DefaultObjectReader  prefix(String fieldName, String propertyPrefix){
        ObjectReader orgReader = getObjectReader(fieldName);

        fieldReaders = fieldReaders.put(fieldName, new ObjectReader() {
            @Override
            public Object read(String name,Function<Class, ObjectReader> readerSupplier, ReadableRow properties) {
                return orgReader.read(name, readerSupplier, new ReadableRow(){
                    @Override
                    public <T> T read(Class<T> cls, String name) {
                        return properties.read(cls, propertyPrefix + name);
                    }
                });
            }
        });
        return this;
    }

    private ObjectReader getObjectReader(String fieldName) {
        ObjectReader orgReader = fieldReaders.get(fieldName);
        if(orgReader == null){
            throw new IllegalArgumentException("Can't find field '" + fieldName + "'. Add the fields first with addAllFields() or addAllFieldsExcept()");
        }
        return orgReader;
    }


    public DefaultObjectReader setFieldReader(String name, ObjectReader fieldReader){
        fieldReaders = fieldReaders.put(name,fieldReader);
        return this;
    }
}
