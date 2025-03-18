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
import org.json.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import redis.clients.jedis.Jedis;


/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final String redisHost = "redis-cluster.vaggjt.0001.use1.cache.amazonaws.com";
    private final int redisPort = 6379;
    
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
        try (Jedis jedis = new Jedis(redisHost, redisPort)) { // Conexão com o Redis
            if (input.getBody() != null) {
                JsonNode body = StringAndJsonConverter.stringToJson(input.getBody());
                user = body.get("user").asText();
                String token = input.getHeaders().get("Authorization");
                try {
                    cpf = body.get("cpf").asText();
                } catch (Exception e) {
                    cpf = null;
                }
                email = body.get("email").asText();

                JSONObject objResponse = new JSONObject();

                objResponse.put("user", user);
                objResponse.put("email", email);
                objResponse.put("isVisiting", cpf == null);
                objResponse.put("jwt", token);


                if (cpf != null) {
                    objResponse.put("cpf", cpf);

                    // Consultar ou salvar CPF no Redis
                    String redisKey = "login:" + cpf;
                    if (jedis.exists(redisKey)) {
                        objResponse.put("message", "CPF already exists in Redis");
                    } else {
                        jedis.set(redisKey, objResponse.toString());
                        jedis.expire(redisKey, 3200);
                        objResponse.put("message", "CPF saved in Redis");
                    }
                }




                strResponse = objResponse.toString();
            }

            return strResponse;
        } catch (Exception e) {
            System.out.println("Error interacting with Redis: " + e.getMessage());
            return null;
        }
    }
}
