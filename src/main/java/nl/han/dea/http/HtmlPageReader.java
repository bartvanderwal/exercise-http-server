package nl.han.dea.http;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlPageReader {

    public FileInfo readFile(String filename) throws IOException {
        var fullFileName = "pages/".concat(filename);
        try {
            var filePath = new File(getClass().getClassLoader().getResource(fullFileName).getFile()).toString();

            // Bron: https://stackoverflow.com/questions/31133361/how-to-get-file-from-resources-when-blank-space-is-in-path#answer-31133466
            Path path = Paths.get(URLDecoder.decode(filePath, StandardCharsets.UTF_8));

            var fileAsString = new String(Files.readAllBytes(path));
            var fileInfo = new FileInfo(fileAsString);
            return fileInfo;
        } catch (NullPointerException n){
            System.out.println();
            throw new A404Exception("Bestand niet gevonden: " + fullFileName, n);
        }
    }
}
