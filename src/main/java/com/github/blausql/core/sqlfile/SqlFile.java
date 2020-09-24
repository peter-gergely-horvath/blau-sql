package com.github.blausql.core.sqlfile;

import java.util.Objects;

public final class SqlFile {
    private String filename;
    private String content;

    public SqlFile(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //CHECKSTYLE.OFF: NeedBraces
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlFile sqlFile = (SqlFile) o;
        return Objects.equals(filename, sqlFile.filename)
                && Objects.equals(content, sqlFile.content);
    }
    //CHECKSTYLE.OFF: NeedBraces

    @Override
    public int hashCode() {
        return Objects.hash(filename, content);
    }

    @Override
    public String toString() {
        return "SqlFile{"
                + "filename='" + filename + '\''
                + ", content='" + content + '\''
                + '}';
    }
}
