package pe.cafejunin;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ArquitecturaTest {
  @Test
  void nucleoNoImportaFrameworksNiAdaptadores() throws Exception {
    var forbidden =
        Pattern.compile(
            "import\\s+(org\\.springframework|jakarta|java\\.sql|pe\\.cafejunin\\.(adapters|infrastructure))");
    for (String layer : new String[] {"domain", "application"}) {
      try (var files = Files.walk(Path.of("src/main/java/pe/cafejunin", layer))) {
        for (var f : files.filter(p -> p.toString().endsWith(".java")).toList())
          assertFalse(
              forbidden.matcher(Files.readString(f)).find(), "Dependencia prohibida en " + f);
      }
    }
  }
}
