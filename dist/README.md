# Ejecutable de demostración

`cafejunin-backend-0.1.0.jar` fue generado con Maven y JDK 17 desde el backend de esta entrega.

Requiere Java 17. Desde la raíz: `java -jar dist/cafejunin-backend-0.1.0.jar --spring.profiles.active=memory`.

Para reconstruir después de editar: `cd backend`, `mvn verify` y copiar `target/cafejunin-backend-0.1.0.jar` a esta carpeta.
Los JAR en dist están ignorados por Git para versionar el código fuente, no los binarios.
