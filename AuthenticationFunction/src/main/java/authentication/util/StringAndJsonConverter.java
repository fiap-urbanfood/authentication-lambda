package authentication.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringAndJsonConverter {

    public static JsonNode stringToJson(String str) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(str);
    }
}
