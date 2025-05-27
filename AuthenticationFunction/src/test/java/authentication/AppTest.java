package authentication;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class AppTest {

    @Test
    public void testHandleRequestSuccess() {
        // Arrange
        App app = new App();
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody("{\"user\": \"User Teste\", \"cpf\": \"060.531.410-11\", \"email\": \"teste@gmail.com\"}");
        Context context = null; // Pode ser mockado se necessário

        // Act
        APIGatewayProxyResponseEvent response = app.handleRequest(requestEvent, context);

        // Assert
        assertNotNull(response);
        assertEquals(422, response.getStatusCode().intValue());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHandleRequestInvalidInput() {
        // Arrange
        App app = new App();
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody("{}"); // Corpo inválido
        Context context = null; // Pode ser mockado se necessário

        // Act
        APIGatewayProxyResponseEvent response = app.handleRequest(requestEvent, context);

        // Assert
        assertNotNull(response);
        assertEquals(422, response.getStatusCode().intValue());
        assertEquals("{}", response.getBody());
    }

    @Test
    public void testHandleRequestException() {
        // Arrange
        App app = new App();
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(null); // Força uma exceção
        Context context = null; // Pode ser mockado se necessário

        // Act
        APIGatewayProxyResponseEvent response = app.handleRequest(requestEvent, context);

        // Assert
        assertNotNull(response);
        assertEquals(422, response.getStatusCode().intValue());
        assertEquals("{}", response.getBody());
    }
}
