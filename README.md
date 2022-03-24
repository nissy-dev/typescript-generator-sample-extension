# typescript-generator-sample-extension

Sample extensions for [typescript-generator](https://github.com/vojtechhabarta/typescript-generator)

## DefaultValueNonNullableExtension

This extension annotates properties with default values as non nullable.

For example for this Java class:

```java
public class SampleClass {
    public String text0;
    public String text1 = "hello";
}
```

This extension outputs this TypeScript interface:

```typescript
export interface SampleClass {
    text0: string | null;
    text1: string;
}
```

## CustomSerializerExtension

This extension outputs type interpreting custom serializers.

For example for this Java class:

```java
public class SampleClass {

     @JsonSerialize(using = CustomSerializer.class)
     private Integer intValue = 0;

}

// Serialize Integer to String
public static class CustomSerializer extends StdSerializer<Integer> {
    public CustomSerializer() {
        super(Integer.class);
    }

    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
```

This extension outputs this TypeScript interface:

```typescript
export interface SampleClass {
    intValue: string;
}
```
