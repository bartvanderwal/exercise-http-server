package nl.han.dea.http;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlPageReader {

    public FileInfo readFile(String filename) throws IOException {
        var fullFileName = "pages/".concat(filename);
        var classLoader = getClass().getClassLoader();
        try {
            var filePath = new File(classLoader.getResource(fullFileName).getFile()).toString();
            // Bron: https://stackoverflow.com/questions/31133361/how-to-get-file-from-resources-when-blank-space-is-in-path#answer-31133466
            var decodedPath = URLDecoder.decode(filePath); // Equivalent met `var decodedPath = filePath.replace("%20", " ");` maar dan nog wat algemener.
            Path path = Paths.get(decodedPath);

            var fileAsString = new String(Files.readAllBytes(path));
            var fileInfo = new FileInfo(fileAsString);
            return fileInfo;
        } catch (NullPointerException n){
            System.out.println();
            throw new A404Exception("Bestand niet gevonden: " + fullFileName, n);
        }
    }
}
