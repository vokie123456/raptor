package com.ppdai.framework.raptor.codegen.core.swagger.tool;

import com.ppdai.framework.raptor.codegen.core.swagger.exception.SwaggerGenException;
import com.ppdai.framework.raptor.codegen.core.swagger.type.FieldType;
import com.ppdai.framework.raptor.codegen.core.utils.CommonUtils;
import io.swagger.models.properties.*;

import java.util.HashMap;
import java.util.Map;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;

/**
 * Created by zhangyicong on 18-3-1.
 */
public class TypeFormatUtil {
    private static final Map<String, Property> wktProperties;
    private static final Map<String, TypeFormat> wktSchemas;


    static {
        wktProperties = new HashMap<>();
        wktProperties.put("google.protobuf.Timestamp", new DateTimeProperty());
        wktProperties.put("google.protobuf.StringValue", new StringProperty());
        wktProperties.put("google.protobuf.Int32Value", new IntegerProperty());
        wktProperties.put("google.protobuf.Int64Value", new LongProperty());
        wktProperties.put("google.protobuf.FloatValue", new FloatProperty());
        wktProperties.put("google.protobuf.DoubleValue", new DoubleProperty());
        wktProperties.put("google.protobuf.BoolValue", new BooleanProperty());
        wktProperties.put("google.protobuf.Struct", new ObjectProperty());
        wktProperties.put("google.protobuf.Value", new ObjectProperty());
        wktProperties.put("google.protobuf.ListValue", new ObjectProperty());
        wktProperties.put("google.protobuf.Duration", new StringProperty());


        wktSchemas = new HashMap<>();
        wktSchemas.put("google.protobuf.Timestamp", new TypeFormat("string", "date-time", null, null));
        wktSchemas.put("google.protobuf.StringValue", new TypeFormat("string", "", null, null));
        wktSchemas.put("google.protobuf.Int32Value", new TypeFormat("integer", "int32", null, null));
        wktSchemas.put("google.protobuf.Int64Value", new TypeFormat("integer", "int64", null, null));
        wktSchemas.put("google.protobuf.FloatValue", new TypeFormat("number", "float", null, null));
        wktSchemas.put("google.protobuf.DoubleValue", new TypeFormat("number", "double", null, null));
        wktSchemas.put("google.protobuf.BoolValue", new TypeFormat("boolean", "boolean", null, null));
        wktSchemas.put("google.protobuf.Struct", new TypeFormat("object", null, null, true));
        wktSchemas.put("google.protobuf.Value", new TypeFormat("object", null, null, true));
        wktSchemas.put("google.protobuf.ListValue", new TypeFormat("object", null, null, true));
        wktSchemas.put("google.protobuf.Duration", new TypeFormat("string", "", null, null));
    }

