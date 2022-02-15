package nl.han.dea.http;

/**
 * Een DTO
 */
public class FileInfo {

    public FileInfo(String content) {
        this.content = content;
        this.contentLength = content.length();
    }
    private String content;

    private int contentLength;

    public String getContent() {
        return content;
    }

    public int getContentLength() {
        return contentLength;
    }
}
