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
            String token = generateToken(input);

            if(token==null){
                return response
                        .withBody("{}")
                        .withStatusCode(422);
            }

            String output = String.format("{ \"token\": \"%s\" }", token);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private String generateToken(APIGatewayProxyRequestEvent input){
        String user = null;
        String cpf = null;
        String email = null;

        String token = null;
        try{
            if(input.getBody() != null){
                String secretKey = "your-secret-key";
                JsonNode body = StringAndJsonConverter.stringToJson(input.getBody());

                user = body.get("user").asText();
                cpf = body.get("cpf").asText();
                email = body.get("email").asText();

                token = Jwts.builder()
                        .setSubject(cpf)
                        .claim("user", user)
                        .claim("email", email)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
                        .signWith(SignatureAlgorithm.HS256, secretKey)
                        .compact();
            }

            return token;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