    private static Map<String, Object> formatType(FieldType fieldType, String typeDefPrefix, String basePackage) {
        Map<String, Object> typeSchema = new HashMap<>(2);

        TypeFormat typeFormat = wktSchemas.get(fieldType.getTypeName());

        if (!fieldType.getTypeName().startsWith("google.protobuf") && typeFormat == null) {
            typeFormat = new TypeFormat();

            switch (fieldType.getType()) {
                case TYPE_BYTES:
                    typeFormat.setType("string");
                    typeFormat.setFormat("byte");
                    break;
                case TYPE_INT32:
                case TYPE_SINT32:
                case TYPE_SFIXED32:
                    typeFormat.setType("integer");
                    typeFormat.setFormat("int32");
                    break;
                case TYPE_UINT32:
                case TYPE_FIXED32:
                case TYPE_INT64:
                case TYPE_SINT64:
                case TYPE_SFIXED64:
                    typeFormat.setType("integer");
                    typeFormat.setFormat("int64");
                    break;
                case TYPE_UINT64:
                case TYPE_FIXED64:
                    typeFormat.setType("string");
                    typeFormat.setFormat("uint64");
                    break;
                case TYPE_FLOAT:
                    typeFormat.setType("number");
                    typeFormat.setFormat("float");
                    break;
                case TYPE_DOUBLE:
                    typeFormat.setType("number");
                    typeFormat.setFormat("double");
                    break;
                case TYPE_BOOL:
                    typeFormat.setType("boolean");
                    typeFormat.setFormat("boolean");
                    break;
                case TYPE_STRING:
                    typeFormat.setType("string");
                    typeFormat.setFormat("");
                    break;
                case TYPE_ENUM:
                case TYPE_MESSAGE:
                case TYPE_GROUP:
                    if (CommonUtils.getPackageNameFromFQPN(fieldType.getFQPN()).equals(basePackage)) {
                        typeFormat.setRef("#/" + typeDefPrefix + "/" + fieldType.getTypeName());
                    } else {
                        typeFormat.setRef("#/" + typeDefPrefix + "/" + fieldType.getFQPN());
                    }
                    break;
            }
        }

        if (typeFormat == null) {
            throw new SwaggerGenException("field name: " + fieldType.getName()
                    + ", type: " + fieldType.getTypeName()
                    + " in message: " + fieldType.getMessage()
                    + " is unsupported");
        }

        if (typeFormat.getRef() == null) { // primitive type
            typeSchema.put("type", typeFormat.getType());
            if (typeFormat.getFormat() != null)
                typeSchema.put("format", typeFormat.getFormat());
            if (typeFormat.getAdditionalProperties() != null) // Dictionaries
                typeSchema.put("additionalProperties", typeFormat.getAdditionalProperties());
        } else { // complex type
            typeSchema.put("$ref", typeFormat.getRef());
        }

        if (fieldType.getLabel().equals(LABEL_REPEATED)
                || fieldType.getTypeName().equals("google.protobuf.ListValue")) {

            Map<String, Object> subTypeSchema = new HashMap<>(typeSchema);
            typeSchema.clear();
            typeSchema.put("type", "array");
            typeSchema.put("items", subTypeSchema);
        }

        return typeSchema;
    }


    private static Property formatProperty(FieldType fieldType, String typeDefPrefix, String basePackage) {
        Map<String, Object> typeSchema = new HashMap<>(2);

        Property property = wktProperties.get(fieldType.getTypeName());

        if (!fieldType.getTypeName().startsWith("google.protobuf") && property == null) {
//            property = new AbstractProperty() {};

            switch (fieldType.getType()) {
                case TYPE_BYTES:
                    property = new ByteArrayProperty();
                    break;
                case TYPE_INT32:
                case TYPE_SINT32:
                case TYPE_SFIXED32:
                    property = new IntegerProperty();
                    break;
                case TYPE_UINT32:
                case TYPE_FIXED32:
                case TYPE_INT64:
                case TYPE_SINT64:
                case TYPE_SFIXED64:
                    property = new LongProperty();
                    break;
                case TYPE_UINT64:
                case TYPE_FIXED64:
                    property = new StringProperty("uint64");
                    break;
                case TYPE_FLOAT:
                    property = new FloatProperty();
                    break;
                case TYPE_DOUBLE:
                    property = new DoubleProperty();
                    break;
                case TYPE_BOOL:
                    property = new BooleanProperty();
                    break;
                case TYPE_STRING:
                    property = new StringProperty();
                    break;
                case TYPE_ENUM:
                case TYPE_MESSAGE:
                case TYPE_GROUP:
                    property = new RefProperty();
                    if (CommonUtils.getPackageNameFromFQPN(fieldType.getFQPN()).equals(basePackage)) {
                        ((RefProperty) property).set$ref("#/" + typeDefPrefix + "/" + fieldType.getTypeName());
                    } else {
                        ((RefProperty) property).set$ref("#/" + typeDefPrefix + "/" + fieldType.getFQPN());
                    }
                    break;
            }
        }

        if (property == null) {
            throw new SwaggerGenException("field name: " + fieldType.getName()
                    + ", type: " + fieldType.getTypeName()
                    + " in message: " + fieldType.getMessage()
                    + " is unsupported");
        }

        if (fieldType.getLabel().equals(LABEL_REPEATED)
                || fieldType.getTypeName().equals("google.protobuf.ListValue")) {
            property = new ArrayProperty(property);
        }

        return property;
    }

    public static Property formatTypeSwagger2(FieldType fieldType, String basePackage) {
        return formatProperty(fieldType, "definitions", basePackage);
    }

    public static Map<String, Object> formatTypeSwagger3(FieldType fieldType) {
        // TODO: 2018/3/7 处理 swagger3 不同包引用问题
        return formatType(fieldType, "components/schemas", "");
    }
}