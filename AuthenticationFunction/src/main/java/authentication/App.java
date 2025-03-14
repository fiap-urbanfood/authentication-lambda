package authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import authentication.util.StringAndJsonConverter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);


        try {
            String checkClient = checkClient(input);

            if(checkClient==null){
                return response
                        .withBody("{}")
                        .withStatusCode(422);
            }

            String output = String.format("{ \"result\": %s }", checkClient);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private String checkClient(APIGatewayProxyRequestEvent input){
        String user = null;
        String cpf = null;
        String email = null;

        String strResponse = null;
        try{
            if(input.getBody() != null){
                JsonNode body = StringAndJsonConverter.stringToJson(input.getBody());

                user = body.get("user").asText();

                try {
                    cpf = body.get("cpf").asText();
                }catch (Exception e){
                    cpf = null;
                }
                email = body.get("email").asText();

                JSONObject objResponse = new JSONObject();

                objResponse.put("user", user);
                if(cpf!=null){
                    objResponse.put("cpf", cpf);
                    //function select cpf no rds
                }

                objResponse.put("email", email);
                objResponse.put("isVisiting", cpf == null ? true : false);
                objResponse.put("teste", "pipeline ok");

                strResponse = objResponse.toString();
            }

            return strResponse;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
